package com.smartoffice.device;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 设备管理服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.smartoffice.device", "com.smartoffice.common"})
@MapperScan("com.smartoffice.device.mapper")
public class DeviceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("设备管理服务启动成功！");
        System.out.println("访问地址: http://localhost:8081");
        System.out.println("========================================");
    }
}
