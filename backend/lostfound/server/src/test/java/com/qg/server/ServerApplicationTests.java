package com.qg.server;

import com.qg.common.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ServerApplicationTests {

    @Test
    void contextLoads() {
        // 用你自己的工具加密 123456
        String pwd = PasswordUtil.encrypt("123456");
        System.out.println("正确密文：" + pwd);
    }

}
