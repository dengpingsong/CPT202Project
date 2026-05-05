package com.cpt202.constant;

/**
 * Centralized business and validation messages used by the backend.
 */
public final class MessageConstants {

    public static final String INVALID_BEARER_TOKEN = "Missing or invalid Authorization Bearer token.";
    public static final String INVALID_LOGIN_STATE = "Login state is invalid or expired. Please log in again.";
    public static final String USER_NOT_FOUND_OR_DENIED = "User does not exist or access is denied.";
    public static final String ACCOUNT_DISABLED = "This account is currently unavailable.";
    public static final String ROLE_REQUIRED_TEMPLATE = "Access denied: current role is %s, but this operation requires %s.";
    public static final String ROLE_MISMATCH = "Login role does not match the current account role.";

    public static final String USERNAME_EXISTS = "Username already exists. Please choose another one.";
    public static final String EMAIL_EXISTS = "Email is already registered. Please use another email.";
    public static final String EMAIL_FORMAT_INVALID = "Invalid email format.";
    public static final String EMAIL_DOMAIN_NOT_ALLOWED = "Unsupported email domain. Please use @student.xjtlu.edu.cn for students or @xjtlu.edu.cn for teachers.";
    public static final String DATA_INTEGRITY_VIOLATION = "The submitted data conflicts with existing records.";
    public static final String INVALID_REQUEST_BODY = "Invalid request body. Please check field formats such as dates.";
    public static final String INTERNAL_SERVER_ERROR = "Unexpected server error. Please check the server log.";
    public static final String INVALID_CREDENTIALS = "Incorrect username or password.";
    public static final String EMAIL_OTP_SENT = "If this email is linked to an active account, a verification code has been sent.";
    public static final String EMAIL_OTP_MAIL_NOT_CONFIGURED = "Email delivery is not configured. Email OTP login is currently unavailable.";
    public static final String EMAIL_OTP_MAIL_SEND_FAILED = "Unable to send the verification email. Please check the email service configuration.";
    public static final String EMAIL_OTP_REQUIRED = "Verification code is required.";
    public static final String EMAIL_OTP_INVALID = "Incorrect verification code. Please try again.";
    public static final String EMAIL_OTP_EXPIRED = "Verification code has expired. Please request a new one.";
    public static final String EMAIL_OTP_REQUEST_TOO_FREQUENT = "Verification codes are being requested too often. Please try again later.";
    public static final String TWO_FACTOR_CODE_REQUIRED = "Two-factor verification code is required.";
    public static final String TWO_FACTOR_CODE_INVALID = "Incorrect two-factor verification code. Please try again.";
    public static final String TWO_FACTOR_NOT_ENABLED = "Two-factor authentication is not enabled for this account.";
    public static final String TWO_FACTOR_ALREADY_ENABLED = "Two-factor authentication is already enabled for this account.";
    public static final String TWO_FACTOR_SETUP_EXPIRED = "Two-factor setup has expired. Please start setup again.";
    public static final String TWO_FACTOR_LOGIN_REQUIRED = "Two-factor verification is required before login.";
    public static final String TWO_FACTOR_CHALLENGE_INVALID = "Two-factor login session is invalid or expired. Please log in again.";
    public static final String PASSWORD_RESET_EMAIL_SENT = "If this email is linked to an account, a reset link has been sent.";
    public static final String PASSWORD_RESET_LINK_INVALID = "Reset link is invalid. Please request a new one.";
    public static final String PASSWORD_RESET_LINK_EXPIRED = "Reset link has expired. Please request a new one.";
    public static final String PASSWORD_RESET_LINK_ALREADY_USED = "Reset link is no longer valid. Please request a new one.";
    public static final String PASSWORD_RESET_TOKEN_REQUIRED = "Reset token is missing.";
    public static final String PASSWORD_RESET_MAIL_NOT_CONFIGURED = "Email delivery is not configured. Password reset by email is currently unavailable.";
    public static final String PASSWORD_RESET_MAIL_SEND_FAILED = "Unable to send the password reset email. Please check the email service configuration.";
    public static final String ACCOUNT_UNAVAILABLE_CONTACT_ADMIN = "This account is currently unavailable. Please contact an administrator.";
    public static final String REGISTER_ROLE_REQUIRED = "Role is required.";
    public static final String STUDENT_NO_REQUIRED = "Student number is required.";
    public static final String STUDENT_PROGRAMME_REQUIRED = "Programme is required.";
    public static final String STUDENT_ENROLLMENT_DATE_REQUIRED = "Enrollment date is required.";
    public static final String TEACHER_STAFF_NO_REQUIRED = "Staff number is required.";
    public static final String TEACHER_DEPARTMENT_REQUIRED = "Department is required.";
    public static final String TEACHER_TITLE_REQUIRED = "Title is required.";
    public static final String ENROLLMENT_DATE_CANNOT_BE_FUTURE = "Enrollment date cannot be in the future.";
    public static final String PROJECT_STATUS_REQUESTED_NOT_ALLOWED_MANUALLY = "Teachers cannot manually change a project status to REQUESTED.";
    public static final String PROJECT_STATUS_ARCHIVED_DISABLED = "Only CLOSED is supported. Please do not set ARCHIVED.";
    public static final String PROJECT_STATUS_TRANSITION_INVALID = "This project status transition is not allowed.";
    public static final String PROJECT_CAPACITY_EXCEEDED = "Project capacity has been reached. No more applications can be accepted.";
    public static final String PROJECT_REQUEST_ALREADY_EXISTS = "You have already applied for this project.";
    public static final String PROJECT_PREFERENCE_RANK_DUPLICATED = "Preference rank cannot be duplicated. Please choose another rank.";
    public static final String PROJECT_CLOSED_AND_REQUEST_CANCELLED = "The project has been closed and the original application was cancelled. Please choose another project.";
    public static final String CATEGORY_NAME_EXISTS = "Category name already exists.";
    public static final String TAG_NAME_EXISTS = "Tag name already exists.";

