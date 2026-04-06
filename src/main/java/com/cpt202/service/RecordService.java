package com.cpt202.service;

import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.vo.ProjectVO;
import com.cpt202.vo.ProjectRequestVO;
import com.cpt202.vo.RequestStatusHistoryVO;

import java.util.List;

/**
 * 管理端记录服务接口。
 * <p>
 * 用于查询项目记录、申请记录以及申请历史记录。
 */
public interface RecordService {

    /**
     * 查询项目记录列表。
     *
     * @return 项目展示对象列表
     */
    List<ProjectVO> listProjectRecords();

    /**
     * 查询申请记录列表。
     *
     * @param status 申请状态筛选条件
     * @return 申请展示对象列表
     */
    List<ProjectRequestVO> listRequestRecords(ProjectRequest.RequestStatus status);

    /**
     * 查询申请历史记录列表。
     *
     * @return 申请状态历史列表
     */
    List<RequestStatusHistoryVO> listRequestHistoryRecords();
}
