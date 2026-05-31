package com.cpt202.dto;

import com.cpt202.model.entity.ProjectRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 项目申请审核参数。
 * <p>
 * 用于教师审核申请时接收审核结果和评语。
 */
@Data
public class ProjectRequestReviewDTO {

    /** 审核后的申请状态。 */
    @NotNull
    private ProjectRequest.RequestStatus requestStatus;

    /** 审核意见或备注。 */
    @Size(max = 500, message = "Decision comment cannot exceed 500 characters.")
    private String decisionComment;
}
