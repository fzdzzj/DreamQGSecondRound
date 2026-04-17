package com.qg.common.constant;

/**
 * 消息常量
 */
public interface MessageConstant {

    // ======================== 通用异常 ========================
    String UNKNOWN_ERROR = "未知错误";
    String NO_PERMISSION = "无权限操作";

    // ======================== 用户 / 账号相关 ========================
    String ACCOUNT_NOT_FOUND = "账号不存在";
    String USER_NOT_FOUND = "用户不存在";
    String ACCOUNT_DISABLED = "账号已被禁用或注销";
    String USER_NOT_AUTHORIZED = "不能封禁管理员或系统用户";

    String USERNAME_OR_PHONE_OR_EMAIL_EXISTS = "用户名或手机号或邮箱已存在";
    String EMAIL_EXISTS = "邮箱已被其他用户占用";
    String PHONE_EXISTS = "手机号已被其他用户占用";

    String PASSWORD_ERROR = "密码错误";
    String PASSWORD_ENCRYPT_ERROR = "密码加密异常";
    String PASSWORD_SAME = "新密码不能与旧密码相同";
    String PASSWORD_CONFIRM_ERROR = "两次密码不一致";

    // ======================== 验证码 ========================
    String CODE_ERROR = "验证码错误或已过期";
    String EMAIL_MISMATCH = "邮箱与当前用户不匹配";

    // ======================== Token 认证 ========================
    String REFRESH_TOKEN_NOT_EMPTY = "refreshToken不能为空";
    String REFRESH_TOKEN_INVALID = "Refresh Token 已失效";
    String REFRESH_TOKEN_EXPIRED = "Refresh Token 已过期";
    String TOKEN_TYPE_ILLEGAL = "非法 Token，类型错误";
    String TOKEN_INVALID = "Token 无效，无法获取用户信息";

    // ======================== 物品 / 失物招领 ========================
    String ALREADY_EXISTS = "已存在";
    String ITEM_NOT_FOUND = "不存在";
    String ITEM_STATUS_INVALID = "物品状态异常";

    String UPDATE_NOT_ALLOWED = "您没有权限修改";
    String VIEW_NOT_ALLOWED = "您没有权限查看";
    String DELETE_NOT_ALLOWED = "您没有权限删除";

    // ======================== 举报 ========================
    String REPORTED_ITEM = "你已经举报过该物品";
    String REPORT_NOT_FOUND = "举报记录不存在";
    String REPORT_NOT_PENDING = "举报记录不是待审核状态";

    // ======================== 置顶申请 ========================
    String PIN_REQUEST_ABSENT = "置顶申请不存在";
    String PIN_REQUEST_AUDIT_PASS = "置顶申请审核通过";

    // ======================== 认领申请 ========================
    String CLAIM_REQUEST_ABSENT = "认领申请不存在";
    String CLAIM_REQUEST_AUDIT_PASS = "认领申请审核通过";
    String CLAIM_REQUEST_AUDIT_REJECT = "认领申请审核拒绝";
    String CLAIM_REQUEST_AUDIT_MORE_INFO_REQUIRED = "需要更多信息";

    // ======================== 留言 / 评论 ========================
    String COMMENT_NOT_FOUND = "留言不存在";

    // ======================== 通知 ========================
    String NOTIFICATION_NOT_FOUND = "通知不存在";

    // ======================== 文件 / 图片上传 ========================
    String IMAGE_UPLOAD_FAILED = "图片上传失败";

    // ======================== AI 相关 ========================
    String AI_GENERATE_FAILED = "AI生成失败";
    String AI_RESULT_NOT_FOUND = "AI结果不存在";
}