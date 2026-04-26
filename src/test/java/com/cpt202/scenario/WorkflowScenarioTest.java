package com.cpt202.scenario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 场景测试（Scenario Tests）— 模拟真实用户通过前端 fetch() 调用后端 API 的完整流程。
 *
 * <h2>与其他测试的区别</h2>
 * <ul>
 *   <li>{@code @Tag("backend")} / TeacherControllerIntegrationTest：单个 Controller 端点的隔离测试，
 *       使用 {@code @Transactional} 自动回滚，不跨角色。</li>
 *   <li>{@code @Tag("frontend")} / CPT202ApplicationTests：静态页面可达性与 HTML 结构测试，不涉及业务。</li>
 *   <li>{@code @Tag("scenario")} / 本类：跨角色、跨步骤的完整业务流测试，<strong>不使用 @Transactional</strong>，
 *       每一步的数据库写入对下一步可见，完整模拟前端 JS fetch() 的调用链。</li>
 * </ul>
 *
 * <h2>为什么不能用 @Transactional？</h2>
 * 场景测试中每个步骤都依赖上一步写入数据库的数据（如注册后才能登录，创建项目后才能申请）。
 * {@code @Transactional} 会在测试结束时回滚所有变更，但更重要的是，
 * 同一事务内的写入对 MockMvc 发起的请求（在不同事务中执行）不可见，会导致后续步骤失败。
 *
 * <h2>数据隔离</h2>
 * H2 in-memory 数据库在每次测试运行时重建（ddl-auto=create-drop），
 * 因此不同测试方法之间天然隔离，无需手动清理。
 */
