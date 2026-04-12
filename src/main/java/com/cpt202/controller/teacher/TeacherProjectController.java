package com.cpt202.controller.teacher;

import com.cpt202.dto.ProjectDTO;
import com.cpt202.dto.ProjectStatusUpdateDTO;
import com.cpt202.dto.TeacherProjectQueryDTO;
import com.cpt202.exception.UnauthorizedAccessException;
import com.cpt202.model.entity.User;
import com.cpt202.result.Result;
import com.cpt202.security.AuthContext;
import com.cpt202.service.CallbackAuthService;
import com.cpt202.service.ProjectService;
import com.cpt202.vo.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端项目接口控制器。
 * 教师可以通过该控制器查询本人项目、查看项目详情，
 * 并执行新增、修改和状态变更等维护操作。
 */
@RestController
@RequestMapping("/api/teacher/projects")
@Tag(name = "Teacher Project API")
public class TeacherProjectController {

    private final ProjectService projectService;
    private final CallbackAuthService callbackAuthService;

    public TeacherProjectController(ProjectService projectService,
                                    CallbackAuthService callbackAuthService) {
        this.projectService = projectService;
        this.callbackAuthService = callbackAuthService;
    }

    /**
     * 查询教师名下项目列表。
     *
     * @param queryDTO 教师项目查询参数
     * @return 项目展示对象列表
     */
    @GetMapping
    @Operation(summary = "List teacher projects")
    public Result<List<ProjectVO>> list(@Valid TeacherProjectQueryDTO queryDTO,
                                        @RequestHeader("Authorization") String authorization) {
        AuthContext authContext = callbackAuthService.requireAuth(authorization, User.UserRole.TEACHER);
        ensureCurrentTeacher(queryDTO.getTeacherId(), authContext);
        return Result.success(projectService.listTeacherProjects(queryDTO.getTeacherId(), queryDTO.getStatus()));
    }

    /**
     * 查询项目详情。
     *
     * @param projectId 项目主键
     * @param teacherId 教师主键
     * @return 项目展示对象
     */
    @GetMapping("/{projectId}")
    @Operation(summary = "Get teacher project details")
    public Result<ProjectVO> getById(@PathVariable Long projectId,
                                     @RequestHeader("Authorization") String authorization) {
        callbackAuthService.requireAuth(authorization, User.UserRole.TEACHER);
        return Result.success(projectService.getProject(projectId));
    }

    /**
     * 新增项目。
     *
     * @param projectDTO 项目新增参数
     * @return 统一成功响应
     */
    @PostMapping
    @Operation(summary = "Create a project")
    public Result<Void> create(@Valid @RequestBody ProjectDTO projectDTO,
                               @RequestHeader("Authorization") String authorization) {
        AuthContext authContext = callbackAuthService.requireAuth(authorization, User.UserRole.TEACHER);
        ensureCurrentTeacher(projectDTO.getTeacherId(), authContext);
        projectService.create(projectDTO);
        return Result.success();
    }

    /**
     * 修改项目。
     *
     * @param projectId 项目主键
     * @param projectDTO 项目更新参数
     * @return 统一成功响应
     */
    @PutMapping("/{projectId}")
    @Operation(summary = "Update a project")
    public Result<Void> update(@PathVariable Long projectId,
                               @Valid @RequestBody ProjectDTO projectDTO,
                               @RequestHeader("Authorization") String authorization) {
        AuthContext authContext = callbackAuthService.requireAuth(authorization, User.UserRole.TEACHER);
        ensureCurrentTeacher(projectDTO.getTeacherId(), authContext);
        projectService.update(projectId, projectDTO);
        return Result.success();
    }

    /**
     * 修改项目状态。
     *
     * @param projectId 项目主键
     * @param projectStatusUpdateDTO 状态修改参数
     * @return 统一成功响应
     */
    @PutMapping("/{projectId}/status")
    @Operation(summary = "Change project status")
    public Result<Void> changeStatus(@PathVariable Long projectId,
                                     @Valid @RequestBody ProjectStatusUpdateDTO projectStatusUpdateDTO,
                                     @RequestHeader("Authorization") String authorization) {
        callbackAuthService.requireAuth(authorization, User.UserRole.TEACHER);
        projectService.changeStatus(projectId, projectStatusUpdateDTO);
        return Result.success();
    }

    private void ensureCurrentTeacher(Long teacherId, AuthContext authContext) {
        if (!authContext.userId().equals(teacherId)) {
            throw new UnauthorizedAccessException("不能操作其他教师名下的项目。");
        }
    }
}
