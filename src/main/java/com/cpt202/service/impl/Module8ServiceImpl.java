package com.cpt202.service.impl;

import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.ProjectRequest.RequestStatus;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.service.Module8Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Module8ServiceImpl implements Module8Service {

    private final ProjectRequestRepository requestRepository;
    private final ProjectRepository projectRepository;

    @Override
    public void validateRequest(Long studentId) {
        // 1. 截止日期校验 (保持不变)
        LocalDateTime deadline = LocalDateTime.of(2026, 5, 29, 23, 59);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new RuleViolationException("The deadline has passed.");
        }

        // 2. 严厉校验：只要有“待处理”或者“已通过”的申请，就直接弹开！
        // 我们改用 existsBy...AndRequestStatusIn 这个方法
        boolean alreadyHasRequest = requestRepository.existsByStudent_StudentIdAndRequestStatusIn(
                studentId,
                List.of(RequestStatus.PENDING, RequestStatus.ACCEPTED)
        );

        if (alreadyHasRequest) {
            throw new RuleViolationException("Operation failed: You already have an active or accepted request.");
        }
    }

    @Transactional
    @Override
    public void onApprovalSuccess(Long requestId) {
        ProjectRequest currentRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuleViolationException("Request record not found."));

        // 修改点：通过对象层级获取 ID
        Long studentId = currentRequest.getStudent().getStudentId();
        Long projectId = currentRequest.getProject().getProjectId();

        List<ProjectRequest> others = requestRepository.findByStudent_StudentIdAndRequestStatus(studentId, RequestStatus.PENDING);
        others.forEach(r -> {
            // 修改点：主键叫 requestId
            if (!r.getRequestId().equals(requestId)) {
                r.setRequestStatus(RequestStatus.REJECTED);
                r.setDecisionComment("System auto-rejected: Already matched elsewhere.");
            }
        });
        requestRepository.saveAll(others);

        Project project = projectRepository.findById(projectId).orElseThrow();
        long currentCount = requestRepository.countByProject_ProjectIdAndRequestStatus(projectId, RequestStatus.ACCEPTED);

        if (currentCount >= project.getMaxStudents()) {
            // 这里确保 Project 实体类里有 setProjectStatus 方法和 CLOSED 枚举
            project.setProjectStatus(Project.ProjectStatus.CLOSED);
            projectRepository.save(project);
        }
    }
}