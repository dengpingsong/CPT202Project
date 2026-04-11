package com.cpt202.service.impl;

import com.cpt202.exception.UnauthorizedAccessException;
import com.cpt202.model.entity.User;
import com.cpt202.repository.UserRepository;
import com.cpt202.security.AuthContext;
import com.cpt202.service.CallbackAuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * 回调式权限认证服务实现类。
 * <p>
 * 自动从当前 HTTP 请求的 Authorization 头中提取 JWT 令牌，
 * 解析并校验身份一致性、角色与账号状态，
 * 通过后返回当前用户上下文，否则抛出 {@link UnauthorizedAccessException}。
 */
@Service
@Slf4j
public class CallbackAuthServiceImpl implements CallbackAuthService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final String secret;
    private final long expirationSeconds;
    private SecretKey signingKey;

    public CallbackAuthServiceImpl(UserRepository userRepository,
                                   @Value("${jwt.secret}") String secret,
                                   @Value("${jwt.expiration-seconds}") long expirationSeconds) {
        this.userRepository = userRepository;
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret length must be at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ==================== 接口方法 ====================

    @Override
    public AuthContext requireAuth(String authorization, User.UserRole requiredRole) {
        return verify(authorization, requiredRole);
    }

    @Override
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .signWith(signingKey)
                .compact();
    }

    // ==================== 私有方法 ====================

    /**
     * 从 Authorization 头值中提取并解析 JWT，返回解析结果。
     */
    private ParsedToken extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedAccessException("缺少有效的 Authorization Bearer Token。");
        }
        String jwt = authorization.substring(BEARER_PREFIX.length()).trim();
        return parseToken(jwt);
    }

    private AuthContext verify(String authorization, User.UserRole requiredRole) {
        ParsedToken parsed = extractToken(authorization);

        User user = userRepository.findById(parsed.userId)
                .orElseThrow(() -> {
                    log.warn("越权访问：用户 {} 不存在", parsed.userId);
                    return new UnauthorizedAccessException("用户不存在，拒绝访问。");
                });

        if (user.getRole() != requiredRole || parsed.role != requiredRole) {
            log.warn("越权访问：用户 {} 角色为 {}，需要 {}", parsed.userId, user.getRole(), requiredRole);
            throw new UnauthorizedAccessException(
                    String.format("权限不足：当前角色为 %s，该操作需要 %s 角色。", user.getRole(), requiredRole));
        }

        if (!ACTIVE_STATUS.equalsIgnoreCase(user.getAccountStatus())) {
            log.warn("越权访问：用户 {} 账号状态为 {}", parsed.userId, user.getAccountStatus());
            throw new UnauthorizedAccessException("账号当前不可用，拒绝访问。");
        }

        return new AuthContext(user.getUserId(), user.getRole());
    }

    private ParsedToken parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long tokenUserId = Long.parseLong(claims.getSubject());
            String username = claims.get("username", String.class);
            User.UserRole role = User.UserRole.valueOf(claims.get("role", String.class));
            return new ParsedToken(tokenUserId, role, username);
        } catch (UnauthorizedAccessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedAccessException("登录状态无效或已过期，请重新登录。");
        }
    }

    /** JWT 解析结果——仅内部使用。 */
    private static class ParsedToken {
        final Long userId;
        final User.UserRole role;
        final String username;

        ParsedToken(Long userId, User.UserRole role, String username) {
            this.userId = userId;
            this.role = role;
            this.username = username;
        }
    }
}
