package com.cpt202.integration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Application smoke tests that cover context startup, friendly page routes,
 * static HTML availability, and the documentation endpoint used by CD health checks.
 */
@SpringBootTest(properties = "knife4j.enable=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IntegrationTestConfiguration.class)
class CPT202ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    /** Confirms the Spring application context can boot in the test profile. */
    @Test
    void contextLoads() {
        // If the application context fails to start, this test will fail.
    }

    /** Verifies friendly routes redirect to the expected static entry pages. */
    @ParameterizedTest
    @CsvSource({
            "/,/login/login.html",
            "/auth/login,/login/login.html",
            "/auth/register,/login/login.html#register",
            "/auth/forgot-password,/login/login.html#reset",
            "/auth/reset-password?token=test-token,/login/login.html?token=test-token#reset"
    })
    void friendlyRoutesRedirectToExpectedPages(String requestPath, String redirectTarget) throws Exception {
        mockMvc.perform(get(requestPath))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(redirectTarget));
    }

            /** Verifies the main static pages and documentation UI are reachable. */
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

    /** Verifies the OpenAPI JSON document is exposed for tooling and health checks. */
    @Test
    void openApiDefinitionIsReachable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi").exists());
    }
}
