import utime as time
from config import (
    SENSOR_REPORT_PERIOD_MS,
    SENSOR_DATA_TOPIC,
    DEVICE_ID,
    DEBUG,
)


class SensorReporterModule:
    def __init__(self, light_sensor, env_sensor, flame_sensor, rgb_led, mqtt, config_handler=None):
        self.light = light_sensor
        self.env = env_sensor
        self.flame = flame_sensor
        self.led = rgb_led
        self.mqtt = mqtt
        self.config_handler = config_handler
        self.last_run = 0

    def _read_light(self):
        try:
            return self.light.read_light()
        except Exception as e:
            if DEBUG:
                print("Reporter light read error:", e)
            return None

    def _read_env(self):
        try:
            t, h = self.env.read()
            return t, h
        except Exception as e:
            if DEBUG:
                print("Reporter env read error:", e)
            return None, None

    def _read_flame(self):
        try:
            return self.flame.detected()
        except Exception as e:
            if DEBUG:
                print("Reporter flame read error:", e)
            return False

    def publish(self):
        lux = self._read_light()
        t, h = self._read_env()
        flame = self._read_flame()
        payload = {
            "deviceId": DEVICE_ID,
            "light": 0 if lux is None else lux,
            "temperature": 0 if t is None else t,
            "humidity": 0 if h is None else h,
            "flame": bool(flame),
            "rgbStatus": bool(self.led.is_on()),
            "timestamp": time.ticks_ms(),
        }
        
        if DEBUG:
            print("=== Sensor Data Report ===")
            print("Light:", lux, "lux")
            print("Temperature:", t, "°C")
            print("Humidity:", h, "%")
            print("Flame detected:", flame)
            print("LED status:", self.led.is_on())
            print("Publishing to topic:", SENSOR_DATA_TOPIC.decode())
        
        try:
            self.mqtt.publish_json(SENSOR_DATA_TOPIC, payload)
            if DEBUG:
                print("✓ Sensor data published successfully")
        except Exception as e:
            if DEBUG:
                print("✗ Reporter publish error:", e)

    def update(self, now_ms=None):
        now = now_ms if now_ms is not None else time.ticks_ms()
        
        # 获取动态配置的上报间隔
        report_interval = SENSOR_REPORT_PERIOD_MS
        if self.config_handler:
            report_interval = self.config_handler.get_data_collect_interval()
        
        if time.ticks_diff(now, self.last_run) >= report_interval:
            self.last_run = now
            self.publish()