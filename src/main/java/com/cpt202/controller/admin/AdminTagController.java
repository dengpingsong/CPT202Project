package com.cpt202.controller.admin;

import com.cpt202.dto.TagDTO;
import com.cpt202.dto.PageQueryDTO;
import com.cpt202.result.PageResult;
import com.cpt202.result.Result;
import com.cpt202.service.TagService;
import com.cpt202.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端标签接口控制器。
 * 负责标签的查询、创建、修改和删除。
 */
@RestController
@RequestMapping("/api/admin/tags")
@Tag(name = "Admin Tag API")
@Slf4j
public class AdminTagController {

    private final TagService tagService;

    public AdminTagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * 查询全部标签列表。
     *
     * @return 标签展示对象列表
     */
    @GetMapping
    @Operation(summary = "List tags")
    public Result<List<TagVO>> list() {
        log.info("List tags");
        return Result.success(tagService.listAll());
    }

    @GetMapping("/page")
    @Operation(summary = "List tags by page")
    public Result<PageResult<TagVO>> listPage(@Valid PageQueryDTO queryDTO) {
        log.info("List tags by page, pageNum: {}, pageSize: {}", queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(tagService.listPage(queryDTO));
    }

    /**
     * 根据标签主键查询标签详情。
     *
     * @param tagId 标签主键
     * @return 标签展示对象
     */
    @GetMapping("/{tagId}")
    @Operation(summary = "Get tag by ID")
    public Result<TagVO> getById(@PathVariable Long tagId) {
        log.info("Get tag by id: {}", tagId);
        return Result.success(tagService.getById(tagId));
    }

    /**
     * 新增标签。
     *
     * @param tagDTO 标签新增参数
     * @return 统一成功响应
     */
    @PostMapping
    @Operation(summary = "Create a tag")
    public Result<Void> create(@Valid @RequestBody TagDTO tagDTO) {
        log.info("Create tag: {}", tagDTO);
        tagService.create(tagDTO);
        return Result.success();
    }

    /**
     * 修改标签。
     *
     * @param tagId 标签主键
     * @param tagDTO 标签更新参数
     * @return 统一成功响应
     */
    @PutMapping("/{tagId}")
    @Operation(summary = "Update a tag")
    public Result<Void> update(@PathVariable Long tagId,
                               @Valid @RequestBody TagDTO tagDTO) {
        log.info("Update tag: {}, payload: {}", tagId, tagDTO);
        tagService.update(tagId, tagDTO);
        return Result.success();
    }

    /**
     * 删除标签。
     *
     * @param tagId 标签主键
     * @return 统一成功响应
     */
    @DeleteMapping("/{tagId}")
    @Operation(summary = "Delete a tag")
    public Result<Void> delete(@PathVariable Long tagId) {
        log.info("Delete tag: {}", tagId);
        tagService.delete(tagId);
        return Result.success();
    }
}