@Tag("scenario")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cpt202scenariodb;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false",
        "jwt.secret=12345678901234567890123456789012"
})
@AutoConfigureMockMvc
class WorkflowScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ============================================================
    // 场景一：注册 → 登录 → 查看个人资料
    //
    // 对应前端行为：
    //   register.html  → POST /api/common/auth/register
    //   login.html     → POST /api/common/auth/login
    //   student-profile.html → GET /api/student/profile/me
    // ============================================================

    @Test
    void studentRegistersLogsInAndViewsProfile() throws Exception {
        // ── Step 1: 注册学生账号（模拟 register.html 表单提交）──────────────────
        String token = doRegister(Map.of(
                "username", "stu_scenario_1",
                "password", "Pass1234!",
                "email", "stu_s1@test.com",
                "fullName", "Scenario Student",
                "role", "STUDENT",
                "studentNo", "2024001",
                "programme", "Software Engineering",
                "enrollmentDate", "2024-09-01"
        ));

        assertThat(token).isNotBlank();

        // ── Step 2: 用相同凭证登录（模拟 login.html 表单提交）───────────────────
        //   前端拿到 token 后存入 localStorage，后续请求带上 Authorization 头
        String loginToken = doLogin("stu_scenario_1", "Pass1234!");
        assertThat(loginToken).isNotBlank();

        // ── Step 3: 携带 token 访问个人资料（模拟 student-profile.html 初始化）──
        mockMvc.perform(get("/api/student/profile/me")
                        .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.username").value("stu_scenario_1"))
                .andExpect(jsonPath("$.data.studentNo").value("2024001"))
                .andExpect(jsonPath("$.data.programme").value("Software Engineering"));
    }

    // ============================================================
    // 场景二：完整项目生命周期（跨角色异步流程）
    //
    // 角色时间线：
    //   Admin  → 创建分类 → 创建标签
    //   Teacher→ 注册 → 发布项目 → 绑定标签
    //   Student→ 注册 → 申请项目 → 查看申请记录
    //
    // 对应前端行为：
    //   Admin: admin panel create-category / create-tag
    //   Teacher: teacher-review/create-project.html → POST /api/teacher/projects
    //            teacher-review/index.html          → PUT /api/teacher/project-tags/{id}
    //   Student: student-project-search-list.html   → GET /api/student/projects
    //            student-request-history.html        → GET /api/student/requests
    // ============================================================

    @Test
    void projectLifecycleAcrossAdminTeacherAndStudent() throws Exception {

        // ════════════════════════════════════════════════════════
        // PHASE 1 — Admin 初始化系统基础数据
        // ════════════════════════════════════════════════════════

        // Step 1: 注册管理员
        String adminToken = doRegister(Map.of(
                "username", "admin_lifecycle",
                "password", "Admin1234!",
                "email", "admin_lc@test.com",
                "fullName", "System Admin",
                "role", "ADMIN"
        ));

        // Step 2: Admin 创建项目分类（模拟管理后台操作）
        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryName", "Artificial Intelligence",
                                "description", "AI-related projects"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // Step 3: 查询分类列表，拿到刚创建的 categoryId
        //   ← 前端在 create-project.html 下拉框里拿到这个列表
        Long categoryId = fetchFirstIdByName(
                get("/api/admin/categories").header("Authorization", "Bearer " + adminToken),
                "categoryName", "Artificial Intelligence",
                "categoryId"
        );

        // Step 4: Admin 创建标签
        mockMvc.perform(post("/api/admin/tags")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tagName", "Machine Learning",
                                "description", "ML methods"
                        ))))
                .andExpect(status().isOk());

        // Step 5: 查询标签列表，拿到 tagId
        Long tagId = fetchFirstIdByName(
                get("/api/admin/tags").header("Authorization", "Bearer " + adminToken),
                "tagName", "Machine Learning",
                "tagId"
        );

        // ════════════════════════════════════════════════════════
        // PHASE 2 — Teacher 注册并发布项目
        // ════════════════════════════════════════════════════════

        // Step 6: 注册教师账号（registration 同时创建 TeacherProfile）
        MvcResult teacherRegResult = doRegisterAndReturn(Map.of(
                "username", "teacher_lifecycle",
                "password", "Teacher1234!",
                "email", "teacher_lc@test.com",
                "fullName", "Dr. Lifecycle",
                "role", "TEACHER",
                "staffNo", "T999",
                "department", "Computer Science",
                "title", "Lecturer"
        ));
        String teacherToken = extractToken(teacherRegResult);
        // authContext.userId() 就是教师的 User ID，用于 ProjectDTO.teacherId 的权限校验
        Long teacherUserId = extractUserId(teacherRegResult);

        // Step 7: 教师创建项目（模拟 create-project.html 提交）
        //   ProjectDTO.teacherId 必须与 JWT 中的 userId 相同（见 ensureCurrentTeacher）
        mockMvc.perform(post("/api/teacher/projects")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teacherId", teacherUserId,
                                "categoryId", categoryId,
                                "title", "Deep Learning Research",
                                "description", "Research on deep neural networks",
                                "requiredSkills", "Python, PyTorch",
                                "topicArea", "Neural Networks",
                                "maxStudents", 2
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // Step 8: 查询教师项目列表，拿到刚创建的 projectId
        //   ← 这与前端 projects.html 列表页逻辑完全一致
        Long projectId = fetchFirstIdByName(
                get("/api/teacher/projects")
                        .param("teacherId", String.valueOf(teacherUserId))
                        .header("Authorization", "Bearer " + teacherToken),
                "title", "Deep Learning Research",
                "projectId"
        );

        // Step 9: 教师为项目绑定标签（模拟 project detail 页标签操作）
        mockMvc.perform(put("/api/teacher/project-tags/" + projectId)
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teacherId", teacherUserId,
                                "tagIds", List.of(tagId)
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 验证标签确实绑定成功
        mockMvc.perform(get("/api/teacher/project-tags/" + projectId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tagName").value("Machine Learning"));

        // ════════════════════════════════════════════════════════
        // PHASE 3 — Student 注册并申请项目
        // ════════════════════════════════════════════════════════

        // Step 10: 注册学生账号
        MvcResult studentRegResult = doRegisterAndReturn(Map.of(
                "username", "student_lifecycle",
                "password", "Student1234!",
                "email", "student_lc@test.com",
                "fullName", "Alice Lifecycle",
                "role", "STUDENT",
                "studentNo", "2024099",
                "programme", "Data Science",
                "enrollmentDate", "2024-09-01"
        ));
        String studentToken = extractToken(studentRegResult);
        // studentId 同样使用 User ID（见 ensureCurrentStudent 权限校验逻辑）
        Long studentUserId = extractUserId(studentRegResult);

        // Step 11: 学生提交项目申请（模拟 student-project-search-list-detail.html 申请按钮）
        mockMvc.perform(post("/api/student/requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "projectId", projectId,
                                "studentId", studentUserId,
                                "preferenceRank", 1,
                                "notes", "I am very interested in deep learning research."
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // Step 12: 学生查看自己的申请记录（模拟 student-request-history.html）
        mockMvc.perform(get("/api/student/requests")
                        .param("studentId", String.valueOf(studentUserId))
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].projectId").value(projectId))
                .andExpect(jsonPath("$.data[0].requestStatus").value("PENDING"));
    }

    // ============================================================
    // 私有辅助方法 — 对应前端 JS 中常用的 fetch + localStorage 操作
    // ============================================================

    /**
     * 注册用户并返回 JWT token（对应前端 register.html 的 fetch POST + localStorage.setItem('token')）。
     */
    private String doRegister(Map<String, Object> payload) throws Exception {
        return extractToken(doRegisterAndReturn(payload));
    }

    /**
     * 注册用户并返回完整 MvcResult（供同时需要 token 和 userId 的场景使用）。
     */
    private MvcResult doRegisterAndReturn(Map<String, Object> payload) throws Exception {
        return mockMvc.perform(post("/api/common/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();
    }

    /**
     * 登录并返回 JWT token（对应前端 login.html 的 fetch POST + localStorage.setItem('token')）。
     */
    private String doLogin(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/common/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();
        return extractToken(result);
    }

    /**
     * 从响应中提取 JWT token（对应前端 const token = data.token）。
     */
    private String extractToken(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }

    /**
     * 从响应中提取 userId（对应前端 localStorage.setItem('userId', data.userId)）。
     */
    private Long extractUserId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("userId").asLong();
    }

    /**
     * 执行一个 GET 请求，在返回的列表中按 nameField 找到匹配项，返回其 idField 值。
     * 对应前端：fetch list → find item → extract id。
     *
     * @param requestBuilder 构造好的 GET 请求（含 Authorization 头）
     * @param nameField      JSON 中用于匹配名称的字段名
     * @param nameValue      期望匹配的名称值
     * @param idField        期望提取的 ID 字段名
     */
    private Long fetchFirstIdByName(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requestBuilder,
            String nameField,
            String nameValue,
            String idField
    ) throws Exception {
        MvcResult result = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        JsonNode dataArray = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertThat(dataArray.isArray()).as("$.data should be an array").isTrue();

        for (JsonNode node : dataArray) {
            if (nameValue.equals(node.path(nameField).asText())) {
                return node.path(idField).asLong();
            }
        }
        throw new AssertionError("No item with " + nameField + "='" + nameValue + "' found in response");
    }
}
