from machine import Pin
from config import FLAME_SENSOR_PIN


class FlameSensor:
    def __init__(self, pin_num=FLAME_SENSOR_PIN):
        # Use official W601 Pin initialization format: (name, number)
        # PB10 for flame sensor
        self.pin = Pin(("PB10", pin_num), Pin.IN, Pin.PULL_UP)

    def detected(self):
        try:
            # 常见火焰模块在检测到火焰时输出低电平
            return self.pin.value() == 0
        except Exception:
            return False

    def value(self):
        try:
            return self.pin.value()
        except Exception:
            return 1