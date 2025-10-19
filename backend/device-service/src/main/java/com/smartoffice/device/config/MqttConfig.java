package com.smartoffice.device.config;

import com.smartoffice.device.mqtt.MqttMessageCallback;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT配置类
 */
@Slf4j
@Configuration
public class MqttConfig {

    @Autowired
    private MqttProperties mqttProperties;

    @Autowired
    private ApplicationContext applicationContext;

    public MqttConfig() {
        log.error("🔧🔧🔧 MqttConfig 构造函数被调用");
    }

    @Bean
    public MqttClient mqttClient() {
        log.error("========== MQTT配置初始化开始 ==========");
        log.error("Broker URL: {}", mqttProperties.getBrokerUrl());
        log.error("Client ID: {}", mqttProperties.getClientId());
        log.error("Topics: sensor-data={}, alarm={}, device-status={}",
                mqttProperties.getTopics() != null ? mqttProperties.getTopics().getSensorData() : "null",
                mqttProperties.getTopics() != null ? mqttProperties.getTopics().getAlarm() : "null",
                mqttProperties.getTopics() != null ? mqttProperties.getTopics().getDeviceStatus() : "null");

        MqttClient mqttClient = null;
        try {
            // 创建MQTT客户端
            mqttClient = new MqttClient(
                    mqttProperties.getBrokerUrl(),
                    mqttProperties.getClientId(),
                    new MemoryPersistence()
            );

            // 设置连接选项
            MqttConnectOptions options = new MqttConnectOptions();
            // 为了保持订阅与会话，避免重连后丢失订阅，设置为 false
            options.setCleanSession(false);
            options.setConnectionTimeout(mqttProperties.getTimeout());
            options.setKeepAliveInterval(mqttProperties.getKeepalive());
            options.setAutomaticReconnect(true);

            // 设置用户名和密码（如果有）
            if (mqttProperties.getUsername() != null && !mqttProperties.getUsername().isEmpty()) {
                options.setUserName(mqttProperties.getUsername());
            }
            if (mqttProperties.getPassword() != null && !mqttProperties.getPassword().isEmpty()) {
                options.setPassword(mqttProperties.getPassword().toCharArray());
            }

            // 连接到MQTT服务器
            log.error("正在连接到MQTT服务器: {}", mqttProperties.getBrokerUrl());
            mqttClient.connect(options);
            log.error("✅ 成功连接到MQTT服务器");

            // 连接成功后再设置回调，避免循环依赖
            try {
                MqttMessageCallback callback = applicationContext.getBean(MqttMessageCallback.class);
                mqttClient.setCallback(callback);
                log.error("✅ 成功设置MQTT回调处理器");
            } catch (Exception e) {
                log.error("❌ 设置MQTT回调失败", e);
            }

            // 订阅主题
            String[] topics = {
                    mqttProperties.getTopics().getSensorData(),
                    mqttProperties.getTopics().getAlarm(),
                    mqttProperties.getTopics().getDeviceStatus()
            };
            int[] qos = {mqttProperties.getQos(), mqttProperties.getQos(), mqttProperties.getQos()};
            mqttClient.subscribe(topics, qos);
            log.error("✅ 成功订阅主题: {}", String.join(", ", topics));

        } catch (MqttException e) {
            log.error("❌❌❌ MQTT服务器连接失败，将以降级模式运行（无法接收设备消息）");
            log.error("❌❌❌ 请检查 EMQ X MQTT Broker 是否在 localhost:1883 运行");
            log.error("❌❌❌ 错误详情: ", e);
            e.printStackTrace();
            // 返回 null，让应用继续启动
            return null;
        } catch (Exception e) {
            log.error("❌❌❌ MQTT配置初始化失败（未知错误）: ", e);
            e.printStackTrace();
            return null;
        }

        return mqttClient;
    }
}
