package com.qg.server.task;

import com.qg.common.constant.RedisConstant;
import com.qg.pojo.entity.SysUser;
import com.qg.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 自动解封用户任务
 * 每5分钟执行一次
 * 找出【剩余过期时间 = 1天】的封禁用户，自动解封
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EnableTask {
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;

    /**
     * 定时任务：自动解封过期封禁用户
     * 找出【剩余过期时间 = 1天】的封禁用户，自动解封
     * <p>
     * 1. 匹配所有封禁 key，格式为：user_banned:userId
     * 2. 获取剩余过期时间（秒）
     * 3. 过滤：剩余时间 = 1天（86400秒）才处理
     * 4. 截取用户ID
     * 5. 执行解封：删除封禁key
     * 6. 解封用户
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void enableUser() {
        log.info("===== 定时任务：开始自动检查并解封过期封禁用户 =====");

        // 1. 匹配所有封禁 key
        Set<String> bannedKeys = redisTemplate.keys(RedisConstant.USER_BANNED_KEY + "*");

        for (String bannedKey : bannedKeys) {
            // 2. 获取剩余过期时间（秒）
            Long expireSeconds = redisTemplate.getExpire(bannedKey, TimeUnit.SECONDS);

            // 3. 过滤：剩余时间 = 1天（86400秒）才处理
            if (expireSeconds == null || expireSeconds <= 0) {
                continue;
            }

            // 剩余1天：86400秒
            if (expireSeconds == 86400) {
                // 4. 截取用户ID
                String userId = bannedKey.replace(RedisConstant.USER_BANNED_KEY, "");
                log.info("检测到即将自动解封的用户，userId: {}", userId);

                // 5. 执行解封：删除封禁key
                redisTemplate.delete(bannedKey);
                // 6. 解封用户
                SysUser user = new SysUser();
                user.setId(Long.parseLong(userId));
                user.setStatus(1);
                userService.updateById(user);
                log.info("用户：{} 已自动解封", userId);
            }
        }
        log.info("===== 定时任务：自动解封执行完成 =====");
    }
}
