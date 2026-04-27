package com.cpt202.controller.admin;

import com.cpt202.dto.CategoryDTO;
import com.cpt202.result.Result;
import com.cpt202.service.CategoryService;
import com.cpt202.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端分类接口控制器。
 * 负责接收前端对分类资源的查询与维护请求，
 * 其中查询接口返回数据，新增、修改、删除接口仅返回操作结果。
 */
@RestController
@RequestMapping("/api/admin/categories")
@Tag(name = "Admin Category API")
@Slf4j
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 查询全部分类列表。
     *
     * @return 分类展示对象列表
     */
    @GetMapping
    @Operation(summary = "List categories")
    public Result<List<CategoryVO>> list() {
        log.info("List categories");
        return Result.success(categoryService.listAll());
    }

    /**
     * 根据分类主键查询分类详情。
     *
     * @param categoryId 分类主键
     * @param operatorId 操作人主键
     * @return 分类展示对象
     */
    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID")
    public Result<CategoryVO> getById(@PathVariable Long categoryId) {
        log.info("Get category by id: {}", categoryId);
        return Result.success(categoryService.getById(categoryId));
    }

    /**
     * 新增分类。
     *
     * @param categoryDTO 分类新增参数
     * @param operatorId 操作人主键
     * @return 统一成功响应
     */
    @PostMapping
    @Operation(summary = "Create a category")
    public Result<Void> create(@Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("Create category: {}", categoryDTO);
        categoryService.create(categoryDTO);
        return Result.success();
    }

    /**
     * 修改指定分类。
     *
     * @param categoryId 分类主键
     * @param categoryDTO 分类更新参数
     * @param operatorId 操作人主键
     * @return 统一成功响应
     */
    @PutMapping("/{categoryId}")
    @Operation(summary = "Update a category")
    public Result<Void> update(@PathVariable Long categoryId,
                               @Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("Update category: {}, payload: {}", categoryId, categoryDTO);
        categoryService.update(categoryId, categoryDTO);
        return Result.success();
    }

    /**
     * 删除指定分类。
     *
     * @param categoryId 分类主键
     * @param operatorId 操作人主键
     * @return 统一成功响应
     */
    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete a category")
    public Result<Void> delete(@PathVariable Long categoryId) {
        log.info("Delete category: {}", categoryId);
        categoryService.delete(categoryId);
        return Result.success();
    }
}
