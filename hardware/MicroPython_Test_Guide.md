# W601 MicroPython智慧办公楼系统测试验证指南

## 📋 概述

本指南提供了W601 MicroPython智慧办公楼系统的完整测试验证方案，包括硬件测试、功能测试、集成测试和性能测试。

## 🎯 测试目标

- ✅ 验证硬件连接正确性
- ✅ 确认传感器数据采集准确性  
- ✅ 测试设备控制响应性
- ✅ 验证WiFi和MQTT通信稳定性
- ✅ 确保自动控制逻辑正确性
- ✅ 评估系统整体性能

## 🛠️ 测试环境准备

### 硬件环境
- W601开发板 (已烧录MicroPython固件)
- 完整传感器模块连接
- USB数据线连接到电脑
- 稳定的WiFi网络环境

### 软件环境
- VS Code + RT-Thread MicroPython插件
- MQTT服务器 (本地或云端)
- MQTTX客户端工具
- 串口调试工具

### 网络环境
- WiFi网络 (2.4GHz)
- MQTT Broker运行正常
- 防火墙允许MQTT端口 (1883)

## 🔧 测试工具

### 1. VS Code MicroPython环境
```bash
# 确保插件正常工作
1. 连接W601开发板
2. 打开REPL终端
3. 测试基本Python命令
```

### 2. MQTTX客户端
```bash
# 下载安装MQTTX
https://mqttx.app/

# 连接配置
Host: localhost (或你的MQTT服务器)
Port: 1883
Client ID: test_client
```

### 3. 串口监视器
```bash
# 参数设置
波特率: 115200
数据位: 8
停止位: 1
校验位: 无
```

## 📝 测试步骤

### 第一阶段：硬件基础测试

#### 1.1 开发板连接测试

**测试目的**：验证开发板基本功能
**测试代码**：
```python
# test_basic.py
import machine
import time

print("=== W601基础功能测试 ===")

# 测试内置LED
print("1. 测试内置LED...")
led = machine.Pin(13, machine.Pin.OUT)
for i in range(5):
    led.value(1)
    time.sleep(0.5)
    led.value(0)
    time.sleep(0.5)
print("内置LED测试完成")

# 测试GPIO输出
print("2. 测试GPIO输出...")
test_pin = machine.Pin(4, machine.Pin.OUT)
test_pin.value(1)
time.sleep(1)
test_pin.value(0)
print("GPIO输出测试完成")

# 测试ADC
print("3. 测试ADC...")
adc = machine.ADC(machine.Pin(2))
adc_value = adc.read()
print(f"ADC读数: {adc_value}")

print("=== 基础功能测试完成 ===")
```

**预期结果**：
- 内置LED闪烁5次
- GPIO输出正常
- ADC能读取到数值

#### 1.2 传感器连接测试

**测试目的**：验证所有传感器连接正确
**测试代码**：
```python
# test_sensors.py
import machine
import dht
import time

print("=== 传感器连接测试 ===")

# 1. DHT22温湿度传感器测试
print("1. 测试DHT22传感器...")
try:
    dht_sensor = dht.DHT22(machine.Pin(1))  # PA1
    dht_sensor.measure()
    temp = dht_sensor.temperature()
    hum = dht_sensor.humidity()
    print(f"✅ DHT22正常 - 温度: {temp}°C, 湿度: {hum}%")
except Exception as e:
    print(f"❌ DHT22错误: {e}")

# 2. 光照传感器测试
print("2. 测试光照传感器...")
try:
    light_adc = machine.ADC(machine.Pin(2))  # PA2
    light_value = light_adc.read()
    light_percent = (light_value / 4095) * 100
    print(f"✅ 光照传感器正常 - 原始值: {light_value}, 百分比: {light_percent:.1f}%")
except Exception as e:
    print(f"❌ 光照传感器错误: {e}")

# 3. 火焰传感器测试
print("3. 测试火焰传感器...")
try:
    flame_pin = machine.Pin(3, machine.Pin.IN)  # PA3
    flame_status = not flame_pin.value()  # 低电平表示检测到火焰
    print(f"✅ 火焰传感器正常 - 状态: {'检测到火焰' if flame_status else '正常'}")
except Exception as e:
    print(f"❌ 火焰传感器错误: {e}")

print("=== 传感器测试完成 ===")
```

