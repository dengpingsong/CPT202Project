package com.cpt202.service.impl;

import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.service.ProjectRequestService;
import com.cpt202.vo.ProjectRequestVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目申请服务实现类。
 * <p>
 * 当前阶段仅完成接口定义，后续将在此落地申请提交、撤回与审核的业务逻辑。
 */
@Service
public class ProjectRequestServiceImpl implements ProjectRequestService {

    /**
     * 学生提交项目申请。
     */
    @Override
    public void create(ProjectRequestCreateDTO projectRequestCreateDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 查询学生申请列表。
     */
    @Override
    public List<ProjectRequestVO> listStudentRequests(Long studentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 查询教师待审核申请列表。
     */
    @Override
    public List<ProjectRequestVO> listTeacherRequests(Long teacherId, ProjectRequest.RequestStatus status) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 学生撤回项目申请。
     */
    @Override
    public void withdraw(Long requestId, Long studentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 教师审核项目申请。
     */
    @Override
    public void review(Long requestId, ProjectRequestReviewDTO projectRequestReviewDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
