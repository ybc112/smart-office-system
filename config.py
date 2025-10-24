#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
W601 Smart Office Building System - Configuration File
Based on RT-Thread MicroPython Firmware
"""

# ==================== Configuration Parameters ====================
# WiFi配置 - 请根据您的实际WiFi网络修改
WIFI_SSID = "SmartOffice_WiFi"  # 修改为您的WiFi名称
WIFI_PASSWORD = "smartoffice123"  # 修改为您的WiFi密码

# MQTT服务器配置 - 根据您的部署环境修改
MQTT_SERVER = "localhost"  # 本地部署使用localhost，远程部署请修改为服务器IP
MQTT_PORT = 1883
MQTT_CLIENT_ID = "W601_001"  # 设备唯一标识，多设备时需要不同的ID
MQTT_USERNAME = ""  # EMQ X默认不需要用户名密码
MQTT_PASSWORD = ""  # EMQ X默认不需要用户名密码
AUTO_RUN = False  # Set to False during development/upload to prevent auto-run

# MQTT主题配置 - 与后端服务保持一致
TOPIC_SENSOR_DATA = "office/sensor/data"  # 传感器数据上报主题
TOPIC_CONTROL_CMD = "office/control/cmd"  # 设备控制命令主题
TOPIC_ALARM = "office/alarm"  # 告警消息主题
TOPIC_STATUS = "office/device/status"  # 设备状态主题
TOPIC_DEVICE_STATUS = "office/device/status"

# 硬件引脚配置 - W601开发板引脚定义
# I2C配置 (用于DHT22温湿度传感器)
I2C_SCL_PIN = 1  # I2C时钟线 (PA1)
I2C_SDA_PIN = 0  # I2C数据线 (PA0)

# 外部传感器引脚
LIGHT_SENSOR_PIN = 23  # 光照传感器ADC引脚 (PB23)
FLAME_SENSOR_PIN = 4   # 火焰传感器数字引脚 (PA4)

# 板载组件引脚
RGB_RED_PIN = 13    # RGB红色LED (PA13)
RGB_GREEN_PIN = 14  # RGB绿色LED (PA14)  
RGB_BLUE_PIN = 15   # RGB蓝色LED (PA15)
BUZZER_PIN = 16     # 蜂鸣器 (PB16)

# 板载按键引脚
USER_KEY_PIN = 23   # 用户按键 (PB23)
RESET_KEY_PIN = 22  # 复位按键 (PB22)

# 系统参数配置
# 传感器数据采集配置
SENSOR_READ_INTERVAL = 30  # 传感器数据读取间隔(秒) - 建议30秒避免频繁采集
TIMER_PERIOD = 10000  # 定时器周期(毫秒) - 10秒检查一次系统状态

# 环境阈值配置 - 根据办公环境调整
LIGHT_THRESHOLD_LOW = 200   # 光照阈值下限(lux) - 低于此值开启照明
LIGHT_THRESHOLD_HIGH = 800  # 光照阈值上限(lux) - 高于此值关闭照明
TEMP_THRESHOLD_LOW = 18.0   # 温度阈值下限(°C) - 低于此值告警
TEMP_THRESHOLD_HIGH = 30.0  # 温度阈值上限(°C) - 高于此值告警
HUMIDITY_THRESHOLD_LOW = 30.0   # 湿度阈值下限(%) - 低于此值告警
HUMIDITY_THRESHOLD_HIGH = 80.0  # 湿度阈值上限(%) - 高于此值告警

# 设备控制配置
AUTO_LIGHTING = True  # 是否启用自动照明控制
FLAME_ALARM_ENABLED = True  # 是否启用火焰告警
BUZZER_DURATION = 5  # 蜂鸣器响铃持续时间(秒)