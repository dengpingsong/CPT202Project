package com.cpt202.service;

import com.cpt202.model.entity.User;

import java.util.function.Supplier;

/**
 * 回调式权限认证服务。
 * <p>
 * 在执行受保护的业务操作前，从请求的 JWT 令牌中获取登录身份，
 * 校验角色与账号状态，通过后回调执行实际的业务逻辑；
 * 不通过则抛出越权异常。
 */
public interface CallbackAuthService {

    /**
     * 从 Authorization 头值中解析 JWT 并获取当前登录用户的主键。
     *
     * @param authorization 请求头 Authorization 的值，格式为 "Bearer xxx"
     * @return 当前登录用户 ID
     */
    Long getCurrentUserId(String authorization);

    /**
     * 校验当前登录用户角色与账号状态后，执行带返回值的业务操作。
     *
     * @param authorization 请求头 Authorization 的值
     * @param requiredRole  所需的角色
     * @param action        通过验证后执行的回调操作
     * @param <T>           返回值类型
     * @return 回调操作的返回值
     */
    <T> T doWithAuthCheck(String authorization, User.UserRole requiredRole, Supplier<T> action);

    /**
     * 校验当前登录用户角色与账号状态后，执行无返回值的业务操作。
     *
     * @param authorization 请求头 Authorization 的值
     * @param requiredRole  所需的角色
     * @param action        通过验证后执行的回调操作
     */
    void doWithAuthCheck(String authorization, User.UserRole requiredRole, Runnable action);

    /**
     * 为指定用户生成 JWT 访问令牌。
     *
     * @param user 用户实体
     * @return JWT 字符串
     */
    String generateToken(User user);
}
