package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.LoginDTO;
import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.dto.ResetPasswordDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.AuthService;
import com.cpt202.service.JwtTokenService;
import com.cpt202.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * 认证服务实现类。
 * <p>
 * 负责处理用户注册、登录、账号校验与登录态生成等逻辑。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ACCOUNT_STATUS = "ACTIVE";
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final JwtTokenService jwtTokenService;

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
        if (userRepository.existsByEmail(registerUserDTO.getEmail())) {
            throw new RuleViolationException(MessageConstants.EMAIL_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        BeanUtils.copyProperties(registerUserDTO, user, "password");
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

        return buildLoginVO(user);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        User user = userRepository.findByUsername(resetPasswordDTO.getUsername())
                .orElseThrow(() -> new BusinessException(MessageConstants.RESET_PASSWORD_IDENTITY_MISMATCH));

        if (!user.getEmail().equalsIgnoreCase(resetPasswordDTO.getEmail().trim())) {
            throw new BusinessException(MessageConstants.RESET_PASSWORD_IDENTITY_MISMATCH);
        }

        user.setPasswordHash(hashPassword(resetPasswordDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void validateRegisterPayload(RegisterUserDTO dto) {
        if (dto.getRole() == null) {
            throw new RuleViolationException(MessageConstants.REGISTER_ROLE_REQUIRED);
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
        return loginVO;
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
