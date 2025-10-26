# W601 Smart Office - Config
# Adjust these values before deploying to the board

# WiFi settings
WIFI_SSID = "iQOO 13"
WIFI_PASSWORD = "01234567890"

# Device identity
# Deprecated: use MQTT_CLIENT_ID for device identity

# MQTT settings
# MQTT服务器配置
MQTT_SERVER = "192.168.8.75"  # 主机局域网IP，连接到运行后端服务的电脑
MQTT_PORT = 1883
MQTT_CLIENT_ID = "W601_001"
MQTT_USERNAME = ""  # EMQX默认无需认证
MQTT_PASSWORD = ""
MQTT_KEEPALIVE = 60
# 兼容旧变量名，避免影响现有代码
DEVICE_ID = MQTT_CLIENT_ID
MQTT_BROKER = MQTT_SERVER
# MQTT topics
# MQTT主题配置（字符串，与后端保持一致）
TOPIC_SENSOR_DATA = "office/sensor/data"
TOPIC_CONTROL_CMD = "office/control/cmd"
TOPIC_ALARM = "office/alarm"
TOPIC_DEVICE_STATUS = "office/device/status"
# MicroPython侧使用字节主题（派生自字符串）
SENSOR_DATA_TOPIC = TOPIC_SENSOR_DATA.encode()
CONTROL_TOPIC = TOPIC_CONTROL_CMD.encode()
ALARM_TOPIC = TOPIC_ALARM.encode()
DEVICE_STATUS_TOPIC = TOPIC_DEVICE_STATUS.encode()

# GPIO pin mapping (W601 IoT Board)
FLAME_SENSOR_PIN = 65  # PB10
LED_R_PIN = 30         # PA13
LED_G_PIN = 31         # PA14
LED_B_PIN = 32         # PA15
BUZZER_PIN = 45        # PB15

# I2C bus (only for AHT10)
I2C1_SCL = 23  # PA0
I2C_SDA = 24   # PA1

# External light sensor (ADC analog input)
LIGHT_ADC_PIN = 7   # PB25/SAR-ADC7 - for analog light sensor (AO pin)

# Lighting thresholds (hysteresis)
LIGHT_ON_LUX = 300
LIGHT_OFF_LUX = 350

# Timing settings (milliseconds)
LIGHTING_PERIOD_MS = 5000
ENVIRONMENT_PERIOD_MS = 10000
FIRE_ALARM_PERIOD_MS = 1000
HEARTBEAT_PERIOD_MS = 30000
SENSOR_REPORT_PERIOD_MS = 10000  # 改为10秒，更频繁地发送数据

# Retry timings (milliseconds)
WIFI_RETRY_MS = 3000
MQTT_RETRY_MS = 5000

# Misc
DEBUG = True