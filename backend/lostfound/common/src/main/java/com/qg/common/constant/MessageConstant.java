package com.qg.common.constant;
/**
 * 消息常量
 */
public interface MessageConstant {
    String ALREADY_EXISTS = "已存在";
    String UNKNOWN_ERROR = "未知错误";
    String PASSWORD_ENCRYPT_ERROR = "密码加密异常";
    String IMAGE_UPLOAD_FAILED = "图片上传失败";
    String EMAIL_NOT_FOUND = "邮箱不存在";
    String ACCOUNT_NOT_FOUND = "账号不存在";
    String PASSWORD_ERROR = "密码错误";
    String USERNAME_OR_PHONE_OR_EMAIL_EXISTS = "用户名或手机号或邮箱已存在";
    String UPDATE_NOT_ALLOWED = "您没有权限修改";
    String ITEM_NOT_FOUND = "不存在";
    String VIEW_NOT_ALLOWED = "您没有权限查看";
    String DELETE_NOT_ALLOWED = "您没有权限删除";
    String ACCOUNT_DISABLED = "账号已被禁用或注销";
    String EMAIL_EXISTS = "邮箱已被其他用户占用";
    String PHONE_EXISTS = "手机号已被其他用户占用";
    String PASSWORD_SAME = "新密码不能与旧密码相同";
    String PASSWORD_CONFIRM_ERROR = "两次密码不一致";

    // Token 相关
    String REFRESH_TOKEN_NOT_EMPTY = "refreshToken不能为空";
    String REFRESH_TOKEN_INVALID = "Refresh Token 已失效";
    String REFRESH_TOKEN_EXPIRED = "Refresh Token 已过期";
    String TOKEN_TYPE_ILLEGAL = "非法 Token，类型错误";
    String TOKEN_INVALID = "Token 无效，无法获取用户信息";
    String REPORTED_ITEM = "你已经举报过该物品";
    String REPORT_NOT_FOUND = "举报记录不存在";
    String REPORT_NOT_PENDING = "举报记录不是待审核状态";
    String ITEM_STATUS_INVALID = "物品状态异常";
    String PIN_REQUEST_ABSENT = "置顶申请不存在";
    String COMMENT_NOT_FOUND = "留言不存在";
    String USER_NOT_FOUND = "用户不存在";
    String PARENT_COMMENT_NOT_FOUND = "父留言不存在";
    String NOTIFICATION_NOT_FOUND = "通知不存在";

    String PRIVATE_MESSAGE_NOT_FOUND = "私聊消息不存在";
    String PRIVATE_MESSAGE_VIEW_NOT_ALLOWED = "无权查看该消息";
    String PRIVATE_MESSAGE_DELETE_NOT_ALLOWED = "无权删除该消息";
    String PRIVATE_MESSAGE_RECEIVER_NOT_FOUND = "接收用户不存在";
    String PRIVATE_MESSAGE_CONTENT_EMPTY = "消息内容不能为空";
    String PRIVATE_MESSAGE_SEND_TO_SELF_NOT_ALLOWED = "不能给自己发送私聊消息";

    String DAILY_LIMIT_EXCEEDED = "今日生成次数已用完";
    String AI_GENERATE_FAILED = "AI生成失败";
    String PIN_REQUEST_AUDIT_PASS = "置顶申请审核通过";
    String NO_PERMISSION = "无权限操作";
    String USER_NOT_AUTHORIZED = "不能封禁管理员或系统用户";
    String AI_RESULT_NOT_FOUND = "AI结果不存在";
    String CLAIM_REQUEST_AUDIT_PASS = "认领申请审核通过";
    String CLAIM_REQUEST_AUDIT_REJECT = "认领申请审核拒绝";
    String CLAIM_REQUEST_AUDIT_MORE_INFO_REQUIRED = "需要更多信息";
    String CLAIM_REQUEST_ABSENT = "认领申请不存在";
}
