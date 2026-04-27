package com.cpt202.service.impl;

import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.service.ProjectRequestValidationService;
import com.cpt202.service.ProjectRequestService;
import com.cpt202.vo.ProjectRequestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Project Request Service Implementation.
 * Integrates Module 8 business rules for project selection.
 */
@Service
@RequiredArgsConstructor
public class ProjectRequestServiceImpl implements ProjectRequestService {

    private final ProjectRequestRepository requestRepository;
    private final ProjectRepository projectRepository;
    private final StudentProfileRepository studentRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final RequestStatusHistoryRepository requestStatusHistoryRepository;
    private final ProjectRequestValidationService projectRequestValidationService;

    /**
     * Student submits a project request. (PBI 1 & 2)
     */
    @Override
    @Transactional
    public void create(ProjectRequestCreateDTO dto) {
        // 1. 检查截止日期和“一人一选”限制
        projectRequestValidationService.validateRequest(dto.getStudentId());

        // 2. 查找关联实体
        StudentProfile student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuleViolationException("Student record not found."));
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuleViolationException("Project record not found."));

        // 3. 构建并保存申请记录
        ProjectRequest request = ProjectRequest.builder()
                .student(student)
                .project(project)
                .preferenceRank(dto.getPreferenceRank())
                .notes(dto.getNotes())
                .requestStatus(ProjectRequest.RequestStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        request = requestRepository.save(request);
        saveHistory(request, null, ProjectRequest.RequestStatus.PENDING, student, "学生提交申请。");
    }

    /**
     * 教师审核项目申请。
     */
    @Override
    @Transactional
    public void review(Long requestId, ProjectRequestReviewDTO dto) {

        ProjectRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("申请记录不存在。"));

        if (request.getProject() == null
                || request.getProject().getTeacher() == null
                || !dto.getTeacherId().equals(request.getProject().getTeacher().getTeacherId())) {
            throw new BusinessException("不能审核其他教师名下项目的申请。");
        }

        TeacherProfile teacher = teacherProfileRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new NotFoundException("教师不存在。"));

        ProjectRequest.RequestStatus oldStatus = request.getRequestStatus();
        request.setRequestStatus(dto.getRequestStatus());
        request.setDecisionComment(dto.getDecisionComment());
        request.setReviewedBy(teacher);
        request.setReviewedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        requestRepository.save(request);
        saveHistory(request, oldStatus, dto.getRequestStatus(), null, dto.getDecisionComment());

        if (dto.getRequestStatus() == ProjectRequest.RequestStatus.ACCEPTED) {
            projectRequestValidationService.onApprovalSuccess(requestId);
        }
    }

    // --- 下面这些方法可以后续根据需求慢慢补全，暂时保留占位符防止报错 ---

    @Override
    public List<ProjectRequestVO> listStudentRequests(Long studentId) {
        return toProjectRequestVOList(requestRepository.findByStudent_StudentIdOrderBySubmittedAtDesc(studentId));
    }

    @Override
    public List<ProjectRequestVO> listTeacherRequests(Long teacherId, ProjectRequest.RequestStatus status) {
        List<ProjectRequest> requests = status == null
                ? requestRepository.findByProject_Teacher_TeacherIdOrderBySubmittedAtDesc(teacherId)
                : requestRepository.findByProject_Teacher_TeacherIdAndRequestStatusOrderBySubmittedAtDesc(teacherId, status);
        return toProjectRequestVOList(requests);
    }

    @Override
    public void withdraw(Long requestId, Long studentId) {
        ProjectRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("申请记录不存在。"));
        if (request.getStudent() == null || !studentId.equals(request.getStudent().getStudentId())) {
            throw new BusinessException("不能撤回其他学生的申请。");
        }
        ProjectRequest.RequestStatus oldStatus = request.getRequestStatus();
        request.setRequestStatus(ProjectRequest.RequestStatus.WITHDRAWN);
        request.setWithdrawnAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
        saveHistory(request, oldStatus, ProjectRequest.RequestStatus.WITHDRAWN, request.getStudent(), "学生撤回申请。");
    }

    private List<ProjectRequestVO> toProjectRequestVOList(List<ProjectRequest> requests) {
        List<ProjectRequestVO> requestVos = new ArrayList<>(requests.size());
        for (ProjectRequest request : requests) {
            requestVos.add(toProjectRequestVO(request));
        }
        return requestVos;
    }

    private ProjectRequestVO toProjectRequestVO(ProjectRequest request) {
        return ProjectRequestVO.builder()
                .requestId(request.getRequestId())
                .projectId(request.getProject() == null ? null : request.getProject().getProjectId())
                .projectTitle(request.getProject() == null ? null : request.getProject().getTitle())
                .studentId(request.getStudent() == null ? null : request.getStudent().getStudentId())
                .studentName(request.getStudent() == null || request.getStudent().getUser() == null
                        ? null : request.getStudent().getUser().getFullName())
                .reviewedByTeacherId(request.getReviewedBy() == null ? null : request.getReviewedBy().getTeacherId())
                .preferenceRank(request.getPreferenceRank())
                .notes(request.getNotes())
                .requestStatus(request.getRequestStatus())
                .decisionComment(request.getDecisionComment())
                .submittedAt(request.getSubmittedAt())
                .reviewedAt(request.getReviewedAt())
                .withdrawnAt(request.getWithdrawnAt())
                .build();
    }

    private void saveHistory(ProjectRequest request,
                             ProjectRequest.RequestStatus oldStatus,
                             ProjectRequest.RequestStatus newStatus,
                             StudentProfile changedBy,
                             String remark) {
        requestStatusHistoryRepository.save(RequestStatusHistory.builder()
                .request(request)
                .oldStatus(oldStatus == null ? null : oldStatus.name())
                .newStatus(newStatus.name())
                .changedBy(changedBy)
                .remark(remark)
                .changedAt(LocalDateTime.now())
                .build());
    }
}
