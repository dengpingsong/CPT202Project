package com.cpt202.controller.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 友好 URL 路由控制器。
 * 将语义化路径重定向到 static 目录下的 HTML 文件，
 * 由 Spring Boot 默认静态资源服务器托管，保证 CSS / 图片等相对路径正确解析。
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/login/login.html";
    }

    @GetMapping("/auth/login")
    public String login() {
        return "redirect:/login/login.html";
    }

    @GetMapping("/auth/register")
    public String register() {
        return "redirect:/login/login.html#register";
    }

    @GetMapping("/auth/forgot-password")
    public String forgotPassword() {
        return "redirect:/login/login.html#reset";
    }

    @GetMapping("/auth/reset-password")
    public String resetPassword(@RequestParam(name = "token", required = false) String token) {
        if (token == null || token.isBlank()) {
            return "redirect:/login/login.html#reset";
        }
        return "redirect:/login/login.html?token=" + token + "#reset";
    }
}
