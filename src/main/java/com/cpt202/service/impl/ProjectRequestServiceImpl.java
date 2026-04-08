package com.cpt202.service.impl;

import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.service.ProjectRequestValidationService;
import com.cpt202.service.ProjectRequestService;
import com.cpt202.vo.ProjectRequestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Student submits a project request. (PBI 1 & 2)
     */
    @Override
    @Transactional
    public void create(ProjectRequestCreateDTO dto) {
        // 1. 调用 Module 8 校验：检查截止日期和“一人一选”限制
        module8Service.validateRequest(dto.getStudentId());

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
                .requestStatus(ProjectRequest.RequestStatus.PENDING) // 初始状态为待处理
                .submittedAt(LocalDateTime.now())
                .build();

        requestRepository.save(request);
    }

    /**
     * 教师审核项目申请。
     */
    @Override
    @Transactional
    public void review(Long requestId, ProjectRequestReviewDTO dto) {

        ProjectRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuleViolationException("Request record not found."));

        request.setRequestStatus(dto.getRequestStatus());

        request.setDecisionComment(dto.getDecisionComment());

        request.setReviewedAt(LocalDateTime.now());

        requestRepository.save(request);

        if (dto.getRequestStatus() == ProjectRequest.RequestStatus.ACCEPTED) {
            projectRequestValidationService.onApprovalSuccess(requestId);
        }
    }

    // --- 下面这些方法可以后续根据需求慢慢补全，暂时保留占位符防止报错 ---

    @Override
    public List<ProjectRequestVO> listStudentRequests(Long studentId) {
        // 后续实现：查询该生的所有申请并转为 VO 供前端展示
        return List.of();
    }

    @Override
    public List<ProjectRequestVO> listTeacherRequests(Long teacherId, ProjectRequest.RequestStatus status) {
        return List.of();
    }

    @Override
    public void withdraw(Long requestId, Long studentId) {
        // 后续实现：学生撤回申请
    }
}
