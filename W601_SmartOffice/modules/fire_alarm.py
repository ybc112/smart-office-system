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
            # 立即响应：闪烁红灯并触发蜂鸣器
            self.led.blink_red(times=3, interval_ms=100)  # 更快更明显的警告
            if DEBUG:
                print("🚨 火焰检测到！立即响应")
        if flame != self.last_flame:
            self.last_flame = flame
            self._publish_alarm(flame)
            if flame and DEBUG:
                print("🔥 火焰告警已发送到服务器")

    def update(self, now_ms=None):
        now = now_ms if now_ms is not None else time.ticks_ms()
        if time.ticks_diff(now, self.last_run) >= FIRE_ALARM_PERIOD_MS:
            self.last_run = now
            self.step()