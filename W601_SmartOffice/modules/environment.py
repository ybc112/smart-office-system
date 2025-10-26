import utime as time
from config import (
    ENVIRONMENT_PERIOD_MS,
    SENSOR_DATA_TOPIC,
    DEVICE_ID,
    DEBUG,
)

TEMP_HEAT_THRESHOLD = 18.0
TEMP_COOL_THRESHOLD = 28.0
HUMI_HUMIDIFY_THRESHOLD = 40.0


class EnvironmentModule:
    def __init__(self, aht10_sensor, mqtt):
        self.sensor = aht10_sensor
        self.mqtt = mqtt
        self.last_run = 0
        self.last_temp = None
        self.last_humi = None
        self.heat_on = False
        self.cool_on = False
        self.humid_on = False

    def _publish_state(self):
        # 停止环境模块的独立上报，改由汇总模块统一上报
        pass

    def step(self):
        try:
            t, h = self.sensor.read()
            self.last_temp = t
            self.last_humi = h
        except Exception as e:
            if DEBUG:
                print("Environment read error:", e)
            return
        # 移除本地加热/制冷/加湿的自动逻辑，交由系统统一控制
        self.heat_on = False
        self.cool_on = False
        self.humid_on = False

    def update(self, now_ms=None):
        now = now_ms if now_ms is not None else time.ticks_ms()
        if time.ticks_diff(now, self.last_run) >= ENVIRONMENT_PERIOD_MS:
            self.last_run = now
            self.step()