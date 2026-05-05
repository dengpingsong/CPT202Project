package com.cpt202;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Application smoke tests that cover context startup, Vue SPA routes,
 * static HTML availability, and the documentation endpoint used by CD health checks.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cpt202testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false",
        "jwt.secret=12345678901234567890123456789012",
        "spring.mail.username=noreply@example.com",
        "knife4j.enable=true"
})
@AutoConfigureMockMvc
class CPT202ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // If the application context fails to start, this test will fail.
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/",
            "/login",
            "/register",
            "/student/dashboard",
            "/teacher/projects",
            "/admin/projects"
    })
    void spaRoutesForwardToVueEntry(String requestPath) throws Exception {
        mockMvc.perform(get(requestPath))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @ParameterizedTest
    @CsvSource({
            "/auth/login,/login",
            "/auth/register,/register",
            "/auth/forgot-password,/login#reset",
            "/auth/reset-password?token=test-token,/login?token=test-token#reset"
    })
    void legacyAuthRoutesRedirectToVueRoutes(String requestPath, String redirectTarget) throws Exception {
        mockMvc.perform(get(requestPath))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(redirectTarget));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/login/login.html",
            "/login/register.html",
            "/admin-review/admin_users.html",
            "/admin-review/admin_requests.html",
            "/admin-review/admin_request_history.html",
            "/doc.html"
    })
    void staticPagesAndDocumentationUiAreReachable(String requestPath) throws Exception {
        mockMvc.perform(get(requestPath))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    void openApiDefinitionIsReachable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi").exists());
    }

    @Test
    void studentRegistrationPersistsEnrollmentDateToProfile() throws Exception {
        String registerResponse = mockMvc.perform(post("/api/common/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "date_student_test",
                                  "password": "123456",
                                  "email": "date.student.test@example.com",
                                  "fullName": "Date Student",
                                  "role": "STUDENT",
                                  "studentNo": "S-DATE-001",
                                  "programme": "Software Engineering",
                                  "enrollmentDate": "2024-09-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = registerResponse.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/student/profile/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.studentNo").value("S-DATE-001"))
                .andExpect(jsonPath("$.data.enrollmentDate").value("2024-09-01"));
    }
}