**预期结果**：
- DHT22能正常读取温湿度
- 光照传感器能读取光照值
- 火焰传感器能检测状态

#### 1.3 执行器测试

**测试目的**：验证RGB灯和蜂鸣器控制
**测试代码**：
```python
# test_actuators.py
import machine
import time

print("=== 执行器测试 ===")

# 1. RGB LED测试
print("1. 测试RGB LED...")
red_pin = machine.Pin(4, machine.Pin.OUT)    # PA4
green_pin = machine.Pin(5, machine.Pin.OUT)  # PA5
blue_pin = machine.Pin(6, machine.Pin.OUT)   # PA6

colors = [
    ("红色", 1, 0, 0),
    ("绿色", 0, 1, 0),
    ("蓝色", 0, 0, 1),
    ("黄色", 1, 1, 0),
    ("紫色", 1, 0, 1),
    ("青色", 0, 1, 1),
    ("白色", 1, 1, 1),
]

for color_name, r, g, b in colors:
    print(f"显示{color_name}...")
    red_pin.value(r)
    green_pin.value(g)
    blue_pin.value(b)
    time.sleep(1)

# 关闭RGB
red_pin.value(0)
green_pin.value(0)
blue_pin.value(0)
print("✅ RGB LED测试完成")

# 2. 蜂鸣器测试
print("2. 测试蜂鸣器...")
buzzer_pin = machine.Pin(7, machine.Pin.OUT)  # PA7

for i in range(3):
    print(f"蜂鸣器响第{i+1}次...")
    buzzer_pin.value(1)
    time.sleep(0.3)
    buzzer_pin.value(0)
    time.sleep(0.3)

print("✅ 蜂鸣器测试完成")
print("=== 执行器测试完成 ===")
```

**预期结果**：
- RGB LED能显示各种颜色
- 蜂鸣器能正常发声

### 第二阶段：网络通信测试

#### 2.1 WiFi连接测试

**测试目的**：验证WiFi连接功能
**测试代码**：
```python
# test_wifi.py
import network
import time

print("=== WiFi连接测试 ===")

# WiFi配置 (请修改为你的WiFi信息)
WIFI_SSID = "your_wifi_ssid"
WIFI_PASSWORD = "your_wifi_password"

def connect_wifi():
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    
    if wlan.isconnected():
        print("WiFi已连接")
        return wlan
    
    print(f"正在连接WiFi: {WIFI_SSID}")
    wlan.connect(WIFI_SSID, WIFI_PASSWORD)
    
    # 等待连接
    timeout = 20
    while not wlan.isconnected() and timeout > 0:
        print(".", end="")
        time.sleep(1)
        timeout -= 1
    
    if wlan.isconnected():
        print(f"\n✅ WiFi连接成功")
        print(f"IP地址: {wlan.ifconfig()[0]}")
        print(f"子网掩码: {wlan.ifconfig()[1]}")
        print(f"网关: {wlan.ifconfig()[2]}")
        print(f"DNS: {wlan.ifconfig()[3]}")
        return wlan
    else:
        print(f"\n❌ WiFi连接失败")
        return None

# 测试WiFi连接
wlan = connect_wifi()

if wlan:
    # 测试网络连通性
    print("测试网络连通性...")
    try:
        import socket
        s = socket.socket()
        s.settimeout(5)
        s.connect(('8.8.8.8', 53))  # 连接Google DNS
        s.close()
        print("✅ 网络连通性正常")
    except Exception as e:
        print(f"❌ 网络连通性测试失败: {e}")

print("=== WiFi测试完成 ===")
```

