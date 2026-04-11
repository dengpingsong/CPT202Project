package com.cpt202.controller.teacher;

import com.cpt202.dto.TeacherProfileUpdateDTO;
import com.cpt202.exception.UnauthorizedAccessException;
import com.cpt202.model.entity.User;
import com.cpt202.result.Result;
import com.cpt202.security.AuthContext;
import com.cpt202.service.CallbackAuthService;
import com.cpt202.service.ProfileService;
import com.cpt202.vo.TeacherProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 教师端资料接口控制器。
 * 用于查询和维护教师个人资料。
 */
@RestController
@RequestMapping("/api/teacher/profile")
@Tag(name = "Teacher Profile API")
@Slf4j
public class TeacherProfileController {

    private final ProfileService profileService;
    private final CallbackAuthService callbackAuthService;

    public TeacherProfileController(ProfileService profileService, CallbackAuthService callbackAuthService) {
        this.profileService = profileService;
        this.callbackAuthService = callbackAuthService;
    }

    /**
     * 查询教师资料详情。
     *
     * @param teacherId 教师主键
     * @return 教师资料展示对象
     */
    @GetMapping("/{teacherId}")
    @Operation(summary = "Get teacher profile")
    public Result<TeacherProfileVO> getById(@PathVariable Long teacherId,
                                            @RequestHeader("Authorization") String authorization) {
        AuthContext authContext = callbackAuthService.requireAuth(authorization, User.UserRole.TEACHER);
        ensureCurrentTeacher(teacherId, authContext);
        log.info("Get teacher profile: {}", teacherId);
        return Result.success(profileService.getTeacherProfile(teacherId));
    }

    /**
     * 修改教师资料。
     *
     * @param teacherId 教师主键
     * @param teacherProfileUpdateDTO 教师资料更新参数
     * @return 统一成功响应
     */
    @PutMapping("/{teacherId}")
    @Operation(summary = "Update teacher profile")
    public Result<Void> update(@PathVariable Long teacherId,
                               @Valid @RequestBody TeacherProfileUpdateDTO teacherProfileUpdateDTO,
                               @RequestHeader("Authorization") String authorization) {
        AuthContext authContext = callbackAuthService.requireAuth(authorization, User.UserRole.TEACHER);
        ensureCurrentTeacher(teacherId, authContext);
        log.info("Update teacher profile: {}, payload: {}", teacherId, teacherProfileUpdateDTO);
        profileService.updateTeacherProfile(teacherId, teacherProfileUpdateDTO);
        return Result.success();
    }

    private void ensureCurrentTeacher(Long teacherId, AuthContext authContext) {
        if (!authContext.userId().equals(teacherId)) {
            throw new UnauthorizedAccessException("不能访问或修改其他教师的资料。");
        }
    }
}
