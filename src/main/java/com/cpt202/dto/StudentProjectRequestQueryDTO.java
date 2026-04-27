package com.cpt202.dto;

import lombok.Data;

/**
 * 学生端申请查询参数。
 * <p>
 * 用于查询某位学生自己的项目申请列表。
 */
@Data
public class StudentProjectRequestQueryDTO {

    /** 当前学生主键。 */
    private Long studentId;
}
