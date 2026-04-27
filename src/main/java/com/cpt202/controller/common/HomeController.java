package com.cpt202.controller.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 友好 URL 路由控制器。
 * 将语义化路径重定向到 static 目录下的 HTML 文件，
 * 由 Spring Boot 默认静态资源服务器托管，保证 CSS / 图片等相对路径正确解析。
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/login.html";
    }

    @GetMapping("/auth/login")
    public String login() {
        return "redirect:/login.html";
    }

    @GetMapping("/auth/register")
    public String register() {
        return "redirect:/register.html";
    }

    @GetMapping("/auth/forgot-password")
    public String forgotPassword() {
        return "redirect:/forgot-password.html";
    }
}
