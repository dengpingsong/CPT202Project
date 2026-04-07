package com.cpt202.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分类展示对象。
 * <p>
 * 用于向前端返回分类信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO {

    /** 分类主键。 */
    private Long categoryId;
    /** 分类名称。 */
    private String categoryName;
    /** 分类说明。 */
    private String description;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
