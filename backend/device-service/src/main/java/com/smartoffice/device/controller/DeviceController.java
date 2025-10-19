package com.smartoffice.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartoffice.common.dto.ControlCommandDTO;
import com.smartoffice.common.entity.DeviceInfo;
import com.smartoffice.common.entity.SensorData;
import com.smartoffice.common.vo.Result;
import com.smartoffice.device.mapper.DeviceInfoMapper;
import com.smartoffice.device.mapper.SensorDataMapper;
import com.smartoffice.device.service.DeviceDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/device")
@CrossOrigin
public class DeviceController {

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Autowired
    private SensorDataMapper sensorDataMapper;

    @Autowired
    private DeviceDataService deviceDataService;

    @Autowired(required = false)  // Redis是可选的
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取所有设备列表
     */
    @GetMapping("/list")
    public Result<List<DeviceInfo>> getDeviceList() {
        try {
            List<DeviceInfo> devices = deviceInfoMapper.selectList(null);
            return Result.success(devices);
        } catch (Exception e) {
            log.error("获取设备列表失败", e);
            return Result.fail("获取设备列表失败");
        }
    }

    /**
     * 根据设备ID获取设备信息
     */
    @GetMapping("/{deviceId}")
    public Result<DeviceInfo> getDeviceInfo(@PathVariable String deviceId) {
        try {
            DeviceInfo device = deviceInfoMapper.selectOne(
                    new LambdaQueryWrapper<DeviceInfo>()
                            .eq(DeviceInfo::getDeviceId, deviceId)
            );
            return Result.success(device);
        } catch (Exception e) {
            log.error("获取设备信息失败", e);
            return Result.fail("获取设备信息失败");
        }
    }

    /**
     * 获取设备最新传感器数据
     */
    @GetMapping("/{deviceId}/latest")
    public Result<Object> getLatestSensorData(@PathVariable String deviceId) {
        try {
            // 优先从Redis获取
            if (redisTemplate != null) {
                try {
                    Object cachedData = redisTemplate.opsForValue().get("sensor:latest:" + deviceId);
                    if (cachedData != null) {
                        return Result.success(cachedData);
                    }
                } catch (Exception e) {
                    log.warn("Redis获取数据失败，从数据库获取: {}", e.getMessage());
                }
            }

            // Redis没有则从数据库获取
            SensorData sensorData = sensorDataMapper.selectOne(
                    new LambdaQueryWrapper<SensorData>()
                            .eq(SensorData::getDeviceId, deviceId)
                            .orderByDesc(SensorData::getDataTime)
                            .last("LIMIT 1")
            );
            return Result.success(sensorData);
        } catch (Exception e) {
            log.error("获取最新传感器数据失败", e);
            return Result.fail("获取最新传感器数据失败");
        }
    }

    /**
     * 分页查询传感器历史数据
     */
    @GetMapping("/{deviceId}/history")
    public Result<Page<SensorData>> getSensorDataHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Page<SensorData> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<SensorData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SensorData::getDeviceId, deviceId)
                    .orderByDesc(SensorData::getDataTime);
            sensorDataMapper.selectPage(page, wrapper);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询传感器历史数据失败", e);
            return Result.fail("查询传感器历史数据失败");
        }
    }

    /**
     * 手动控制设备
     */
    @PostMapping("/control")
    public Result<String> controlDevice(@RequestBody ControlCommandDTO command) {
        try {
            log.info("收到设备控制请求: deviceId={}, action={}",
                    command.getDeviceId(), command.getAction());

            // 发送MQTT控制命令
            deviceDataService.sendDeviceCommand(command.getDeviceId(), command.getAction());

            return Result.success("控制命令已发送");
        } catch (Exception e) {
            log.error("设备控制失败", e);
            return Result.fail("设备控制失败");
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Device Service is running");
    }
}
