package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qg.common.constant.CodeTimeConstant;
import com.qg.common.constant.EmailTypeConstant;
import com.qg.common.exception.BaseException;
import com.qg.common.properties.MailProperties;
import com.qg.pojo.entity.EmailVerificationCode;
import com.qg.pojo.entity.SysUser;
import com.qg.server.mapper.EmailVerificationCodeDao;
import com.qg.server.service.EmailVerificationCodeService;
import com.qg.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationCodeServiceImpl implements EmailVerificationCodeService {

    private final JavaMailSender mailSender;
    private final EmailVerificationCodeDao dao;
    private final UserService userService;
    private final MailProperties mailProperties;

    /**
     * 发送邮箱验证码
     */
    @Override
    public void sendCode(String email, String type) {
        log.info("发送邮箱验证码，邮箱：{}，类型：{}", email, type);
        //登录或改密码判断账号存在
        SysUser user = userService.getByEmail(email);
        if (user == null&&(type.equals(EmailTypeConstant.LOGIN)||type.equals(EmailTypeConstant.CHANGE_PASSWORD))) {
            throw new BaseException("账号不存在");
        }
        // 查询最近一次发送时间（邮箱 + 类型）
        QueryWrapper<EmailVerificationCode> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email)
                .eq("type", type)
                .orderByDesc("create_time")
                .last("LIMIT 1");

        EmailVerificationCode last = dao.selectOne(wrapper);

        if (last != null) {
            long secondsSinceLast = Duration.between(last.getExpireTime(), LocalDateTime.now()).getSeconds();
            if (secondsSinceLast < CodeTimeConstant.CODE_INTERVAL) {
                throw new RuntimeException("验证码发送过于频繁，请稍后再试");
            }
        }
        // 生成安全的6位验证码
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        // 保存验证码
        EmailVerificationCode codeEntity = new EmailVerificationCode();
        codeEntity.setEmail(email);
        codeEntity.setCode(code);
        codeEntity.setType(type);
        codeEntity.setExpireTime(LocalDateTime.now().plusSeconds(CodeTimeConstant.CODE_EXPIRE_TIME));
        dao.insert(codeEntity);

        // 发送邮件（异常捕获 + 必须 setFrom）
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername()); // 必须写
            message.setTo(email);
            message.setSubject("验证码");
            message.setText("您的验证码是：" + code + "，" + CodeTimeConstant.CODE_EXPIRE_TIME + "秒内有效，请不要泄露给他人。");
            mailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    /**
     * 校验验证码
     */
    @Override
    public boolean verifyCode(String email, String type, String code) {
        QueryWrapper<EmailVerificationCode> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(EmailVerificationCode::getEmail, email)
                .eq(EmailVerificationCode::getType, type)
                .eq(EmailVerificationCode::getCode, code)
                .gt(EmailVerificationCode::getExpireTime, LocalDateTime.now());

        return dao.selectOne(wrapper) != null;
    }
}