**预期结果**：
- 成功连接到WiFi网络
- 获取到IP地址
- 网络连通性正常

#### 2.2 MQTT连接测试

**测试目的**：验证MQTT客户端功能
**测试代码**：
```python
# test_mqtt.py
import network
import time
from umqtt.simple import MQTTClient
import json

print("=== MQTT连接测试 ===")

# MQTT配置
MQTT_SERVER = "localhost"  # 修改为你的MQTT服务器
MQTT_PORT = 1883
MQTT_CLIENT_ID = "w601_test"
MQTT_USER = ""  # 如果需要认证
MQTT_PASSWORD = ""

# 测试主题
TEST_TOPIC = "test/w601"

def mqtt_callback(topic, msg):
    print(f"收到消息 - 主题: {topic.decode()}, 内容: {msg.decode()}")

def test_mqtt():
    try:
        # 创建MQTT客户端
        client = MQTTClient(
            client_id=MQTT_CLIENT_ID,
            server=MQTT_SERVER,
            port=MQTT_PORT,
            user=MQTT_USER,
            password=MQTT_PASSWORD
        )
        
        # 设置回调函数
        client.set_callback(mqtt_callback)
        
        # 连接到MQTT服务器
        print(f"正在连接MQTT服务器: {MQTT_SERVER}:{MQTT_PORT}")
        client.connect()
        print("✅ MQTT连接成功")
        
        # 订阅测试主题
        client.subscribe(TEST_TOPIC)
        print(f"✅ 订阅主题: {TEST_TOPIC}")
        
        # 发布测试消息
        test_message = {
            "device_id": "W601_TEST",
            "timestamp": time.time(),
            "message": "MQTT测试消息"
        }
        
        client.publish(TEST_TOPIC, json.dumps(test_message))
        print("✅ 发布测试消息")
        
        # 等待接收消息
        print("等待接收消息...")
        for i in range(5):
            client.check_msg()
            time.sleep(1)
        
        # 断开连接
        client.disconnect()
        print("✅ MQTT测试完成")
        
    except Exception as e:
        print(f"❌ MQTT测试失败: {e}")

# 先确保WiFi连接
wlan = network.WLAN(network.STA_IF)
if wlan.isconnected():
    test_mqtt()
else:
    print("❌ 请先连接WiFi")

print("=== MQTT测试完成 ===")
```

**预期结果**：
- 成功连接到MQTT服务器
- 能够订阅和发布消息
- 消息收发正常

### 第三阶段：功能集成测试

#### 3.1 传感器数据上报测试

