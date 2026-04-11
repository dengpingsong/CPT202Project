package com.cpt202.controller.admin;

import com.cpt202.dto.AdminRequestRecordQueryDTO;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.result.Result;
import com.cpt202.service.RecordService;
import com.cpt202.vo.ProjectVO;
import com.cpt202.vo.ProjectRequestVO;
import com.cpt202.vo.RequestStatusHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端记录接口控制器。
 * 用于统一查询项目记录、申请记录以及申请历史记录，
 * 方便管理端进行数据审查与运营分析。
 */
@RestController
@RequestMapping("/api/admin/records")
@Tag(name = "Admin Record API")
@Slf4j
public class AdminRecordController {

    private final RecordService recordService;

    /**
     * 构造器注入记录服务。
     *
     * @param recordService 记录服务
     */
    public AdminRecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/projects")
    @Operation(summary = "List project records")
    public Result<List<ProjectVO>> listProjectRecords(@RequestHeader("Authorization") String authorization) {
        callbackAuthService.requireAuth(authorization, User.UserRole.ADMIN);
        log.info("List project records");
        return Result.success(recordService.listProjectRecords());
    }

    @GetMapping("/requests")
    @Operation(summary = "List request records")
    public Result<List<ProjectRequestVO>> listRequestRecords(AdminRequestRecordQueryDTO queryDTO,
                                                             @RequestHeader("Authorization") String authorization) {
        callbackAuthService.requireAuth(authorization, User.UserRole.ADMIN);
        log.info("List request records, status: {}", queryDTO.getStatus());
        return Result.success(recordService.listRequestRecords(queryDTO.getStatus()));
    }

    @GetMapping("/request-history")
    @Operation(summary = "List request history records")
    public Result<List<RequestStatusHistoryVO>> listRequestHistoryRecords(@RequestHeader("Authorization") String authorization) {
        callbackAuthService.requireAuth(authorization, User.UserRole.ADMIN);
        log.info("List request history records");
        return Result.success(recordService.listRequestHistoryRecords());
    }
}
