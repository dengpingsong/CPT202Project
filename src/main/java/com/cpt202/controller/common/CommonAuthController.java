package com.cpt202.controller.common;

import com.cpt202.dto.LoginDTO;
import com.cpt202.dto.EmailOtpLoginDTO;
import com.cpt202.dto.EmailOtpRequestDTO;
import com.cpt202.dto.PasswordResetConfirmDTO;
import com.cpt202.dto.PasswordResetRequestDTO;
import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.dto.TwoFactorLoginVerifyDTO;
import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.UnauthorizedAccessException;
import com.cpt202.properties.JwtProperties;
import com.cpt202.result.Result;
import com.cpt202.service.AuthService;
import com.cpt202.service.JwtTokenService;
import com.cpt202.service.UserAuthStateService;
import com.cpt202.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
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
    private final JwtTokenService jwtTokenService;
    private final UserAuthStateService userAuthStateService;
    private final JwtProperties jwtProperties;

    /**
     * 构造器注入认证服务。
     *
     * @param authService 认证服务
     */
    public CommonAuthController(AuthService authService,
                                JwtTokenService jwtTokenService,
                                UserAuthStateService userAuthStateService,
                                JwtProperties jwtProperties) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
        this.userAuthStateService = userAuthStateService;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 注册新用户并返回登录所需的展示信息。
     *
     * @param registerUserDTO 注册参数
     * @return 登录展示对象
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register a common user",
            description = "Registers a student or teacher account after email OTP verification. The role is inferred from the email domain."
    )
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
    @Operation(
            summary = "Log in a common user",
            description = "Authenticates username and password. Accounts with TOTP enabled receive a temporary two-factor challenge instead of a JWT."
    )
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    /**
     * 发送邮箱验证码登录邮件。
     *
     * @param requestDTO 邮箱参数
     * @return 统一成功响应
     */
    @PostMapping("/email-otp/send")
    @Operation(
            summary = "Send email OTP for login",
            description = "Sends a one-time login code when the email belongs to an active account. The response is intentionally generic to reduce account enumeration."
    )
    public Result<String> sendEmailOtp(@Valid @RequestBody EmailOtpRequestDTO requestDTO) {
        authService.sendEmailLoginOtp(requestDTO);
        return Result.success(MessageConstants.EMAIL_OTP_SENT);
    }

    /**
     * 发送注册邮箱验证码邮件。
     * 调用前会校验邮箱域名是否在允许范围（@student.xjtlu.edu.cn 或 @xjtlu.edu.cn）
     * 以及邮箱是否已被注册。
     *
     * @param requestDTO 邮箱参数
     * @return 统一成功响应
     */
    @PostMapping("/register/email-otp/send")
    @Operation(
            summary = "Send email OTP for registration",
            description = "Sends a one-time registration code after validating the XJTLU student or teacher email domain."
    )
    public Result<String> sendRegisterEmailOtp(@Valid @RequestBody EmailOtpRequestDTO requestDTO) {
        authService.sendEmailRegisterOtp(requestDTO);
        return Result.success(MessageConstants.EMAIL_OTP_SENT);
    }

    /**
     * 通过邮箱验证码登录。
     *
     * @param loginDTO 邮箱验证码登录参数
     * @return 登录展示对象
     */
    @PostMapping("/email-otp/login")
    @Operation(
            summary = "Log in with email OTP",
            description = "Completes passwordless login with a cached one-time email code and consumes the code after success."
    )
    public Result<LoginVO> loginWithEmailOtp(@Valid @RequestBody EmailOtpLoginDTO loginDTO) {
        return Result.success(authService.loginWithEmailOtp(loginDTO));
    }

    @PostMapping("/2fa/verify-login")
    @Operation(
            summary = "Verify password login with TOTP",
            description = "Completes a password login that requires TOTP by validating the temporary challenge token and authenticator code."
    )
    public Result<LoginVO> verifyPasswordLoginTwoFactor(@Valid @RequestBody TwoFactorLoginVerifyDTO verifyDTO) {
        return Result.success(authService.verifyPasswordLoginTwoFactor(verifyDTO));
    }

    /**
     * 发送密码重置链接邮件。
     *
     * @param requestDTO 邮箱参数
     * @return 统一成功响应
     */
    @PostMapping("/forgot-password")
    @Operation(
            summary = "Send password reset email",
            description = "Sends a password reset link for active accounts. Unknown or inactive accounts receive the same success response to reduce account enumeration."
    )
    public Result<String> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO requestDTO) {
        authService.requestPasswordReset(requestDTO);
        return Result.success(MessageConstants.PASSWORD_RESET_EMAIL_SENT);
    }

    /**
     * 通过重置令牌更新密码。
     *
     * @param confirmDTO 重置确认参数
     * @return 统一成功响应
     */
    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password by email token",
            description = "Consumes a valid reset token, updates the password, and invalidates cached authentication state for the account."
    )
    public Result<Void> resetPassword(@Valid @RequestBody PasswordResetConfirmDTO confirmDTO) {
        authService.resetPassword(confirmDTO);
        return Result.success();
    }

    /**
     * 用户退出登录。
     *
     * @return 统一成功响应
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Log out the current user",
            description = "Requires a Bearer token and clears the cached authentication state for the current account."
    )
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = jwtTokenService.parseToken(extractBearerToken(request)).userId();
        userAuthStateService.evictUserAuthState(userId);
        return Result.success();
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(jwtProperties.getTokenName());
        String tokenPrefix = jwtProperties.getTokenPrefix();
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(tokenPrefix)) {
            throw new UnauthorizedAccessException(MessageConstants.INVALID_BEARER_TOKEN);
        }

        String token = authorization.substring(tokenPrefix.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedAccessException(MessageConstants.INVALID_BEARER_TOKEN);
        }
        return token;
    }
}
