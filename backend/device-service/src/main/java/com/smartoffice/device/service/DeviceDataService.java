package com.smartoffice.device.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartoffice.common.constants.SystemConstants;
import com.smartoffice.common.dto.AlarmMessageDTO;
import com.smartoffice.common.dto.ControlCommandDTO;
import com.smartoffice.common.dto.SensorDataDTO;
import com.smartoffice.common.entity.AlarmLog;
import com.smartoffice.common.entity.DeviceInfo;
import com.smartoffice.common.entity.SensorData;
import com.smartoffice.common.entity.SystemConfig;
import com.smartoffice.device.mapper.AlarmLogMapper;
import com.smartoffice.device.mapper.DeviceInfoMapper;
import com.smartoffice.device.mapper.SensorDataMapper;
import com.smartoffice.device.mapper.SystemConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 设备数据处理服务
 */
@Slf4j
@Service
public class DeviceDataService {

    @Autowired
    private SensorDataMapper sensorDataMapper;

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Autowired
    private AlarmLogMapper alarmLogMapper;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired(required = false)
    private MqttClient mqttClient;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 处理传感器数据
     */
    public void processSensorData(SensorDataDTO sensorDataDTO) {
        try {
            // 1. 保存传感器数据到数据库
            SensorData sensorData = new SensorData();
            sensorData.setDeviceId(sensorDataDTO.getDeviceId());
            sensorData.setLight(sensorDataDTO.getLight() != null ? BigDecimal.valueOf(sensorDataDTO.getLight()) : null);
            sensorData.setTemperature(sensorDataDTO.getTemperature() != null ? BigDecimal.valueOf(sensorDataDTO.getTemperature()) : null);
            sensorData.setHumidity(sensorDataDTO.getHumidity() != null ? BigDecimal.valueOf(sensorDataDTO.getHumidity()) : null);
            sensorData.setFlame(sensorDataDTO.getFlame() != null && sensorDataDTO.getFlame() ? 1 : 0);
            sensorData.setRgbStatus(sensorDataDTO.getRgbStatus() != null && sensorDataDTO.getRgbStatus() ? 1 : 0);
            sensorData.setDataTime(LocalDateTime.now());
            sensorDataMapper.insert(sensorData);

            // 2. 缓存最新数据到Redis
            if (redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set(
                            "sensor:latest:" + sensorDataDTO.getDeviceId(),
                            sensorDataDTO,
                            24, TimeUnit.HOURS
                    );
                } catch (Exception e) {
                    log.warn("Redis缓存失败，继续执行: {}", e.getMessage());
                }
            }

            // 3. 更新设备在线状态
            updateDeviceOnlineStatus(sensorDataDTO.getDeviceId());

            // 4. 检查并触发自动控制逻辑
            checkAndTriggerAutoControl(sensorDataDTO);

            // 5. 通过WebSocket推送到前端
            if (messagingTemplate != null) {
                messagingTemplate.convertAndSend("/topic/sensor-data", sensorDataDTO);
            }

            log.info("成功处理传感器数据: deviceId={}", sensorDataDTO.getDeviceId());
        } catch (Exception e) {
            log.error("处理传感器数据失败", e);
        }
    }

    /**
     * 处理告警消息
     */
    public void processAlarm(AlarmMessageDTO alarmMessageDTO) {
        try {
            // 保存告警记录
            AlarmLog alarmLog = new AlarmLog();
            alarmLog.setDeviceId(alarmMessageDTO.getDeviceId());
            alarmLog.setAlarmType(alarmMessageDTO.getAlarmType());
            alarmLog.setAlarmLevel(alarmMessageDTO.getLevel());
            alarmLog.setAlarmMessage(alarmMessageDTO.getMessage());
            alarmLog.setStatus(SystemConstants.ALARM_STATUS_UNHANDLED);
            alarmLog.setAlarmTime(LocalDateTime.now());
            alarmLogMapper.insert(alarmLog);

            // 通过WebSocket推送告警到前端
            if (messagingTemplate != null) {
                messagingTemplate.convertAndSend("/topic/alarm", alarmLog);
            }

            log.info("成功处理告警消息: deviceId={}, type={}",
                    alarmMessageDTO.getDeviceId(), alarmMessageDTO.getAlarmType());
        } catch (Exception e) {
            log.error("处理告警消息失败", e);
        }
    }

    /**
     * 检查并触发自动控制逻辑
     */
    private void checkAndTriggerAutoControl(SensorDataDTO sensorData) {
        try {
            // 获取阈值配置
            Map<String, String> thresholds = getThresholds();

            // 1. 光照控制逻辑
            if (sensorData.getLight() != null) {
                double lightLow = Double.parseDouble(thresholds.getOrDefault("light.threshold.low", "300"));
                double lightHigh = Double.parseDouble(thresholds.getOrDefault("light.threshold.high", "350"));

                if (sensorData.getLight() < lightLow && !Boolean.TRUE.equals(sensorData.getRgbStatus())) {
                    // 光照过低，开灯
                    sendControlCommand(sensorData.getDeviceId(), SystemConstants.ACTION_RGB_ON);
                    log.info("自动开灯: deviceId={}, light={}", sensorData.getDeviceId(), sensorData.getLight());
                } else if (sensorData.getLight() > lightHigh && Boolean.TRUE.equals(sensorData.getRgbStatus())) {
                    // 光照充足，关灯
                    sendControlCommand(sensorData.getDeviceId(), SystemConstants.ACTION_RGB_OFF);
                    log.info("自动关灯: deviceId={}, light={}", sensorData.getDeviceId(), sensorData.getLight());
                }
            }

            // 2. 温度控制逻辑
            if (sensorData.getTemperature() != null) {
                double tempLow = Double.parseDouble(thresholds.getOrDefault("temperature.threshold.low", "18"));
                double tempHigh = Double.parseDouble(thresholds.getOrDefault("temperature.threshold.high", "28"));

                if (sensorData.getTemperature() < tempLow) {
                    // 温度过低，开启空调制热
                    sendControlCommand(sensorData.getDeviceId(), SystemConstants.ACTION_AC_HEAT);
                    log.info("温度过低，已自动开启空调制热: deviceId={}, temp={}",
                            sensorData.getDeviceId(), sensorData.getTemperature());
                } else if (sensorData.getTemperature() > tempHigh) {
                    // 温度过高，开启空调制冷
                    sendControlCommand(sensorData.getDeviceId(), SystemConstants.ACTION_AC_COOL);
                    log.info("温度过高，已自动开启空调制冷: deviceId={}, temp={}",
                            sensorData.getDeviceId(), sensorData.getTemperature());
                } else {
                    // 温度正常，关闭空调
                    sendControlCommand(sensorData.getDeviceId(), SystemConstants.ACTION_AC_OFF);
                }
            }

            // 3. 湿度控制逻辑
            if (sensorData.getHumidity() != null) {
                double humidityLow = Double.parseDouble(thresholds.getOrDefault("humidity.threshold.low", "40"));
                double humidityHigh = Double.parseDouble(thresholds.getOrDefault("humidity.threshold.high", "70"));

                if (sensorData.getHumidity() < humidityLow) {
                    // 湿度过低，开启加湿器
                    sendControlCommand(sensorData.getDeviceId(), SystemConstants.ACTION_HUMIDIFIER_ON);
                    log.info("湿度过低，已自动开启加湿器: deviceId={}, humidity={}",
                            sensorData.getDeviceId(), sensorData.getHumidity());
                } else if (sensorData.getHumidity() > humidityHigh) {
                    // 湿度正常，关闭加湿器
                    sendControlCommand(sensorData.getDeviceId(), SystemConstants.ACTION_HUMIDIFIER_OFF);
                    log.info("湿度恢复正常，已关闭加湿器: deviceId={}, humidity={}",
                            sensorData.getDeviceId(), sensorData.getHumidity());
                }
            }

            // 4. 火焰检测
            if (Boolean.TRUE.equals(sensorData.getFlame())) {
                // 触发蜂鸣器
                sendControlCommand(sensorData.getDeviceId(), SystemConstants.ACTION_BUZZER_ON);
                log.warn("检测到火焰，已触发蜂鸣器！deviceId={}", sensorData.getDeviceId());

                // 发送告警
                AlarmMessageDTO alarmMessage = new AlarmMessageDTO(
                        sensorData.getDeviceId(),
                        SystemConstants.ALARM_TYPE_FIRE,
                        SystemConstants.ALARM_LEVEL_CRITICAL,
                        "检测到火焰！"
                );
                processAlarm(alarmMessage);
            }

        } catch (Exception e) {
            log.error("自动控制逻辑执行失败", e);
        }
    }

    /**
     * 发送控制命令到设备
     */
    private void sendControlCommand(String deviceId, String action) {
        try {
            if (mqttClient == null) {
                log.warn("MQTT客户端未连接，无法发送控制命令: deviceId={}, action={}", deviceId, action);
                return;
            }
            
            ControlCommandDTO command = new ControlCommandDTO(deviceId, action);
            String payload = JSON.toJSONString(command);

            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);

            mqttClient.publish("office/control/cmd", message);
            log.info("发送控制命令成功: deviceId={}, action={}", deviceId, action);
        } catch (Exception e) {
            log.error("发送控制命令失败", e);
        }
    }

    /**
     * 公开方法：供Controller调用发送设备命令
     */
    public void sendDeviceCommand(String deviceId, String action) {
        log.info("[后端] 设备控制命令执行: 设备ID={}, 操作={}", deviceId, action);
        sendControlCommand(deviceId, action);
        log.info("[后端] 设备控制命令已发送到MQTT: 设备ID={}, 操作={}", deviceId, action);
    }

    /**
     * 更新设备在线状态
     */
    private void updateDeviceOnlineStatus(String deviceId) {
        try {
            DeviceInfo deviceInfo = deviceInfoMapper.selectOne(
                    new LambdaQueryWrapper<DeviceInfo>()
                            .eq(DeviceInfo::getDeviceId, deviceId)
            );

            if (deviceInfo != null) {
                deviceInfo.setOnlineStatus(1);
                deviceInfo.setStatus(SystemConstants.DEVICE_STATUS_ONLINE);
                deviceInfo.setLastOnlineTime(LocalDateTime.now());
                deviceInfoMapper.updateById(deviceInfo);
            }
        } catch (Exception e) {
            log.error("更新设备在线状态失败", e);
        }
    }

    /**
     * 获取阈值配置
     */
    private Map<String, String> getThresholds() {
        Map<String, String> thresholds = new HashMap<>();
        try {
            // 从Redis缓存获取
            if (redisTemplate != null) {
                try {
                    Object cached = redisTemplate.opsForValue().get("system:thresholds");
                    if (cached != null) {
                        return (Map<String, String>) cached;
                    }
                } catch (Exception e) {
                    log.warn("Redis获取阈值失败，从数据库获取: {}", e.getMessage());
                }
            }

            // 从数据库获取
            LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemConfig::getConfigType, "THRESHOLD");
            systemConfigMapper.selectList(wrapper).forEach(config -> {
                thresholds.put(config.getConfigKey(), config.getConfigValue());
            });

            // 缓存到Redis
            if (redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set("system:thresholds", thresholds, 1, TimeUnit.HOURS);
                } catch (Exception e) {
                    log.warn("Redis缓存阈值失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("获取阈值配置失败", e);
        }
        return thresholds;
    }
}
