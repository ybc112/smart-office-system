package com.smartoffice.device.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MQTT配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    /**
     * MQTT服务器地址
     */
    private String brokerUrl;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 主题配置
     */
    private Topics topics;

    /**
     * QoS
     */
    private Integer qos = 1;

    /**
     * 超时时间
     */
    private Integer timeout = 30;

    /**
     * 心跳间隔
     */
    private Integer keepalive = 60;

    @Data
    public static class Topics {
        private String sensorData;
        private String alarm;
        private String deviceStatus;
    }
}
