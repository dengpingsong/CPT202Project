package com.cpt202.controller.teacher;

import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.dto.TeacherProjectRequestQueryDTO;
import com.cpt202.result.Result;
import com.cpt202.service.ProjectRequestService;
import com.cpt202.vo.ProjectRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端申请审核接口控制器。
 * 教师可通过该控制器查看待审核申请，并执行审核动作。
 */
@RestController
@RequestMapping("/api/teacher/requests")
@Tag(name = "Teacher Request Review API")
public class TeacherProjectRequestController {

    private final ProjectRequestService projectRequestService;

    /**
     * 构造器注入项目申请服务。
     *
     * @param projectRequestService 项目申请服务
     */
    public TeacherProjectRequestController(ProjectRequestService projectRequestService) {
        this.projectRequestService = projectRequestService;
    }

    /**
     * 查询教师待审核或指定状态的申请列表。
     *
     * @param queryDTO 教师申请查询参数
     * @return 申请展示对象列表
     */
    @GetMapping
    @Operation(summary = "List teacher requests for review")
    public Result<List<ProjectRequestVO>> list(@Valid TeacherProjectRequestQueryDTO queryDTO) {
        return Result.success(projectRequestService.listTeacherRequests(queryDTO.getTeacherId(), queryDTO.getStatus()));
    }

    /**
     * 审核指定申请。
     *
     * @param requestId 申请主键
     * @param projectRequestReviewDTO 审核参数
     * @return 统一成功响应
     */
    @PutMapping("/{requestId}/review")
    @Operation(summary = "Review a project request")
    public Result<Void> review(@PathVariable Long requestId,
                               @Valid @RequestBody ProjectRequestReviewDTO projectRequestReviewDTO) {
        projectRequestService.review(requestId, projectRequestReviewDTO);
        return Result.success();
    }
}
