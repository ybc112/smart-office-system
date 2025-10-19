#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
W601虚拟设备模拟器
模拟W601开发板的传感器数据上报和控制命令接收
"""

import json
import time
import random
from datetime import datetime
import paho.mqtt.client as mqtt

class W601Simulator:
    def __init__(self, device_id="W601_001", broker="localhost", port=1883):
        """
        初始化模拟器
        :param device_id: 设备ID
        :param broker: MQTT服务器地址
        :param port: MQTT服务器端口
        """
        self.device_id = device_id
        self.broker = broker
        self.port = port

        # 设备状态
        self.rgb_status = False  # RGB灯状态
        self.buzzer_status = False  # 蜂鸣器状态
        self.flame_detected = False  # 火焰检测状态（可手动切换测试）

        # 连接状态标志
        self.connected = False

        # MQTT客户端
        self.client = mqtt.Client(client_id=f"W601_Simulator_{device_id}")
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        self.client.on_disconnect = self.on_disconnect

        # 数据上报间隔（秒）
        self.report_interval = 30

    def on_connect(self, client, userdata, flags, rc):
        """MQTT连接回调"""
        if rc == 0:
            self.connected = True
            print(f"[{self.get_time()}] ✅ 成功连接到MQTT服务器")
            # 订阅控制命令主题
            self.client.subscribe("office/control/cmd")
            print(f"[{self.get_time()}] 📡 已订阅控制命令主题")
        else:
            self.connected = False
            print(f"[{self.get_time()}] ❌ 连接失败，错误码: {rc}")

    def on_disconnect(self, client, userdata, rc):
        """MQTT断开连接回调"""
        self.connected = False
        if rc != 0:
            print(f"[{self.get_time()}] ⚠️  与MQTT服务器断开连接，错误码: {rc}")

    def on_message(self, client, userdata, msg):
        """MQTT消息接收回调"""
        try:
            payload = msg.payload.decode('utf-8')
            data = json.loads(payload)

            print(f"\n[{self.get_time()}] 📥 收到控制命令:")
            print(f"  Device ID: {data.get('deviceId')}")
            print(f"  Action: {data.get('action')}")

            # 只处理发给当前设备的命令
            if data.get('deviceId') == self.device_id:
                action = data.get('action')

                if action == 'rgb_on':
                    self.rgb_status = True
                    print(f"[{self.get_time()}] 💡 RGB灯已开启")
                elif action == 'rgb_off':
                    self.rgb_status = False
                    print(f"[{self.get_time()}] 🌑 RGB灯已关闭")
                elif action == 'buzzer_on':
                    self.buzzer_status = True
                    print(f"[{self.get_time()}] 🔔 蜂鸣器已开启")
                elif action == 'buzzer_off':
                    self.buzzer_status = False
                    print(f"[{self.get_time()}] 🔕 蜂鸣器已关闭")

        except Exception as e:
            print(f"[{self.get_time()}] ❌ 处理控制命令失败: {e}")

    def generate_sensor_data(self):
        """生成模拟的传感器数据"""
        current_hour = datetime.now().hour

        # 根据时间模拟光照强度
        if 8 <= current_hour <= 18:
            # 白天：300-800 lux
            light = random.uniform(300, 800)
        else:
            # 夜晚：50-250 lux
            light = random.uniform(50, 250)

        # 模拟温度：基准25℃，上下波动±3℃
        temperature = 25 + random.uniform(-3, 3)

        # 模拟湿度：基准55%，上下波动±10%
        humidity = 55 + random.uniform(-10, 10)

        # 火焰检测（可手动修改self.flame_detected来测试告警）
        flame = self.flame_detected

        return {
            "deviceId": self.device_id,
            "light": round(light, 2),
            "temperature": round(temperature, 2),
            "humidity": round(humidity, 2),
            "flame": flame,
            "rgbStatus": self.rgb_status,
            "timestamp": int(time.time() * 1000)
        }

    def publish_sensor_data(self):
        """发布传感器数据"""
        if not self.connected:
            print(f"[{self.get_time()}] ⚠️  MQTT未连接，跳过本次发送")
            return

        data = self.generate_sensor_data()
        payload = json.dumps(data)

        result = self.client.publish("office/sensor/data", payload, qos=1)

        if result.rc == mqtt.MQTT_ERR_SUCCESS:
            print(f"\n[{self.get_time()}] 📤 发送传感器数据:")
            print(f"  光照: {data['light']} lux")
            print(f"  温度: {data['temperature']} ℃")
            print(f"  湿度: {data['humidity']} %")
            print(f"  火焰: {'🔥 检测到' if data['flame'] else '✅ 正常'}")
            print(f"  RGB灯: {'💡 开启' if data['rgbStatus'] else '🌑 关闭'}")
        else:
            print(f"[{self.get_time()}] ❌ 发送失败，返回码: {result.rc}")

    def publish_alarm(self, alarm_type, level, message):
        """发布告警消息"""
        alarm_data = {
            "deviceId": self.device_id,
            "alarmType": alarm_type,
            "level": level,
            "message": message,
            "timestamp": int(time.time() * 1000)
        }
        payload = json.dumps(alarm_data)
        self.client.publish("office/alarm", payload, qos=1)
        print(f"[{self.get_time()}] 🚨 发送告警: {message}")

    def run(self):
        """启动模拟器"""
        try:
            print("=" * 60)
            print(f"W601虚拟设备模拟器")
            print(f"设备ID: {self.device_id}")
            print(f"MQTT服务器: {self.broker}:{self.port}")
            print("=" * 60)

            # 连接MQTT服务器
            self.client.connect(self.broker, self.port, 60)
            self.client.loop_start()

            # 等待连接成功
            timeout = 10
            for i in range(timeout):
                if self.connected:
                    break
                time.sleep(1)

            if not self.connected:
                print(f"[{self.get_time()}] ❌ 连接超时，请检查MQTT服务器")
                return

            print(f"\n[{self.get_time()}] 🚀 模拟器已启动，每{self.report_interval}秒上报一次数据")
            print(f"[{self.get_time()}] 💡 提示: 修改代码中的 flame_detected 为 True 可测试火灾告警")
            print(f"[{self.get_time()}] ⏸️  按 Ctrl+C 停止模拟器\n")

            # 定时发送传感器数据
            while True:
                self.publish_sensor_data()
                time.sleep(self.report_interval)

        except KeyboardInterrupt:
            print(f"\n[{self.get_time()}] 👋 模拟器已停止")
        except Exception as e:
            print(f"\n[{self.get_time()}] ❌ 错误: {e}")
            import traceback
            traceback.print_exc()
        finally:
            self.client.loop_stop()
            self.client.disconnect()

    @staticmethod
    def get_time():
        """获取当前时间字符串"""
        return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


if __name__ == "__main__":
    # 创建模拟器实例
    # 可以修改参数来模拟不同的设备
    simulator = W601Simulator(
        device_id="W601_001",
        broker="localhost",
        port=1883
    )

    # 启动模拟器
    simulator.run()
