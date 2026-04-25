package com.cpt202;

import com.cpt202.model.entity.Category;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.Tag;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.CategoryRepository;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TagRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.CallbackAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TeacherControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CallbackAuthService callbackAuthService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectRequestRepository projectRequestRepository;

    private User teacherUser;
    private TeacherProfile teacherProfile;
    private StudentProfile studentProfile;
    private Category category;
    private Tag tagA;
    private Tag tagB;
    private Project project;
    private ProjectRequest request;
    private String teacherAuthorization;

    @BeforeEach
    void setUp() {
        teacherUser = userRepository.save(User.builder()
                .username("teacher1")
                .passwordHash("hashed")
                .email("teacher1@example.com")
                .fullName("Teacher One")
                .role(User.UserRole.TEACHER)
                .accountStatus("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        teacherProfile = teacherProfileRepository.save(TeacherProfile.builder()
                .staffNo("T001")
                .department("Computer Science")
                .title("Professor")
                .researchArea("Distributed Systems")
                .office("Room 101")
                .updatedAt(LocalDateTime.now())
                .user(teacherUser)
                .build());

        User studentUser = userRepository.save(User.builder()
                .username("student1")
                .passwordHash("hashed")
                .email("student1@example.com")
                .fullName("Student One")
                .role(User.UserRole.STUDENT)
                .accountStatus("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        studentProfile = studentProfileRepository.save(StudentProfile.builder()
                .studentNo("S001")
                .programme("Software Engineering")
                .enrollmentDate(LocalDate.of(2024, 9, 1))
                .phone("123456789")
                .interests("AI")
                .updatedAt(LocalDateTime.now())
                .user(studentUser)
                .build());

        category = categoryRepository.save(Category.builder()
                .categoryName("AI")
                .description("Artificial Intelligence")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        tagA = tagRepository.save(Tag.builder()
                .tagName("Java")
                .description("Java")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        tagB = tagRepository.save(Tag.builder()
                .tagName("Spring")
                .description("Spring")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        project = projectRepository.save(Project.builder()
                .teacher(teacherProfile)
                .category(category)
                .title("Initial Project")
                .description("Initial Description")
                .requiredSkills("Java")
                .topicArea("Backend")
                .maxStudents(2)
                .currentAgreedCount(0)
                .projectStatus(Project.ProjectStatus.AVAILABLE)
                .publishDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        request = projectRequestRepository.save(ProjectRequest.builder()
                .project(project)
                .student(studentProfile)
                .preferenceRank(1)
                .notes("Interested")
                .requestStatus(ProjectRequest.RequestStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        teacherAuthorization = "Bearer " + callbackAuthService.generateToken(teacherUser);
    }

    @Test
    void teacherProfileEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/teacher/profile/me")
                        .header("Authorization", teacherAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.teacherId").value(teacherProfile.getTeacherId()))
                .andExpect(jsonPath("$.data.fullName").value("Teacher One"));

        mockMvc.perform(put("/api/teacher/profile/me")
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Teacher Updated",
                                "email", "teacher-updated@example.com",
                                "department", "Data Science",
                                "title", "Associate Professor",
                                "researchArea", "LLM Systems",
                                "office", "Room 202"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        TeacherProfile refreshed = teacherProfileRepository.findById(teacherProfile.getTeacherId()).orElseThrow();
        assertThat(refreshed.getDepartment()).isEqualTo("Data Science");
        assertThat(refreshed.getUser().getFullName()).isEqualTo("Teacher Updated");
    }

    @Test
    void teacherProjectEndpointsWork() throws Exception {
        mockMvc.perform(post("/api/teacher/projects")
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teacherId", teacherProfile.getTeacherId(),
                                "categoryId", category.getCategoryId(),
                                "title", "New Project",
                                "description", "New Description",
                                "requiredSkills", "Spring Boot",
                                "topicArea", "Web",
                                "maxStudents", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Project createdProject = projectRepository.findByTeacher_TeacherIdOrderByCreatedAtDesc(teacherProfile.getTeacherId())
                .stream()
                .filter(savedProject -> "New Project".equals(savedProject.getTitle()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/teacher/projects")
                        .header("Authorization", teacherAuthorization)
                        .param("teacherId", teacherProfile.getTeacherId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].teacherId").value(teacherProfile.getTeacherId()));

        mockMvc.perform(get("/api/teacher/projects/{projectId}", createdProject.getProjectId())
                        .header("Authorization", teacherAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("New Project"));

        mockMvc.perform(put("/api/teacher/projects/{projectId}", createdProject.getProjectId())
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teacherId", teacherProfile.getTeacherId(),
                                "categoryId", category.getCategoryId(),
                                "title", "Updated Project",
                                "description", "Updated Description",
                                "requiredSkills", "Spring Data",
                                "topicArea", "Persistence",
                                "maxStudents", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(put("/api/teacher/projects/{projectId}/status", createdProject.getProjectId())
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teacherId", teacherProfile.getTeacherId(),
                                "projectStatus", "CLOSED",
                                "remark", "Manual close"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Project refreshed = projectRepository.findById(createdProject.getProjectId()).orElseThrow();
        assertThat(refreshed.getTitle()).isEqualTo("Updated Project");
        assertThat(refreshed.getProjectStatus()).isEqualTo(Project.ProjectStatus.CLOSED);
    }

    @Test
    void teacherProjectTagEndpointsWork() throws Exception {
        mockMvc.perform(put("/api/teacher/project-tags/{projectId}", project.getProjectId())
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teacherId", teacherProfile.getTeacherId(),
                                "tagIds", List.of(tagA.getTagId(), tagB.getTagId())))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/api/teacher/project-tags/{projectId}", project.getProjectId())
                        .header("Authorization", teacherAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void teacherRequestEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/teacher/requests")
                        .header("Authorization", teacherAuthorization)
                        .param("teacherId", teacherProfile.getTeacherId().toString())
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].requestId").value(request.getRequestId()));

        mockMvc.perform(put("/api/teacher/requests/{requestId}/review", request.getRequestId())
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teacherId", teacherProfile.getTeacherId(),
                                "requestStatus", "REJECTED",
                                "decisionComment", "Not a match"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        ProjectRequest refreshed = projectRequestRepository.findById(request.getRequestId()).orElseThrow();
        assertThat(refreshed.getRequestStatus()).isEqualTo(ProjectRequest.RequestStatus.REJECTED);
        assertThat(refreshed.getReviewedBy().getTeacherId()).isEqualTo(teacherProfile.getTeacherId());
    }
}
