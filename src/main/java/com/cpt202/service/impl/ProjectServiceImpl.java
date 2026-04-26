package com.cpt202.service.impl;

import com.cpt202.dto.ProjectDTO;
import com.cpt202.dto.ProjectStatusUpdateDTO;
import com.cpt202.dto.StudentProjectQueryDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectStatusHistory;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.repository.CategoryRepository;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectStatusHistoryRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.repository.specification.ProjectSpecifications;
import com.cpt202.result.PageResult;
import com.cpt202.service.ProjectService;
import com.cpt202.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目服务实现类。
 * <p>
 * 负责项目查询、创建、修改和状态流转等业务逻辑的实现。
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectStatusHistoryRepository projectStatusHistoryRepository;

    /**
     * 查询学生端项目列表（DB 端过滤 + 分页）。
     */
    @Override
    public PageResult<ProjectVO> listStudentProjects(StudentProjectQueryDTO queryDTO) {
        Specification<Project> spec = ProjectSpecifications.studentQuery(
                queryDTO.getKeyword(),
                queryDTO.getCategoryId(),
                queryDTO.getStatus(),
                queryDTO.getTagIds());
        Pageable pageable = PageRequest.of(
                queryDTO.getPageNum() - 1,
                queryDTO.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Project> page = projectRepository.findAll(spec, pageable);
        List<ProjectVO> records = page.getContent().stream()
                .map(this::toProjectVO)
                .collect(Collectors.toList());
        return new PageResult<>(page.getTotalElements(), records);
    }

    /**
     * 查询教师端项目列表。
     */
    @Override
    public List<ProjectVO> listTeacherProjects(Long teacherId, Project.ProjectStatus status) {
        List<Project> projects = status == null
                ? projectRepository.findByTeacher_TeacherIdOrderByCreatedAtDesc(teacherId)
                : projectRepository.findByTeacher_TeacherIdAndProjectStatusOrderByCreatedAtDesc(teacherId, status);
        return projects.stream().map(this::toProjectVO).collect(Collectors.toList());
    }

    /**
     * 查询项目详情。
     */
    @Override
    public ProjectVO getProject(Long projectId) {
        return toProjectVO(getProjectEntity(projectId));
    }

    /**
     * 新增项目。
     */
    @Override
    @Transactional
    public void create(ProjectDTO projectDTO) {
        TeacherProfile teacher = teacherProfileRepository.findById(projectDTO.getTeacherId())
                .orElseThrow(() -> new NotFoundException("教师不存在。"));

        Project project = Project.builder()
                .teacher(teacher)
                .category(categoryRepository.findById(projectDTO.getCategoryId())
                        .orElseThrow(() -> new NotFoundException("项目分类不存在。")))
                .title(projectDTO.getTitle())
                .description(projectDTO.getDescription())
                .requiredSkills(projectDTO.getRequiredSkills())
                .topicArea(projectDTO.getTopicArea())
                .maxStudents(projectDTO.getMaxStudents())
                .currentAgreedCount(0)
                .projectStatus(Project.ProjectStatus.AVAILABLE)
                .publishDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        projectRepository.save(project);
    }

    /**
     * 修改项目。
     */
    @Override
    @Transactional
    public void update(Long projectId, ProjectDTO projectDTO) {
        Project project = getOwnedProject(projectId, projectDTO.getTeacherId());
        project.setCategory(categoryRepository.findById(projectDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("项目分类不存在。")));
        project.setTitle(projectDTO.getTitle());
        project.setDescription(projectDTO.getDescription());
        project.setRequiredSkills(projectDTO.getRequiredSkills());
        project.setTopicArea(projectDTO.getTopicArea());
        project.setMaxStudents(projectDTO.getMaxStudents());
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    /**
     * 修改项目状态。
     */
    @Override
    @Transactional
    public void changeStatus(Long projectId, ProjectStatusUpdateDTO projectStatusUpdateDTO) {
        Project project = getOwnedProject(projectId, projectStatusUpdateDTO.getTeacherId());
        Project.ProjectStatus oldStatus = project.getProjectStatus();
        project.setProjectStatus(projectStatusUpdateDTO.getProjectStatus());
        project.setUpdatedAt(LocalDateTime.now());
        if (projectStatusUpdateDTO.getProjectStatus() == Project.ProjectStatus.CLOSED
                || projectStatusUpdateDTO.getProjectStatus() == Project.ProjectStatus.ARCHIVED) {
            project.setCloseDate(LocalDateTime.now());
        }
        projectRepository.save(project);

        projectStatusHistoryRepository.save(ProjectStatusHistory.builder()
                .project(project)
                .oldStatus(oldStatus == null ? null : oldStatus.name())
                .newStatus(projectStatusUpdateDTO.getProjectStatus().name())
                .changedBy(project.getTeacher())
                .remark(projectStatusUpdateDTO.getRemark())
                .changedAt(LocalDateTime.now())
                .build());
    }

    private Project getProjectEntity(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("项目不存在。"));
    }

    private Project getOwnedProject(Long projectId, Long teacherId) {
        Project project = getProjectEntity(projectId);
        if (project.getTeacher() == null || !teacherId.equals(project.getTeacher().getTeacherId())) {
            throw new BusinessException("不能操作其他教师名下的项目。");
        }
        return project;
    }

    private ProjectVO toProjectVO(Project project) {
        return ProjectVO.builder()
                .projectId(project.getProjectId())
                .teacherId(project.getTeacher() == null ? null : project.getTeacher().getTeacherId())
                .teacherName(project.getTeacher() == null || project.getTeacher().getUser() == null
                        ? null : project.getTeacher().getUser().getFullName())
                .categoryId(project.getCategory() == null ? null : project.getCategory().getCategoryId())
                .categoryName(project.getCategory() == null ? null : project.getCategory().getCategoryName())
                .title(project.getTitle())
                .description(project.getDescription())
                .requiredSkills(project.getRequiredSkills())
                .topicArea(project.getTopicArea())
                .maxStudents(project.getMaxStudents())
                .currentAgreedCount(project.getCurrentAgreedCount())
                .projectStatus(project.getProjectStatus())
                .publishDate(project.getPublishDate())
                .closeDate(project.getCloseDate())
                .build();
    }
}
