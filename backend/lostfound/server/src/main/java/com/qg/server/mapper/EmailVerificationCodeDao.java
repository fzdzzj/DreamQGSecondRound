package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.EmailVerificationCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮箱验证码数据访问层
 */
@Mapper
public interface EmailVerificationCodeDao extends BaseMapper<EmailVerificationCode> {
}
