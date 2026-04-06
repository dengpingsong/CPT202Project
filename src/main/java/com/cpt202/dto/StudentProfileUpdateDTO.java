package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 学生资料更新参数。
 * <p>
 * 同时包含用户主表中的基础信息与学生资料表中的扩展信息。
 */
@Data
public class StudentProfileUpdateDTO {

    /** 学生姓名。 */
    @NotBlank
    private String fullName;

    /** 学生邮箱。 */
    @NotBlank
    private String email;

    /** 专业。 */
    private String programme;
    /** 年级。 */
    private Integer year;
    /** 联系电话。 */
    private String phone;
    /** 兴趣方向。 */
    private String interests;
}
