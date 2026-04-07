package com.cpt202.dto;

import com.cpt202.model.entity.ProjectRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 教师端申请查询参数。
 * <p>
 * 用于封装教师查看待审核申请或指定状态申请时的筛选条件。
 */
@Data
public class TeacherProjectRequestQueryDTO {

    /** 当前教师主键。 */
    @NotNull
    private Long teacherId;
    /** 申请状态筛选条件，可选。 */
    private ProjectRequest.RequestStatus status;
}
