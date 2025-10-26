import utime as time
from machine import Pin
from config import BUZZER_PIN


class Buzzer:
    def __init__(self):
        # Use official W601 Pin initialization format: (name, number)
        # PB15 for buzzer
        self.pin = Pin(("PB15", BUZZER_PIN), Pin.OUT_PP)
        self.off()

    def on(self):
        try:
            self.pin.value(1)
        except Exception:
            pass

    def off(self):
        try:
            self.pin.value(0)
        except Exception:
            pass

    def beep(self, duration_ms=500):
        self.on()
        time.sleep_ms(duration_ms)
        self.off()