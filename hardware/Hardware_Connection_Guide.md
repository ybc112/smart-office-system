# W601智慧办公楼系统硬件连接指南

## 📋 概述

本指南详细说明W601开发板与各传感器和执行器的连接方法，以及固件烧录步骤。即使没有硬件基础，也能按照本指南完成硬件搭建。

> **重要提示**: 本项目已提供完整的MicroPython代码，位于 `W601_SmartOffice/` 目录下，可直接烧录使用。

## 🎯 硬件清单

### 主控板
- **W601 IoT Board开发板** × 1
  - 内置WiFi模块
  - 支持MicroPython
  - 丰富的GPIO接口

### 传感器模块
- **DHT22温湿度传感器** × 1
- **光照传感器** (光敏电阻模块) × 1  
- **火焰传感器** × 1
- **RGB LED灯** × 1
- **蜂鸣器模块** × 1

### 连接材料
- **杜邦线** (公对母、母对母) 若干
- **面包板** × 1 (可选)
- **USB数据线** × 1 (Type-C或Micro-USB)

## 🔌 W601开发板引脚说明

### GPIO引脚分布
```
W601开发板引脚图：
                    ┌─────────────────┐
                    │      USB        │
                    └─────────────────┘
    3V3  ●●  GND     │                 │     GND  ●●  3V3
    PA0  ●●  PA1     │                 │     PB0  ●●  PB1  
    PA2  ●●  PA3     │                 │     PB2  ●●  PB3
    PA4  ●●  PA5     │                 │     PB4  ●●  PB5
    PA6  ●●  PA7     │                 │     PB6  ●●  PB7
    PA8  ●●  PA9     │                 │     PB8  ●●  PB9
    PA10 ●●  PA11    │                 │     PB10 ●●  PB11
    PA12 ●●  PA13    │                 │     PB12 ●●  PB13
    PA14 ●●  PA15    │                 │     PB14 ●●  PB15
                     └─────────────────┘
```

### 重要引脚功能
- **3V3**: 3.3V电源输出
- **GND**: 接地
- **PA0-PA15**: GPIO端口A (0-15号引脚)
- **PB0-PB15**: GPIO端口B (0-15号引脚)

## 🔧 传感器连接详解

### 1. DHT22温湿度传感器

**DHT22引脚说明：**
```
DHT22传感器：
┌─────────────┐
│  1  2  3  4 │
└─────────────┘
1: VCC (3.3V)
2: DATA (数据)
3: NC (不连接)
4: GND (接地)
```

**连接方法：**
```
DHT22 → W601
VCC   → 3V3
DATA  → PA1
GND   → GND
```

**连接步骤：**
1. 将DHT22的VCC引脚连接到W601的3V3
2. 将DHT22的DATA引脚连接到W601的PA1
3. 将DHT22的GND引脚连接到W601的GND
4. 在DATA引脚和VCC之间连接一个4.7kΩ上拉电阻(可选)

### 2. 光照传感器 (光敏电阻模块)

**光敏电阻模块引脚：**
```
光敏电阻模块：
┌─────────────┐
│ VCC GND AO  │
└─────────────┘
VCC: 电源正极 (3.3V)
GND: 电源负极
AO:  模拟输出
```

**连接方法：**
```
光敏电阻 → W601
VCC      → 3V3
GND      → GND
AO       → PA2 (ADC引脚)
```

**连接步骤：**
1. 将光敏电阻模块的VCC连接到W601的3V3
2. 将光敏电阻模块的GND连接到W601的GND
3. 将光敏电阻模块的AO连接到W601的PA2

### 3. 火焰传感器

**火焰传感器引脚：**
```
火焰传感器：
┌─────────────┐
│ VCC GND DO  │
└─────────────┘
VCC: 电源正极 (3.3V)
GND: 电源负极  
DO:  数字输出
```

**连接方法：**
```
火焰传感器 → W601
VCC        → 3V3
GND        → GND
DO         → PA3
```

