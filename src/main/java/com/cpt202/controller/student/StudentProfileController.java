package com.cpt202.controller.student;

import com.cpt202.dto.StudentProfileUpdateDTO;
import com.cpt202.exception.UnauthorizedAccessException;
import com.cpt202.model.entity.User;
import com.cpt202.result.Result;
import com.cpt202.security.AuthContext;
import com.cpt202.service.CallbackAuthService;
import com.cpt202.service.ProfileService;
import com.cpt202.vo.StudentProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 学生端资料接口控制器。
 * 用于查询和维护学生个人资料。
 */
@RestController
@RequestMapping("/api/student/profile")
@Tag(name = "Student Profile API")
@Slf4j
public class StudentProfileController {

    private final ProfileService profileService;
    private final CallbackAuthService callbackAuthService;

    public StudentProfileController(ProfileService profileService, CallbackAuthService callbackAuthService) {
        this.profileService = profileService;
        this.callbackAuthService = callbackAuthService;
    }

    /**
     * 查询学生资料详情。
     *
     * @param studentId 学生主键
     * @return 学生资料展示对象
     */
    @GetMapping("/{studentId}")
    @Operation(summary = "Get student profile")
    public Result<StudentProfileVO> getById(@PathVariable Long studentId,
                                            @RequestHeader("Authorization") String authorization) {
        AuthContext authContext = callbackAuthService.requireAuth(authorization, User.UserRole.STUDENT);
        ensureCurrentStudent(studentId, authContext);
        log.info("Get student profile: {}", studentId);
        return Result.success(profileService.getStudentProfile(studentId));
    }

    /**
     * 修改学生资料。
     *
     * @param studentId 学生主键
     * @param studentProfileUpdateDTO 学生资料更新参数
     * @return 统一成功响应
     */
    @PutMapping("/{studentId}")
    @Operation(summary = "Update student profile")
    public Result<Void> update(@PathVariable Long studentId,
                               @Valid @RequestBody StudentProfileUpdateDTO studentProfileUpdateDTO,
                               @RequestHeader("Authorization") String authorization) {
        AuthContext authContext = callbackAuthService.requireAuth(authorization, User.UserRole.STUDENT);
        ensureCurrentStudent(studentId, authContext);
        log.info("Update student profile: {}, payload: {}", studentId, studentProfileUpdateDTO);
        profileService.updateStudentProfile(studentId, studentProfileUpdateDTO);
        return Result.success();
    }

    private void ensureCurrentStudent(Long studentId, AuthContext authContext) {
        if (!authContext.userId().equals(studentId)) {
            throw new UnauthorizedAccessException("不能访问或修改其他学生的资料。");
        }
    }
}
