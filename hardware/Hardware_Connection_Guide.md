# W601智慧办公楼系统硬件连接指南

## 📋 概述

本指南详细说明W601开发板与各传感器和执行器的连接方法，以及固件烧录步骤。即使没有硬件基础，也能按照本指南完成硬件搭建。

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

## 🎯 下一步

硬件连接完成后：

1. **运行完整测试程序**
   - 使用提供的测试代码验证所有功能
   
2. **部署智慧办公楼代码**
   - 上传 `W601_SmartOffice_MicroPython.py`
   - 配置WiFi和MQTT参数

3. **系统集成测试**
   - 连接到后端系统
   - 验证数据传输功能

---

🎉 **恭喜！** 你已经完成了W601智慧办公楼系统的硬件连接。现在可以开始测试和运行完整的系统了！