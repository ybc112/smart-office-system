import utime as time
import ujson
from config import (
    CONFIG_UPDATE_TOPIC,
    DEBUG,
)


class ConfigHandlerModule:
    """配置处理模块，负责接收和应用配置更新"""
    
    def __init__(self, mqtt_client, setup_callback=True):
        self.mqtt = mqtt_client
        self.config_params = {
            "data_collect_interval": 10000  # 默认10秒，单位毫秒
        }
        if setup_callback:
            self._setup_mqtt_callback()
    
    def _setup_mqtt_callback(self):
        """设置MQTT消息回调"""
        original_callback = self.mqtt._callback
        
        def config_callback(topic, msg):
            # 处理配置更新消息
            if topic == CONFIG_UPDATE_TOPIC:
                self._handle_config_update(msg)
            # 调用原始回调处理其他消息
            elif original_callback:
                original_callback(topic, msg)
        
        self.mqtt.set_callback(config_callback)
    
    def _handle_config_update(self, msg):
        """处理配置更新消息"""
        try:
            config_data = ujson.loads(msg.decode())
            if DEBUG:
                print("[CONFIG] 收到配置更新:", config_data)
            
            # 处理数据采集间隔配置
            if "data.collect.interval" in config_data:
                interval_seconds = int(config_data["data.collect.interval"])
                interval_ms = interval_seconds * 1000
                self.config_params["data_collect_interval"] = interval_ms
                if DEBUG:
                    print("[CONFIG] 更新数据采集间隔: {}秒 ({}毫秒)".format(interval_seconds, interval_ms))
            
            # 可以在这里添加其他配置项的处理
            
        except Exception as e:
            if DEBUG:
                print("[CONFIG] 配置更新处理异常:", e)
    
    def subscribe_config_updates(self):
        """订阅配置更新主题"""
        try:
            self.mqtt.subscribe(CONFIG_UPDATE_TOPIC)
            if DEBUG:
                print("[CONFIG] 订阅配置更新主题:", CONFIG_UPDATE_TOPIC.decode())
        except Exception as e:
            if DEBUG:
                print("[CONFIG] 订阅配置主题失败:", e)
    
    def get_data_collect_interval(self):
        """获取当前数据采集间隔（毫秒）"""
        return self.config_params["data_collect_interval"]