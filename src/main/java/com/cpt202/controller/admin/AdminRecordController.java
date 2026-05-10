package com.cpt202.controller.admin;

import com.cpt202.dto.AdminRequestRecordQueryDTO;
import com.cpt202.dto.PageQueryDTO;
import com.cpt202.result.PageResult;
import com.cpt202.result.Result;
import com.cpt202.service.RecordService;
import com.cpt202.vo.ProjectVO;
import com.cpt202.vo.ProjectRequestVO;
import com.cpt202.vo.RequestStatusHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    public AdminRecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/projects")
    @Operation(summary = "List project records")
    public Result<List<ProjectVO>> listProjectRecords() {
        log.info("List project records");
        return Result.success(recordService.listProjectRecords());
    }

    @GetMapping("/projects/page")
    @Operation(summary = "List project records by page")
    public Result<PageResult<ProjectVO>> listProjectRecordsPage(@Valid PageQueryDTO queryDTO) {
        log.info("List project records by page, pageNum: {}, pageSize: {}", queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(recordService.listProjectRecordsPage(queryDTO));
    }

    @GetMapping("/requests")
    @Operation(summary = "List request records")
    public Result<List<ProjectRequestVO>> listRequestRecords(AdminRequestRecordQueryDTO queryDTO) {
        log.info("List request records, status: {}", queryDTO.getStatus());
        return Result.success(recordService.listRequestRecords(queryDTO.getStatus()));
    }

    @GetMapping("/requests/page")
    @Operation(summary = "List request records by page")
    public Result<PageResult<ProjectRequestVO>> listRequestRecordsPage(@Valid AdminRequestRecordQueryDTO queryDTO) {
        log.info("List request records by page, status: {}, pageNum: {}, pageSize: {}",
                queryDTO.getStatus(), queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(recordService.listRequestRecordsPage(queryDTO));
    }

    @GetMapping("/request-history")
    @Operation(summary = "List request history records")
    public Result<List<RequestStatusHistoryVO>> listRequestHistoryRecords() {
        log.info("List request history records");
        return Result.success(recordService.listRequestHistoryRecords());
    }

    @GetMapping("/request-history/page")
    @Operation(summary = "List request history records by page")
    public Result<PageResult<RequestStatusHistoryVO>> listRequestHistoryRecordsPage(@Valid PageQueryDTO queryDTO) {
        log.info("List request history records by page, pageNum: {}, pageSize: {}", queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(recordService.listRequestHistoryRecordsPage(queryDTO));
    }
}
