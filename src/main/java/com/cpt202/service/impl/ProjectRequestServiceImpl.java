package com.cpt202.service.impl;

import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.StudentProfileRepository;
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
