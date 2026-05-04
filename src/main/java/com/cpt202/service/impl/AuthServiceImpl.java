package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.constant.RedisKeyConstants;
import com.cpt202.dto.EmailOtpLoginDTO;
import com.cpt202.dto.EmailOtpRequestDTO;
import com.cpt202.dto.LoginDTO;
import com.cpt202.dto.PasswordResetConfirmDTO;
import com.cpt202.dto.PasswordResetRequestDTO;
import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.dto.TwoFactorLoginVerifyDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.PasswordResetToken;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.PasswordResetTokenRepository;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.AuthService;
import com.cpt202.service.EmailLoginOtpMailService;
import com.cpt202.service.JwtTokenService;
import com.cpt202.service.PasswordResetMailService;
import com.cpt202.service.RedisCacheService;
import com.cpt202.service.TwoFactorAuthService;
import com.cpt202.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * 认证服务实现类。
 * <p>
 * 负责处理用户注册、登录、账号校验与登录态生成等逻辑。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ACCOUNT_STATUS = "ACTIVE";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordResetMailService passwordResetMailService;
    private final EmailLoginOtpMailService emailLoginOtpMailService;
    private final RedisCacheService redisCacheService;
    private final TwoFactorAuthService twoFactorAuthService;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Value("${app.password-reset-token-expiration-minutes:30}")
    private long passwordResetTokenExpirationMinutes;

    @Value("${app.email-login-otp-expiration-minutes:5}")
    private long emailLoginOtpExpirationMinutes;

    @Value("${app.email-login-otp-cooldown-seconds:60}")
    private long emailLoginOtpCooldownSeconds;

    /**
     * 注册新用户。
     *
     * @param registerUserDTO 注册参数
     * @return 登录展示对象
     */
    @Override
    @Transactional
    public LoginVO register(RegisterUserDTO registerUserDTO) {
        validateRegisterPayload(registerUserDTO);

        if (userRepository.existsByUsername(registerUserDTO.getUsername())) {
            throw new RuleViolationException(MessageConstants.USERNAME_EXISTS);
        }
        if (userRepository.existsByEmail(registerUserDTO.getEmail().trim())) {
            throw new RuleViolationException(MessageConstants.EMAIL_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        BeanUtils.copyProperties(registerUserDTO, user, "password");
        user.setEmail(registerUserDTO.getEmail().trim());
        user.setPasswordHash(hashPassword(registerUserDTO.getPassword()));
        user.setAccountStatus(DEFAULT_ACCOUNT_STATUS);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        user = userRepository.save(user);

        if (user.getRole() == User.UserRole.STUDENT) {
            StudentProfile studentProfile = new StudentProfile();
            BeanUtils.copyProperties(registerUserDTO, studentProfile);
            studentProfile.setUpdatedAt(now);
            studentProfile.setUser(user);
            studentProfileRepository.save(studentProfile);
            user.setStudentProfile(studentProfile);
        } else if (user.getRole() == User.UserRole.TEACHER) {
            TeacherProfile teacherProfile = new TeacherProfile();
            BeanUtils.copyProperties(registerUserDTO, teacherProfile);
            teacherProfile.setUpdatedAt(now);
            teacherProfile.setUser(user);
            teacherProfileRepository.save(teacherProfile);
            user.setTeacherProfile(teacherProfile);
        }

        return buildLoginVO(user);
    }

    /**
     * 用户登录。
     *
     * @param loginDTO 登录参数
     * @return 登录展示对象
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessException(MessageConstants.INVALID_CREDENTIALS));

        if (!user.getPasswordHash().equals(hashPassword(loginDTO.getPassword()))) {
            throw new BusinessException(MessageConstants.INVALID_CREDENTIALS);
        }

        if (!DEFAULT_ACCOUNT_STATUS.equalsIgnoreCase(user.getAccountStatus())) {
            throw new BusinessException(MessageConstants.ACCOUNT_UNAVAILABLE_CONTACT_ADMIN);
        }

        if (Boolean.TRUE.equals(user.getTwoFactorEnabled()) && StringUtils.hasText(user.getTwoFactorSecret())) {
            return buildTwoFactorRequiredLoginVO(user);
        }

        return buildLoginVO(user);
    }

    @Override
    public void sendEmailLoginOtp(EmailOtpRequestDTO requestDTO) {
        String normalizedEmail = requestDTO.getEmail().trim();
        validateEmail(normalizedEmail);

        String cooldownKey = RedisKeyConstants.EMAIL_LOGIN_OTP_COOLDOWN_PREFIX + normalizedEmail.toLowerCase();
        if (redisCacheService.get(cooldownKey, String.class).isPresent()) {
            throw new RuleViolationException(MessageConstants.EMAIL_OTP_REQUEST_TOO_FREQUENT);
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (user == null || !DEFAULT_ACCOUNT_STATUS.equalsIgnoreCase(user.getAccountStatus())) {
            return;
        }

        String otp = generateEmailOtp();
        String otpKey = RedisKeyConstants.EMAIL_LOGIN_OTP_PREFIX + normalizedEmail.toLowerCase();
        try {
            redisCacheService.set(otpKey, otp, Duration.ofMinutes(emailLoginOtpExpirationMinutes));
            redisCacheService.set(cooldownKey, "1", Duration.ofSeconds(emailLoginOtpCooldownSeconds));
            emailLoginOtpMailService.sendLoginOtpMail(user, otp);
        } catch (RuntimeException ex) {
            redisCacheService.delete(otpKey);
            redisCacheService.delete(cooldownKey);
            throw ex;
        }
    }

    @Override
    public LoginVO loginWithEmailOtp(EmailOtpLoginDTO loginDTO) {
        String normalizedEmail = loginDTO.getEmail().trim();
        validateEmail(normalizedEmail);

        String submittedOtp = loginDTO.getOtp().trim();
        if (!StringUtils.hasText(submittedOtp)) {
            throw new RuleViolationException(MessageConstants.EMAIL_OTP_REQUIRED);
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BusinessException(MessageConstants.INVALID_CREDENTIALS));

        if (!DEFAULT_ACCOUNT_STATUS.equalsIgnoreCase(user.getAccountStatus())) {
            throw new BusinessException(MessageConstants.ACCOUNT_UNAVAILABLE_CONTACT_ADMIN);
        }

        String otpKey = RedisKeyConstants.EMAIL_LOGIN_OTP_PREFIX + normalizedEmail.toLowerCase();
        String storedOtp = redisCacheService.get(otpKey, String.class)
                .orElseThrow(() -> new BusinessException(MessageConstants.EMAIL_OTP_EXPIRED));

        if (!storedOtp.equals(submittedOtp)) {
            throw new BusinessException(MessageConstants.EMAIL_OTP_INVALID);
        }

        redisCacheService.delete(otpKey);
        return buildLoginVO(user);
    }

    @Override
    public LoginVO verifyPasswordLoginTwoFactor(TwoFactorLoginVerifyDTO verifyDTO) {
        String challengeToken = verifyDTO.getChallengeToken().trim();
        String code = verifyDTO.getCode().trim();
        if (!StringUtils.hasText(code)) {
            throw new RuleViolationException(MessageConstants.TWO_FACTOR_CODE_REQUIRED);
        }
        User user = twoFactorAuthService.verifyLoginChallenge(challengeToken, code);
        if (!DEFAULT_ACCOUNT_STATUS.equalsIgnoreCase(user.getAccountStatus())) {
            throw new BusinessException(MessageConstants.ACCOUNT_UNAVAILABLE_CONTACT_ADMIN);
        }
        return buildLoginVO(user);
    }

    @Override
    @Transactional
    public void requestPasswordReset(PasswordResetRequestDTO requestDTO) {
        passwordResetTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        String normalizedEmail = requestDTO.getEmail().trim();
        validateEmail(normalizedEmail);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (user == null || !DEFAULT_ACCOUNT_STATUS.equalsIgnoreCase(user.getAccountStatus())) {
            return;
        }

        invalidateActiveResetTokens(user, LocalDateTime.now());

        String rawToken = generateResetToken();
        LocalDateTime now = LocalDateTime.now();

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(rawToken);
        passwordResetToken.setCreatedAt(now);
        passwordResetToken.setExpiresAt(now.plusMinutes(passwordResetTokenExpirationMinutes));
        passwordResetTokenRepository.save(passwordResetToken);

        passwordResetMailService.sendPasswordResetMail(user, buildResetLink(rawToken));
    }

    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new RuleViolationException(MessageConstants.EMAIL_FORMAT_INVALID);
        }
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetConfirmDTO confirmDTO) {
        if (!StringUtils.hasText(confirmDTO.getToken())) {
            throw new RuleViolationException(MessageConstants.PASSWORD_RESET_TOKEN_REQUIRED);
        }

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(confirmDTO.getToken().trim())
                .orElseThrow(() -> new BusinessException(MessageConstants.PASSWORD_RESET_LINK_INVALID));

        if (passwordResetToken.getUsedAt() != null) {
            throw new BusinessException(MessageConstants.PASSWORD_RESET_LINK_ALREADY_USED);
        }

        if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(MessageConstants.PASSWORD_RESET_LINK_EXPIRED);
        }

        User user = passwordResetToken.getUser();
        user.setPasswordHash(hashPassword(confirmDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        passwordResetToken.setUsedAt(LocalDateTime.now());
        userRepository.save(user);
        passwordResetTokenRepository.save(passwordResetToken);
        invalidateOtherActiveTokens(user, passwordResetToken.getResetId(), LocalDateTime.now());
    }

    private void validateRegisterPayload(RegisterUserDTO dto) {
        if (dto.getRole() == null) {
            throw new RuleViolationException(MessageConstants.REGISTER_ROLE_REQUIRED);
        }
        if (!EMAIL_PATTERN.matcher(dto.getEmail().trim()).matches()) {
            throw new RuleViolationException(MessageConstants.EMAIL_FORMAT_INVALID);
        }

        if (dto.getRole() == User.UserRole.STUDENT) {
            if (dto.getStudentNo() == null || dto.getStudentNo().isBlank()) {
                throw new RuleViolationException(MessageConstants.STUDENT_NO_REQUIRED);
            }
            if (dto.getProgramme() == null || dto.getProgramme().isBlank()) {
                throw new RuleViolationException(MessageConstants.STUDENT_PROGRAMME_REQUIRED);
            }
            if (dto.getEnrollmentDate() == null) {
                throw new RuleViolationException(MessageConstants.STUDENT_ENROLLMENT_DATE_REQUIRED);
            }
            if (dto.getEnrollmentDate().isAfter(LocalDate.now())) {
                throw new RuleViolationException(MessageConstants.ENROLLMENT_DATE_CANNOT_BE_FUTURE);
            }
        }

        if (dto.getRole() == User.UserRole.TEACHER) {
            if (dto.getStaffNo() == null || dto.getStaffNo().isBlank()) {
                throw new RuleViolationException(MessageConstants.TEACHER_STAFF_NO_REQUIRED);
            }
            if (dto.getDepartment() == null || dto.getDepartment().isBlank()) {
                throw new RuleViolationException(MessageConstants.TEACHER_DEPARTMENT_REQUIRED);
            }
            if (dto.getTitle() == null || dto.getTitle().isBlank()) {
                throw new RuleViolationException(MessageConstants.TEACHER_TITLE_REQUIRED);
            }
        }
    }

    private LoginVO buildLoginVO(User user) {
        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken(jwtTokenService.generateToken(user));
        loginVO.setTwoFactorRequired(false);
        loginVO.setTwoFactorChallengeToken(null);
        return loginVO;
    }

    private LoginVO buildTwoFactorRequiredLoginVO(User user) {
        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken("");
        loginVO.setTwoFactorRequired(true);
        loginVO.setTwoFactorChallengeToken(twoFactorAuthService.createLoginChallenge(user));
        return loginVO;
    }

    private void invalidateActiveResetTokens(User user, LocalDateTime usedAt) {
        for (PasswordResetToken existingToken : passwordResetTokenRepository.findAllByUserAndUsedAtIsNull(user)) {
            existingToken.setUsedAt(usedAt);
        }
    }

    private void invalidateOtherActiveTokens(User user, Long currentResetId, LocalDateTime usedAt) {
        for (PasswordResetToken existingToken : passwordResetTokenRepository.findAllByUserAndUsedAtIsNull(user)) {
            if (!existingToken.getResetId().equals(currentResetId)) {
                existingToken.setUsedAt(usedAt);
            }
        }
    }

    private String buildResetLink(String rawToken) {
        String baseUrl = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        return "%s/auth/reset-password?token=%s".formatted(baseUrl, rawToken);
    }

    private String generateResetToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String generateEmailOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
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
