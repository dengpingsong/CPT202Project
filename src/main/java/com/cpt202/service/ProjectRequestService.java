package com.cpt202.service;

import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.vo.ProjectRequestVO;

import java.util.List;

/**
 * 项目申请服务接口。
 * <p>
 * 负责学生提交申请、撤回申请，以及教师审核申请等业务。
 */
public interface ProjectRequestService {

    /**
     * 学生提交项目申请。
     *
     * @param projectRequestCreateDTO 申请创建参数
     */
    void create(ProjectRequestCreateDTO projectRequestCreateDTO);

    /**
     * 查询学生本人申请列表。
     *
     * @param studentId 学生主键
     * @return 申请展示对象列表
     */
    List<ProjectRequestVO> listStudentRequests(Long studentId);

    /**
     * 查询教师待审核或已审核申请列表。
     *
     * @param teacherId 教师主键
     * @param status 申请状态
     * @return 申请展示对象列表
     */
    List<ProjectRequestVO> listTeacherRequests(Long teacherId, ProjectRequest.RequestStatus status);

    /**
     * 学生撤回申请。
     *
     * @param requestId 申请主键
     * @param studentId 学生主键
     */
    void withdraw(Long requestId, Long studentId);

    /**
     * 教师审核申请。
     *
     * @param requestId 申请主键
     * @param projectRequestReviewDTO 审核参数
     */
    void review(Long requestId, ProjectRequestReviewDTO projectRequestReviewDTO);
}
