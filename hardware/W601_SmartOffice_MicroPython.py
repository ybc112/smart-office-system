#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
W601智慧办公楼系统 - MicroPython版本
基于RT-Thread MicroPython固件实现
支持WiFi连接、MQTT通信、传感器数据采集、设备控制等功能
"""

import network
import time
import json
import machine
from machine import Pin, PWM, ADC, I2C, Timer
from umqtt.simple import MQTTClient
import gc

# ==================== 配置参数 ====================
# WiFi配置
WIFI_SSID = "your_wifi_ssid"
WIFI_PASSWORD = "your_wifi_password"

# MQTT服务器配置
MQTT_SERVER = "192.168.1.100"
MQTT_PORT = 1883
MQTT_CLIENT_ID = "W601_MicroPython_001"
MQTT_USERNAME = ""
MQTT_PASSWORD = ""

# MQTT主题定义
TOPIC_SENSOR_DATA = "office/sensor/data"
TOPIC_CONTROL_CMD = "office/control/cmd"
TOPIC_ALARM = "office/alarm"
TOPIC_STATUS = "office/device/status"
TOPIC_CONFIG_UPDATE = "office/config/update"

# 硬件引脚配置 (基于W601 IoT Board)
PIN_DHT22_DATA = 0      # PA0 - 温湿度传感器数据引脚
PIN_LIGHT_SENSOR = 23   # PB23 - 光照传感器 (ADC)
PIN_FLAME_SENSOR = 4    # PA4 - 火焰传感器
PIN_RGB_RED = 13        # PA13 - RGB红色
PIN_RGB_GREEN = 14      # PA14 - RGB绿色  
PIN_RGB_BLUE = 15       # PA15 - RGB蓝色
PIN_BUZZER = 15         # PB15 - 蜂鸣器

# 设备状态
device_status = {
    "device_id": MQTT_CLIENT_ID,
    "rgb_status": False,
    "buzzer_status": False,
    "wifi_connected": False,
    "mqtt_connected": False,
    "last_sensor_time": 0,
    "flame_detected": False
}

# 传感器数据
sensor_data = {
    "temperature": 0.0,
    "humidity": 0.0,
    "light": 0.0,
    "flame": False
}

# 动态配置参数
config_params = {
    "data_collect_interval": 10000  # 默认10秒，单位毫秒
}

# 全局变量
wifi = None
mqtt_client = None
rgb_red = None
rgb_green = None
rgb_blue = None
buzzer = None
light_adc = None
flame_pin = None
system_timer = None

# ==================== 硬件初始化 ====================
def init_hardware():
    """初始化硬件设备"""
    global rgb_red, rgb_green, rgb_blue, buzzer, light_adc, flame_pin
    
    print("[INIT] 初始化硬件设备...")
    
    try:
        # 初始化RGB LED (PWM控制)
        rgb_red = PWM(Pin(PIN_RGB_RED), freq=1000, duty=0)
        rgb_green = PWM(Pin(PIN_RGB_GREEN), freq=1000, duty=0)
        rgb_blue = PWM(Pin(PIN_RGB_BLUE), freq=1000, duty=0)
        
        # 初始化蜂鸣器
        buzzer = Pin(PIN_BUZZER, Pin.OUT)
        buzzer.value(0)
        
        # 初始化光照传感器 (ADC)
        light_adc = ADC(Pin(PIN_LIGHT_SENSOR))
        
        # 初始化火焰传感器
        flame_pin = Pin(PIN_FLAME_SENSOR, Pin.IN, Pin.PULL_UP)
        
        print("[INIT] ✅ 硬件初始化完成")
        
        # 执行自检
        perform_self_check()
        
    except Exception as e:
        print(f"[ERROR] 硬件初始化失败: {e}")

def perform_self_check():
    """设备自检"""
    print("[SELF-CHECK] 开始设备自检...")
    
    # RGB灯自检
    print("[SELF-CHECK] 测试RGB灯...")
    set_rgb_color(512, 0, 0)  # 红色
    time.sleep(0.5)
    set_rgb_color(0, 512, 0)  # 绿色
    time.sleep(0.5)
    set_rgb_color(0, 0, 512)  # 蓝色
    time.sleep(0.5)
    set_rgb_off()
    
    # 蜂鸣器自检
    print("[SELF-CHECK] 测试蜂鸣器...")
    buzzer.value(1)
    time.sleep(0.2)
    buzzer.value(0)
    
    # 传感器自检
    print("[SELF-CHECK] 测试传感器...")
    read_sensors()
    
    print("[SELF-CHECK] ✅ 设备自检完成")

# ==================== WiFi连接管理 ====================
def init_wifi():
    """初始化WiFi连接"""
    global wifi
    
    print(f"[WIFI] 正在连接WiFi: {WIFI_SSID}")
    
    try:
        # 创建WiFi对象
        wifi = network.WLAN(network.STA_IF)
        wifi.active(True)
        
        # 连接WiFi
        wifi.connect(WIFI_SSID, WIFI_PASSWORD)
        
        # 等待连接
        timeout = 20
        while not wifi.isconnected() and timeout > 0:
            print(f"[WIFI] 连接中... 剩余时间: {timeout}s")
            time.sleep(1)
            timeout -= 1
        
        if wifi.isconnected():
            device_status["wifi_connected"] = True
            ip_info = wifi.ifconfig()
            print(f"[WIFI] ✅ WiFi连接成功!")
            print(f"[WIFI] IP地址: {ip_info[0]}")
            print(f"[WIFI] 子网掩码: {ip_info[1]}")
            print(f"[WIFI] 网关: {ip_info[2]}")
            print(f"[WIFI] DNS: {ip_info[3]}")
            return True
        else:
            print("[WIFI] ❌ WiFi连接失败，请检查SSID和密码")
            return False
            
    except Exception as e:
        print(f"[WIFI] ❌ WiFi连接异常: {e}")
        return False

def check_wifi_connection():
    """检查WiFi连接状态"""
    global wifi
    
    if wifi and wifi.isconnected():
        device_status["wifi_connected"] = True
        return True
    else:
        device_status["wifi_connected"] = False
        print("[WIFI] ⚠️ WiFi连接断开，尝试重连...")
        return init_wifi()

# ==================== MQTT客户端管理 ====================
def init_mqtt():
    """初始化MQTT客户端"""
    global mqtt_client
    
    if not device_status["wifi_connected"]:
        print("[MQTT] ❌ WiFi未连接，无法初始化MQTT")
        return False
    
    try:
        print(f"[MQTT] 正在连接MQTT服务器: {MQTT_SERVER}:{MQTT_PORT}")
        
        # 创建MQTT客户端
        mqtt_client = MQTTClient(
            client_id=MQTT_CLIENT_ID,
            server=MQTT_SERVER,
            port=MQTT_PORT,
            user=MQTT_USERNAME,
            password=MQTT_PASSWORD,
            keepalive=60
        )
        
        # 设置消息回调
        mqtt_client.set_callback(on_mqtt_message)
        
        # 连接服务器
        mqtt_client.connect()
        
        # 订阅控制命令主题
        mqtt_client.subscribe(TOPIC_CONTROL_CMD)
        
        # 订阅配置更新主题
        mqtt_client.subscribe(TOPIC_CONFIG_UPDATE)
        
        device_status["mqtt_connected"] = True
        print("[MQTT] ✅ MQTT连接成功")
        print(f"[MQTT] 📡 已订阅主题: {TOPIC_CONTROL_CMD}")
        
        # 发布设备上线状态
        publish_device_status("online")
        
        return True
        
    except Exception as e:
        print(f"[MQTT] ❌ MQTT连接失败: {e}")
        device_status["mqtt_connected"] = False
        return False

def on_mqtt_message(topic, msg):
    """MQTT消息回调函数"""
    try:
        topic_str = topic.decode('utf-8')
        msg_str = msg.decode('utf-8')
        
        print(f"[MQTT] 📥 收到消息 - 主题: {topic_str}")
        print(f"[MQTT] 📥 消息内容: {msg_str}")
        
        if topic_str == TOPIC_CONTROL_CMD:
            handle_control_command(msg_str)
        elif topic_str == TOPIC_CONFIG_UPDATE:
            handle_config_update(msg_str)
            
    except Exception as e:
        print(f"[MQTT] ❌ 处理消息失败: {e}")

def handle_control_command(msg_str):
    """处理控制命令"""
    try:
        # 解析JSON命令
        cmd_data = json.loads(msg_str)
        device_id = cmd_data.get("deviceId", "")
        action = cmd_data.get("action", "")
        
        print(f"[CONTROL] 设备ID: {device_id}")
        print(f"[CONTROL] 动作: {action}")
        
        # 检查设备ID是否匹配
        if device_id != MQTT_CLIENT_ID:
            print(f"[CONTROL] ⚠️ 设备ID不匹配，忽略命令")
            return
        
        # 执行控制命令
        if action == "rgb_on":
            set_rgb_on()
            print("[CONTROL] ✅ RGB灯已开启")
        elif action == "rgb_off":
            set_rgb_off()
            print("[CONTROL] ✅ RGB灯已关闭")
        elif action == "buzzer_on":
            set_buzzer_on()
            print("[CONTROL] ✅ 蜂鸣器已开启")
        elif action == "buzzer_off":
            set_buzzer_off()
            print("[CONTROL] ✅ 蜂鸣器已关闭")
        else:
            print(f"[CONTROL] ⚠️ 未知命令: {action}")
            
    except Exception as e:
        print(f"[CONTROL] ❌ 处理控制命令失败: {e}")

def check_mqtt_connection():
    """检查MQTT连接状态"""
    global mqtt_client
    
    if not device_status["wifi_connected"]:
        return False
    
    if device_status["mqtt_connected"]:
        try:
            # 检查连接并处理消息
            mqtt_client.check_msg()
            return True
        except:
            device_status["mqtt_connected"] = False
            print("[MQTT] ⚠️ MQTT连接断开，尝试重连...")
            return init_mqtt()
    else:
        return init_mqtt()

# ==================== 传感器数据采集 ====================
def read_sensors():
    """读取所有传感器数据"""
    global sensor_data
    
    try:
        # 读取温湿度传感器 (模拟数据，实际需要DHT22库)
        # 这里使用模拟数据，实际项目中需要安装DHT22 MicroPython库
        sensor_data["temperature"] = 25.0 + (machine.rng() % 100) / 10.0 - 5.0
        sensor_data["humidity"] = 55.0 + (machine.rng() % 100) / 10.0 - 5.0
        
        # 读取光照传感器 (ADC值转换为lux)
        adc_value = light_adc.read()
        # 将ADC值(0-1023)转换为光照强度(0-1000 lux)
        sensor_data["light"] = (adc_value / 1023.0) * 1000.0
        
        # 读取火焰传感器 (数字输入，低电平表示检测到火焰)
        flame_detected = not flame_pin.value()  # 取反，因为是低电平触发
        sensor_data["flame"] = flame_detected
        device_status["flame_detected"] = flame_detected
        
        # 更新最后读取时间
        device_status["last_sensor_time"] = time.ticks_ms()
        
        return sensor_data
        
    except Exception as e:
        print(f"[SENSOR] ❌ 传感器读取失败: {e}")
        return None

def publish_sensor_data():
    """发布传感器数据"""
    if not device_status["mqtt_connected"]:
        print("[PUBLISH] ⚠️ MQTT未连接，跳过数据发送")
        return False
    
    try:
        # 读取传感器数据
        data = read_sensors()
        if data is None:
            return False
        
        # 构造发送数据
        payload = {
            "deviceId": MQTT_CLIENT_ID,
            "temperature": round(data["temperature"], 2),
            "humidity": round(data["humidity"], 2),
            "light": round(data["light"], 2),
            "flame": data["flame"],
            "rgbStatus": device_status["rgb_status"],
            "buzzerStatus": device_status["buzzer_status"],
            "timestamp": time.ticks_ms()
        }
        
        # 发送数据
        json_data = json.dumps(payload)
        mqtt_client.publish(TOPIC_SENSOR_DATA, json_data)
        
        print(f"[PUBLISH] 📤 传感器数据已发送:")
        print(f"  温度: {payload['temperature']}°C")
        print(f"  湿度: {payload['humidity']}%")
        print(f"  光照: {payload['light']} lux")
        print(f"  火焰: {'🔥 检测到' if payload['flame'] else '✅ 正常'}")
        print(f"  RGB: {'💡 开启' if payload['rgbStatus'] else '🌑 关闭'}")
        
        return True
        
    except Exception as e:
        print(f"[PUBLISH] ❌ 发送传感器数据失败: {e}")
        return False

def publish_alarm(alarm_type, level, message):
    """发布告警消息"""
    if not device_status["mqtt_connected"]:
        return False
    
    try:
        alarm_data = {
            "deviceId": MQTT_CLIENT_ID,
            "alarmType": alarm_type,
            "level": level,
            "message": message,
            "location": "智慧办公楼-W601设备",
            "timestamp": time.ticks_ms()
        }
        
        json_data = json.dumps(alarm_data)
        mqtt_client.publish(TOPIC_ALARM, json_data)
        
        print(f"[ALARM] 🚨 告警已发送: {message}")
        return True
        
    except Exception as e:
        print(f"[ALARM] ❌ 发送告警失败: {e}")
        return False

def publish_device_status(status):
    """发布设备状态"""
    if not device_status["mqtt_connected"]:
        return False
    
    try:
        status_data = {
            "deviceId": MQTT_CLIENT_ID,
            "status": status,
            "ip": wifi.ifconfig()[0] if wifi and wifi.isconnected() else "",
            "rssi": wifi.status('rssi') if wifi and wifi.isconnected() else 0,
            "freeMemory": gc.mem_free(),
            "timestamp": time.ticks_ms()
        }
        
        json_data = json.dumps(status_data)
        mqtt_client.publish(TOPIC_STATUS, json_data)
        
        print(f"[STATUS] 📊 设备状态已发送: {status}")
        return True
        
    except Exception as e:
        print(f"[STATUS] ❌ 发送设备状态失败: {e}")
        return False

# ==================== 设备控制函数 ====================
def set_rgb_on():
    """开启RGB灯 (白色)"""
    global rgb_red, rgb_green, rgb_blue
    rgb_red.duty(512)
    rgb_green.duty(512)
    rgb_blue.duty(512)
    device_status["rgb_status"] = True

def set_rgb_off():
    """关闭RGB灯"""
    global rgb_red, rgb_green, rgb_blue
    rgb_red.duty(0)
    rgb_green.duty(0)
    rgb_blue.duty(0)
    device_status["rgb_status"] = False

def set_rgb_color(red, green, blue):
    """设置RGB灯颜色 (0-1023)"""
    global rgb_red, rgb_green, rgb_blue
    rgb_red.duty(red)
    rgb_green.duty(green)
    rgb_blue.duty(blue)
    device_status["rgb_status"] = (red > 0 or green > 0 or blue > 0)

def set_buzzer_on():
    """开启蜂鸣器"""
    buzzer.value(1)
    device_status["buzzer_status"] = True

def set_buzzer_off():
    """关闭蜂鸣器"""
    buzzer.value(0)
    device_status["buzzer_status"] = False

# ==================== 自动控制逻辑 ====================
def handle_config_update(msg_str):
    """处理配置更新消息"""
    try:
        config_data = json.loads(msg_str)
        print(f"[CONFIG] 📥 收到配置更新: {config_data}")
        
        # 处理数据采集间隔配置
        if "data.collect.interval" in config_data:
            interval_seconds = int(config_data["data.collect.interval"])
            interval_ms = interval_seconds * 1000
            config_params["data_collect_interval"] = interval_ms
            print(f"[CONFIG] ✅ 更新数据采集间隔: {interval_seconds}秒 ({interval_ms}毫秒)")
            
            # 重新启动定时器以应用新的间隔
            restart_timer()
        
        # 可以在这里添加其他配置项的处理
        
    except Exception as e:
        print(f"[CONFIG] ❌ 配置更新处理异常: {e}")

def restart_timer():
    """重新启动定时器以应用新的配置"""
    global system_timer
    try:
        if system_timer:
            system_timer.deinit()
            print("[TIMER] 🔄 停止旧定时器")
        
        # 使用新的间隔创建定时器
        interval_ms = config_params["data_collect_interval"]
        system_timer = Timer(0)
        system_timer.init(period=interval_ms, mode=Timer.PERIODIC, callback=timer_callback)
        print(f"[TIMER] ✅ 启动新定时器，间隔: {interval_ms}毫秒")
        
    except Exception as e:
        print(f"[TIMER] ❌ 定时器重启异常: {e}")

def check_flame_alarm():
    """检查火焰告警 - 优化为更快响应"""
    if sensor_data["flame"]:
        # 立即响应：开启蜂鸣器和红色RGB灯
        set_buzzer_on()
        set_rgb_color(1023, 0, 0)  # 红色告警
        
        # 发送告警消息
        publish_alarm("FIRE", "CRITICAL", "🚨 检测到火焰！立即疏散！")
        
        print("[ALARM] 🚨 火焰告警触发！立即响应")
        return True
    else:
        # 火焰消失，关闭告警
        if device_status["buzzer_status"]:
            set_buzzer_off()
            set_rgb_off()
            print("[ALARM] ✅ 火焰告警解除")
        return False

def auto_lighting_control():
    """自动照明控制"""
    light_level = sensor_data["light"]
    
    # 光照不足时自动开灯
    if light_level < 300 and not device_status["rgb_status"]:
        set_rgb_on()
        print(f"[AUTO] 💡 光照不足({light_level:.1f} lux)，自动开启照明")
        
    # 光照充足时自动关灯
    elif light_level > 350 and device_status["rgb_status"] and not device_status["flame_detected"]:
        set_rgb_off()
        print(f"[AUTO] 🌞 光照充足({light_level:.1f} lux)，自动关闭照明")

def monitor_system_status():
    """系统状态监控"""
    try:
        # 内存使用监控
        free_mem = gc.mem_free()
        if free_mem < 10000:  # 小于10KB时告警
            print(f"[MONITOR] ⚠️ 内存不足: {free_mem} bytes")
            gc.collect()  # 强制垃圾回收
        
        # 发送心跳
        publish_device_status("running")
        
        print(f"[MONITOR] 💓 系统心跳 - 空闲内存: {free_mem} bytes")
        
    except Exception as e:
        print(f"[MONITOR] ❌ 系统监控异常: {e}")

# ==================== 定时器回调 ====================
def timer_callback(timer):
    """定时器回调函数"""
    try:
        # 检查WiFi连接
        check_wifi_connection()
        
        # 检查MQTT连接
        check_mqtt_connection()
        
        # 发布传感器数据
        publish_sensor_data()
        
        # 检查火焰告警
        check_flame_alarm()
        
        # 自动照明控制
        if not device_status["flame_detected"]:
            auto_lighting_control()
        
        # 系统状态监控 (每5次执行一次)
        if time.ticks_ms() % 150000 < 30000:  # 大约每150秒执行一次
            monitor_system_status()
            
    except Exception as e:
        print(f"[TIMER] ❌ 定时器回调异常: {e}")

# ==================== 主程序 ====================
def main():
    """主程序入口"""
    global system_timer
    
    print("=" * 50)
    print("W601智慧办公楼系统 - MicroPython版本")
    print("基于RT-Thread MicroPython固件")
    print("=" * 50)
    
    try:
        # 1. 初始化硬件
        init_hardware()
        time.sleep(1)
        
        # 2. 连接WiFi
        if not init_wifi():
            print("[MAIN] ❌ WiFi连接失败，程序退出")
            return
        
        time.sleep(2)
        
        # 3. 连接MQTT服务器
        if not init_mqtt():
            print("[MAIN] ❌ MQTT连接失败，程序退出")
            return
        
        time.sleep(1)
        
        # 4. 启动定时器 (使用配置的间隔)
        interval_ms = config_params["data_collect_interval"]
        system_timer = Timer(0)
        system_timer.init(period=interval_ms, mode=Timer.PERIODIC, callback=timer_callback)
        
        print("[MAIN] 🚀 系统启动完成，开始运行...")
        print(f"[MAIN] 📊 数据上报间隔: {interval_ms/1000}秒")
        print("[MAIN] ⏸️ 按 Ctrl+C 停止程序")
        
        # 5. 主循环
        while True:
            try:
                # 处理MQTT消息
                if device_status["mqtt_connected"]:
                    mqtt_client.check_msg()
                
                # 短暂延时
                time.sleep(0.1)
                
            except KeyboardInterrupt:
                print("\n[MAIN] 👋 程序被用户中断")
                break
            except Exception as e:
                print(f"[MAIN] ❌ 主循环异常: {e}")
                time.sleep(1)
                
    except Exception as e:
        print(f"[MAIN] ❌ 程序异常: {e}")
        
    finally:
        # 清理资源
        cleanup()

def cleanup():
    """清理资源"""
    global system_timer, mqtt_client
    
    print("[CLEANUP] 正在清理资源...")
    
    try:
        # 停止定时器
        if system_timer:
            system_timer.deinit()
        
        # 关闭设备
        set_rgb_off()
        set_buzzer_off()
        
        # 发送离线状态
        if device_status["mqtt_connected"]:
            publish_device_status("offline")
        
        # 断开MQTT连接
        if mqtt_client:
            mqtt_client.disconnect()
        
        print("[CLEANUP] ✅ 资源清理完成")
        
    except Exception as e:
        print(f"[CLEANUP] ❌ 清理资源失败: {e}")

# ==================== 程序入口 ====================
if __name__ == "__main__":
    main()