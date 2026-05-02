package com.cpt202.controller.admin;

import com.cpt202.context.BaseContext;
import com.cpt202.dto.AdminProfileUpdateDTO;
import com.cpt202.dto.ChangePasswordDTO;
import com.cpt202.result.Result;
import com.cpt202.service.ProfileService;
import com.cpt202.vo.AdminProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员个人资料接口。
 */
@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
@Tag(name = "Admin Profile API")
public class AdminProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    @Operation(summary = "Get current admin profile")
    public Result<AdminProfileVO> getMyProfile() {
        return Result.success(profileService.getAdminProfile(BaseContext.getCurrentUserId()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current admin profile")
    public Result<Void> updateMyProfile(@Valid @RequestBody AdminProfileUpdateDTO adminProfileUpdateDTO) {
        profileService.updateAdminProfile(BaseContext.getCurrentUserId(), adminProfileUpdateDTO);
        return Result.success();
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change current admin password")
    public Result<Void> changeMyPassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        profileService.changePassword(BaseContext.getCurrentUserId(), changePasswordDTO);
        return Result.success();
    }
}
