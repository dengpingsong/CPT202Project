package com.cpt202.service.impl;

import com.cpt202.dto.StudentProfileUpdateDTO;
import com.cpt202.dto.TeacherProfileUpdateDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.repository.StudentProfileRepository;
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

        StudentProfileVO studentProfileVO = new StudentProfileVO();
        studentProfileVO.setStudentId(studentProfile.getStudentId());
        studentProfileVO.setStudentNo(studentProfile.getStudentNo());
        studentProfileVO.setPhone(studentProfile.getPhone());
        studentProfileVO.setInterests(studentProfile.getInterests());
        studentProfileVO.setAcademicYear(studentProfile.getAcademicYear());
        studentProfileVO.setEnrollmentDate(studentProfile.getEnrollmentDate());
        studentProfileVO.setProgramme(studentProfile.getProgramme());
        studentProfileVO.setUpdatedAt(studentProfile.getUpdatedAt());
        studentProfileVO.setFullName(studentProfile.getUser().getFullName());
        studentProfileVO.setUsername(studentProfile.getUser().getUsername());
        studentProfileVO.setEmail(studentProfile.getUser().getEmail());

        return studentProfileVO;
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