**连接步骤：**
1. 将火焰传感器的VCC连接到W601的3V3
2. 将火焰传感器的GND连接到W601的GND
3. 将火焰传感器的DO连接到W601的PA3

### 4. RGB LED灯

**RGB LED引脚：**
```
RGB LED (共阴极)：
┌─────────────┐
│ R  G  B GND │
└─────────────┘
R:   红色引脚
G:   绿色引脚
B:   蓝色引脚
GND: 公共负极
```

**连接方法：**
```
RGB LED → W601
R       → PA4 (通过220Ω电阻)
G       → PA5 (通过220Ω电阻)
B       → PA6 (通过220Ω电阻)
GND     → GND
```

**连接步骤：**
1. 在每个颜色引脚和W601之间串联220Ω限流电阻
2. 将RGB LED的R引脚通过电阻连接到W601的PA4
3. 将RGB LED的G引脚通过电阻连接到W601的PA5
4. 将RGB LED的B引脚通过电阻连接到W601的PA6
5. 将RGB LED的GND连接到W601的GND

### 5. 蜂鸣器模块

**蜂鸣器模块引脚：**
```
蜂鸣器模块：
┌─────────────┐
│ VCC GND I/O │
└─────────────┘
VCC: 电源正极 (3.3V)
GND: 电源负极
I/O: 控制信号
```

**连接方法：**
```
蜂鸣器 → W601
VCC    → 3V3
GND    → GND
I/O    → PA7
```

**连接步骤：**
1. 将蜂鸣器模块的VCC连接到W601的3V3
2. 将蜂鸣器模块的GND连接到W601的GND
3. 将蜂鸣器模块的I/O连接到W601的PA7

## 📊 完整连接表

| 设备 | 设备引脚 | W601引脚 | 说明 |
|------|----------|----------|------|
| DHT22 | VCC | 3V3 | 电源正极 |
| DHT22 | DATA | PA1 | 数据线 |
| DHT22 | GND | GND | 电源负极 |
| 光敏电阻 | VCC | 3V3 | 电源正极 |
| 光敏电阻 | AO | PA2 | 模拟输出 |
| 光敏电阻 | GND | GND | 电源负极 |
| 火焰传感器 | VCC | 3V3 | 电源正极 |
| 火焰传感器 | DO | PA3 | 数字输出 |
| 火焰传感器 | GND | GND | 电源负极 |
| RGB LED | R | PA4 | 红色(需220Ω电阻) |
| RGB LED | G | PA5 | 绿色(需220Ω电阻) |
| RGB LED | B | PA6 | 蓝色(需220Ω电阻) |
| RGB LED | GND | GND | 公共负极 |
| 蜂鸣器 | VCC | 3V3 | 电源正极 |
| 蜂鸣器 | I/O | PA7 | 控制信号 |
| 蜂鸣器 | GND | GND | 电源负极 |

## 🔥 固件烧录详细步骤

### 准备工作

1. **下载必要文件**
   - W601 MicroPython固件 (.fls文件)
   - 烧录工具 (WM_Tool.exe)
   - USB驱动程序

2. **安装USB驱动**
   - 连接W601到电脑
   - 如果系统未自动识别，手动安装CH340或CP2102驱动

### 烧录步骤

#### 方法1：使用WM_Tool烧录工具

1. **进入下载模式**
   ```
   步骤：
   1. 按住W601开发板上的BOOT按键不放
   2. 按一下RESET按键，然后松开
   3. 松开BOOT按键
   4. 此时开发板进入下载模式
   ```

2. **配置烧录工具**
   ```
   1. 打开WM_Tool.exe
   2. 选择正确的COM端口
   3. 波特率设置为115200
   4. 选择固件文件(.fls)
   5. 烧录地址设置为0x08000000
   ```

