package com.smartoffice.device.mqtt;

import com.alibaba.fastjson2.JSON;
import com.smartoffice.common.dto.AlarmMessageDTO;
import com.smartoffice.common.dto.SensorDataDTO;
import com.smartoffice.device.service.DeviceDataService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MQTT消息回调处理器
 */
@Slf4j
@Component
public class MqttMessageCallback implements MqttCallback {

    @Autowired
    private DeviceDataService deviceDataService;

    /**
     * 连接丢失回调
     */
    @Override
    public void connectionLost(Throwable cause) {
        log.error("❌❌❌ MQTT连接丢失!!!", cause);
        cause.printStackTrace();
    }

    /**
     * 消息到达回调
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            String payload = new String(message.getPayload(), "UTF-8");
            log.error("🔔🔔🔔 收到MQTT消息 - Topic: {}, Payload: {}", topic, payload);

            // 根据主题分发处理
            if (topic.equals("office/sensor/data")) {
                handleSensorData(payload);
            } else if (topic.equals("office/alarm")) {
                handleAlarm(payload);
            } else if (topic.equals("office/device/status")) {
                handleDeviceStatus(payload);
            }
        } catch (Exception e) {
            log.error("处理MQTT消息失败", e);
        }
    }

    /**
     * 消息发送完成回调
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("MQTT消息发送完成");
    }

    /**
     * 处理传感器数据
     */
    private void handleSensorData(String payload) {
        try {
            SensorDataDTO sensorData = JSON.parseObject(payload, SensorDataDTO.class);
            deviceDataService.processSensorData(sensorData);
            log.info("成功处理传感器数据: {}", sensorData.getDeviceId());
        } catch (Exception e) {
            log.error("处理传感器数据失败", e);
        }
    }

    /**
     * 处理告警消息
     */
    private void handleAlarm(String payload) {
        try {
            AlarmMessageDTO alarmMessage = JSON.parseObject(payload, AlarmMessageDTO.class);
            deviceDataService.processAlarm(alarmMessage);
            log.info("成功处理告警消息: {}", alarmMessage.getDeviceId());
        } catch (Exception e) {
            log.error("处理告警消息失败", e);
        }
    }

    /**
     * 处理设备状态
     */
    private void handleDeviceStatus(String payload) {
        try {
            // 这里可以处理设备上下线状态
            log.info("收到设备状态消息: {}", payload);
        } catch (Exception e) {
            log.error("处理设备状态失败", e);
        }
    }
}