    public static final String CATEGORY_NOT_FOUND = "Category not found.";
    public static final String CATEGORY_TO_UPDATE_NOT_FOUND = "Category to update was not found.";
    public static final String CATEGORY_TO_DELETE_NOT_FOUND = "Category to delete was not found.";
    public static final String TAG_NOT_FOUND = "Tag not found.";
    public static final String TAG_TO_UPDATE_NOT_FOUND = "Tag to update was not found.";
    public static final String TAG_TO_DELETE_NOT_FOUND = "Tag to delete was not found.";
    public static final String PROJECT_NOT_FOUND = "Project not found.";
    public static final String PROJECT_CATEGORY_NOT_FOUND = "Project category not found.";
    public static final String TEACHER_NOT_FOUND = "Teacher not found.";
    public static final String PARTIAL_TAGS_NOT_FOUND = "Some tags were not found.";
    public static final String REQUEST_NOT_FOUND = "Application request not found.";
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String STUDENT_PROFILE_NOT_FOUND = "Student profile not found.";
    public static final String TEACHER_PROFILE_NOT_FOUND = "Teacher profile not found.";

    public static final String CANNOT_OPERATE_OTHER_TEACHER_PROJECT = "You cannot operate on another teacher's project.";
    public static final String CANNOT_UPDATE_OTHER_TEACHER_TAGS = "You cannot update tags for another teacher's project.";
    public static final String CANNOT_REVIEW_OTHER_TEACHER_REQUEST = "You cannot review applications for another teacher's project.";
    public static final String CANNOT_WITHDRAW_OTHER_STUDENT_REQUEST = "You cannot withdraw another student's application.";
    public static final String CANNOT_VIEW_OTHER_STUDENT_HISTORY = "You cannot view another student's application history.";
    public static final String INCORRECT_OLD_PASSWORD = "Old password is incorrect.";
    public static final String NEW_PASSWORD_SAME_AS_OLD = "New password cannot be the same as the old password.";
    public static final String TWO_FACTOR_DISABLE_PASSWORD_REQUIRED = "Please enter your current password before disabling two-factor authentication.";

    public static final String NON_STUDENT_PROFILE_ACCESS = "This user is not a student. Student profile cannot be accessed.";
    public static final String NON_STUDENT_PROFILE_UPDATE = "This user is not a student. Student profile cannot be updated.";
    public static final String NON_TEACHER_PROFILE_ACCESS = "This user is not a teacher. Teacher profile cannot be accessed.";
    public static final String NON_TEACHER_PROFILE_UPDATE = "This user is not a teacher. Teacher profile cannot be updated.";
    public static final String NON_ADMIN_PROFILE_ACCESS = "This user is not an admin. Admin profile cannot be accessed.";
    public static final String NON_ADMIN_PROFILE_UPDATE = "This user is not an admin. Admin profile cannot be updated.";

    public static final String REQUEST_DEADLINE_PASSED = "The deadline has passed.";
    public static final String ACTIVE_REQUEST_EXISTS = "Operation failed: You already have an agreed project.";
    public static final String PROJECT_NOT_ACCEPTING_REQUESTS = "This project is closed or archived and cannot accept new requests.";
    public static final String REQUEST_RECORD_NOT_FOUND = "Request record not found.";
    public static final String STUDENT_RECORD_NOT_FOUND = "Student record not found.";
    public static final String PROJECT_RECORD_NOT_FOUND = "Project record not found.";

    private MessageConstants() {
    }
}