3. **开始烧录**
   ```
   1. 点击"开始烧录"按钮
   2. 等待烧录进度条完成
   3. 看到"烧录成功"提示
   4. 按RESET按键重启开发板
   ```

#### 方法2：使用RT-Thread Studio

1. **打开RT-Thread Studio**
2. **选择烧录功能**
   - 菜单：工具 → 固件烧录
3. **配置参数**
   - 芯片型号：W601
   - 固件文件：选择MicroPython固件
   - 端口：选择对应COM端口
4. **执行烧录**
   - 点击烧录按钮
   - 等待完成

### 验证烧录结果

1. **连接串口工具**
   ```
   参数设置：
   - 波特率：115200
   - 数据位：8
   - 停止位：1
   - 校验位：无
   ```

2. **测试MicroPython**
   ```python
   # 按RESET重启后应该看到：
   MicroPython v1.x.x on 2023-xx-xx; W601 with W601
   Type "help()" for more information.
   >>> 
   
   # 测试基本功能：
   >>> print("Hello W601!")
   Hello W601!
   
   >>> import machine
   >>> led = machine.Pin(13, machine.Pin.OUT)
   >>> led.value(1)
   ```

## 🔍 硬件测试步骤

### 1. 基础连接测试

**测试LED闪烁：**
```python
import machine
import time

# 测试内置LED
led = machine.Pin(13, machine.Pin.OUT)
for i in range(10):
    led.value(1)
    time.sleep(0.5)
    led.value(0)
    time.sleep(0.5)
```

### 2. 传感器测试

**测试DHT22：**
```python
import machine
import dht
import time

# 初始化DHT22
sensor = dht.DHT22(machine.Pin(1))  # PA1

try:
    sensor.measure()
    temp = sensor.temperature()
    hum = sensor.humidity()
    print(f"温度: {temp}°C, 湿度: {hum}%")
except Exception as e:
    print(f"DHT22错误: {e}")
```

**测试光照传感器：**
```python
import machine

# 初始化ADC
adc = machine.ADC(machine.Pin(2))  # PA2

# 读取光照值
light_value = adc.read()
print(f"光照值: {light_value}")
```

**测试火焰传感器：**
```python
import machine

# 初始化火焰传感器
flame_pin = machine.Pin(3, machine.Pin.IN)  # PA3

# 读取火焰状态
flame_detected = not flame_pin.value()  # 低电平表示检测到火焰
print(f"火焰检测: {'是' if flame_detected else '否'}")
```

### 3. 执行器测试

**测试RGB LED：**
```python
import machine
import time

# 初始化RGB引脚
red = machine.Pin(4, machine.Pin.OUT)    # PA4
green = machine.Pin(5, machine.Pin.OUT)  # PA5
blue = machine.Pin(6, machine.Pin.OUT)   # PA6

# 测试各种颜色
colors = [
    (1, 0, 0),  # 红色
    (0, 1, 0),  # 绿色
    (0, 0, 1),  # 蓝色
    (1, 1, 0),  # 黄色
    (1, 0, 1),  # 紫色
    (0, 1, 1),  # 青色
    (1, 1, 1),  # 白色
]

for r, g, b in colors:
    red.value(r)
    green.value(g)
    blue.value(b)
    time.sleep(1)
```

**测试蜂鸣器：**
```python
import machine
import time

# 初始化蜂鸣器
buzzer = machine.Pin(7, machine.Pin.OUT)  # PA7

# 测试蜂鸣器
for i in range(3):
    buzzer.value(1)
    time.sleep(0.2)
    buzzer.value(0)
    time.sleep(0.2)
```

## ⚠️ 注意事项

### 安全提醒
1. **电源安全**
   - 确保所有设备使用3.3V电源
   - 避免短路，检查连线正确性
   - 不要在通电状态下插拔连线

2. **静电防护**
   - 触摸开发板前先释放静电
   - 在干燥环境中要特别小心

3. **连线检查**
   - 仔细核对引脚连接
   - 确保电源和地线连接正确
   - 检查是否有虚接

