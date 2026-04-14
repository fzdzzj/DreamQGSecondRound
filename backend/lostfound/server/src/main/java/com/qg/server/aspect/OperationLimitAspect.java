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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLimitAspect {
    private final UserService userService;
    private final OperationLogService operationLogService;
    private final RedisTemplate<String, Object> redisTemplate;
    @Around("@annotation(com.qg.server.anno.AntiBot)")
    public Object checkAntiBot(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = BaseContext.getCurrentId();
        checkUserStatus(userId);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AntiBot antiBot = method.getAnnotation(AntiBot.class);
        String actionType = antiBot.value();
        incrementActionCounter(userId, actionType);
        return joinPoint.proceed();
    }

    private void incrementActionCounter(Long userId, String actionType) {
        switch (actionType) {
            case LimitTypeConstant.USER_CLAIM_LIMIT:
                Long claimCount = redisTemplate.opsForValue().increment(RedisConstant.USER_CLAIM_LIMIT_KEY+userId);
                if(claimCount==1){
                    redisTemplate.expire(RedisConstant.USER_CLAIM_LIMIT_KEY+userId, Duration.ofMinutes(1));
                }
                if (claimCount > RedisConstant.USER_CLAIM_POST_LIMIT) { // 1 分钟超过 5 次封禁
                    log.warn("用户 {} 在 1 分钟内进行多次认领操作，封禁该用户", userId);
                    banUser(userId,actionType);
                    throw new BaseException(403,"您已进行多次认领行为，对你进行封禁，请等待7天后重试或者联系管理员");
                }
                break;
            case LimitTypeConstant.USER_EDIT_POST_LIMIT:
                Long editPostCount = redisTemplate.opsForValue().increment(RedisConstant.USER_EDIT_POST_LIMIT_KEY+userId);
                if (editPostCount==1) { // 1 分钟超过 5 次封禁
                    redisTemplate.expire(RedisConstant.USER_EDIT_POST_LIMIT_KEY + userId, Duration.ofMinutes(1));
                }
                if (editPostCount > RedisConstant.USER_EDIT_POST_LIMIT) { // 1 分钟超过 5 次封禁
                    log.warn("用户 {} 在 1 分钟内进行多次编辑帖子操作，封禁该用户", userId);
                    banUser(userId,actionType);
                    throw new BaseException(403,"您已进行多次编辑行为，对你进行封禁，请等待7天后重试或者联系管理员");
                }
                break;
            case LimitTypeConstant.USER_POST_LIMIT:
                Long postCount =  redisTemplate.opsForValue().increment(RedisConstant.USER_POST_LIMIT_KEY+userId);
                if (postCount==1) { // 1 分钟超过 5 次封禁
                    redisTemplate.expire(RedisConstant.USER_POST_LIMIT_KEY + userId, Duration.ofMinutes(1));
                }
                if (postCount > RedisConstant.USER_POST_LIMIT) { // 1 分钟超过 5 次封禁
                    log.warn("用户 {} 在 1 分钟内进行多次发帖子操作，封禁该用户", userId);
                    banUser(userId,actionType);
                    throw new BaseException(403,"您已进行多次发帖子行为，对你进行封禁，请等待7天后重试或者联系管理员");
                }
                break;
        }
    }
    public void checkUserStatus(Long userId) {
        if (redisTemplate.opsForValue().get(RedisConstant.USER_BANNED_KEY + userId) != null) {
            log.warn("用户 {} 已被封禁，无法进行操作", userId);
            throw new BaseException(403,"您已被封禁，请等待7天后重试或者联系管理员");
        }
    }
    public void banUser(Long userId,String actionType) {
        redisTemplate.opsForValue().set(RedisConstant.USER_BANNED_KEY + userId, true);
        redisTemplate.expire(RedisConstant.USER_BANNED_KEY + userId, Duration.ofDays(8));
        log.info("用户 {} 被封禁，封禁原因：{}", userId,actionType);
        SysUser sysUser = userService.getById(userId);
        sysUser.setStatus(0);
        userService.updateById(sysUser);
        log.info("用户 {} 封禁成功", userId);
        UserActionLog userActionLog = new UserActionLog();
        userActionLog.setUserId(userId);
        userActionLog.setActionType(actionType);
        operationLogService.save(userActionLog);
    }
}
