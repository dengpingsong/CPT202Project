package com.cpt202.service.impl;

import com.cpt202.constant.JwtClaimsConstant;
import com.cpt202.constant.SecurityConstants;
import com.cpt202.exception.UnauthorizedAccessException;
import com.cpt202.model.entity.User;
import com.cpt202.properties.JwtProperties;
import com.cpt202.repository.UserRepository;
import com.cpt202.security.AuthContext;
import com.cpt202.service.JwtTokenService;
import com.cpt202.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

/**
 * Default JWT token service.
 */
@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        signingKey = JwtUtil.createSigningKey(jwtProperties.getSecret());
    }

    @Override
    public String generateToken(User user) {
        return JwtUtil.createToken(signingKey, jwtProperties.getExpirationSeconds(), user);
    }

    @Override
    public AuthContext parseToken(String token) {
        Claims claims;
        try {
            claims = JwtUtil.parseToken(signingKey, token);
        } catch (Exception ex) {
            throw new UnauthorizedAccessException("登录状态无效或已过期，请重新登录。");
        }

        Long userId = claims.get(JwtClaimsConstant.USER_ID, Long.class);
        String roleValue = claims.get(JwtClaimsConstant.ROLE, String.class);
        User.UserRole tokenRole = parseRole(roleValue);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedAccessException("用户不存在，拒绝访问。"));

        if (user.getRole() != tokenRole) {
            throw new UnauthorizedAccessException("登录角色与当前账号角色不一致。");
        }
        if (!SecurityConstants.ACTIVE_ACCOUNT_STATUS.equalsIgnoreCase(user.getAccountStatus())) {
            throw new UnauthorizedAccessException("账号当前不可用，拒绝访问。");
        }

        return new AuthContext(user.getUserId(), user.getRole());
    }

    private User.UserRole parseRole(String roleValue) {
        try {
            return User.UserRole.valueOf(roleValue);
        } catch (Exception ex) {
            throw new UnauthorizedAccessException("登录状态无效或已过期，请重新登录。");
        }
    }
}