### 常见问题

1. **传感器读数异常**
   - 检查电源连接
   - 确认引脚配置正确
   - 检查传感器是否损坏

2. **LED不亮**
   - 检查限流电阻是否连接
   - 确认LED极性正确
   - 检查引脚输出状态

3. **蜂鸣器无声**
   - 检查电源连接
   - 确认控制信号正确
   - 测试蜂鸣器是否损坏

## 📸 连接示意图

```
完整连接示意图：

                    W601开发板
                ┌─────────────────┐
    DHT22 ──────┤ PA1         3V3 ├──── 各模块VCC
    光敏电阻 ────┤ PA2         GND ├──── 各模块GND
    火焰传感器 ──┤ PA3             │
    RGB-R ──────┤ PA4             │
    RGB-G ──────┤ PA5             │
    RGB-B ──────┤ PA6             │
    蜂鸣器 ──────┤ PA7             │
                └─────────────────┘
                        │
                    USB连接电脑
```

## 🚀 固件烧录与配置

### 1. 准备工作

#### 安装开发环境
1. **下载Thonny IDE**
   - 访问 https://thonny.org/
   - 下载并安装最新版本

2. **配置MicroPython**
   - 连接W601到电脑
   - 在Thonny中选择"MicroPython (generic)"解释器
   - 配置正确的串口

#### 获取项目代码
项目提供了完整的MicroPython代码，位于 `W601_SmartOffice/` 目录：

```
W601_SmartOffice/
├── main.py              # 主程序入口
├── config.py            # 配置文件
├── wifi_manager.py      # WiFi管理
├── mqtt_client.py       # MQTT客户端
├── pins.py              # 引脚定义
├── sensors/             # 传感器模块
│   ├── aht10.py         # 温湿度传感器
│   ├── light_adc.py     # 光照传感器
│   └── flame.py         # 火焰传感器
├── devices/             # 设备控制
│   ├── rgb_led.py       # RGB LED
│   └── buzzer.py        # 蜂鸣器
├── modules/             # 功能模块
│   ├── environment.py   # 环境监控
│   ├── fire_alarm.py    # 火灾报警
│   ├── heartbeat.py     # 心跳检测
│   └── reporter.py      # 数据上报
└── umqtt/               # MQTT库
    └── simple.py
```

### 2. 配置系统参数

#### 编辑配置文件
打开 `config.py` 文件，修改以下配置：

```python
# WiFi配置
WIFI_SSID = "你的WiFi名称"
WIFI_PASSWORD = "你的WiFi密码"

# MQTT配置
MQTT_BROKER = "192.168.1.100"  # 修改为你的服务器IP地址
MQTT_PORT = 1883
DEVICE_ID = "W601_001"         # 设备唯一标识

# 传感器阈值配置
LIGHT_THRESHOLD = 300          # 光照阈值（低于此值开灯）
TEMP_HIGH_THRESHOLD = 28       # 高温阈值
TEMP_LOW_THRESHOLD = 18        # 低温阈值
HUMIDITY_HIGH_THRESHOLD = 70   # 高湿度阈值
HUMIDITY_LOW_THRESHOLD = 30    # 低湿度阈值

# 数据上报间隔（秒）
REPORT_INTERVAL = 30
```

#### 重要配置说明

1. **WiFi配置**
   - 确保WiFi名称和密码正确
   - W601只支持2.4GHz WiFi网络
   - 确保网络信号强度良好

2. **MQTT配置**
   - `MQTT_BROKER`: 运行后端服务的服务器IP地址
   - 如果在同一台电脑上运行，可以使用局域网IP
   - 确保防火墙允许1883端口通信

3. **设备ID**
   - 每个设备必须有唯一的ID
   - 格式建议：`W601_001`, `W601_002` 等

### 3. 上传代码到W601

