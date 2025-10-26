import utime as time
from config import (
    FIRE_ALARM_PERIOD_MS,
    ALARM_TOPIC,
    DEVICE_ID,
    DEBUG,
)


class FireAlarmModule:
    def __init__(self, flame_sensor, rgb_led, mqtt):
        self.sensor = flame_sensor
        self.led = rgb_led
        self.mqtt = mqtt
        self.last_run = 0
        self.last_flame = False

    def _publish_alarm(self, flame):
        if not flame:
            return
        try:
            self.mqtt.publish_json(ALARM_TOPIC, {
                "deviceId": DEVICE_ID,
                "alarmType": "FIRE",
                "level": "CRITICAL",
                "message": "检测到火焰！",
                "timestamp": time.ticks_ms(),
            })
        except Exception as e:
            if DEBUG:
                print("FireAlarm publish error:", e)

    def step(self):
        try:
            flame = self.sensor.detected()
        except Exception as e:
            if DEBUG:
                print("FireAlarm read error:", e)
            return
        if flame:
            # Local warning: blink red
            self.led.blink_red(times=1, interval_ms=150)
        if flame != self.last_flame:
            self.last_flame = flame
            self._publish_alarm(flame)

    def update(self, now_ms=None):
        now = now_ms if now_ms is not None else time.ticks_ms()
        if time.ticks_diff(now, self.last_run) >= FIRE_ALARM_PERIOD_MS:
            self.last_run = now
            self.step()