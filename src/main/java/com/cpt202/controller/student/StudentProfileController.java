package com.cpt202.controller.student;

import com.cpt202.context.BaseContext;
import com.cpt202.dto.StudentProfileUpdateDTO;
import com.cpt202.result.Result;
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

    public StudentProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * 查询学生资料详情。
     *
     * @return 学生资料展示对象
     */
    @GetMapping("/me")
    @Operation(summary = "Get current student profile")
    public Result<StudentProfileVO> getMyProfile() {
        Long studentId = BaseContext.getCurrentUserId();
        log.info("Get student profile: {}", studentId);
        return Result.success(profileService.getStudentProfile(studentId));
    }

    /**
     * 修改学生资料。
     *
     * @param studentProfileUpdateDTO 学生资料更新参数
     * @return 统一成功响应
     */
    @PutMapping("/me")
    @Operation(summary = "Update current student profile")
    public Result<Void> updateMyProfile(@Valid @RequestBody StudentProfileUpdateDTO studentProfileUpdateDTO) {
        Long studentId = BaseContext.getCurrentUserId();
        log.info("Update student profile: {}, payload: {}", studentId, studentProfileUpdateDTO);
        profileService.updateStudentProfile(studentId, studentProfileUpdateDTO);
        return Result.success();
    }
}
