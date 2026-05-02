package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.ProjectRequest.RequestStatus;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.service.ProjectRequestValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectRequestValidationServiceImpl implements ProjectRequestValidationService {

    private final ProjectRequestRepository requestRepository;
    private final ProjectRepository projectRepository;
    private final RequestStatusHistoryRepository requestStatusHistoryRepository;

    @Override
    public void validateRequest(Long studentId, Project project) {
        // 1. 截止日期校验
        LocalDateTime deadline = LocalDateTime.of(2026, 5, 29, 23, 59);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new RuleViolationException(MessageConstants.REQUEST_DEADLINE_PASSED);
        }

        // 2. 已关闭或已归档的项目不能接收新申请。
        if (project == null
                || project.getProjectStatus() == Project.ProjectStatus.CLOSED
                || project.getProjectStatus() == Project.ProjectStatus.ARCHIVED) {
            throw new RuleViolationException(MessageConstants.PROJECT_NOT_ACCEPTING_REQUESTS);
        }

        // 3. 只限制已经持有 agreed 项目的学生继续申请。
        boolean alreadyHasRequest = requestRepository.existsByStudent_StudentIdAndRequestStatusIn(
                studentId,
                List.of(RequestStatus.ACCEPTED)
        );

        if (alreadyHasRequest) {
            throw new RuleViolationException(MessageConstants.ACTIVE_REQUEST_EXISTS);
        }
    }

    @Transactional
    @Override
    public void onApprovalSuccess(Long requestId) {
        ProjectRequest currentRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuleViolationException(MessageConstants.REQUEST_RECORD_NOT_FOUND));

        Long studentId = currentRequest.getStudent().getStudentId();
        Long projectId = currentRequest.getProject().getProjectId();

        List<ProjectRequest> others = requestRepository.findByStudent_StudentIdAndRequestStatus(studentId, RequestStatus.PENDING);
        for (ProjectRequest request : others) {
            if (request.getRequestId().equals(requestId)) {
                continue;
            }

            RequestStatus oldStatus = request.getRequestStatus();
            LocalDateTime now = LocalDateTime.now();
            request.setRequestStatus(RequestStatus.REJECTED);
            request.setDecisionComment("System auto-rejected: Already matched elsewhere.");
            request.setUpdatedAt(now);

            RequestStatusHistory history = new RequestStatusHistory();
            history.setRequest(request);
            history.setOldStatus(oldStatus == null ? null : oldStatus.name());
            history.setNewStatus(RequestStatus.REJECTED.name());
            history.setChangedBy(null);
            history.setRemark("系统自动驳回：该学生已在其他项目中被录取。");
            history.setChangedAt(now);
            requestStatusHistoryRepository.save(history);
        }
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
