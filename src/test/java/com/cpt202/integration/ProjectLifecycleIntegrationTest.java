package com.cpt202.integration;

import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.support.ProjectSelectionWorkflowSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Scenario-level integration test for the main business flow.
 *
 * The current backend exposes no administrator archive endpoint. Even though the
 * ProjectStatus enum still contains ARCHIVED, the teacher status API explicitly
 * rejects switching to it and the admin controller surface has no replacement.
 *
 * To keep the scenario executable against the real code, the workflow therefore
 * treats the current terminal state as follows:
 * 1. teacher creates a project with a single seat
 * 2. student submits a request
 * 3. teacher accepts that request
 * 4. the system automatically closes the project because capacity is full
 * 5. admin audits project/request/history records for the finished workflow
 *
 * Each business step is extracted into its own helper method so future changes
 * can insert, remove or rewrite a single step without turning the scenario test
 * into one large opaque method.
 */
@Cpt202IntegrationTest
class ProjectLifecycleIntegrationTest extends ProjectSelectionWorkflowSupport {

    /** Creates one teacher-student-admin scenario fixture before each run. */
    @BeforeEach
    void setUp() {
        initializeProjectSelectionActors("Scenario");
    }

    /**
     * Full role-based lifecycle test.
     *
     * The method itself stays compact and readable by delegating each role-owned
     * action to a helper named after the business step it represents.
     */
    @Test
    void teacherStudentAdminLifecycleFormsAuditableClosedProject() throws Exception {
        Long projectId = teacherPublishesProject("Scenario project used for workflow integration testing.", 1);
        Project createdProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(createdProject.getProjectStatus()).isEqualTo(Project.ProjectStatus.AVAILABLE);
        teacherBindsProjectTags(projectId);
        studentCanDiscoverProject(projectId);

        Long requestId = studentSubmitsRequest(projectId, "Scenario request from student.");

        Project requestedProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(requestedProject.getProjectStatus()).isEqualTo(Project.ProjectStatus.REQUESTED);
        teacherCanSeePendingRequest(requestId);
        teacherApprovesRequestAndSeesClosedProject(projectId, requestId, "Accepted in scenario workflow.");

        ProjectRequest acceptedRequest = projectRequestRepository.findById(requestId).orElseThrow();
        Project closedProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(acceptedRequest.getRequestStatus()).isEqualTo(ProjectRequest.RequestStatus.ACCEPTED);
        assertThat(closedProject.getProjectStatus()).isEqualTo(Project.ProjectStatus.CLOSED);
        assertThat(closedProject.getCurrentAgreedCount()).isEqualTo(1);

        studentCanReviewAcceptedRequestAndHistory(requestId);
        adminCanAuditCompletedWorkflow(projectId, requestId);
    }

    /**
     * PBI 7.2/7.3 evidence: teacher approval records the reviewer, closes a
     * full project, and creates system-authored audit entries for the student's
     * other pending requests.
     */
    @Test
    void teacherApprovalRejectsOtherPendingRequestsWithAuditableActors() throws Exception {
        String originalTitle = projectTitle;
        Long primaryProjectId = teacherPublishesProject("Primary request for allocation consistency.", 1);

        projectTitle = originalTitle + " Backup";
        Long backupProjectId = teacherPublishesProject("Backup request that should be auto-rejected.", 2);

        Long acceptedRequestId = studentSubmitsRequest(primaryProjectId, 1, "Primary application.");
        Long autoRejectedRequestId = studentSubmitsRequest(backupProjectId, 2, "Backup application.");
        String decisionComment = "Accepted after reviewing applicant context.";

        mockMvc.perform(put("/api/teacher/requests/{requestId}/review", acceptedRequestId)
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "requestStatus", "ACCEPTED",
                                "decisionComment", decisionComment))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        ProjectRequest acceptedRequest = projectRequestRepository.findById(acceptedRequestId).orElseThrow();
        ProjectRequest autoRejectedRequest = projectRequestRepository.findById(autoRejectedRequestId).orElseThrow();
        Project primaryProject = projectRepository.findById(primaryProjectId).orElseThrow();

        assertThat(acceptedRequest.getRequestStatus()).isEqualTo(ProjectRequest.RequestStatus.ACCEPTED);
        assertThat(acceptedRequest.getReviewedBy().getTeacherId()).isEqualTo(teacherProfile.getTeacherId());
        assertThat(acceptedRequest.getDecisionComment()).isEqualTo(decisionComment);
        assertThat(autoRejectedRequest.getRequestStatus()).isEqualTo(ProjectRequest.RequestStatus.REJECTED);
        assertThat(autoRejectedRequest.getDecisionComment()).isEqualTo("System auto-rejected: Already matched elsewhere.");
        assertThat(primaryProject.getCurrentAgreedCount()).isEqualTo(1);
        assertThat(primaryProject.getProjectStatus()).isEqualTo(Project.ProjectStatus.CLOSED);

        List<RequestStatusHistory> acceptedHistory =
                requestStatusHistoryRepository.findByRequest_RequestIdOrderByChangedAtAsc(acceptedRequestId);
        List<RequestStatusHistory> autoRejectedHistory =
                requestStatusHistoryRepository.findByRequest_RequestIdOrderByChangedAtAsc(autoRejectedRequestId);

        assertThat(acceptedHistory).extracting(RequestStatusHistory::getNewStatus)
                .containsExactly(ProjectRequest.RequestStatus.PENDING.name(), ProjectRequest.RequestStatus.ACCEPTED.name());
        assertThat(acceptedHistory.get(1).getActorType()).isEqualTo(RequestStatusHistory.HistoryActorType.TEACHER);
        assertThat(acceptedHistory.get(1).getChangedByTeacher().getTeacherId()).isEqualTo(teacherProfile.getTeacherId());
        assertThat(autoRejectedHistory).extracting(RequestStatusHistory::getNewStatus)
                .containsExactly(ProjectRequest.RequestStatus.PENDING.name(), ProjectRequest.RequestStatus.REJECTED.name());
        assertThat(autoRejectedHistory.get(1).getActorType()).isEqualTo(RequestStatusHistory.HistoryActorType.SYSTEM);
    }
}
