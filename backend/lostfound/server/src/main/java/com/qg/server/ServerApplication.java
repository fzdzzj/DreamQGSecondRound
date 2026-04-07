package com.qg.server;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@ComponentScan("com.qg")
@ServletComponentScan       // 扫描Servlet、Filter、Listener
@MapperScan("com.qg.server.mapper")  // MyBatis Mapper接口扫描
@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})    // SpringBoot 自动配置
@EnableAsync               // 开启异步任务支持
@EnableScheduling
public class ServerApplication {

    public static void main(String[] args) {
        log.info("校园失物招领系统启动中");
        SpringApplication.run(ServerApplication.class, args);
        log.info("校园失物招领系统启动成功，服务运行在 http://localhost:8080");
    }

}
