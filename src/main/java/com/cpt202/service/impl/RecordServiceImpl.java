package com.cpt202.service.impl;

import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.service.RecordService;
import com.cpt202.vo.ProjectVO;
import com.cpt202.vo.ProjectRequestVO;
import com.cpt202.vo.RequestStatusHistoryVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端记录服务实现类。
 * <p>
 * 用于组装项目记录、申请记录和申请历史记录等管理端所需查询结果。
 */
@Service
public class RecordServiceImpl implements RecordService {

    /**
     * 查询项目记录列表。
     *
     * @return 项目展示对象列表
     */
    @Override
    public List<ProjectVO> listProjectRecords() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 查询申请记录列表。
     *
     * @param status 申请状态筛选条件
     * @return 申请展示对象列表
     */
    @Override
    public List<ProjectRequestVO> listRequestRecords(ProjectRequest.RequestStatus status) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 查询申请历史记录列表。
     *
     * @return 申请状态历史列表
     */
    @Override
    public List<RequestStatusHistoryVO> listRequestHistoryRecords() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
