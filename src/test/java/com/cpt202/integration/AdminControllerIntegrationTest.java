package com.cpt202.integration;

import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for administrator-facing controllers.
 *
 * The admin role in this codebase is mostly about audit and maintenance, so the
 * test suite focuses on profile management, category/tag CRUD, user-state updates
 * and record visibility after a real teacher/student workflow has taken place.
 */
@Cpt202IntegrationTest
class AdminControllerIntegrationTest extends IntegrationTestSupport {

    private User adminUser;
    private String adminAuthorization;
    private User studentUser;
    private Project project;
    private ProjectRequest acceptedRequest;
    private String boundTagName;

    @BeforeEach
    void setUp() {
        String suffix = uniqueSuffix();

        adminUser = createAdminUser(
                "admin" + suffix,
                "admin" + suffix + "@example.com",
                "Admin " + suffix
        );
        adminAuthorization = authorizationFor(adminUser);

        User teacherUser = createUser(
                "teacher-admin" + suffix,
                "teacher-admin" + suffix + "@example.com",
                "Teacher Admin " + suffix,
                User.UserRole.TEACHER
        );
        TeacherProfile teacherProfile = createTeacherProfile(teacherUser, "TA" + suffix);

        studentUser = createUser(
                "student-admin" + suffix,
                "student-admin" + suffix + "@example.com",
                "Student Admin " + suffix,
                User.UserRole.STUDENT
        );
        StudentProfile studentProfile = createStudentProfile(studentUser, "SA" + suffix);

        project = createProject(teacherProfile, createCategory("AdminFlowCategory" + suffix), "Admin Flow Project " + suffix, 1);
        boundTagName = "AdminFlowTag" + suffix;
        Long boundTagId = createTag(boundTagName).getTagId();
        projectTagService.bindProjectTags(project.getProjectId(), teacherProfile.getTeacherId(), List.of(boundTagId));

        ProjectRequestCreateDTO createDTO = new ProjectRequestCreateDTO();
        createDTO.setProjectId(project.getProjectId());
        createDTO.setPreferenceRank(1);
        createDTO.setNotes("Prepared for admin record assertions.");
        projectRequestService.create(studentProfile.getStudentId(), createDTO);

        acceptedRequest = projectRequestRepository.findByStudent_StudentIdOrderBySubmittedAtDesc(studentProfile.getStudentId())
                .stream()
                .findFirst()
                .orElseThrow();

        ProjectRequestReviewDTO reviewDTO = new ProjectRequestReviewDTO();
        reviewDTO.setRequestStatus(ProjectRequest.RequestStatus.ACCEPTED);
        reviewDTO.setDecisionComment("Accepted for admin workflow checks.");
        projectRequestService.review(acceptedRequest.getRequestId(), teacherProfile.getTeacherId(), reviewDTO);

        acceptedRequest = projectRequestRepository.findById(acceptedRequest.getRequestId()).orElseThrow();
    }

