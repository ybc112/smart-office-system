package com.smartoffice.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartoffice.common.entity.SystemConfig;
import com.smartoffice.common.vo.Result;
import com.smartoffice.device.mapper.SystemConfigMapper;
import lombok.extern.slf4j.Slf4j;
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
}
