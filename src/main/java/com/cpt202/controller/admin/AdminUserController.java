package com.cpt202.controller.admin;

import com.cpt202.dto.AdminUserQueryDTO;
import com.cpt202.dto.AdminUserUpdateDTO;
import com.cpt202.result.Result;
import com.cpt202.service.CallbackAuthService;
import com.cpt202.service.UserAdminService;
import com.cpt202.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端用户接口控制器。
 * 提供用户列表查询与账号状态修改能力。
 */
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin User API")
public class AdminUserController {

    private final UserAdminService userAdminService;
    private final CallbackAuthService callbackAuthService;

    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
        this.callbackAuthService = callbackAuthService;
    }

    /**
     * 按角色和账号状态筛选用户列表。
     *
     * @param queryDTO 用户查询参数
     * @return 用户展示对象列表
     */
    @GetMapping
    @Operation(summary = "List users")
    public Result<List<UserVO>> list(AdminUserQueryDTO queryDTO,
                                     @RequestHeader("Authorization") String authorization) {
        callbackAuthService.requireAuth(authorization, User.UserRole.ADMIN);
        return Result.success(userAdminService.listUsers(queryDTO.getRole(), queryDTO.getAccountStatus()));
    }

    /**
     * 修改指定用户的账号状态。
     *
     * @param userId 用户主键
     * @param accountStatus 账号状态
     * @param operatorId 操作人主键
     * @return 统一成功响应
     */
    @PutMapping("/{userId}/status")
    @Operation(summary = "Update user status")
    public Result<Void> updateStatus(@PathVariable Long userId,
                                     @RequestParam String accountStatus,
                                     @RequestHeader("Authorization") String authorization) {
        callbackAuthService.requireAuth(authorization, User.UserRole.ADMIN);
        userAdminService.updateStatus(userId, accountStatus);
        return Result.success();
    }

    /**
     * 修改指定用户的基础信息。
     *
     * @param userId 用户主键
     * @param updateDTO 用户基础信息
     * @return 统一成功响应
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update user basic information")
    public Result<Void> updateUser(@PathVariable Long userId,
                                   @Valid @RequestBody AdminUserUpdateDTO updateDTO) {
        userAdminService.updateUser(userId, updateDTO);
        return Result.success();
    }
}
