package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.constant.CodeTimeConstant;
import com.qg.common.constant.EmailTypeConstant;
import com.qg.common.exception.BaseException;
import com.qg.common.properties.MailProperties;
import com.qg.pojo.entity.EmailVerificationCode;
import com.qg.pojo.entity.SysUser;
import com.qg.server.mapper.EmailVerificationCodeDao;
import com.qg.server.mapper.UserDao;
import com.qg.server.service.EmailVerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 邮箱验证码服务实现类
 * 发送邮箱验证码
 * 校验邮箱验证码
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationCodeServiceImpl implements EmailVerificationCodeService {

    private final JavaMailSender mailSender;
    private final EmailVerificationCodeDao dao;
    private final UserDao userDao;
    private final MailProperties mailProperties;

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱
     * @param type  验证码类型
     *              1. 登录或改密码判断账号存在
     *              2. 注册判断账号不存在
     *              3. 查询最近一次发送时间（邮箱 + 类型）
     *              4. 判断是否过于频繁发送验证码
     *              5. 生成安全的6位验证码
     *              6. 保存验证码到数据库
     *              7. 发送邮件
     */
    @Override
    public void sendCode(String email, String type) {
        log.info("发送邮箱验证码，邮箱：{}，类型：{}", email, type);
        //1.登录或改密码判断账号存在
        SysUser user = userDao.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email));
        if (user == null && (type.equals(EmailTypeConstant.LOGIN) || type.equals(EmailTypeConstant.CHANGE_PASSWORD))) {
            log.warn("账号不存在，邮箱：{}", email);
            throw new BaseException(400, "账号不存在");
        }
        //2.注册判断账号不存在
        if (type.equals(EmailTypeConstant.REGISTER)) {
            if (user != null) {
                log.warn("账号已存在，邮箱：{}", email);
                throw new BaseException(400, "账号已存在");
            }
        }
        //3.查询最近一次发送时间（邮箱 + 类型）
        LambdaQueryWrapper<EmailVerificationCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmailVerificationCode::getEmail, email)
                .eq(EmailVerificationCode::getType, type)
                .orderByDesc(EmailVerificationCode::getExpireTime)
                .last("LIMIT 1");

        EmailVerificationCode last = dao.selectOne(wrapper);
        log.info("最近一次发送：{}", last);
        //4.判断是否过于频繁发送验证码
        if (last != null) {
            long secondsSinceLast = Duration.between(last.getExpireTime(), LocalDateTime.now()).getSeconds();
            if (secondsSinceLast < CodeTimeConstant.CODE_INTERVAL) {
                throw new BaseException(400, "验证码发送过于频繁，请稍后再试");
            }
        }
        //5. 生成安全的6位验证码
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        //6. 保存验证码到数据库
        EmailVerificationCode codeEntity = new EmailVerificationCode();
        codeEntity.setEmail(email);
        codeEntity.setCode(code);
        codeEntity.setType(type);
        codeEntity.setExpireTime(LocalDateTime.now().plusSeconds(CodeTimeConstant.CODE_EXPIRE_TIME));
        dao.insert(codeEntity);

        //7. 发送邮件（异常捕获 + 必须 setFrom）
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername()); // 必须写
            message.setTo(email);
            message.setSubject("验证码");
            message.setText("您的验证码是：" + code + "，" + CodeTimeConstant.CODE_EXPIRE_TIME + "秒内有效，请不要泄露给他人。");
            mailSender.send(message);
        } catch (MailException e) {
            throw new BaseException(500, "邮件发送失败，请稍后重试", e);
        }
    }

    /**
     * 校验验证码
     *
     * @param email 邮箱
     * @param type  验证码类型
     * @param code  验证码
     * @return 校验结果
     * 查询数据库中是否存在该验证码
     */
    @Override
    public boolean verifyCode(String email, String type, String code) {
        log.info("校验邮箱验证码，邮箱：{}，类型：{}，验证码：{}", email, type, code);
        LambdaQueryWrapper<EmailVerificationCode> wrapper = new LambdaQueryWrapper<>();
        if (email != null && type != null && code != null) {
            wrapper.eq(EmailVerificationCode::getEmail, email)
                    .eq(EmailVerificationCode::getType, type)
                    .eq(EmailVerificationCode::getCode, code)
                    .gt(EmailVerificationCode::getExpireTime, LocalDateTime.now());
        }
        return dao.selectOne(wrapper) != null;
    }
}