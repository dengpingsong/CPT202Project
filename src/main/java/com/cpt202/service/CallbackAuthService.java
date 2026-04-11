package com.cpt202.service;

import com.cpt202.model.entity.User;
import com.cpt202.security.AuthContext;

/**
 * 回调式权限认证服务。
 * <p>
 * 从请求的 JWT 令牌中提取登录身份，
 * 校验角色与账号状态，并返回当前认证用户上下文。
 */
public interface CallbackAuthService {

    /**
     * 校验当前登录用户角色与账号状态，并返回认证上下文。
     *
     * @param authorization 请求头 Authorization 的值
     * @param requiredRole  所需的角色
     * @return 当前认证用户上下文
     */
    AuthContext requireAuth(String authorization, User.UserRole requiredRole);

    /**
     * 为指定用户生成 JWT 访问令牌。
     *
     * @param user 用户实体
     * @return JWT 字符串
     */
    String generateToken(User user);
}
