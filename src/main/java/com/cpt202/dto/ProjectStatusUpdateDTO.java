package com.cpt202.dto;

import com.cpt202.model.entity.Project;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 项目状态修改参数。
 * <p>
 * 用于教师修改项目状态时接收请求体。
 */
@Data
public class ProjectStatusUpdateDTO {

    /** 执行本次状态修改的教师主键。 */
    private Long teacherId;

    /** 目标项目状态。 */
    @NotNull
    private Project.ProjectStatus projectStatus;

    /** 状态修改备注。 */
    private String remark;
}
