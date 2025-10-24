# RT-Thread MicroPython W601开发环境搭建指南

## 📋 概述

本指南将帮助你搭建基于RT-Thread MicroPython的W601智慧办公楼系统开发环境。RT-Thread MicroPython为W601开发板提供了完整的Python开发支持，让你可以使用Python语言进行物联网硬件开发。

## 🎯 开发环境特点

- **🐍 Python语言开发**：使用熟悉的Python语法进行硬件编程
- **🔌 即插即用**：无需复杂的C语言环境配置
- **📡 丰富的库支持**：内置WiFi、MQTT、传感器等库
- **🔄 实时调试**：支持REPL交互式开发
- **📱 跨平台支持**：Windows、Linux、Mac都支持

## 🛠️ 环境要求

### 硬件要求
- **W601 IoT Board开发板** (正点原子版本或其他兼容版本)
- **USB数据线** (Type-C或Micro-USB，根据开发板接口)
- **传感器模块**：
  - DHT22温湿度传感器
  - 光照传感器 (光敏电阻或专用传感器)
  - 火焰传感器
  - RGB LED灯
  - 蜂鸣器

### 软件要求
- **操作系统**：Windows 10/11、Ubuntu 18.04+、macOS 10.15+
- **Python 3.7+** (用于开发环境，非必需)
- **Visual Studio Code** (推荐IDE)

## 📦 安装步骤

### 第一步：安装VS Code和插件

1. **下载安装VS Code**
   ```bash
   # 访问官网下载
   https://code.visualstudio.com/
   ```

2. **安装RT-Thread MicroPython插件**
   - 打开VS Code
   - 按 `Ctrl+Shift+X` 打开扩展面板
   - 搜索 `RT-Thread MicroPython`
   - 点击安装 `RT-Thread MicroPython` 插件

3. **安装Python插件** (可选，用于代码补全)
   - 搜索并安装 `Python` 插件
   - 安装 `Pylance` 插件 (Python语言服务器)

### 第二步：下载W601 MicroPython固件

1. **获取固件文件**
   ```bash
   # 方式1：从RT-Thread官方论坛下载
   https://www.rt-thread.org/qa/forum.php
   
   # 方式2：从GitHub获取
   https://github.com/SummerGift/micropython_for_w601
   
   # 方式3：从正点原子官网下载
   http://www.openedv.com/
   ```

2. **固件文件说明**
   - `W601_MicroPython_xxx.fls` - 主固件文件
   - `W601_MicroPython_xxx.img` - 镜像文件 (部分版本)

### 第三步：烧录MicroPython固件

#### 方法1：使用官方烧录工具

1. **下载烧录工具**
   - 下载 `WM_Tool.exe` (联盛德官方工具)
   - 或使用 `RT-Thread Studio` 内置烧录功能

2. **烧录步骤**
   ```
   1. 将W601开发板通过USB连接到电脑
   2. 按住开发板上的BOOT按键
   3. 按一下RESET按键，然后松开BOOT按键 (进入下载模式)
   4. 打开烧录工具，选择对应的COM端口
   5. 选择MicroPython固件文件
   6. 点击"开始烧录"
   7. 等待烧录完成
   ```

#### 方法2：使用RT-Thread Studio

1. **安装RT-Thread Studio**
   ```bash
   # 下载地址
   https://www.rt-thread.org/studio.html
   ```

2. **烧录固件**
   ```
   1. 打开RT-Thread Studio
   2. 选择 "工具" -> "固件烧录"
   3. 选择W601芯片型号
   4. 选择MicroPython固件文件
   5. 点击烧录
   ```

### 第四步：配置开发环境

1. **设置VS Code终端** (Windows用户)
   ```json
   // 在VS Code设置中添加
   {
       "terminal.integrated.defaultProfile.windows": "PowerShell"
   }
   ```

2. **Linux用户权限配置**
   ```bash
   # 将用户添加到dialout组
   sudo usermod -aG dialout $USER
   
   # 重启系统使配置生效
   sudo reboot
   ```

## 🚀 创建第一个项目

### 第一步：创建MicroPython工程

1. **在VS Code中创建工程**
   - 按 `Ctrl+Shift+P` 打开命令面板
   - 输入 `MicroPython: Create Project`
   - 选择 `Create a blank MicroPython project`
   - 选择工程目录

2. **或基于示例创建**
   - 选择 `Create project from examples`
   - 选择适合的示例模板

### 第二步：连接开发板

1. **连接设备**
   - 将W601开发板通过USB连接到电脑
   - 确保已烧录MicroPython固件
   - 按RESET按键重启开发板

2. **在VS Code中连接**
   - 点击VS Code左下角的连接按钮
   - 选择对应的COM端口 (如COM3、COM4等)
   - 连接成功后会显示设备信息

### 第三步：测试连接

