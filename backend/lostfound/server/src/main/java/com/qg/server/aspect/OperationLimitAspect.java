package com.qg.server.aspect;

import com.qg.common.constant.LimitTypeConstant;
import com.qg.common.constant.RedisConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.exception.BaseException;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.entity.UserActionLog;
import com.qg.server.anno.AntiBot;
import com.qg.server.service.OperationLogService;
import com.qg.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 机器人限制切面
 * 1. 检查用户状态
 * 2. 检查用户操作类型
 * 3. 检查用户操作频率
 * 4. 检查用户操作时间
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLimitAspect {

    private final UserService userService;
    private final OperationLogService operationLogService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationContext applicationContext;

    private OperationLimitAspect getSelf() {
        return applicationContext.getBean(OperationLimitAspect.class);
    }

    /**
     * 拦截所有带有 @AntiBot 注解的方法
     */
    @Around("@annotation(com.qg.server.anno.AntiBot)")
    public Object checkAntiBot(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = BaseContext.getCurrentId();
        // 1.检查用户状态
        checkUserStatus(userId);
        // 2.获取操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AntiBot antiBot = method.getAnnotation(AntiBot.class);
        String actionType = antiBot.value();
        // 3.检查用户操作频率
        checkActionFrequency(userId, actionType);
        return joinPoint.proceed();
    }

    /**
     * 统一检查操作频率
     */
    private void checkActionFrequency(Long userId, String actionType) {
        String redisKey;
        int limitCount;
        String errorMsg;

        // 配置不同操作的规则
        switch (actionType) {
            case LimitTypeConstant.USER_CLAIM_LIMIT:
                redisKey = RedisConstant.USER_CLAIM_LIMIT_KEY + userId;
                limitCount = RedisConstant.USER_CLAIM_POST_LIMIT;
                errorMsg = "您已进行多次认领行为，对你进行封禁，请等待7天后重试或者联系管理员";
                break;
            case LimitTypeConstant.USER_EDIT_POST_LIMIT:
                redisKey = RedisConstant.USER_EDIT_POST_LIMIT_KEY + userId;
                limitCount = RedisConstant.USER_EDIT_POST_LIMIT;
                errorMsg = "您已进行多次编辑行为，对你进行封禁，请等待7天后重试或者联系管理员";
                break;
            case LimitTypeConstant.USER_POST_LIMIT:
                redisKey = RedisConstant.USER_POST_LIMIT_KEY + userId;
                limitCount = RedisConstant.USER_POST_LIMIT;
                errorMsg = "您已进行多次发帖子行为，对你进行封禁，请等待7天后重试或者联系管理员";
                break;
            default:
                log.warn("未知的操作类型：{}", actionType);
                return;
        }

        // 统一执行计数 + 限流逻辑
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);
        if (currentCount == 1) {
            redisTemplate.expire(redisKey, Duration.ofMinutes(1));
            log.info("用户 {} 在1分钟内第一次执行操作：{}", userId, actionType);
        }

        // 超过限流则封禁
        if (currentCount > limitCount) {
            log.warn("用户 {} 操作{}频率超限，次数：{}", userId, actionType, currentCount);
            getSelf().banUser(userId, actionType);
            throw new BaseException(403, errorMsg);
        }
    }

    /**
     * 检查用户是否被封禁
     */
    public void checkUserStatus(Long userId) {
        if (redisTemplate.hasKey(RedisConstant.USER_BANNED_KEY + userId)) {
            log.warn("用户 {} 已被封禁，禁止操作", userId);
            throw new BaseException(403, "您已被封禁，请等待7天后重试或者联系管理员");
        }
    }

    /**
     * 封禁用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long userId, String actionType) {
        // Redis 封禁 7天
        redisTemplate.opsForValue().set(RedisConstant.USER_BANNED_KEY + userId, true, Duration.ofDays(7));

        // 更新数据库状态
        SysUser sysUser = userService.getById(userId);
        sysUser.setStatus(0);
        userService.updateById(sysUser);

        // 记录日志
        UserActionLog logEntity = new UserActionLog();
        logEntity.setUserId(userId);
        logEntity.setActionType(actionType);
        operationLogService.save(logEntity);

        log.info("用户 {} 已因【{}】被封禁7天", userId, actionType);
    }
}