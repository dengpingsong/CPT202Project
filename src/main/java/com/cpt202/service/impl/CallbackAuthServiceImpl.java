package com.cpt202.service.impl;

import com.cpt202.exception.UnauthorizedAccessException;
import com.cpt202.model.entity.User;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.CallbackAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * 回调式权限认证服务实现类。
 * <p>
 * 根据 userId 查找用户，校验角色是否匹配以及账号是否可用，
 * 通过后回调执行业务逻辑，否则抛出 {@link UnauthorizedAccessException}。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CallbackAuthServiceImpl implements CallbackAuthService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private final UserRepository userRepository;

    @Override
    public <T> T doWithAuthCheck(Long userId, User.UserRole requiredRole, Supplier<T> action) {
        verifyUserRole(userId, requiredRole);
        return action.get();
    }

    @Override
    public void doWithAuthCheck(Long userId, User.UserRole requiredRole, Runnable action) {
        verifyUserRole(userId, requiredRole);
        action.run();
    }

    private void verifyUserRole(Long userId, User.UserRole requiredRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("越权访问：用户 {} 不存在", userId);
                    return new UnauthorizedAccessException("用户不存在，拒绝访问。");
                });

        if (user.getRole() != requiredRole) {
            log.warn("越权访问：用户 {} 角色为 {}，需要 {}", userId, user.getRole(), requiredRole);
            throw new UnauthorizedAccessException(
                    String.format("权限不足：当前角色为 %s，该操作需要 %s 角色。", user.getRole(), requiredRole));
        }

        if (!ACTIVE_STATUS.equalsIgnoreCase(user.getAccountStatus())) {
            log.warn("越权访问：用户 {} 账号状态为 {}", userId, user.getAccountStatus());
            throw new UnauthorizedAccessException("账号当前不可用，拒绝访问。");
        }
    }
}