#### 使用Thonny IDE上传
1. **连接设备**
   ```
   - 用USB线连接W601到电脑
   - 打开Thonny IDE
   - 选择正确的串口和解释器
   ```

2. **上传文件**
   ```
   - 在Thonny中打开项目文件
   - 右键选择"Upload to /"
   - 按照目录结构上传所有文件
   ```

3. **验证上传**
   ```python
   # 在Thonny的Shell中执行
   import os
   os.listdir('/')
   # 应该看到所有上传的文件
   ```

#### 文件上传顺序
建议按以下顺序上传：
1. `umqtt/simple.py` (MQTT库)
2. `config.py` (配置文件)
3. `pins.py` (引脚定义)
4. `sensors/` 目录下所有文件
5. `devices/` 目录下所有文件
6. `modules/` 目录下所有文件
7. `wifi_manager.py`
8. `mqtt_client.py`
9. `main.py` (最后上传)

### 4. 测试与验证

#### 基础功能测试
```python
# 在Thonny Shell中测试各个模块

# 测试WiFi连接
from wifi_manager import WiFiManager
wifi = WiFiManager()
wifi.connect()

# 测试传感器
from sensors.aht10 import AHT10Sensor
sensor = AHT10Sensor()
temp, hum = sensor.read()
print(f"温度: {temp}°C, 湿度: {hum}%")

# 测试RGB LED
from devices.rgb_led import RGBLed
rgb = RGBLed()
rgb.set_color(255, 0, 0)  # 红色
```

#### 完整系统测试
```python
# 运行主程序
exec(open('main.py').read())

# 观察输出，应该看到：
# - WiFi连接成功
# - MQTT连接成功
# - 传感器数据读取
# - 数据上报到服务器
```

### 5. 系统集成

#### 后端配置检查
确保后端服务的MQTT配置正确：
```yaml
# backend/device-service/src/main/resources/application.yml
mqtt:
  broker-url: tcp://192.168.1.100:1883  # 与W601配置一致
  topics:
    sensor-data: office/sensor/data
    alarm: office/alarm
    control: office/control/cmd
```

#### 数据流验证
1. **启动后端服务**
2. **启动W601设备**
3. **检查后端日志**，应该看到：
   ```
   Received sensor data from W601_001
   Processing temperature: 25.5°C
   Processing humidity: 55.0%
   ```
4. **检查前端界面**，应该显示实时数据

### 6. 故障排除

#### WiFi连接问题
```python
# 检查WiFi状态
import network
wlan = network.WLAN(network.STA_IF)
print("WiFi状态:", wlan.isconnected())
print("IP地址:", wlan.ifconfig())
```

#### MQTT连接问题
```python
# 测试MQTT连接
from mqtt_client import MQTTManager
mqtt = MQTTManager()
mqtt.connect()
# 检查连接状态和错误信息
```

#### 传感器读取问题
```python
# 逐个测试传感器
from sensors.aht10 import AHT10Sensor
from sensors.light_adc import LightSensor
from sensors.flame import FlameSensor

# 测试温湿度传感器
aht10 = AHT10Sensor()
print("温湿度:", aht10.read())

# 测试光照传感器
light = LightSensor()
print("光照:", light.read())

# 测试火焰传感器
flame = FlameSensor()
print("火焰:", flame.read())
```

## 🎯 下一步

硬件连接完成后：

1. **运行完整测试程序**
   - 使用提供的测试代码验证所有功能
   
2. **部署智慧办公楼代码**
   - 上传 `W601_SmartOffice/` 目录下的所有文件
   - 配置WiFi和MQTT参数

3. **系统集成测试**
   - 连接到后端系统
   - 验证数据传输功能
   - 测试自动控制功能

4. **查看完整运行指南**
   - 参考 `项目完整运行指南.md`
   - 了解整个系统的启动流程

---

🎉 **恭喜！** 你已经完成了W601智慧办公楼系统的硬件连接。现在可以开始测试和运行完整的系统了！