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
    public static final String EMAIL_FORMAT_INVALID = "邮箱格式不正确。";
    public static final String EMAIL_DOMAIN_NOT_ALLOWED = "邮箱域名不被支持，请使用 @student.xjtlu.edu.cn（学生）或 @xjtlu.edu.cn（教师）邮箱注册。";
    public static final String DATA_INTEGRITY_VIOLATION = "数据与现有记录冲突，请检查后重试。";
    public static final String INVALID_REQUEST_BODY = "请求体格式不正确，请检查日期等字段格式。";
    public static final String INTERNAL_SERVER_ERROR = "服务器发生未预期错误，请稍后再试。";
    public static final String INVALID_CREDENTIALS = "用户名或密码错误。";
    public static final String EMAIL_OTP_SENT = "如果该邮箱已绑定可用账号，验证码已发送，请注意查收。";
    public static final String EMAIL_OTP_MAIL_NOT_CONFIGURED = "系统尚未配置发信邮箱，暂时无法通过邮箱验证码登录。";
    public static final String EMAIL_OTP_MAIL_SEND_FAILED = "验证码邮件发送失败，请检查邮件服务配置。";
    public static final String EMAIL_OTP_REQUIRED = "验证码不能为空。";
    public static final String EMAIL_OTP_INVALID = "验证码错误，请重新输入。";
    public static final String EMAIL_OTP_EXPIRED = "验证码已过期，请重新获取。";
    public static final String EMAIL_OTP_REQUEST_TOO_FREQUENT = "验证码发送过于频繁，请稍后再试。";
    public static final String TWO_FACTOR_CODE_REQUIRED = "双重验证验证码不能为空。";
    public static final String TWO_FACTOR_CODE_INVALID = "双重验证验证码错误，请重试。";
    public static final String TWO_FACTOR_NOT_ENABLED = "当前账号尚未开启双重验证。";
    public static final String TWO_FACTOR_ALREADY_ENABLED = "当前账号已开启双重验证。";
    public static final String TWO_FACTOR_SETUP_EXPIRED = "双重验证初始化已过期，请重新开始配置。";
    public static final String TWO_FACTOR_LOGIN_REQUIRED = "当前账号需要完成双重验证后才能登录。";
    public static final String TWO_FACTOR_CHALLENGE_INVALID = "双重验证登录会话无效或已过期，请重新登录。";
    public static final String PASSWORD_RESET_EMAIL_SENT = "如果该邮箱已绑定账号，重置链接已发送，请注意查收。";
    public static final String PASSWORD_RESET_LINK_INVALID = "重置链接无效，请重新申请。";
    public static final String PASSWORD_RESET_LINK_EXPIRED = "重置链接已过期，请重新申请。";
    public static final String PASSWORD_RESET_LINK_ALREADY_USED = "重置链接已失效，请重新申请。";
    public static final String PASSWORD_RESET_TOKEN_REQUIRED = "缺少重置令牌。";
    public static final String PASSWORD_RESET_MAIL_NOT_CONFIGURED = "系统尚未配置发信邮箱，暂时无法通过邮件找回密码。";
    public static final String PASSWORD_RESET_MAIL_SEND_FAILED = "密码重置邮件发送失败，请检查邮件服务配置。";
    public static final String ACCOUNT_UNAVAILABLE_CONTACT_ADMIN = "账户当前不可用，请联系管理员。";
    public static final String STUDENT_NO_REQUIRED = "学生学号不能为空。";
    public static final String STUDENT_PROGRAMME_REQUIRED = "学生专业不能为空。";
    public static final String STUDENT_ENROLLMENT_DATE_REQUIRED = "入学日期不能为空。";
    public static final String TEACHER_STAFF_NO_REQUIRED = "教师工号不能为空。";
    public static final String TEACHER_DEPARTMENT_REQUIRED = "教师院系不能为空。";
    public static final String TEACHER_TITLE_REQUIRED = "教师职称不能为空。";
    public static final String ENROLLMENT_DATE_CANNOT_BE_FUTURE = "入学日期不能晚于今天。";
    public static final String PROJECT_STATUS_REQUESTED_NOT_ALLOWED_MANUALLY = "教师不能手动将项目状态改为 REQUESTED。";
    public static final String PROJECT_STATUS_ARCHIVED_DISABLED = "当前系统仅保留 CLOSED 状态，请不要再设置 ARCHIVED。";
    public static final String PROJECT_STATUS_TRANSITION_INVALID = "当前项目状态不允许这样修改。";
    public static final String PROJECT_CAPACITY_EXCEEDED = "项目人数已满，无法继续同意申请。";
    public static final String PROJECT_REQUEST_ALREADY_EXISTS = "你已经申请过该项目，不能重复申请。";
    public static final String PROJECT_PREFERENCE_RANK_DUPLICATED = "志愿顺位不能重复，请选择其他顺位。";
    public static final String PROJECT_CLOSED_AND_REQUEST_CANCELLED = "项目已关闭，原申请结果已取消，请重新选择其他项目。";
    public static final String CATEGORY_NAME_EXISTS = "分类名称已存在，请勿重复创建。";
    public static final String TAG_NAME_EXISTS = "标签名称已存在，请勿重复创建。";

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
    public static final String TWO_FACTOR_DISABLE_PASSWORD_REQUIRED = "关闭双重验证前请输入当前密码。";

    public static final String NON_STUDENT_PROFILE_ACCESS = "该用户不是学生角色。无法查询学生资料。";
    public static final String NON_STUDENT_PROFILE_UPDATE = "该用户不是学生角色。无法修改学生资料。";
    public static final String NON_TEACHER_PROFILE_ACCESS = "该用户不是教师角色。无法查询教师资料。";
    public static final String NON_TEACHER_PROFILE_UPDATE = "该用户不是教师角色。无法修改教师资料。";
    public static final String NON_ADMIN_PROFILE_ACCESS = "该用户不是管理员角色。无法查询管理员资料。";
    public static final String NON_ADMIN_PROFILE_UPDATE = "该用户不是管理员角色。无法修改管理员资料。";

    public static final String REQUEST_DEADLINE_PASSED = "The deadline has passed.";
    public static final String ACTIVE_REQUEST_EXISTS = "Operation failed: You already have an agreed project.";
    public static final String PROJECT_NOT_ACCEPTING_REQUESTS = "This project is closed or archived and cannot accept new requests.";
    public static final String REQUEST_RECORD_NOT_FOUND = "Request record not found.";
    public static final String STUDENT_RECORD_NOT_FOUND = "Student record not found.";
    public static final String PROJECT_RECORD_NOT_FOUND = "Project record not found.";

    public static final String REQUEST_NOT_PENDING_CANNOT_REVIEW = "该申请当前不是待审核状态，不能重复审批。";
    public static final String AUTO_REJECT_ALREADY_MATCHED = "System auto-rejected: Already matched elsewhere.";
    public static final String AUTO_REJECT_ALREADY_MATCHED_REMARK = "系统自动驳回：该学生已在其他项目中被录取。";
    public static final String REQUEST_SUBMIT_REMARK = "学生提交申请。";
    public static final String REQUEST_WITHDRAW_REMARK = "学生撤回申请。";
    public static final String AUTO_CANCEL_PROJECT_CLOSED_REMARK = "系统自动取消：教师关闭了该项目。";

    private MessageConstants() {
    }
}
