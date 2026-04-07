package com.cpt202.service.impl;

import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.service.ProjectRequestValidationService;
import com.cpt202.service.ProjectRequestService;
import com.cpt202.vo.ProjectRequestVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目申请服务实现类。
 * <p>
 * 当前阶段仅完成接口定义，后续将在此落地申请提交、撤回与审核的业务逻辑。
 */
@Service
public class ProjectRequestServiceImpl implements ProjectRequestService {

    private final ProjectRequestRepository requestRepository;
    private final ProjectRepository projectRepository;
    private final StudentProfileRepository studentRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final ProjectRequestValidationService projectRequestValidationService;

    /**
     * 学生提交项目申请。
     */
    @Override
    @Transactional
    public void create(ProjectRequestCreateDTO dto) {
        // 1. 检查截止日期和“一人一选”限制
        projectRequestValidationService.validateRequest(dto.getStudentId());

    /**
     * 学生提交项目申请。
     */
    @Override
    @Transactional
    public void create(ProjectRequestCreateDTO dto) {
        // 1. 检查截止日期和“一人一选”限制
        projectRequestValidationService.validateRequest(dto.getStudentId());

    /**
     * 查询学生申请列表。
     */
    @Override
    public void create(ProjectRequestCreateDTO projectRequestCreateDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 学生提交项目申请。
     */
    @Override
    @Transactional
    public void create(ProjectRequestCreateDTO dto) {
        // 1. 检查截止日期和“一人一选”限制
        projectRequestValidationService.validateRequest(dto.getStudentId());

    /**
     * 查询学生申请列表。
     */
    @Override
    public void create(ProjectRequestCreateDTO projectRequestCreateDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 查询学生申请列表。
     */
    @Override
    public List<ProjectRequestVO> listStudentRequests(Long studentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 查询教师待审核申请列表。
     */
    @Override
    public List<ProjectRequestVO> listTeacherRequests(Long teacherId, ProjectRequest.RequestStatus status) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 学生撤回项目申请。
     */
    @Override
    public void withdraw(Long requestId, Long studentId) {
        throw new UnsupportedOperationException("Not implemented yet");
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

        request.setRequestStatus(dto.getRequestStatus());
        request.setDecisionComment(dto.getDecisionComment());
        request.setReviewedBy(teacher);
        request.setReviewedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        requestRepository.save(request);

        if (dto.getRequestStatus() == ProjectRequest.RequestStatus.ACCEPTED) {
            projectRequestValidationService.onApprovalSuccess(requestId);
        }
    }

    // --- 下面这些方法可以后续根据需求慢慢补全，暂时保留占位符防止报错 ---

    @Override
    public List<ProjectRequestVO> listStudentRequests(Long studentId) {
        return requestRepository.findByStudent_StudentIdOrderBySubmittedAtDesc(studentId)
                .stream()
                .map(this::toProjectRequestVO)
                .toList();
    }

    @Override
    public List<ProjectRequestVO> listTeacherRequests(Long teacherId, ProjectRequest.RequestStatus status) {
        List<ProjectRequest> requests = status == null
                ? requestRepository.findByProject_Teacher_TeacherIdOrderBySubmittedAtDesc(teacherId)
                : requestRepository.findByProject_Teacher_TeacherIdAndRequestStatusOrderBySubmittedAtDesc(teacherId, status);
        return requests.stream().map(this::toProjectRequestVO).toList();
    }

    @Override
    public void withdraw(Long requestId, Long studentId) {
        ProjectRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("申请记录不存在。"));
        if (request.getStudent() == null || !studentId.equals(request.getStudent().getStudentId())) {
            throw new BusinessException("不能撤回其他学生的申请。");
        }
        request.setRequestStatus(ProjectRequest.RequestStatus.WITHDRAWN);
        request.setWithdrawnAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
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
}
