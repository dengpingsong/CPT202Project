package com.cpt202.service.impl;

import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.service.RecordService;
import com.cpt202.vo.ProjectVO;
import com.cpt202.vo.ProjectRequestVO;
import com.cpt202.vo.RequestStatusHistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理端记录服务实现类。
 * <p>
 * 用于组装项目记录、申请记录和申请历史记录等管理端所需查询结果。
 */
@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final ProjectRepository projectRepository;
    private final ProjectRequestRepository projectRequestRepository;
    private final RequestStatusHistoryRepository requestStatusHistoryRepository;

    /**
     * 查询项目记录列表。
     *
     * @return 项目展示对象列表
     */
    @Override
    public List<ProjectVO> listProjectRecords() {
        List<Project> projects = projectRepository.findAll();
        List<ProjectVO> projectVos = new ArrayList<>(projects.size());
        for (Project project : projects) {
            projectVos.add(ProjectVO.builder()
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
                    .build());
        }
        return projectVos;
    }

    /**
     * 查询申请记录列表。
     *
     * @param status 申请状态筛选条件
     * @return 申请展示对象列表
     */
    @Override
    public List<ProjectRequestVO> listRequestRecords(ProjectRequest.RequestStatus status) {
        List<ProjectRequest> requests = status == null
                ? projectRequestRepository.findAllByOrderBySubmittedAtDesc()
                : projectRequestRepository.findByRequestStatusOrderBySubmittedAtDesc(status);
        List<ProjectRequestVO> requestVos = new ArrayList<>(requests.size());
        for (ProjectRequest request : requests) {
            requestVos.add(ProjectRequestVO.builder()
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
                    .build());
        }
        return requestVos;
    }

    /**
     * 查询申请历史记录列表。
     *
     * @return 申请状态历史列表
     */
    @Override
    public List<RequestStatusHistoryVO> listRequestHistoryRecords() {
        List<RequestStatusHistory> histories = requestStatusHistoryRepository.findAllByOrderByChangedAtAsc();
        List<RequestStatusHistoryVO> historyVos = new ArrayList<>(histories.size());
        for (RequestStatusHistory history : histories) {
            historyVos.add(RequestStatusHistoryVO.builder()
                    .historyId(history.getHistoryId())
                    .requestId(history.getRequest() == null ? null : history.getRequest().getRequestId())
                    .oldStatus(history.getOldStatus())
                    .newStatus(history.getNewStatus())
                    .changedByStudentId(history.getChangedBy() == null ? null : history.getChangedBy().getStudentId())
                    .changedByStudentName(history.getChangedBy() == null || history.getChangedBy().getUser() == null
                            ? null : history.getChangedBy().getUser().getFullName())
                    .remark(history.getRemark())
                    .changedAt(history.getChangedAt())
                    .build());
        }
        return historyVos;
    }
}
