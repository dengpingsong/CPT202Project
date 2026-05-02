package com.cpt202.service;

import com.cpt202.dto.LoginDTO;
import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.dto.PasswordResetConfirmDTO;
import com.cpt202.dto.PasswordResetRequestDTO;
import com.cpt202.vo.LoginVO;

/**
 * 认证服务接口。
 * <p>
 * 负责处理用户注册与登录逻辑，并返回前端登录态所需的展示信息。
 */
public interface AuthService {

    /**
     * 注册新用户。
     *
     * @param registerUserDTO 注册参数
     * @return 登录展示对象
     */
    LoginVO register(RegisterUserDTO registerUserDTO);

    /**
     * 用户登录。
     *
     * @param loginDTO 登录参数
     * @return 登录展示对象
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 根据邮箱发送密码重置链接。
     *
     * @param requestDTO 邮箱参数
     */
    void requestPasswordReset(PasswordResetRequestDTO requestDTO);

    /**
     * 通过重置令牌更新密码。
     *
     * @param confirmDTO 重置确认参数
     */
    void resetPassword(PasswordResetConfirmDTO confirmDTO);
}
