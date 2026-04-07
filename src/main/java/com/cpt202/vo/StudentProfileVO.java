package com.cpt202.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生资料展示对象。
 * <p>
 * 用于向前端返回学生基础资料和扩展资料。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileVO {

    /** 学生主键。 */
    private Long studentId;
    /** 用户名。 */
    private String username;
    /** 邮箱。 */
    private String email;
    /** 真实姓名。 */
    private String fullName;
    /** 学号。 */
    private String studentNo;
    /** 专业。 */
    private String programme;
    /** 学年。 */
    private Integer academicYear;
    /** 联系电话。 */
    private String phone;
    /** 兴趣方向。 */
    private String interests;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
