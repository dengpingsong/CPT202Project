package com.cpt202.service.impl;

import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.constant.MessageConstants;
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
import org.springframework.beans.BeanUtils;
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
     * Student submits a project request.
     */
    @Override
    @Transactional
    public void create(Long studentId, ProjectRequestCreateDTO dto) {
        // 1. 查找关联实体
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuleViolationException(MessageConstants.STUDENT_RECORD_NOT_FOUND));
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuleViolationException(MessageConstants.PROJECT_RECORD_NOT_FOUND));

        // 2. 检查截止日期、项目状态和分配限制
        projectRequestValidationService.validateRequest(studentId, project);

        // 3. 构建并保存申请记录
        LocalDateTime now = LocalDateTime.now();
        ProjectRequest request = new ProjectRequest();
        BeanUtils.copyProperties(dto, request, "projectId");
        request.setRequestStatus(ProjectRequest.RequestStatus.PENDING);
        request.setSubmittedAt(now);
        request.setUpdatedAt(now);
        request.setStudent(student);
        request.setProject(project);

        request = requestRepository.save(request);
        saveHistory(request, null, ProjectRequest.RequestStatus.PENDING, student, "学生提交申请。");
    }

    /**
     * 教师审核项目申请。
     */
    @Override
    @Transactional
    public void review(Long requestId, Long teacherId, ProjectRequestReviewDTO dto) {

        ProjectRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.REQUEST_NOT_FOUND));

        if (request.getProject() == null
                || request.getProject().getTeacher() == null
                || !teacherId.equals(request.getProject().getTeacher().getTeacherId())) {
            throw new BusinessException(MessageConstants.CANNOT_REVIEW_OTHER_TEACHER_REQUEST);
        }

        TeacherProfile teacher = teacherProfileRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.TEACHER_NOT_FOUND));

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
                .orElseThrow(() -> new NotFoundException(MessageConstants.REQUEST_NOT_FOUND));
        if (request.getStudent() == null || !studentId.equals(request.getStudent().getStudentId())) {
            throw new BusinessException(MessageConstants.CANNOT_WITHDRAW_OTHER_STUDENT_REQUEST);
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
        ProjectRequestVO requestVO = new ProjectRequestVO();
        BeanUtils.copyProperties(request, requestVO);
        requestVO.setProjectId(request.getProject() == null ? null : request.getProject().getProjectId());
        requestVO.setProjectTitle(request.getProject() == null ? null : request.getProject().getTitle());
        requestVO.setStudentId(request.getStudent() == null ? null : request.getStudent().getStudentId());
        requestVO.setStudentName(request.getStudent() == null || request.getStudent().getUser() == null
                ? null : request.getStudent().getUser().getFullName());
        requestVO.setReviewedByTeacherId(request.getReviewedBy() == null ? null : request.getReviewedBy().getTeacherId());
        return requestVO;
    }

    private void saveHistory(ProjectRequest request,
                             ProjectRequest.RequestStatus oldStatus,
                             ProjectRequest.RequestStatus newStatus,
                             StudentProfile changedBy,
                             String remark) {
        RequestStatusHistory history = new RequestStatusHistory();
        history.setRequest(request);
        history.setOldStatus(oldStatus == null ? null : oldStatus.name());
        history.setNewStatus(newStatus.name());
        history.setChangedBy(changedBy);
        history.setRemark(remark);
        history.setChangedAt(LocalDateTime.now());
        requestStatusHistoryRepository.save(history);
    }
}
