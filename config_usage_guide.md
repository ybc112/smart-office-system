# W601智慧办公楼系统配置文件使用指南

## 概述

`config.py` 是W601智慧办公楼系统的核心配置文件，包含了WiFi连接、MQTT通信、硬件引脚、系统参数等所有重要配置。

## 配置项说明

### 1. WiFi网络配置

```python
WIFI_SSID = "SmartOffice_WiFi"      # 您的WiFi网络名称
WIFI_PASSWORD = "smartoffice123"    # 您的WiFi密码
```

**修改说明：**
- 将 `SmartOffice_WiFi` 替换为您实际的WiFi网络名称
- 将 `smartoffice123` 替换为您的WiFi密码
- 确保WiFi网络支持2.4GHz频段（W601不支持5GHz）

### 2. MQTT服务器配置

```python
MQTT_SERVER = "localhost"           # MQTT服务器地址
MQTT_PORT = 1883                   # MQTT端口
MQTT_CLIENT_ID = "W601_001"        # 设备唯一标识
MQTT_USERNAME = ""                 # 用户名（可选）
MQTT_PASSWORD = ""                 # 密码（可选）
```

**修改说明：**
- **本地部署**：保持 `localhost`
- **远程部署**：修改为服务器的实际IP地址，如 `192.168.1.100`
- **多设备部署**：每个设备需要不同的 `MQTT_CLIENT_ID`，如 `W601_002`、`W601_003`

### 3. MQTT主题配置

```python
TOPIC_SENSOR_DATA = "office/sensor/data"    # 传感器数据上报
TOPIC_CONTROL_CMD = "office/control/cmd"    # 设备控制命令
TOPIC_ALARM = "office/alarm"                # 告警消息
TOPIC_STATUS = "office/device/status"       # 设备状态
```

**注意：** 这些主题必须与后端服务配置保持一致，通常不需要修改。

### 4. 硬件引脚配置

#### I2C配置（温湿度传感器）
```python
I2C_SCL_PIN = 1  # I2C时钟线 (PA1)
I2C_SDA_PIN = 0  # I2C数据线 (PA0)
```

#### 外部传感器引脚
```python
LIGHT_SENSOR_PIN = 23  # 光照传感器ADC引脚 (PB23)
FLAME_SENSOR_PIN = 4   # 火焰传感器数字引脚 (PA4)
```

#### 板载组件引脚
```python
RGB_RED_PIN = 13    # RGB红色LED (PA13)
RGB_GREEN_PIN = 14  # RGB绿色LED (PA14)
RGB_BLUE_PIN = 15   # RGB蓝色LED (PA15)
BUZZER_PIN = 16     # 蜂鸣器 (PB16)
```

**修改说明：**
- 如果您的硬件连接与默认配置不同，请根据实际连接修改引脚号
- 引脚号格式：PA0-PA31 对应 0-31，PB0-PB31 对应 32-63

### 5. 系统参数配置

#### 数据采集配置
```python
SENSOR_READ_INTERVAL = 30  # 传感器读取间隔(秒)
TIMER_PERIOD = 10000       # 定时器周期(毫秒)
```

#### 环境阈值配置
```python
LIGHT_THRESHOLD_LOW = 200    # 光照下限(lux)
LIGHT_THRESHOLD_HIGH = 800   # 光照上限(lux)
TEMP_THRESHOLD_LOW = 18.0    # 温度下限(°C)
TEMP_THRESHOLD_HIGH = 30.0   # 温度上限(°C)
HUMIDITY_THRESHOLD_LOW = 30.0   # 湿度下限(%)
HUMIDITY_THRESHOLD_HIGH = 80.0  # 湿度上限(%)
```

#### 设备控制配置
```python
AUTO_LIGHTING = True         # 自动照明控制
FLAME_ALARM_ENABLED = True   # 火焰告警功能
BUZZER_DURATION = 5          # 蜂鸣器响铃时间(秒)
```

## 配置修改步骤

### 1. 基本网络配置
1. 修改 `WIFI_SSID` 和 `WIFI_PASSWORD` 为您的WiFi信息
2. 如果MQTT服务器不在本机，修改 `MQTT_SERVER` 为服务器IP

### 2. 多设备部署
如果您有多个W601设备，需要为每个设备设置不同的配置：

```python
# 设备1
MQTT_CLIENT_ID = "W601_001"

# 设备2  
MQTT_CLIENT_ID = "W601_002"

# 设备3
MQTT_CLIENT_ID = "W601_003"
```

### 3. 环境阈值调整
根据您的办公环境特点调整阈值：

- **光照阈值**：根据办公室自然光照情况调整
- **温度阈值**：根据当地气候和空调设置调整
- **湿度阈值**：根据当地湿度环境调整

## 常见问题

### Q1: WiFi连接失败
- 检查WiFi名称和密码是否正确
- 确认WiFi网络是2.4GHz频段
- 检查WiFi信号强度

### Q2: MQTT连接失败
- 检查MQTT服务器地址是否正确
- 确认MQTT服务器正在运行（Docker: `docker-compose ps`）
- 检查防火墙设置

### Q3: 传感器读取异常
- 检查硬件连接是否正确
- 验证引脚配置是否匹配实际连接
- 检查传感器供电是否正常

### Q4: 设备控制无响应
- 确认MQTT连接正常
- 检查设备ID是否匹配
- 验证控制命令格式

## 配置验证

修改配置后，可以通过以下方式验证：

1. **语法检查**：
   ```bash
   python -m py_compile config.py
   ```

2. **连接测试**：
   - 上传到W601设备后查看串口输出
   - 检查WiFi和MQTT连接状态

3. **功能测试**：
   - 在MQTTX工具中订阅传感器数据主题
   - 通过Web界面查看设备状态

## 技术支持

如果遇到配置问题，请：
1. 检查串口输出的错误信息
2. 参考项目文档中的故障排除指南
3. 确认硬件连接和网络环境