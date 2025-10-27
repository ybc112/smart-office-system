# W601 Smart Office - Main
import utime as time
import ujson
from machine import I2C, Pin

from config import (
    MQTT_CLIENT_ID,
    I2C1_SCL, I2C_SDA,
    CONTROL_TOPIC,
    DEBUG,
)

from wifi_manager import WiFiManager
from mqtt_client import MqttClient

from sensors.aht10 import AHT10
from sensors.light_adc import LightADC
from sensors.flame import FlameSensor

from devices.rgb_led import RGBLed
from devices.buzzer import Buzzer

from modules.fire_alarm import FireAlarmModule
from modules.heartbeat import HeartbeatModule
from modules.reporter import SensorReporterModule
from modules.config_handler import ConfigHandlerModule


def setup_i2c():
    """Initialize I2C with proper W601 pin configuration"""
    try:
        # Use official W601 Pin initialization format: (name, number)
        scl = Pin(("clk", I2C1_SCL), Pin.OUT_OD)  # I2C1_SCL
        sda = Pin(("sda", I2C_SDA), Pin.OUT_OD)  # I2C_SDA
        i2c = I2C(-1, scl, sda, freq=100000)
        print("I2C initialized (id=-1 SoftI2C, 100kHz)")
        return i2c
    except Exception as e:
        print("I2C initialization failed:", e)
        return None


def main():
    # Hardware buses
    i2c = setup_i2c()

    # Sensors
    aht = AHT10(i2c)
    light = LightADC()

    def _safe_init(sensor, name, i2c, addr=None):
        try:
            if addr is not None:
                devs = i2c.scan() if i2c else []
                if DEBUG:
                    print("%s scan: %s" % (name, devs))
                if addr not in devs:
                    raise OSError("%s not found on bus (0x%02x)" % (name, addr))
            sensor.init() if hasattr(sensor, 'init') else None
        except OSError as e:
            if e.args and e.args[0] == 110:
                print("%s init timeout [Errno 110]. Check wiring/pull-ups/SCL/SDA." % name)
            else:
                if DEBUG:
                    print("%s init error: %s" % (name, e))

    _safe_init(aht, "AHT10", i2c, 0x38)
    # LightADC is ADC-only; no I2C scan needed

    flame = FlameSensor()

    # Devices
    led = RGBLed()
    buzzer = Buzzer()

    # WiFi & MQTT
    wifi = WiFiManager()
    if wifi.ensure_connected():
        ip = wifi.ip()
        print("WiFi connected, IP:", ip)
    else:
        print("WiFi connection failed")

    mqtt = MqttClient()

    # 创建配置处理模块
    config_handler = ConfigHandlerModule(mqtt)

    def on_message(topic, msg):
        try:
            cmd = ujson.loads(msg)
            if cmd.get("deviceId") != MQTT_CLIENT_ID:
                return
            action = cmd.get("action")
            if action == "rgb_on":
                led.white()
            elif action == "rgb_off":
                led.off()
            elif action == "buzzer_on":
                buzzer.on()
            elif action == "buzzer_off":
                buzzer.off()
        except Exception as e:
            print("Control command handling failed:", e)

    # Connect and subscribe
    mqtt.set_callback(on_message)
    if mqtt.ensure_connected():
        print("MQTT connected to", mqtt.server)
        mqtt.subscribe(CONTROL_TOPIC)
        print("MQTT subscribed to", CONTROL_TOPIC)
        
        # 订阅配置更新主题
        config_handler.subscribe_config_updates()
    else:
        print("MQTT connection failed")

    # Modules
    fire_alarm = FireAlarmModule(flame, led, mqtt)
    heartbeat = HeartbeatModule(wifi, mqtt)
    reporter = SensorReporterModule(light, aht, flame, led, mqtt, config_handler)

    # Main loop
    while True:
        try:
            # Ensure connectivity
            wifi_ok = wifi.ensure_connected()
            mqtt_ok = mqtt.ensure_connected()
            
            if not wifi_ok:
                print("WiFi disconnected, retrying...")
                continue
            if not mqtt_ok:
                print("MQTT disconnected, retrying...")
                continue

            # Pump MQTT messages
            mqtt.check_msg()

            # Run modules
            now = time.ticks_ms()
            fire_alarm.update(now)
            heartbeat.update(now)
            reporter.update(now)
        except Exception as e:
            if DEBUG:
                print("Main loop error:", e)
        finally:
            time.sleep_ms(100)


if __name__ == "__main__":
    main()