**测试目的**：验证传感器数据采集和上报
**测试代码**：
```python
# test_sensor_data.py
import machine
import network
import dht
import time
import json
from umqtt.simple import MQTTClient

print("=== 传感器数据上报测试 ===")

# 配置参数
MQTT_SERVER = "localhost"
MQTT_PORT = 1883
MQTT_CLIENT_ID = "w601_sensor_test"
SENSOR_TOPIC = "office/sensor/data"

class SensorTest:
    def __init__(self):
        # 初始化传感器
        self.dht_sensor = dht.DHT22(machine.Pin(1))  # PA1
        self.light_adc = machine.ADC(machine.Pin(2))  # PA2
        self.flame_pin = machine.Pin(3, machine.Pin.IN)  # PA3
        
        # 初始化MQTT客户端
        self.mqtt_client = None
        
    def read_sensors(self):
        """读取所有传感器数据"""
        sensor_data = {}
        
        try:
            # 读取温湿度
            self.dht_sensor.measure()
            sensor_data['temperature'] = round(self.dht_sensor.temperature(), 2)
            sensor_data['humidity'] = round(self.dht_sensor.humidity(), 2)
        except Exception as e:
            print(f"DHT22读取错误: {e}")
            sensor_data['temperature'] = None
            sensor_data['humidity'] = None
        
        try:
            # 读取光照
            light_raw = self.light_adc.read()
            sensor_data['light'] = round((light_raw / 4095) * 100, 2)
        except Exception as e:
            print(f"光照传感器读取错误: {e}")
            sensor_data['light'] = None
        
        try:
            # 读取火焰状态
            sensor_data['flame'] = not self.flame_pin.value()
        except Exception as e:
            print(f"火焰传感器读取错误: {e}")
            sensor_data['flame'] = None
        
        return sensor_data
    
    def connect_mqtt(self):
        """连接MQTT服务器"""
        try:
            self.mqtt_client = MQTTClient(
                client_id=MQTT_CLIENT_ID,
                server=MQTT_SERVER,
                port=MQTT_PORT
            )
            self.mqtt_client.connect()
            print("✅ MQTT连接成功")
            return True
        except Exception as e:
            print(f"❌ MQTT连接失败: {e}")
            return False
    
    def publish_sensor_data(self, data):
        """发布传感器数据"""
        if not self.mqtt_client:
            return False
        
        try:
            message = {
                "device_id": "W601_TEST",
                "timestamp": time.time(),
                "data": data
            }
            
            self.mqtt_client.publish(SENSOR_TOPIC, json.dumps(message))
            print(f"✅ 数据发布成功: {data}")
            return True
        except Exception as e:
            print(f"❌ 数据发布失败: {e}")
            return False
    
    def run_test(self, duration=60):
        """运行传感器测试"""
        if not self.connect_mqtt():
            return
        
        print(f"开始传感器数据测试，持续{duration}秒...")
        start_time = time.time()
        
        while time.time() - start_time < duration:
            # 读取传感器数据
            sensor_data = self.read_sensors()
            print(f"传感器数据: {sensor_data}")
            
            # 发布数据
            self.publish_sensor_data(sensor_data)
            
            # 等待10秒
            time.sleep(10)
        
        # 断开MQTT连接
        if self.mqtt_client:
            self.mqtt_client.disconnect()
        
        print("✅ 传感器数据测试完成")

# 运行测试
test = SensorTest()
test.run_test(60)  # 测试60秒
```

**预期结果**：
- 能正常读取所有传感器数据
- 数据格式正确
- MQTT发布成功

#### 3.2 设备控制测试

