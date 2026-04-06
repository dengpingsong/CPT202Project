package com.cpt202.dto;

import com.cpt202.model.entity.ProjectRequest;
import lombok.Data;

/**
 * 管理端申请记录查询参数。
 * 用于封装管理端在查询申请记录时的筛选条件。
 */
@Data
public class AdminRequestRecordQueryDTO {

    /** 申请状态筛选条件，可选。 */
    private ProjectRequest.RequestStatus status;
}