    /**
     * Profile endpoints are the lightest admin surface, so this test checks the
     * token-authenticated read/update path before moving on to broader admin APIs.
     */
    @Test
    void adminProfileEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/admin/profile/me")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.fullName").value(adminUser.getFullName()))
                .andExpect(jsonPath("$.data.email").value(adminUser.getEmail()));

        mockMvc.perform(put("/api/admin/profile/me")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Updated Admin",
                                "email", "updated-admin@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        User refreshedAdmin = userRepository.findById(adminUser.getUserId()).orElseThrow();
        assertThat(refreshedAdmin.getFullName()).isEqualTo("Updated Admin");
        assertThat(refreshedAdmin.getEmail()).isEqualTo("updated-admin@example.com");
    }

    /**
     * Category CRUD is isolated with unique names so the assertions stay stable
     * even when seed data or other tests populate the same in-memory database.
     */
    @Test
    void adminCategoryEndpointsWork() throws Exception {
        String suffix = uniqueSuffix();
        String categoryName = "ManagedCategory" + suffix;

        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryName", categoryName,
                                "description", "Created by admin integration test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Long categoryId = categoryRepository.findAll().stream()
                .filter(category -> categoryName.equals(category.getCategoryName()))
                .findFirst()
                .orElseThrow()
                .getCategoryId();

        mockMvc.perform(get("/api/admin/categories")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].categoryName").value(hasItem(categoryName)));

        mockMvc.perform(get("/api/admin/categories/{categoryId}", categoryId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryName").value(categoryName));

        mockMvc.perform(put("/api/admin/categories/{categoryId}", categoryId)
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryName", categoryName + "Updated",
                                "description", "Updated by admin integration test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(categoryRepository.findById(categoryId).orElseThrow().getCategoryName())
                .isEqualTo(categoryName + "Updated");

        mockMvc.perform(delete("/api/admin/categories/{categoryId}", categoryId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    /**
     * Tag CRUD mirrors category CRUD but uses a different resource, so keeping it
     * in its own test makes failures point directly to the broken admin surface.
     */
    @Test
    void adminTagEndpointsWork() throws Exception {
        String suffix = uniqueSuffix();
        String tagName = "ManagedTag" + suffix;

        mockMvc.perform(post("/api/admin/tags")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tagName", tagName,
                                "description", "Created by admin integration test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Long tagId = tagRepository.findAll().stream()
                .filter(tag -> tagName.equals(tag.getTagName()))
                .findFirst()
                .orElseThrow()
                .getTagId();

        mockMvc.perform(get("/api/admin/tags")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].tagName").value(hasItem(tagName)));

        mockMvc.perform(get("/api/admin/tags/{tagId}", tagId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tagName").value(tagName));

        mockMvc.perform(put("/api/admin/tags/{tagId}", tagId)
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tagName", tagName + "Updated",
                                "description", "Updated by admin integration test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(tagRepository.findById(tagId).orElseThrow().getTagName()).isEqualTo(tagName + "Updated");

        mockMvc.perform(delete("/api/admin/tags/{tagId}", tagId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(tagRepository.findById(tagId)).isEmpty();
    }

    /**
     * User management is one of the few write operations owned solely by admin,
     * so the test validates both filtered listing and status mutation.
     */
    @Test
    void adminUserEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", adminAuthorization)
                        .param("role", "STUDENT")
                        .param("accountStatus", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data[?(@.userId == %d)].accountStatus", studentUser.getUserId()))
                        .value(hasItem("ACTIVE")));

        mockMvc.perform(put("/api/admin/users/{userId}/status", studentUser.getUserId())
                        .header("Authorization", adminAuthorization)
                        .param("accountStatus", "DISABLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(userRepository.findById(studentUser.getUserId()).orElseThrow().getAccountStatus())
                .isEqualTo("DISABLED");
    }

    /**
     * Record endpoints are the admin-side view of the entire business workflow.
     * This test verifies the admin can see the closed project, the accepted
     * request, the request history and the project's bound tags.
     */
    @Test
    void adminRecordAndProjectTagEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/admin/records/projects")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data[?(@.projectId == %d)].projectStatus", project.getProjectId()))
                        .value(hasItem("CLOSED")));

        mockMvc.perform(get("/api/admin/records/requests")
                        .header("Authorization", adminAuthorization)
                        .param("status", "ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data[?(@.requestId == %d)].requestStatus", acceptedRequest.getRequestId()))
                        .value(hasItem("ACCEPTED")));

        mockMvc.perform(get("/api/admin/records/request-history")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data[?(@.requestId == %d)].newStatus", acceptedRequest.getRequestId()))
                        .value(hasItem("ACCEPTED")));

        mockMvc.perform(get("/api/admin/projects/{projectId}/tags", project.getProjectId())
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[*].tagName").value(hasItem(boundTagName)));
    }
}