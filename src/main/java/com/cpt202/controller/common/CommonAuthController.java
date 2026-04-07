package com.cpt202.controller.common;

import com.cpt202.dto.LoginDTO;
import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.result.Result;
import com.cpt202.service.AuthService;
import com.cpt202.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 公共认证接口控制器。
 * 负责注册、登录与退出等无需区分学生/教师/管理员入口的通用认证能力。
 */
@RestController
@RequestMapping("/api/common/auth")
@Tag(name = "Common Authentication API")
public class CommonAuthController {

    private final AuthService authService;

    /**
     * 构造器注入认证服务。
     *
     * @param authService 认证服务
     */
    public CommonAuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 注册新用户并返回登录所需的展示信息。
     *
     * @param registerUserDTO 注册参数
     * @return 登录展示对象
     */
    @PostMapping("/register")
    @Operation(summary = "Register a common user")
    public Result<LoginVO> register(@Valid @RequestBody RegisterUserDTO registerUserDTO) {
        return Result.success(authService.register(registerUserDTO));
    }

    /**
     * 用户登录。
     *
     * @param loginDTO 登录参数
     * @return 登录展示对象
     */
    @PostMapping("/login")
    @Operation(summary = "Log in a common user")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    /**
     * 用户退出登录。
     *
     * @return 统一成功响应
     */
    @PostMapping("/logout")
    @Operation(summary = "Log out the current user")
    public Result<Void> logout() {
        return Result.success();
    }
}
