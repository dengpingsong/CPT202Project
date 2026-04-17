package com.cpt202.controller.student;

import com.cpt202.dto.StudentProjectQueryDTO;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.User;
import com.cpt202.result.Result;
import com.cpt202.security.AuthContext;
import com.cpt202.service.CallbackAuthService;
import com.cpt202.service.ProjectService;
import com.cpt202.service.TagService;
import com.cpt202.service.impl.TagServiceImpl;
import com.cpt202.vo.ProjectVO;
import com.cpt202.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端项目接口控制器。
 * 提供学生查询可申请项目列表与项目详情的入口。
 */
@RestController
@RequestMapping("/api/student/projects")
@Tag(name = "Student Project API")
public class StudentProjectController {

    private final ProjectService projectService;
    private final TagService tagService;
    private final CallbackAuthService callbackAuthService;

    /**
     * 构造器注入项目服务。
     *
     * @param projectService 项目服务
     */
    public StudentProjectController(ProjectService projectService, TagService tagService, CallbackAuthService callbackAuthService) {
        this.projectService = projectService;
        this.tagService = tagService;
        this.callbackAuthService = callbackAuthService;
    }

    /**
     * 查询学生可见的项目列表。
     *
     * @param queryDTO 项目筛选参数
     * @return 项目展示对象列表
     */
    @GetMapping
    @Operation(summary = "List available projects")
    public Result<List<ProjectVO>> list(StudentProjectQueryDTO queryDTO) {
        return Result.success(projectService.listStudentProjects(
                queryDTO.getKeyword(),
                queryDTO.getCategoryId(),
                queryDTO.getStatus()));
    }

    /**
     * 根据项目主键查询项目详情。
     *
     * @param projectId 项目主键
     * @return 项目展示对象
     */
    @GetMapping("/{projectId}")
    @Operation(summary = "Get project details")
    public Result<ProjectVO> getById(@PathVariable Long projectId) {
        return Result.success(projectService.getProject(projectId));
    }

    /**
     * 获取所有的Tag
     */
    @GetMapping("/tags")
    @Operation(summary = "Get all available tags for filtering")
    public Result<List<TagVO>> getAlLTags() {
        return Result.success(tagService.listAll());
    }
}
