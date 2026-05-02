package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Password reset confirmation payload.
 */
@Data
public class PasswordResetConfirmDTO {

    @NotBlank(message = "重置令牌不能为空。")
    private String token;

    @NotBlank
    @Size(min = 6, message = "新密码长度不能少于 6 位。")
    private String newPassword;
}
