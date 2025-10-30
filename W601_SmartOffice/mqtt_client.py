import utime as time
import ujson
from umqtt.simple import MQTTClient
from config import (
    MQTT_CLIENT_ID,
    MQTT_SERVER,
    MQTT_PORT,
    MQTT_KEEPALIVE,
    MQTT_RETRY_MS,
    DEBUG,
    MQTT_USERNAME,
    MQTT_PASSWORD,
)


class MqttClient:
    def __init__(self, client_id=MQTT_CLIENT_ID, server=MQTT_SERVER, port=MQTT_PORT, keepalive=MQTT_KEEPALIVE):
        self.client_id = client_id
        self.server = server
        self.port = port
        self.keepalive = keepalive
        self._client = None
        self._connected = False
        self._callback = None
        self._subscriptions = []

    def set_callback(self, cb):
        self._callback = cb
        if self._client:
            self._client.set_callback(cb)

    def connect(self):
        try:
            self._client = MQTTClient(
                self.client_id,
                self.server,
                port=self.port,
                user=MQTT_USERNAME or None,
                password=MQTT_PASSWORD or None,
                keepalive=self.keepalive,
            )
            if self._callback:
                self._client.set_callback(self._callback)
            self._client.connect()
            self._connected = True
            if DEBUG:
                print("MQTT connected")
            # resubscribe if needed
            for t in self._subscriptions:
                try:
                    self._client.subscribe(t)
                except Exception:
                    pass
            return True
        except Exception as e:
            self._connected = False
            if DEBUG:
                print("MQTT connect error:", e)
            return False

    def ensure_connected(self):
        if self._connected:
            return True
        ok = self.connect()
        if not ok:
            time.sleep_ms(MQTT_RETRY_MS)
        return ok

    def disconnect(self):
        try:
            if self._client:
                self._client.disconnect()
        except Exception:
            pass
        self._connected = False

    def is_connected(self):
        return self._connected

    def subscribe(self, topic_bytes):
        # Avoid blocking subscribe when not connected
        if not self._connected or not self._client:
            if topic_bytes not in self._subscriptions:
                self._subscriptions.append(topic_bytes)
            if DEBUG:
                print("MQTT not connected; postponed subscribe")
            return
        try:
            self._client.subscribe(topic_bytes)
            if topic_bytes not in self._subscriptions:
                self._subscriptions.append(topic_bytes)
        except Exception as e:
            if DEBUG:
                print("MQTT subscribe error:", e)
            self._connected = False

    def publish(self, topic_bytes, payload_bytes):
        try:
            if self._client:
                self._client.publish(topic_bytes, payload_bytes)
        except Exception as e:
            if DEBUG:
                print("MQTT publish error:", e)
            self._connected = False  # force reconnect on next ensure

    def publish_json(self, topic_bytes, obj):
        try:
            payload = ujson.dumps(obj).encode('utf-8')
            self.publish(topic_bytes, payload)
        except Exception as e:
            if DEBUG:
                print("MQTT publish_json error:", e)

    def check_msg(self):
        # Non-blocking check for incoming messages
        try:
            if self._client:
                self._client.check_msg()
        except Exception as e:
            if DEBUG:
                print("MQTT check_msg error:", e)
            self._connected = False