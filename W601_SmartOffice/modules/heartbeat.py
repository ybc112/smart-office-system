import utime as time
from config import (
    HEARTBEAT_PERIOD_MS,
    DEVICE_STATUS_TOPIC,
    DEVICE_ID,
    DEBUG,
)


class HeartbeatModule:
    def __init__(self, wifi_mgr, mqtt):
        self.wifi = wifi_mgr
        self.mqtt = mqtt
        self.start_ms = time.ticks_ms()
        self.last_run = 0

    def _uptime_ms(self):
        return time.ticks_diff(time.ticks_ms(), self.start_ms)

    def publish(self):
        payload = {
            "deviceId": DEVICE_ID,
            "status": "ONLINE" if self.wifi.is_connected() else "OFFLINE",
            "ip": self.wifi.ip(),
            "uptimeMs": self._uptime_ms(),
            "timestamp": time.ticks_ms(),
        }
        try:
            self.mqtt.publish_json(DEVICE_STATUS_TOPIC, payload)
        except Exception as e:
            if DEBUG:
                print("Heartbeat publish error:", e)

    def update(self, now_ms=None):
        now = now_ms if now_ms is not None else time.ticks_ms()
        if time.ticks_diff(now, self.last_run) >= HEARTBEAT_PERIOD_MS:
            self.last_run = now
            self.publish()