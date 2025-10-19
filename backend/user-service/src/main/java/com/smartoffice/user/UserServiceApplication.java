package com.smartoffice.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.smartoffice.user", "com.smartoffice.common"})
@MapperScan("com.smartoffice.user.mapper")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("用户服务启动成功！");
        System.out.println("访问地址: http://localhost:8082");
        System.out.println("========================================");
    }
}
