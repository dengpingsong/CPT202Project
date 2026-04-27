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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Application smoke tests that cover context startup, friendly page routes,
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
    @CsvSource({
            "/,/login/login.html",
            "/auth/login,/login/login.html",
            "/auth/register,/login/login.html#register",
            "/auth/forgot-password,/login/login.html#reset"
    })
    void friendlyRoutesRedirectToExpectedPages(String requestPath, String redirectTarget) throws Exception {
        mockMvc.perform(get(requestPath))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(redirectTarget));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/login/login.html",
            "/login/register.html",
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
}
