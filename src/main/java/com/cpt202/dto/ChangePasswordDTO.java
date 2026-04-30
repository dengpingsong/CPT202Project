package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for changing a user's own password.
 */
@Data
public class ChangePasswordDTO {

    /** The user's current password, used for identity verification. */
    @NotBlank(message = "旧密码不能为空。")
    private String oldPassword;

    /** The new password to replace the current one. */
    @NotBlank(message = "新密码不能为空。")
    @Size(min = 6, message = "新密码长度不能少于 6 位。")
    private String newPassword;
}
