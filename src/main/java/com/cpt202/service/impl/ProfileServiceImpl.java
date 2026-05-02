package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.AdminProfileUpdateDTO;
import com.cpt202.dto.ChangePasswordDTO;
import com.cpt202.dto.StudentProfileUpdateDTO;
import com.cpt202.dto.TeacherProfileUpdateDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.ProfileService;
import com.cpt202.vo.AdminProfileVO;
import com.cpt202.vo.StudentProfileVO;
import com.cpt202.vo.TeacherProfileVO;
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
 * 用户资料服务实现类。
 * 当前阶段仅保留方法骨架，后续将在此实现用户主表与资料表的联合更新逻辑。
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final UserRepository userRepository;

    /**
     * 查询学生资料。
     *
     * @param studentId 学生主键
     * @return 学生资料展示对象
     */
    @Override
    public StudentProfileVO getStudentProfile(Long studentId) {
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.STUDENT_PROFILE_NOT_FOUND));

        if (profile.getUser() == null || profile.getUser().getRole() != User.UserRole.STUDENT) {
            throw new BusinessException(MessageConstants.NON_STUDENT_PROFILE_ACCESS);
        }

        StudentProfileVO profileVO = new StudentProfileVO();
        BeanUtils.copyProperties(profile, profileVO);
        profileVO.setUsername(profile.getUser().getUsername());
        profileVO.setEmail(profile.getUser().getEmail());
        profileVO.setFullName(profile.getUser().getFullName());
        profileVO.setAcademicYear(profile.getAcademicYear());

        return profileVO;
    }

    /**
     * 修改学生资料。
     *
     * @param studentId 学生主键
     * @param studentProfileUpdateDTO 学生资料更新参数
     */
    @Override
    @Transactional
    public void updateStudentProfile(Long studentId, StudentProfileUpdateDTO studentProfileUpdateDTO) {
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.STUDENT_PROFILE_NOT_FOUND));

        if (profile.getUser() == null || profile.getUser().getRole() != User.UserRole.STUDENT) {
            throw new BusinessException(MessageConstants.NON_STUDENT_PROFILE_UPDATE);
        }

        BeanUtils.copyProperties(studentProfileUpdateDTO, profile, "fullName", "email");
        profile.getUser().setFullName(studentProfileUpdateDTO.getFullName());
        profile.getUser().setEmail(studentProfileUpdateDTO.getEmail());
        profile.setUpdatedAt(LocalDateTime.now());

        studentProfileRepository.save(profile);
    }

    /**
     * 查询教师资料。
     *
     * @param teacherId 教师主键
     * @return 教师资料展示对象
     */
    @Override
    public TeacherProfileVO getTeacherProfile(Long teacherId) {
        TeacherProfile profile = teacherProfileRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.TEACHER_PROFILE_NOT_FOUND));

        if (profile.getUser() == null || profile.getUser().getRole() != User.UserRole.TEACHER) {
            throw new BusinessException(MessageConstants.NON_TEACHER_PROFILE_ACCESS);
        }

        TeacherProfileVO profileVO = new TeacherProfileVO();
        BeanUtils.copyProperties(profile, profileVO);
        profileVO.setUsername(profile.getUser().getUsername());
        profileVO.setEmail(profile.getUser().getEmail());
        profileVO.setFullName(profile.getUser().getFullName());
        return profileVO;
    }

    /**
     * 修改教师资料。
     *
     * @param teacherId 教师主键
     * @param teacherProfileUpdateDTO 教师资料更新参数
     */
    @Override
    @Transactional
    public void updateTeacherProfile(Long teacherId, TeacherProfileUpdateDTO teacherProfileUpdateDTO) {
        TeacherProfile profile = teacherProfileRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.TEACHER_PROFILE_NOT_FOUND));

        if (profile.getUser() == null || profile.getUser().getRole() != User.UserRole.TEACHER) {
            throw new BusinessException(MessageConstants.NON_TEACHER_PROFILE_UPDATE);
        }

        BeanUtils.copyProperties(teacherProfileUpdateDTO, profile, "fullName", "email");
        profile.getUser().setFullName(teacherProfileUpdateDTO.getFullName());
        profile.getUser().setEmail(teacherProfileUpdateDTO.getEmail());
        profile.setUpdatedAt(LocalDateTime.now());

        teacherProfileRepository.save(profile);
    }

    @Override
    public AdminProfileVO getAdminProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.USER_NOT_FOUND));

        if (user.getRole() != User.UserRole.ADMIN) {
            throw new BusinessException(MessageConstants.NON_ADMIN_PROFILE_ACCESS);
        }

        AdminProfileVO profileVO = new AdminProfileVO();
        BeanUtils.copyProperties(user, profileVO);
        return profileVO;
    }

    @Override
    @Transactional
    public void updateAdminProfile(Long userId, AdminProfileUpdateDTO adminProfileUpdateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.USER_NOT_FOUND));

        if (user.getRole() != User.UserRole.ADMIN) {
            throw new BusinessException(MessageConstants.NON_ADMIN_PROFILE_UPDATE);
        }

        user.setFullName(adminProfileUpdateDTO.getFullName());
        user.setEmail(adminProfileUpdateDTO.getEmail());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Change the password for the given user.
     * Verifies the old password before applying the new hash.
     *
     * @param userId            current user's primary key
     * @param changePasswordDTO old and new password payload
     */
    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.USER_NOT_FOUND));

        String oldHash = hashPassword(changePasswordDTO.getOldPassword());
        if (!oldHash.equals(user.getPasswordHash())) {
            throw new BusinessException(MessageConstants.INCORRECT_OLD_PASSWORD);
        }

        String newHash = hashPassword(changePasswordDTO.getNewPassword());
        if (newHash.equals(user.getPasswordHash())) {
            throw new BusinessException(MessageConstants.NEW_PASSWORD_SAME_AS_OLD);
        }

        user.setPasswordHash(newHash);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
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
