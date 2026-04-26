package com.cpt202.controller.admin;

import com.cpt202.dto.AdminUserQueryDTO;
import com.cpt202.result.Result;
import com.cpt202.service.UserAdminService;
import com.cpt202.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    /**
     * 构造器注入管理端用户服务。
     *
     * @param userAdminService 用户管理服务
     */
    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    /**
     * 按角色和账号状态筛选用户列表。
     *
     * @param queryDTO 用户查询参数
     * @return 用户展示对象列表
     */
    @GetMapping
    @Operation(summary = "List users")
    public Result<List<UserVO>> list(AdminUserQueryDTO queryDTO) {
        return Result.success(userAdminService.listUsers(queryDTO.getRole(), queryDTO.getAccountStatus()));
    }

    /**
     * 修改指定用户的账号状态。
     *
     * @param userId 用户主键
     * @param accountStatus 账号状态
     * @return 统一成功响应
     */
    @PutMapping("/{userId}/status")
    @Operation(summary = "Update user status")
    public Result<Void> updateStatus(@PathVariable Long userId,
                                     @RequestParam String accountStatus) {
        userAdminService.updateStatus(userId, accountStatus);
        return Result.success();
    }
}