1. **打开REPL终端**
   - 连接成功后会自动打开REPL终端
   - 或按 `Ctrl+Shift+P` 输入 `MicroPython: Open REPL`

2. **测试基本功能**
   ```python
   # 在REPL中输入以下命令测试
   >>> print("Hello W601!")
   Hello W601!
   
   >>> import machine
   >>> led = machine.Pin(13, machine.Pin.OUT)
   >>> led.value(1)  # 点亮LED
   >>> led.value(0)  # 熄灭LED
   ```

## 📁 项目结构

```
W601_SmartOffice_Project/
├── main.py                 # 主程序文件
├── boot.py                 # 启动配置文件
├── config.py              # 配置参数文件
├── lib/                   # 自定义库目录
│   ├── sensors.py         # 传感器驱动
│   ├── mqtt_client.py     # MQTT客户端
│   └── utils.py           # 工具函数
├── examples/              # 示例代码
│   ├── led_test.py        # LED测试
│   ├── sensor_test.py     # 传感器测试
│   └── wifi_test.py       # WiFi测试
└── README.md              # 项目说明
```

## 🔧 常用开发命令

### VS Code快捷键
```
Ctrl+Shift+P        # 打开命令面板
Alt+Q               # 快速运行当前文件
Ctrl+Shift+D        # 下载文件到开发板
Ctrl+Shift+U        # 从开发板上传文件
Ctrl+Shift+R        # 打开REPL终端
```

### REPL常用命令
```python
# 查看文件列表
import os
os.listdir()

# 查看内存使用
import gc
gc.mem_free()

# 软重启
import machine
machine.soft_reset()

# 硬重启  
machine.reset()

# 查看网络状态
import network
wlan = network.WLAN(network.STA_IF)
wlan.isconnected()
wlan.ifconfig()
```

## 📚 库文件说明

### 内置库
- **machine** - 硬件控制 (GPIO、PWM、ADC、I2C、SPI等)
- **network** - 网络功能 (WiFi连接)
- **time** - 时间相关函数
- **json** - JSON数据处理
- **gc** - 垃圾回收和内存管理

### 扩展库
- **umqtt.simple** - MQTT客户端
- **urequests** - HTTP请求
- **dht** - DHT温湿度传感器 (需要安装)

## 🔍 调试技巧

### 1. 使用print调试
```python
print(f"传感器值: {sensor_value}")
print(f"WiFi状态: {wlan.isconnected()}")
```

### 2. 异常处理
```python
try:
    # 可能出错的代码
    result = risky_operation()
except Exception as e:
    print(f"错误: {e}")
```

### 3. 内存监控
```python
import gc
print(f"空闲内存: {gc.mem_free()} bytes")
gc.collect()  # 强制垃圾回收
```

### 4. 实时文件运行
- 使用 `Alt+Q` 快速运行当前文件
- 修改代码后立即测试，无需重新烧录

## ⚠️ 常见问题

### 1. 无法连接设备
**问题**：VS Code无法连接到W601开发板
**解决方案**：
- 检查USB线缆连接
- 确认COM端口号正确
- 检查是否已烧录MicroPython固件
- 尝试按RESET按键重启开发板

### 2. 导入模块失败
**问题**：`ImportError: no module named 'xxx'`
**解决方案**：
- 检查模块名称拼写
- 确认模块是否已安装到开发板
- 使用 `os.listdir()` 查看文件是否存在

### 3. 内存不足
**问题**：`MemoryError: memory allocation failed`
**解决方案**：
- 调用 `gc.collect()` 进行垃圾回收
- 减少全局变量使用
- 优化代码逻辑，释放不用的对象

### 4. WiFi连接失败
**问题**：无法连接到WiFi网络
**解决方案**：
- 检查SSID和密码是否正确
- 确认WiFi信号强度
- 尝试重启开发板
- 检查路由器设置

## 🎯 下一步

环境搭建完成后，你可以：

1. **运行智慧办公楼示例代码**
   - 使用提供的 `W601_SmartOffice_MicroPython.py`
   - 修改WiFi和MQTT配置参数

2. **学习MicroPython语法**
   - 查看官方文档：https://docs.micropython.org/
   - 学习RT-Thread MicroPython特性

3. **硬件连接和测试**
   - 按照硬件连接图连接传感器
   - 逐个测试各个功能模块

4. **系统集成测试**
   - 连接到你的MQTT服务器
   - 测试与后端系统的数据交互

## 📞 技术支持

- **RT-Thread官方论坛**：https://www.rt-thread.org/qa/forum.php
- **MicroPython官方文档**：https://docs.micropython.org/
- **GitHub项目**：https://github.com/SummerGift/micropython_for_w601
- **QQ交流群**：703840633

---

🎉 **恭喜！** 你已经成功搭建了RT-Thread MicroPython开发环境。现在可以开始你的W601智慧办公楼系统开发之旅了！