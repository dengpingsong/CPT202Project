package com.cpt202.service.impl;

import com.cpt202.dto.StudentProfileUpdateDTO;
import com.cpt202.dto.TeacherProfileUpdateDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.service.ProfileService;
import com.cpt202.vo.StudentProfileVO;
import com.cpt202.vo.TeacherProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户资料服务实现类。
 * <p>
 * 实现学生端与教师端资料的查询与更新，并做角色一致性校验。
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    /**
     * 查询学生资料。
     *
     * @param studentId 学生主键
     * @return 学生资料展示对象
     */
    @Override
    public StudentProfileVO getStudentProfile(Long studentId) {
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("学生资料未找到。"));

        if (profile.getUser() == null || profile.getUser().getRole() != User.UserRole.STUDENT) {
            throw new BusinessException("该用户不是学生角色。无法查询学生资料。" );
        }

        return StudentProfileVO.builder()
                .studentId(profile.getStudentId())
                .username(profile.getUser().getUsername())
                .email(profile.getUser().getEmail())
                .fullName(profile.getUser().getFullName())
                .studentNo(profile.getStudentNo())
                .programme(profile.getProgramme())
                .enrollmentDate(profile.getEnrollmentDate())
                .academicYear(profile.getAcademicYear())
                .phone(profile.getPhone())
                .interests(profile.getInterests())
                .updatedAt(profile.getUpdatedAt())
                .build();
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
                .orElseThrow(() -> new NotFoundException("学生资料未找到。"));

        if (profile.getUser() == null || profile.getUser().getRole() != User.UserRole.STUDENT) {
            throw new BusinessException("该用户不是学生角色。无法修改学生资料。" );
        }

        profile.getUser().setFullName(studentProfileUpdateDTO.getFullName());
        profile.getUser().setEmail(studentProfileUpdateDTO.getEmail());
        profile.setProgramme(studentProfileUpdateDTO.getProgramme());
        profile.setEnrollmentDate(studentProfileUpdateDTO.getEnrollmentDate());
        profile.setPhone(studentProfileUpdateDTO.getPhone());
        profile.setInterests(studentProfileUpdateDTO.getInterests());
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
                .orElseThrow(() -> new BusinessException("教师资料未找到。"));

        if (profile.getUser() == null || profile.getUser().getRole() != User.UserRole.TEACHER) {
            throw new BusinessException("该用户不是教师角色。无法查询教师资料。" );
        }

        return TeacherProfileVO.builder()
                .teacherId(profile.getTeacherId())
                .username(profile.getUser().getUsername())
                .email(profile.getUser().getEmail())
                .fullName(profile.getUser().getFullName())
                .staffNo(profile.getStaffNo())
                .department(profile.getDepartment())
                .title(profile.getTitle())
                .researchArea(profile.getResearchArea())
                .office(profile.getOffice())
                .updatedAt(profile.getUpdatedAt())
                .build();
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
                .orElseThrow(() -> new BusinessException("教师资料未找到。"));

        if (profile.getUser() == null || profile.getUser().getRole() != User.UserRole.TEACHER) {
            throw new BusinessException("该用户不是教师角色。无法修改教师资料。" );
        }

        profile.getUser().setFullName(teacherProfileUpdateDTO.getFullName());
        profile.getUser().setEmail(teacherProfileUpdateDTO.getEmail());
        profile.setDepartment(teacherProfileUpdateDTO.getDepartment());
        profile.setTitle(teacherProfileUpdateDTO.getTitle());
        profile.setResearchArea(teacherProfileUpdateDTO.getResearchArea());
        profile.setOffice(teacherProfileUpdateDTO.getOffice());
        profile.setUpdatedAt(LocalDateTime.now());

        teacherProfileRepository.save(profile);
    }
}
