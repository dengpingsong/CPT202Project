package com.cpt202.service.impl;

import com.cpt202.dto.LoginDTO;
import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.service.AuthService;
import com.cpt202.service.CallbackAuthService;
import com.cpt202.vo.LoginVO;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类。
 * <p>
 * 后续将在该类中实现注册、登录、账号校验与登录态生成等逻辑。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ACCOUNT_STATUS = "ACTIVE";
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final CallbackAuthService callbackAuthService;

    /**
     * 注册新用户。
     *
     * @param registerUserDTO 注册参数
     * @return 登录展示对象
     */
    @Override
    public LoginVO register(RegisterUserDTO registerUserDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
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

        if (dto.getRole() == User.UserRole.ADMIN) {
            throw new RuleViolationException("管理员账号不能通过公共注册接口创建。");
        }

        if (dto.getRole() == User.UserRole.STUDENT) {
            if (dto.getStudentNo() == null || dto.getStudentNo().isBlank()) {
                throw new RuleViolationException("学生学号不能为空。");
            }
            if (dto.getProgramme() == null || dto.getProgramme().isBlank()) {
                throw new RuleViolationException("学生专业不能为空。");
            }
            if (dto.getEnrollmentDate() == null) {
                throw new RuleViolationException("入学日期不能为空。");
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
                .token(callbackAuthService.generateToken(user))
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
