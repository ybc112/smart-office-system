import utime as time
from machine import Pin
from config import LED_R_PIN, LED_G_PIN, LED_B_PIN

LED_ON  = 0
LED_OFF = 1


class RGBLed:
    def __init__(self):
        # Use official W601 Pin initialization format: (name, number)
        self.r = Pin(("PA13", LED_R_PIN), Pin.OUT_PP)  # PA13 for LED_R
        self.g = Pin(("PA14", LED_G_PIN), Pin.OUT_PP)  # PA14 for LED_G
        self.b = Pin(("PA15", LED_B_PIN), Pin.OUT_PP)  # PA15 for LED_B
        self.off()

    def set_raw(self, r, g, b):
        self.r.value(r)
        self.g.value(g)
        self.b.value(b)

    def off(self):
        self.set_raw(LED_OFF, LED_OFF, LED_OFF)

    def on(self, rr=255, gg=255, bb=255):
        # Treat non-zero as ON for each channel
        r = LED_ON if rr else LED_OFF
        g = LED_ON if gg else LED_OFF
        b = LED_ON if bb else LED_OFF
        self.set_raw(r, g, b)

    def white(self):
        self.on(255, 255, 255)

    def red(self):
        self.on(255, 0, 0)

    def blink_red(self, times=3, interval_ms=200):
        for _ in range(times):
            self.red()
            time.sleep_ms(interval_ms)
            self.off()
            time.sleep_ms(interval_ms)

    def is_on(self):
        return (self.r.value() == LED_ON) or (self.g.value() == LED_ON) or (self.b.value() == LED_ON)