package com.smartoffice.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartoffice.common.constants.MqttTopicConstants;
import com.smartoffice.common.entity.SystemConfig;
import com.smartoffice.common.vo.Result;
import com.smartoffice.device.mapper.SystemConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置Controller
 */
@Slf4j
@RestController
@RequestMapping("/config")
@CrossOrigin
public class SystemConfigController {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private MqttClient mqttClient;

    /**
     * 获取所有阈值配置
     */
    @GetMapping("/thresholds")
    public Result<Map<String, String>> getThresholds() {
        try {
            Map<String, String> thresholds = new HashMap<>();
            List<SystemConfig> configs = systemConfigMapper.selectList(
                    new LambdaQueryWrapper<SystemConfig>()
                            .eq(SystemConfig::getConfigType, "THRESHOLD")
            );

            for (SystemConfig config : configs) {
                thresholds.put(config.getConfigKey(), config.getConfigValue());
            }

            return Result.success(thresholds);
        } catch (Exception e) {
            log.error("获取阈值配置失败", e);
            return Result.fail("获取阈值配置失败");
        }
    }

    /**
     * 获取配置列表
     */
    @GetMapping("/list")
    public Result<List<SystemConfig>> getConfigList(@RequestParam(required = false) String configType) {
        try {
            LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
            if (configType != null && !configType.isEmpty()) {
                wrapper.eq(SystemConfig::getConfigType, configType);
            }
            List<SystemConfig> configs = systemConfigMapper.selectList(wrapper);
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取配置列表失败", e);
            return Result.fail("获取配置列表失败");
        }
    }

    /**
     * 更新配置
     */
    @PutMapping("/update")
    public Result<String> updateConfig(@RequestBody SystemConfig configParam) {
        try {
            SystemConfig config = systemConfigMapper.selectOne(
                    new LambdaQueryWrapper<SystemConfig>()
                            .eq(SystemConfig::getConfigKey, configParam.getConfigKey())
            );

            if (config != null) {
                // 配置项存在，更新值
                config.setConfigValue(configParam.getConfigValue());
                systemConfigMapper.updateById(config);

                // 清除Redis缓存
                if (redisTemplate != null) {
                    try {
                        redisTemplate.delete("system:thresholds");
                    } catch (Exception ex) {
                        log.warn("清除Redis缓存失败: {}", ex.getMessage());
                    }
                }

                log.info("更新配置: {}={}", config.getConfigKey(), config.getConfigValue());
                
                // 推送采集间隔配置到硬件设备（包括火焰检测间隔和数据采集间隔）
                if (config.getConfigKey().equals("flame.detect.interval") || 
                    config.getConfigKey().equals("data.collect.interval")) {
                    pushConfigToDevices(config.getConfigKey(), config.getConfigValue());
                }
                
                return Result.success("配置已更新");
            } else {
                // 配置项不存在，自动创建
                SystemConfig newConfig = new SystemConfig();
                newConfig.setConfigKey(configParam.getConfigKey());
                newConfig.setConfigValue(configParam.getConfigValue());
                newConfig.setConfigType(configParam.getConfigType() != null ? configParam.getConfigType() : "SYSTEM");
                
                // 根据配置键设置默认描述
                String description = getDefaultDescription(configParam.getConfigKey());
                newConfig.setDescription(description != null ? description : "系统配置");
                
                systemConfigMapper.insert(newConfig);
                
                log.info("创建新配置: {}={}", newConfig.getConfigKey(), newConfig.getConfigValue());
                
                // 推送采集间隔配置到硬件设备（包括火焰检测间隔和数据采集间隔）
                if (newConfig.getConfigKey().equals("flame.detect.interval") || 
                    newConfig.getConfigKey().equals("data.collect.interval")) {
                    pushConfigToDevices(newConfig.getConfigKey(), newConfig.getConfigValue());
                }
                
                return Result.success("配置已创建");
            }
        } catch (Exception e) {
            log.error("更新配置失败", e);
            return Result.fail("更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取配置项的默认描述
     */
    private String getDefaultDescription(String configKey) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("light.collect.interval", "光照采集间隔（秒）");
        descriptions.put("temp.humidity.collect.interval", "温湿度采集间隔（秒）");
        descriptions.put("flame.detect.interval", "火焰检测间隔（秒）");
        descriptions.put("data.collect.interval", "数据采集间隔（秒）");
        descriptions.put("data.retention.days", "数据保留天数");
        descriptions.put("mqtt.broker.url", "MQTT服务器地址");
        descriptions.put("mqtt.client.id", "MQTT客户端ID");
        descriptions.put("alarm.email.enable", "邮件告警开关");
        descriptions.put("alarm.sms.enable", "短信告警开关");
        descriptions.put("light.threshold.low", "光照阈值下限（低于此值开灯）");
        descriptions.put("light.threshold.high", "光照阈值上限（高于此值关灯）");
        descriptions.put("temperature.threshold.low", "温度阈值下限");
        descriptions.put("temperature.threshold.high", "温度阈值上限");
        descriptions.put("humidity.threshold.low", "湿度阈值下限");
        descriptions.put("humidity.threshold.high", "湿度阈值上限");
        return descriptions.get(configKey);
    }

    /**
     * 更新阈值配置
     */
    @PutMapping("/threshold")
    public Result<String> updateThreshold(@RequestBody Map<String, String> params) {
        try {
            String configKey = params.get("configKey");
            String configValue = params.get("configValue");

            SystemConfig config = systemConfigMapper.selectOne(
                    new LambdaQueryWrapper<SystemConfig>()
                            .eq(SystemConfig::getConfigKey, configKey)
            );

            if (config != null) {
                config.setConfigValue(configValue);
                systemConfigMapper.updateById(config);

                // 清除Redis缓存
                if (redisTemplate != null) {
                    try {
                        redisTemplate.delete("system:thresholds");
                    } catch (Exception ex) {
                        log.warn("清除Redis缓存失败: {}", ex.getMessage());
                    }
                }

                log.info("更新阈值配置: {}={}", configKey, configValue);
                return Result.success("配置已更新");
            } else {
                return Result.fail("配置项不存在");
            }
        } catch (Exception e) {
            log.error("更新阈值配置失败", e);
            return Result.fail("更新阈值配置失败");
        }
    }

    /**
     * 获取所有系统配置
     */
    @GetMapping("/all")
    public Result<List<SystemConfig>> getAllConfigs() {
        try {
            List<SystemConfig> configs = systemConfigMapper.selectList(null);
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取系统配置失败", e);
            return Result.fail("获取系统配置失败");
        }
    }

    /**
     * 推送配置到硬件设备
     */
    private void pushConfigToDevices(String configKey, String configValue) {
        if (mqttClient == null) {
            log.warn("MQTT客户端未初始化，无法推送配置");
            return;
        }

        try {
            // 构建配置更新消息 - 硬件端期望直接使用configKey作为键名
            Map<String, Object> configMessage = new HashMap<>();
            configMessage.put(configKey, configValue);
            configMessage.put("timestamp", System.currentTimeMillis());

            String jsonMessage = new ObjectMapper().writeValueAsString(configMessage);

            // 发布到配置更新主题
            mqttClient.publish(MqttTopicConstants.TOPIC_CONFIG_UPDATE, jsonMessage.getBytes(), 1, false);
            
            log.info("配置已推送到硬件设备: {}={}", configKey, configValue);
        } catch (Exception e) {
            log.error("推送配置到硬件设备失败: {}", e.getMessage(), e);
        }
    }
}
