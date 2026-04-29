package com.cpt202.constant;

/**
 * Centralized business and validation messages used by the backend.
 */
public final class MessageConstants {

    public static final String INVALID_BEARER_TOKEN = "缺少有效的 Authorization Bearer Token。";
    public static final String INVALID_LOGIN_STATE = "登录状态无效或已过期，请重新登录。";
    public static final String USER_NOT_FOUND_OR_DENIED = "用户不存在，拒绝访问。";
    public static final String ACCOUNT_DISABLED = "账号当前不可用，拒绝访问。";
    public static final String ROLE_REQUIRED_TEMPLATE = "权限不足：当前角色为 %s，该操作需要 %s 角色。";
    public static final String ROLE_MISMATCH = "登录角色与当前账号角色不一致。";

    public static final String USERNAME_EXISTS = "用户名已存在，请更换后重试。";
    public static final String EMAIL_EXISTS = "邮箱已被注册，请更换后重试。";
    public static final String INVALID_CREDENTIALS = "用户名或密码错误。";
    public static final String ACCOUNT_UNAVAILABLE_CONTACT_ADMIN = "账户当前不可用，请联系管理员。";
    public static final String REGISTER_ROLE_REQUIRED = "注册角色不能为空。";
    public static final String STUDENT_NO_REQUIRED = "学生学号不能为空。";
    public static final String STUDENT_PROGRAMME_REQUIRED = "学生专业不能为空。";
    public static final String STUDENT_ENROLLMENT_DATE_REQUIRED = "入学日期不能为空。";
    public static final String TEACHER_STAFF_NO_REQUIRED = "教师工号不能为空。";
    public static final String TEACHER_DEPARTMENT_REQUIRED = "教师院系不能为空。";
    public static final String TEACHER_TITLE_REQUIRED = "教师职称不能为空。";

    public static final String CATEGORY_NOT_FOUND = "分类不存在。";
    public static final String CATEGORY_TO_UPDATE_NOT_FOUND = "要修改的分类不存在。";
    public static final String CATEGORY_TO_DELETE_NOT_FOUND = "要删除的分类不存在。";
    public static final String TAG_NOT_FOUND = "标签不存在。";
    public static final String TAG_TO_UPDATE_NOT_FOUND = "要修改的标签不存在。";
    public static final String TAG_TO_DELETE_NOT_FOUND = "要删除的标签不存在。";
    public static final String PROJECT_NOT_FOUND = "项目不存在。";
    public static final String PROJECT_CATEGORY_NOT_FOUND = "项目分类不存在。";
    public static final String TEACHER_NOT_FOUND = "教师不存在。";
    public static final String PARTIAL_TAGS_NOT_FOUND = "部分标签不存在。";
    public static final String REQUEST_NOT_FOUND = "申请记录不存在。";
    public static final String USER_NOT_FOUND = "用户不存在。";
    public static final String STUDENT_PROFILE_NOT_FOUND = "学生资料未找到。";
    public static final String TEACHER_PROFILE_NOT_FOUND = "教师资料未找到。";

    public static final String CANNOT_OPERATE_OTHER_TEACHER_PROJECT = "不能操作其他教师名下的项目。";
    public static final String CANNOT_UPDATE_OTHER_TEACHER_TAGS = "不能修改其他教师名下项目的标签。";
    public static final String CANNOT_REVIEW_OTHER_TEACHER_REQUEST = "不能审核其他教师名下项目的申请。";
    public static final String CANNOT_WITHDRAW_OTHER_STUDENT_REQUEST = "不能撤回其他学生的申请。";
    public static final String CANNOT_VIEW_OTHER_STUDENT_HISTORY = "不能查看其他学生的申请历史。";
    public static final String INCORRECT_OLD_PASSWORD = "旧密码不正确。";
    public static final String NEW_PASSWORD_SAME_AS_OLD = "新密码不能与旧密码相同。";

    public static final String NON_STUDENT_PROFILE_ACCESS = "该用户不是学生角色。无法查询学生资料。";
    public static final String NON_STUDENT_PROFILE_UPDATE = "该用户不是学生角色。无法修改学生资料。";
    public static final String NON_TEACHER_PROFILE_ACCESS = "该用户不是教师角色。无法查询教师资料。";
    public static final String NON_TEACHER_PROFILE_UPDATE = "该用户不是教师角色。无法修改教师资料。";

    public static final String REQUEST_DEADLINE_PASSED = "The deadline has passed.";
    public static final String ACTIVE_REQUEST_EXISTS = "Operation failed: You already have an active or accepted request.";
    public static final String REQUEST_RECORD_NOT_FOUND = "Request record not found.";
    public static final String STUDENT_RECORD_NOT_FOUND = "Student record not found.";
    public static final String PROJECT_RECORD_NOT_FOUND = "Project record not found.";

    private MessageConstants() {
    }
}
