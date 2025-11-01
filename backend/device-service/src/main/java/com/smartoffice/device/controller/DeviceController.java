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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 根据办公室ID获取设备列表
     */
    @GetMapping("/office/{officeId}")
    public Result<List<DeviceInfo>> getDevicesByOfficeId(@PathVariable Long officeId) {
        try {
            List<DeviceInfo> devices = deviceInfoMapper.selectList(
                    new LambdaQueryWrapper<DeviceInfo>()
                            .eq(DeviceInfo::getOfficeId, officeId)
            );
            return Result.success(devices);
        } catch (Exception e) {
            log.error("获取办公室设备列表失败", e);
            return Result.fail("获取办公室设备列表失败");
        }
    }

    /**
     * 根据办公区ID获取设备列表
     */
    @GetMapping("/work-area/{workAreaId}")
    public Result<List<DeviceInfo>> getDevicesByWorkAreaId(@PathVariable Long workAreaId) {
        try {
            List<DeviceInfo> devices = deviceInfoMapper.selectList(
                    new LambdaQueryWrapper<DeviceInfo>()
                            .eq(DeviceInfo::getWorkAreaId, workAreaId)
            );
            return Result.success(devices);
        } catch (Exception e) {
            log.error("获取办公区设备列表失败", e);
            return Result.fail("获取办公区设备列表失败");
        }
    }

    /**
     * 根据设备类型获取设备列表
     */
    @GetMapping("/type/{deviceType}")
    public Result<List<DeviceInfo>> getDevicesByType(@PathVariable String deviceType) {
        try {
            List<DeviceInfo> devices = deviceInfoMapper.selectList(
                    new LambdaQueryWrapper<DeviceInfo>()
                            .eq(DeviceInfo::getDeviceType, deviceType)
            );
            return Result.success(devices);
        } catch (Exception e) {
            log.error("获取设备类型列表失败", e);
            return Result.fail("获取设备类型列表失败");
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
            
            if (sensorData != null) {
                // 转换为前端期望的格式，添加timestamp字段
                Map<String, Object> result = new HashMap<>();
                result.put("deviceId", sensorData.getDeviceId());
                result.put("light", sensorData.getLight());
                result.put("temperature", sensorData.getTemperature());
                result.put("humidity", sensorData.getHumidity());
                result.put("flame", sensorData.getFlame() != null && sensorData.getFlame() == 1);
                result.put("rgbStatus", sensorData.getRgbStatus() != null && sensorData.getRgbStatus() == 1);
                // 将dataTime转换为timestamp（毫秒）
                if (sensorData.getDataTime() != null) {
                    result.put("timestamp", sensorData.getDataTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
                } else {
                    // 如果dataTime为空，使用当前时间
                    result.put("timestamp", System.currentTimeMillis());
                }
                return Result.success(result);
            }
            
            return Result.success(null);
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
     * 添加设备
     */
    @PostMapping("/add")
    public Result<String> addDevice(@RequestBody DeviceInfo deviceInfo) {
        try {
            log.info("[后端] 添加设备请求: {}", deviceInfo.getDeviceName());
            
            // 自动生成设备编号
            String deviceId = generateDeviceId(deviceInfo.getDeviceType());
            deviceInfo.setDeviceId(deviceId);
            
            // 设置默认值
            if (deviceInfo.getOnlineStatus() == null) {
                deviceInfo.setOnlineStatus(0);
            }
            if (deviceInfo.getStatus() == null) {
                deviceInfo.setStatus("OFFLINE");
            }
            
            deviceInfoMapper.insert(deviceInfo);
            log.info("[后端] 设备添加成功: {} (设备编号: {})", deviceInfo.getDeviceName(), deviceId);
            
            return Result.success("设备添加成功，设备编号：" + deviceId);
        } catch (Exception e) {
            log.error("[后端] 设备添加失败", e);
            return Result.fail("设备添加失败");
        }
    }
    
    /**
     * 生成设备编号
     */
    private String generateDeviceId(String deviceType) {
        String prefix;
        switch (deviceType) {
            case "AIR_CONDITIONER":
                prefix = "AC";
                break;
            case "HUMIDIFIER":
                prefix = "HM";
                break;
            case "LIGHT":
                prefix = "LT";
                break;
            case "SENSOR":
                prefix = "SN";
                break;
            default:
                prefix = "DV";
                break;
        }
        
        // 查询该类型设备的最大编号
        List<DeviceInfo> devices = deviceInfoMapper.selectList(
                new LambdaQueryWrapper<DeviceInfo>()
                        .like(DeviceInfo::getDeviceId, prefix)
                        .orderByDesc(DeviceInfo::getDeviceId)
        );
        
        int maxNumber = 0;
        for (DeviceInfo device : devices) {
            String deviceId = device.getDeviceId();
            if (deviceId.startsWith(prefix)) {
                try {
                    String numberPart = deviceId.substring(prefix.length());
                    int number = Integer.parseInt(numberPart);
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    // 忽略非数字后缀的设备编号
                }
            }
        }
        
        return prefix + String.format("%03d", maxNumber + 1);
    }

    /**
     * 更新设备
     */
    @PutMapping("/update")
    public Result<String> updateDevice(@RequestBody DeviceInfo deviceInfo) {
        try {
            log.info("[后端] 更新设备请求: {}", deviceInfo.getDeviceName());
            
            deviceInfoMapper.updateById(deviceInfo);
            log.info("[后端] 设备更新成功: {}", deviceInfo.getDeviceName());
            
            return Result.success("设备更新成功");
        } catch (Exception e) {
            log.error("[后端] 设备更新失败", e);
            return Result.fail("设备更新失败");
        }
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/{deviceId}")
    public Result<String> deleteDevice(@PathVariable String deviceId) {
        try {
            log.info("[后端] 删除设备请求: deviceId={}", deviceId);
            
            DeviceInfo device = deviceInfoMapper.selectOne(
                    new LambdaQueryWrapper<DeviceInfo>()
                            .eq(DeviceInfo::getDeviceId, deviceId)
            );
            
            if (device == null) {
                return Result.fail("设备不存在");
            }
            
            deviceInfoMapper.deleteById(device.getId());
            log.info("[后端] 设备删除成功: {}", device.getDeviceName());
            
            return Result.success("设备删除成功");
        } catch (Exception e) {
            log.error("[后端] 设备删除失败", e);
            return Result.fail("设备删除失败");
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