**测试目的**：验证设备控制响应
**测试代码**：
```python
# test_device_control.py
import machine
import network
import time
import json
from umqtt.simple import MQTTClient

print("=== 设备控制测试 ===")

# 配置参数
MQTT_SERVER = "localhost"
MQTT_PORT = 1883
MQTT_CLIENT_ID = "w601_control_test"
CONTROL_TOPIC = "office/control/cmd"

class DeviceControlTest:
    def __init__(self):
        # 初始化设备控制引脚
        self.red_pin = machine.Pin(4, machine.Pin.OUT)    # PA4
        self.green_pin = machine.Pin(5, machine.Pin.OUT)  # PA5
        self.blue_pin = machine.Pin(6, machine.Pin.OUT)   # PA6
        self.buzzer_pin = machine.Pin(7, machine.Pin.OUT) # PA7
        
        # 设备状态
        self.rgb_status = {"r": 0, "g": 0, "b": 0}
        self.buzzer_status = False
        
        # MQTT客户端
        self.mqtt_client = None
    
    def mqtt_callback(self, topic, msg):
        """MQTT消息回调"""
        try:
            command = json.loads(msg.decode())
            print(f"收到控制命令: {command}")
            self.handle_command(command)
        except Exception as e:
            print(f"命令处理错误: {e}")
    
    def handle_command(self, command):
        """处理控制命令"""
        cmd_type = command.get('type')
        
        if cmd_type == 'rgb_on':
            self.set_rgb_on()
        elif cmd_type == 'rgb_off':
            self.set_rgb_off()
        elif cmd_type == 'rgb_color':
            color = command.get('color', {})
            self.set_rgb_color(color.get('r', 0), color.get('g', 0), color.get('b', 0))
        elif cmd_type == 'buzzer_on':
            self.set_buzzer_on()
        elif cmd_type == 'buzzer_off':
            self.set_buzzer_off()
        else:
            print(f"未知命令类型: {cmd_type}")
    
    def set_rgb_on(self):
        """开启RGB灯 (白色)"""
        self.red_pin.value(1)
        self.green_pin.value(1)
        self.blue_pin.value(1)
        self.rgb_status = {"r": 1, "g": 1, "b": 1}
        print("✅ RGB灯开启 (白色)")
    
    def set_rgb_off(self):
        """关闭RGB灯"""
        self.red_pin.value(0)
        self.green_pin.value(0)
        self.blue_pin.value(0)
        self.rgb_status = {"r": 0, "g": 0, "b": 0}
        print("✅ RGB灯关闭")
    
    def set_rgb_color(self, r, g, b):
        """设置RGB颜色"""
        self.red_pin.value(r)
        self.green_pin.value(g)
        self.blue_pin.value(b)
        self.rgb_status = {"r": r, "g": g, "b": b}
        print(f"✅ RGB颜色设置: R={r}, G={g}, B={b}")
    
    def set_buzzer_on(self):
        """开启蜂鸣器"""
        self.buzzer_pin.value(1)
        self.buzzer_status = True
        print("✅ 蜂鸣器开启")
    
    def set_buzzer_off(self):
        """关闭蜂鸣器"""
        self.buzzer_pin.value(0)
        self.buzzer_status = False
        print("✅ 蜂鸣器关闭")
    
    def connect_mqtt(self):
        """连接MQTT并订阅控制主题"""
        try:
            self.mqtt_client = MQTTClient(
                client_id=MQTT_CLIENT_ID,
                server=MQTT_SERVER,
                port=MQTT_PORT
            )
            
            self.mqtt_client.set_callback(self.mqtt_callback)
            self.mqtt_client.connect()
            self.mqtt_client.subscribe(CONTROL_TOPIC)
            print(f"✅ MQTT连接成功，订阅主题: {CONTROL_TOPIC}")
            return True
        except Exception as e:
            print(f"❌ MQTT连接失败: {e}")
            return False
    
    def run_test(self):
        """运行控制测试"""
        if not self.connect_mqtt():
            return
        
        print("设备控制测试启动，等待控制命令...")
        print("可以通过MQTTX发送以下测试命令:")
        print('1. RGB开启: {"type": "rgb_on"}')
        print('2. RGB关闭: {"type": "rgb_off"}')
        print('3. RGB颜色: {"type": "rgb_color", "color": {"r": 1, "g": 0, "b": 0}}')
        print('4. 蜂鸣器开: {"type": "buzzer_on"}')
        print('5. 蜂鸣器关: {"type": "buzzer_off"}')
        
        try:
            while True:
                self.mqtt_client.check_msg()
                time.sleep(0.1)
        except KeyboardInterrupt:
            print("\n测试中断")
        finally:
            if self.mqtt_client:
                self.mqtt_client.disconnect()
            print("✅ 设备控制测试完成")

# 运行测试
test = DeviceControlTest()
test.run_test()
```

**预期结果**：
- 能接收MQTT控制命令
- RGB灯响应正确
- 蜂鸣器控制正常

### 第四阶段：完整系统测试

#### 4.1 部署完整系统代码

**步骤**：
1. 将 `W601_SmartOffice_MicroPython.py` 重命名为 `main.py`
2. 修改配置参数
3. 上传到W601开发板
4. 重启设备

**配置修改**：
```python
# 在main.py中修改以下配置
WIFI_SSID = "your_wifi_ssid"
WIFI_PASSWORD = "your_wifi_password"
MQTT_SERVER = "your_mqtt_server"
MQTT_PORT = 1883
```

#### 4.2 系统功能验证

**验证清单**：
- [ ] WiFi自动连接
- [ ] MQTT客户端连接
- [ ] 传感器数据定时上报
- [ ] 设备控制响应
- [ ] 火焰告警功能
- [ ] 智能照明控制
- [ ] 系统状态监控
- [ ] 设备自检功能

