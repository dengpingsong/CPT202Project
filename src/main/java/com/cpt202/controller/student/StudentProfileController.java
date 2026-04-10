package com.cpt202.controller.student;

import com.cpt202.dto.StudentProfileUpdateDTO;
import com.cpt202.model.entity.User;
import com.cpt202.result.Result;
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
    public Result<StudentProfileVO> getById(@PathVariable Long studentId) {
        log.info("Get student profile: {}", studentId);
        return Result.success(
                callbackAuthService.doWithAuthCheck(studentId, User.UserRole.STUDENT,
                        () -> profileService.getStudentProfile(studentId)));
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
                               @Valid @RequestBody StudentProfileUpdateDTO studentProfileUpdateDTO) {
        log.info("Update student profile: {}, payload: {}", studentId, studentProfileUpdateDTO);
        callbackAuthService.doWithAuthCheck(studentId, User.UserRole.STUDENT,
                () -> profileService.updateStudentProfile(studentId, studentProfileUpdateDTO));
        return Result.success();
    }
}
