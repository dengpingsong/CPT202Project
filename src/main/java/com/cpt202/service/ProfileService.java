package com.cpt202.service;

import com.cpt202.dto.StudentProfileUpdateDTO;
import com.cpt202.dto.TeacherProfileUpdateDTO;
import com.cpt202.vo.StudentProfileVO;
import com.cpt202.vo.TeacherProfileVO;

/**
 * 用户资料服务接口。
 * <p>
 * 该接口统一封装学生端与教师端资料的查询与维护操作。
 */
public interface ProfileService {

    /**
     * 查询学生资料。
     *
     * @param studentId 学生主键
     * @return 学生资料展示对象
     */
    StudentProfileVO getStudentProfile(Long studentId);

    /**
     * 修改学生资料。
     *
     * @param studentId 学生主键
     * @param studentProfileUpdateDTO 学生资料更新参数
     */
    void updateStudentProfile(Long studentId, StudentProfileUpdateDTO studentProfileUpdateDTO);

    /**
     * 查询教师资料。
     *
     * @param teacherId 教师主键
     * @return 教师资料展示对象
     */
    TeacherProfileVO getTeacherProfile(Long teacherId);

    /**
     * 修改教师资料。
     *
     * @param teacherId 教师主键
     * @param teacherProfileUpdateDTO 教师资料更新参数
     */
    void updateTeacherProfile(Long teacherId, TeacherProfileUpdateDTO teacherProfileUpdateDTO);
}