#### 4.3 性能测试

**测试项目**：

1. **数据上报频率测试**
   - 验证30秒定时上报
   - 检查数据完整性

2. **响应时间测试**
   - 控制命令响应时间 < 1秒
   - 告警响应时间 < 2秒

3. **稳定性测试**
   - 连续运行24小时
   - 网络断线重连测试

4. **内存使用测试**
   ```python
   import gc
   print(f"空闲内存: {gc.mem_free()} bytes")
   ```

## 📊 测试报告模板

### 测试环境信息
- **测试日期**：YYYY-MM-DD
- **固件版本**：MicroPython v1.x.x
- **硬件版本**：W601 IoT Board
- **网络环境**：WiFi SSID, 信号强度

### 测试结果汇总

| 测试项目 | 测试结果 | 备注 |
|----------|----------|------|
| 硬件连接 | ✅ 通过 | 所有传感器正常 |
| WiFi连接 | ✅ 通过 | 连接时间: 5秒 |
| MQTT通信 | ✅ 通过 | 连接稳定 |
| 传感器数据 | ✅ 通过 | 数据准确 |
| 设备控制 | ✅ 通过 | 响应及时 |
| 自动控制 | ✅ 通过 | 逻辑正确 |
| 系统稳定性 | ✅ 通过 | 运行24小时无异常 |

### 性能指标

| 指标 | 目标值 | 实际值 | 结果 |
|------|--------|--------|------|
| 数据上报间隔 | 30秒 | 30±1秒 | ✅ |
| 控制响应时间 | <1秒 | 0.5秒 | ✅ |
| 告警响应时间 | <2秒 | 1秒 | ✅ |
| 内存使用率 | <80% | 65% | ✅ |
| WiFi重连时间 | <10秒 | 8秒 | ✅ |

### 问题记录

| 问题描述 | 严重程度 | 解决方案 | 状态 |
|----------|----------|----------|------|
| 偶尔DHT22读取失败 | 低 | 增加重试机制 | 已解决 |
| 网络断线后重连较慢 | 中 | 优化重连逻辑 | 已解决 |

## 🔍 故障排查指南

### 常见问题及解决方案

1. **传感器读数异常**
   ```python
   # 检查传感器连接
   import machine
   pin = machine.Pin(1, machine.Pin.IN)
   print(f"引脚状态: {pin.value()}")
   ```

2. **WiFi连接失败**
   ```python
   # 检查WiFi状态
   import network
   wlan = network.WLAN(network.STA_IF)
   print(f"WiFi状态: {wlan.status()}")
   ```

3. **MQTT连接问题**
   ```python
   # 测试网络连通性
   import socket
   s = socket.socket()
   s.connect(('mqtt_server', 1883))
   s.close()
   ```

4. **内存不足**
   ```python
   # 内存清理
   import gc
   gc.collect()
   print(f"清理后内存: {gc.mem_free()}")
   ```

## 📈 性能优化建议

1. **内存优化**
   - 及时释放不用的对象
   - 使用生成器代替列表
   - 定期调用垃圾回收

2. **网络优化**
   - 实现断线重连机制
   - 使用QoS控制消息质量
   - 优化数据传输频率

3. **功耗优化**
   - 使用深度睡眠模式
   - 降低传感器采样频率
   - 优化LED亮度

## 🎯 验收标准

### 功能验收
- ✅ 所有传感器数据正常采集
- ✅ 设备控制响应及时准确
- ✅ 网络通信稳定可靠
- ✅ 自动控制逻辑正确
- ✅ 异常处理机制完善

### 性能验收
- ✅ 数据上报准时率 > 95%
- ✅ 控制响应时间 < 1秒
- ✅ 系统稳定运行 > 24小时
- ✅ 内存使用率 < 80%
- ✅ 网络重连时间 < 10秒

---

🎉 **测试完成！** 通过本指南的完整测试，你的W601智慧办公楼系统已经准备好投入使用了！