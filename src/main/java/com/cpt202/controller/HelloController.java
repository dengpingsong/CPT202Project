package com.cpt202.controller;

import com.cpt202.model.ApiResponse;
import com.cpt202.service.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple REST controller that demonstrates a health-check and greeting endpoint.
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    /**
     * GET /api/hello?name={name}
     * Returns a greeting wrapped in the standard {@link ApiResponse}.
     */
    @GetMapping("/hello")
    public ApiResponse<String> hello(
            @RequestParam(value = "name", defaultValue = "") String name) {
        return ApiResponse.success(helloService.greet(name));
    }

    /**
     * GET /api/health
     * Simple liveness probe.
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("UP");
    }
}
