package com.cpt202.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 项目标签绑定参数。
 * <p>
 * 用于教师为指定项目重新设置标签集合。
 */
@Data
public class ProjectTagBindDTO {

    /** 发起本次绑定操作的教师主键。 */
    @NotNull
    private Long teacherId;

    /** 需要绑定到项目上的标签主键集合，不能为空。 */
    @NotEmpty
    private List<Long> tagIds;
}
