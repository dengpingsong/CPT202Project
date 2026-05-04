package com.cpt202.unit.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.ProjectDTO;
import com.cpt202.dto.ProjectStatusUpdateDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.model.entity.Category;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.ProjectStatusHistory;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.CategoryRepository;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.ProjectStatusHistoryRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.service.impl.ProjectServiceImpl;
import com.cpt202.vo.ProjectVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProjectStatusHistoryRepository projectStatusHistoryRepository;

    @Mock
    private ProjectRequestRepository projectRequestRepository;

    @Mock
    private RequestStatusHistoryRepository requestStatusHistoryRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void createShouldInitializeProjectAsAvailable() {
        Long teacherId = 10L;
        Long categoryId = 20L;
        TeacherProfile teacher = teacherProfile(teacherId);
        Category category = category(categoryId, "AI");
        ProjectDTO dto = projectDTO(categoryId, "New Project", 3);

        when(teacherProfileRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            ReflectionTestUtils.setField(project, "projectId", 99L);
            return project;
        });

        ProjectVO saved = projectService.create(teacherId, dto);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        Project persisted = projectCaptor.getValue();
        assertThat(persisted.getTeacher()).isEqualTo(teacher);
        assertThat(persisted.getCategory()).isEqualTo(category);
        assertThat(persisted.getProjectStatus()).isEqualTo(Project.ProjectStatus.AVAILABLE);
        assertThat(persisted.getCurrentAgreedCount()).isZero();
        assertThat(persisted.getPublishDate()).isNotNull();
        assertThat(saved.getProjectId()).isEqualTo(99L);
        assertThat(saved.getTitle()).isEqualTo("New Project");
    }

    @Test
    void changeStatusShouldRejectManualRequestedTransition() {
        Project project = project(11L, teacherProfile(1L), Project.ProjectStatus.AVAILABLE);
        when(projectRepository.findById(11L)).thenReturn(Optional.of(project));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> projectService.changeStatus(11L, 1L, statusDTO(Project.ProjectStatus.REQUESTED, "not allowed")));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.PROJECT_STATUS_REQUESTED_NOT_ALLOWED_MANUALLY);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void changeStatusShouldRejectArchivedTransition() {
        Project project = project(12L, teacherProfile(2L), Project.ProjectStatus.AVAILABLE);
        when(projectRepository.findById(12L)).thenReturn(Optional.of(project));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> projectService.changeStatus(12L, 2L, statusDTO(Project.ProjectStatus.ARCHIVED, "disabled")));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.PROJECT_STATUS_ARCHIVED_DISABLED);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void changeStatusShouldRejectReopenOfClosedProject() {
        Project project = project(13L, teacherProfile(3L), Project.ProjectStatus.CLOSED);
        when(projectRepository.findById(13L)).thenReturn(Optional.of(project));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> projectService.changeStatus(13L, 3L, statusDTO(Project.ProjectStatus.AVAILABLE, "reopen")));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.PROJECT_STATUS_TRANSITION_INVALID);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void changeStatusShouldCloseProjectAndCancelActiveRequests() {
        Project project = project(14L, teacherProfile(4L), Project.ProjectStatus.AGREED);
        project.setCurrentAgreedCount(1);
        ProjectRequest pendingRequest = request(31L, ProjectRequest.RequestStatus.PENDING);
        ProjectRequest acceptedRequest = request(32L, ProjectRequest.RequestStatus.ACCEPTED);

        when(projectRepository.findById(14L)).thenReturn(Optional.of(project));
        when(projectRequestRepository.findByProject_ProjectIdAndRequestStatusIn(eq(14L), anyList()))
                .thenReturn(List.of(pendingRequest, acceptedRequest));

        projectService.changeStatus(14L, 4L, statusDTO(Project.ProjectStatus.CLOSED, "manual close"));

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        ArgumentCaptor<List<ProjectRequest>> requestsCaptor = ArgumentCaptor.forClass((Class<List<ProjectRequest>>) (Class<?>) List.class);
        ArgumentCaptor<RequestStatusHistory> requestHistoryCaptor = ArgumentCaptor.forClass(RequestStatusHistory.class);
        ArgumentCaptor<ProjectStatusHistory> projectHistoryCaptor = ArgumentCaptor.forClass(ProjectStatusHistory.class);

        verify(projectRepository).save(projectCaptor.capture());
        verify(projectRequestRepository).saveAll(requestsCaptor.capture());
        verify(requestStatusHistoryRepository, times(2)).save(requestHistoryCaptor.capture());
        verify(projectStatusHistoryRepository).save(projectHistoryCaptor.capture());

        Project savedProject = projectCaptor.getValue();
        assertThat(savedProject.getProjectStatus()).isEqualTo(Project.ProjectStatus.CLOSED);
        assertThat(savedProject.getCurrentAgreedCount()).isZero();
        assertThat(savedProject.getCloseDate()).isNotNull();

        List<ProjectRequest> cancelledRequests = requestsCaptor.getValue();
        assertThat(cancelledRequests).allSatisfy(request -> {
            assertThat(request.getRequestStatus()).isEqualTo(ProjectRequest.RequestStatus.REJECTED);
            assertThat(request.getDecisionComment()).isEqualTo(MessageConstants.PROJECT_CLOSED_AND_REQUEST_CANCELLED);
            assertThat(request.getReviewedAt()).isNotNull();
        });

        assertThat(requestHistoryCaptor.getAllValues()).hasSize(2);
        assertThat(requestHistoryCaptor.getAllValues()).allSatisfy(history -> {
            assertThat(history.getNewStatus()).isEqualTo(ProjectRequest.RequestStatus.REJECTED.name());
            assertThat(history.getRemark()).isEqualTo("系统自动取消：教师关闭了该项目。");
        });

        ProjectStatusHistory history = projectHistoryCaptor.getValue();
        assertThat(history.getOldStatus()).isEqualTo(Project.ProjectStatus.AGREED.name());
        assertThat(history.getNewStatus()).isEqualTo(Project.ProjectStatus.CLOSED.name());
        assertThat(history.getRemark()).isEqualTo("manual close");
    }

    private ProjectDTO projectDTO(Long categoryId, String title, Integer maxStudents) {
        ProjectDTO dto = new ProjectDTO();
        dto.setCategoryId(categoryId);
        dto.setTitle(title);
        dto.setDescription(title + " description");
        dto.setRequiredSkills("Java");
        dto.setTopicArea("Backend");
        dto.setMaxStudents(maxStudents);
        return dto;
    }

    private ProjectStatusUpdateDTO statusDTO(Project.ProjectStatus status, String remark) {
        ProjectStatusUpdateDTO dto = new ProjectStatusUpdateDTO();
        dto.setProjectStatus(status);
        dto.setRemark(remark);
        return dto;
    }

    private TeacherProfile teacherProfile(Long teacherId) {
        TeacherProfile teacherProfile = new TeacherProfile();
        ReflectionTestUtils.setField(teacherProfile, "teacherId", teacherId);
        User user = new User();
        user.setUserId(teacherId);
        user.setRole(User.UserRole.TEACHER);
        teacherProfile.setUser(user);
        return teacherProfile;
    }

    private Category category(Long categoryId, String categoryName) {
        Category category = new Category();
        ReflectionTestUtils.setField(category, "categoryId", categoryId);
        category.setCategoryName(categoryName);
        return category;
    }

    private Project project(Long projectId, TeacherProfile teacherProfile, Project.ProjectStatus status) {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "projectId", projectId);
        project.setTeacher(teacherProfile);
        project.setProjectStatus(status);
        project.setMaxStudents(2);
        return project;
    }

    private ProjectRequest request(Long requestId, ProjectRequest.RequestStatus status) {
        ProjectRequest request = new ProjectRequest();
        ReflectionTestUtils.setField(request, "requestId", requestId);
        request.setRequestStatus(status);
        return request;
    }
}