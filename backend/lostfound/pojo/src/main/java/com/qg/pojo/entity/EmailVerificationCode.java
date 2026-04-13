package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("email_verification_code")
public class EmailVerificationCode {
    @TableId
    private Long id;
    private String email;
    private String code;
    private String type; // 1.REGISTER / 2.LOGIN / 3.RESET_PASSWORD
    private LocalDateTime expireTime;
}
