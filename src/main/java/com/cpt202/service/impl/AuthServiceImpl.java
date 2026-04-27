package com.cpt202.service.impl;

import com.cpt202.dto.LoginDTO;
import com.cpt202.dto.RegisterUserDTO;
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
            throw new RuleViolationException("用户名已存在，请更换后重试。");
        }
        if (userRepository.existsByEmail(registerUserDTO.getEmail())) {
            throw new RuleViolationException("邮箱已被注册，请更换后重试。");
        }

        User user = User.builder()
                .username(registerUserDTO.getUsername())
                .passwordHash(hashPassword(registerUserDTO.getPassword()))
                .email(registerUserDTO.getEmail())
                .fullName(registerUserDTO.getFullName())
                .role(registerUserDTO.getRole())
                .accountStatus(DEFAULT_ACCOUNT_STATUS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        if (user.getRole() == User.UserRole.STUDENT) {
            StudentProfile studentProfile = StudentProfile.builder()
                    .studentNo(registerUserDTO.getStudentNo())
                    .programme(registerUserDTO.getProgramme())
                    .academicYear(registerUserDTO.getYear())
                    .phone(registerUserDTO.getPhone())
                    .interests(registerUserDTO.getInterests())
                    .updatedAt(LocalDateTime.now())
                    .user(user)
                    .build();
            studentProfileRepository.save(studentProfile);
            user.setStudentProfile(studentProfile);
        } else if (user.getRole() == User.UserRole.TEACHER) {
            TeacherProfile teacherProfile = TeacherProfile.builder()
                    .staffNo(registerUserDTO.getStaffNo())
                    .department(registerUserDTO.getDepartment())
                    .title(registerUserDTO.getTitle())
                    .researchArea(registerUserDTO.getResearchArea())
                    .office(registerUserDTO.getOffice())
                    .updatedAt(LocalDateTime.now())
                    .user(user)
                    .build();
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
                .orElseThrow(() -> new BusinessException("用户名或密码错误。"));

        if (!user.getPasswordHash().equals(hashPassword(loginDTO.getPassword()))) {
            throw new BusinessException("用户名或密码错误。" );
        }

        if (!DEFAULT_ACCOUNT_STATUS.equalsIgnoreCase(user.getAccountStatus())) {
            throw new BusinessException("账户当前不可用，请联系管理员。" );
        }

        return buildLoginVO(user);
    }

    private void validateRegisterPayload(RegisterUserDTO dto) {
        if (dto.getRole() == null) {
            throw new RuleViolationException("注册角色不能为空。");
        }

        if (dto.getRole() == User.UserRole.STUDENT) {
            if (dto.getStudentNo() == null || dto.getStudentNo().isBlank()) {
                throw new RuleViolationException("学生学号不能为空。");
            }
            if (dto.getProgramme() == null || dto.getProgramme().isBlank()) {
                throw new RuleViolationException("学生专业不能为空。");
            }
            if (dto.getYear() == null) {
                throw new RuleViolationException("学生年级不能为空。");
            }
        }

        if (dto.getRole() == User.UserRole.TEACHER) {
            if (dto.getStaffNo() == null || dto.getStaffNo().isBlank()) {
                throw new RuleViolationException("教师工号不能为空。");
            }
            if (dto.getDepartment() == null || dto.getDepartment().isBlank()) {
                throw new RuleViolationException("教师院系不能为空。");
            }
            if (dto.getTitle() == null || dto.getTitle().isBlank()) {
                throw new RuleViolationException("教师职称不能为空。");
            }
        }
    }

    private LoginVO buildLoginVO(User user) {
        return LoginVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .token(jwtTokenService.generateToken(user))
                .build();
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
