package com.qg.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务配置类
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
    /**
     * 创建异步任务线程池
     *
     * @return 线程池
     */
    @Bean("aiTaskExecutor")
    public Executor aiTaskExecutor() {
        //创建线程池
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数
        executor.setCorePoolSize(5);
        //最大线程数
        executor.setMaxPoolSize(8);
        //线程队列最大线程数
        executor.setQueueCapacity(100);
        //线程池名称前缀
        executor.setThreadNamePrefix("ai-task-");
        //线程池对拒绝任务的处理策略：拒绝策略
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //初始化线程池
        executor.initialize();

        return executor;
    }
}
