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
import org.springframework.stereotype.Service;

/**
 * 用户资料服务实现类。
 * <p>
 * 当前阶段仅保留方法骨架，后续将在此实现用户主表与资料表的联合更新逻辑。
 */
@Service
public class ProfileServiceImpl implements ProfileService {

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
    public void updateStudentProfile(Long studentId, StudentProfileUpdateDTO studentProfileUpdateDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 查询教师资料。
     *
     * @param teacherId 教师主键
     * @return 教师资料展示对象
     */
    @Override
    public TeacherProfileVO getTeacherProfile(Long teacherId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 修改教师资料。
     *
     * @param teacherId 教师主键
     * @param teacherProfileUpdateDTO 教师资料更新参数
     */
    @Override
    public void updateTeacherProfile(Long teacherId, TeacherProfileUpdateDTO teacherProfileUpdateDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
