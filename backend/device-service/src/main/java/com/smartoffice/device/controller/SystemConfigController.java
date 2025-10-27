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
                
                // 如果是数据采集间隔配置，推送到硬件设备
                if ("data.collect.interval".equals(config.getConfigKey())) {
                    pushConfigToDevices(config.getConfigKey(), config.getConfigValue());
                }
                
                return Result.success("配置已更新");
            } else {
                return Result.fail("配置项不存在");
            }
        } catch (Exception e) {
            log.error("更新配置失败", e);
            return Result.fail("更新配置失败");
        }
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
            // 构建配置更新消息
            Map<String, Object> configMessage = new HashMap<>();
            configMessage.put("configKey", configKey);
            configMessage.put("configValue", configValue);
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
