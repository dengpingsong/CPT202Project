package com.cpt202.controller;

import com.cpt202.service.HelloService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for {@link HelloController} using {@code @WebMvcTest}.
 * Only the web layer is loaded; the service is mocked with Mockito.
 */
@WebMvcTest(HelloController.class)
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HelloService helloService;

    @Test
    @DisplayName("GET /api/hello with name param returns greeting")
    void hello_withName_returnsGreeting() throws Exception {
        when(helloService.greet("Alice")).thenReturn("Hello, Alice!");

        mockMvc.perform(get("/api/hello").param("name", "Alice"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value(200))
               .andExpect(jsonPath("$.message").value("Success"))
               .andExpect(jsonPath("$.data").value("Hello, Alice!"));
    }

    @Test
    @DisplayName("GET /api/hello without name param returns default greeting")
    void hello_withoutName_returnsDefaultGreeting() throws Exception {
        when(helloService.greet("")).thenReturn("Hello, World!");

        mockMvc.perform(get("/api/hello"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data").value("Hello, World!"));
    }

    @Test
    @DisplayName("GET /api/health returns UP")
    void health_returnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data").value("UP"));
    }
}
