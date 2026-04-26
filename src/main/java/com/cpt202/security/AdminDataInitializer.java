package com.cpt202.security;

import com.cpt202.model.entity.User;
import com.cpt202.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * 系统启动时自动创建默认管理员账号。
 * <p>
 * 若数据库中已存在对应用户名的账号则跳过，不重复创建。
 * 账号信息通过 application.properties 中的 {@code admin.default.*} 属性配置，
 * 支持通过环境变量覆盖。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    @Value("${admin.default.username}")
    private String defaultUsername;

    @Value("${admin.default.password}")
    private String defaultPassword;

    @Value("${admin.default.email}")
    private String defaultEmail;

    @Value("${admin.default.fullName}")
    private String defaultFullName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername(defaultUsername)) {
            log.info("默认管理员账号 '{}' 已存在，跳过初始化。", defaultUsername);
            return;
        }

        User admin = User.builder()
                .username(defaultUsername)
                .passwordHash(hashPassword(defaultPassword))
                .email(defaultEmail)
                .fullName(defaultFullName)
                .role(User.UserRole.ADMIN)
                .accountStatus("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(admin);
        log.info("默认管理员账号 '{}' 创建成功。请在首次登录后修改密码。", defaultUsername);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to hash password", ex);
        }
    }
}
