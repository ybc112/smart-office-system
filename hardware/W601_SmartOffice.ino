/*
 * W601智慧办公楼系统硬件端代码
 * 基于Arduino IDE开发
 * 
 * 功能：
 * 1. WiFi连接
 * 2. MQTT通信
 * 3. 传感器数据采集和上报
 * 4. 设备控制响应
 * 5. 自动控制逻辑
 * 
 * 作者：智慧办公楼系统
 * 版本：v1.0
 */

#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <DHT.h>

// ==================== 配置参数 ====================
// WiFi配置
const char* WIFI_SSID = "your_wifi_ssid";
const char* WIFI_PASSWORD = "your_wifi_password";

// MQTT配置
const char* MQTT_SERVER = "192.168.1.100";  // 替换为你的服务器IP
const int MQTT_PORT = 1883;
const char* DEVICE_ID = "W601_001";
const char* MQTT_CLIENT_ID = "W601_SmartOffice_001";

// MQTT主题
const char* TOPIC_SENSOR_DATA = "office/sensor/data";
const char* TOPIC_CONTROL_CMD = "office/control/cmd";
const char* TOPIC_ALARM = "office/alarm";
const char* TOPIC_CONFIG_UPDATE = "office/config/update";

// 硬件引脚定义
#define DHT_PIN 2           // 温湿度传感器引脚
#define DHT_TYPE DHT22      // 传感器类型
#define LIGHT_SENSOR_PIN A0 // 光照传感器引脚
#define FLAME_SENSOR_PIN 3  // 火焰传感器引脚
#define RGB_RED_PIN 9       // RGB红色引脚
#define RGB_GREEN_PIN 10    // RGB绿色引脚
#define RGB_BLUE_PIN 11     // RGB蓝色引脚
#define BUZZER_PIN 8        // 蜂鸣器引脚

// 数据上报间隔（毫秒）
unsigned long reportInterval = 30000; // 默认30秒，可通过配置更新
unsigned long lightCollectInterval = 10000; // 光照采集间隔，默认10秒
unsigned long tempHumidityCollectInterval = 10000; // 温湿度采集间隔，默认10秒
unsigned long flameDetectInterval = 5000; // 火焰检测间隔，默认5秒

// ==================== 全局变量 ====================
WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);
DHT dht(DHT_PIN, DHT_TYPE);

// 设备状态
bool rgbStatus = false;
bool buzzerStatus = false;
bool flameDetected = false;

// 时间控制
unsigned long lastReportTime = 0;
unsigned long lastMqttReconnect = 0;

// 传感器数据
float temperature = 0.0;
float humidity = 0.0;
float lightLevel = 0.0;

// ==================== 初始化函数 ====================
void setup() {
    Serial.begin(115200);
    Serial.println("=== W601智慧办公楼系统启动 ===");
    
    // 初始化硬件
    initHardware();
    
    // 初始化WiFi
    initWiFi();
    
    // 初始化MQTT
    initMQTT();
    
    // 执行设备自检
    performSelfCheck();
    
    Serial.println("=== 系统初始化完成 ===");
}

// ==================== 主循环 ====================
void loop() {
    // 检查WiFi连接状态
    checkWiFiConnection();
    
    // 保持MQTT连接
    if (!mqttClient.connected()) {
        reconnectMQTT();
    }
    mqttClient.loop();
    
    // 定时上报传感器数据
    if (millis() - lastReportTime >= reportInterval) {
        readSensors();
        publishSensorData();
        lastReportTime = millis();
    }
    
    // 检查火焰传感器（实时检测）
    checkFlameAlarm();
    
    // 智能照明控制
    autoLightingControl();
    
    // 系统状态监控
    monitorSystemStatus();
    
    delay(100); // 短暂延时
}

// ==================== 硬件初始化 ====================
void initHardware() {
    Serial.println("初始化硬件...");
    
    // 初始化传感器
    dht.begin();
    
    // 初始化引脚
    pinMode(LIGHT_SENSOR_PIN, INPUT);
    pinMode(FLAME_SENSOR_PIN, INPUT);
    pinMode(RGB_RED_PIN, OUTPUT);
    pinMode(RGB_GREEN_PIN, OUTPUT);
    pinMode(RGB_BLUE_PIN, OUTPUT);
    pinMode(BUZZER_PIN, OUTPUT);
    
    // 初始状态：关闭所有设备
    setRGBColor(0, 0, 0);
    digitalWrite(BUZZER_PIN, LOW);
    
    Serial.println("硬件初始化完成");
}

// ==================== WiFi连接 ====================
void initWiFi() {
    Serial.print("连接WiFi: ");
    Serial.println(WIFI_SSID);
    
    // 设置WiFi模式
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    
    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 30) {
        delay(500);
        Serial.print(".");
        attempts++;
        
        // 每10次尝试重新开始连接
        if (attempts % 10 == 0) {
            Serial.println();
            Serial.println("重新尝试WiFi连接...");
            WiFi.disconnect();
            delay(1000);
            WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
        }
    }
    
    if (WiFi.status() == WL_CONNECTED) {
        Serial.println();
        Serial.println("WiFi连接成功!");
        Serial.print("IP地址: ");
        Serial.println(WiFi.localIP());
        Serial.print("信号强度: ");
        Serial.print(WiFi.RSSI());
        Serial.println(" dBm");
    } else {
        Serial.println();
        Serial.println("WiFi连接失败!");
        Serial.println("请检查:");
        Serial.println("1. WiFi名称和密码是否正确");
        Serial.println("2. WiFi信号是否足够强");
        Serial.println("3. 路由器是否正常工作");
        
        // 进入配置模式或重启
        Serial.println("10秒后重启设备...");
        delay(10000);
        ESP.restart();
    }
}

// ==================== WiFi状态检查 ====================
void checkWiFiConnection() {
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("WiFi连接丢失，尝试重连...");
        WiFi.disconnect();
        delay(1000);
        WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
        
        int attempts = 0;
        while (WiFi.status() != WL_CONNECTED && attempts < 10) {
            delay(500);
            Serial.print(".");
            attempts++;
        }
        
        if (WiFi.status() == WL_CONNECTED) {
            Serial.println();
            Serial.println("WiFi重连成功!");
        } else {
            Serial.println();
            Serial.println("WiFi重连失败!");
        }
    }
}

// ==================== MQTT初始化 ====================
void initMQTT() {
    mqttClient.setServer(MQTT_SERVER, MQTT_PORT);
    mqttClient.setCallback(onMqttMessage);
    
    // 连接MQTT服务器
    reconnectMQTT();
}

// ==================== MQTT重连 ====================
void reconnectMQTT() {
    // 确保WiFi已连接
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("WiFi未连接，跳过MQTT连接");
        return;
    }
    
    // 避免频繁重连
    if (millis() - lastMqttReconnect < 5000) {
        return;
    }
    lastMqttReconnect = millis();
    
    Serial.print("连接MQTT服务器...");
    
    // 设置连接选项
    mqttClient.setKeepAlive(60);
    mqttClient.setSocketTimeout(30);
    
    if (mqttClient.connect(MQTT_CLIENT_ID)) {
        Serial.println("连接成功!");
        
        // 订阅控制命令主题
        if (mqttClient.subscribe(TOPIC_CONTROL_CMD, 1)) {
            Serial.print("订阅主题成功: ");
            Serial.println(TOPIC_CONTROL_CMD);
        } else {
            Serial.println("订阅主题失败!");
        }
        
        // 订阅配置更新主题
        if (mqttClient.subscribe(TOPIC_CONFIG_UPDATE, 1)) {
            Serial.print("订阅配置主题成功: ");
            Serial.println(TOPIC_CONFIG_UPDATE);
        } else {
            Serial.println("订阅配置主题失败!");
        }
        
        // 发送设备上线消息
        publishDeviceStatus("online");
        
    } else {
        Serial.print("连接失败, rc=");
        Serial.print(mqttClient.state());
        Serial.print(" (");
        
        // 详细错误信息
        switch (mqttClient.state()) {
            case -4:
                Serial.print("连接超时");
                break;
            case -3:
                Serial.print("连接丢失");
                break;
            case -2:
                Serial.print("连接失败");
                break;
            case -1:
                Serial.print("连接断开");
                break;
            case 1:
                Serial.print("协议版本错误");
                break;
            case 2:
                Serial.print("客户端ID被拒绝");
                break;
            case 3:
                Serial.print("服务器不可用");
                break;
            case 4:
                Serial.print("用户名或密码错误");
                break;
            case 5:
                Serial.print("未授权");
                break;
            default:
                Serial.print("未知错误");
                break;
        }
        Serial.println(") 5秒后重试");
    }
}

// ==================== MQTT消息回调 ====================
void onMqttMessage(char* topic, byte* payload, unsigned int length) {
    // 转换消息为字符串
    String message = "";
    for (int i = 0; i < length; i++) {
        message += (char)payload[i];
    }
    
    Serial.println("收到MQTT消息:");
    Serial.print("主题: ");
    Serial.println(topic);
    Serial.print("内容: ");
    Serial.println(message);
    
    // 处理控制命令
    if (String(topic) == TOPIC_CONTROL_CMD) {
        handleControlCommand(message);
    }
    // 处理配置更新
    else if (String(topic) == TOPIC_CONFIG_UPDATE) {
        handleConfigUpdate(message);
    }
}

// ==================== 处理控制命令 ====================
void handleControlCommand(String message) {
    // 解析JSON
    DynamicJsonDocument doc(1024);
    deserializeJson(doc, message);
    
    String deviceId = doc["deviceId"];
    String action = doc["action"];
    
    // 只处理发给当前设备的命令
    if (deviceId != DEVICE_ID) {
        return;
    }
    
    Serial.print("执行控制命令: ");
    Serial.println(action);
    
    // 执行相应的控制动作
    if (action == "rgb_on") {
        setRGBOn();
    } else if (action == "rgb_off") {
        setRGBOff();
    } else if (action == "buzzer_on") {
        setBuzzerOn();
    } else if (action == "buzzer_off") {
        setBuzzerOff();
    }
}

// ==================== 处理配置更新 ====================
void handleConfigUpdate(String message) {
    // 解析JSON
    DynamicJsonDocument doc(1024);
    deserializeJson(doc, message);
    
    Serial.print("收到配置更新: ");
    Serial.println(message);
    
    // 处理各种采集间隔配置
    if (doc.containsKey("data.collect.interval")) {
        int intervalSeconds = doc["data.collect.interval"];
        reportInterval = intervalSeconds * 1000; // 转换为毫秒
        
        Serial.print("更新数据采集间隔: ");
        Serial.print(intervalSeconds);
        Serial.print("秒 (");
        Serial.print(reportInterval);
        Serial.println("毫秒)");
    }
    
    if (doc.containsKey("light.collect.interval")) {
        int intervalSeconds = doc["light.collect.interval"];
        lightCollectInterval = intervalSeconds * 1000; // 转换为毫秒
        
        Serial.print("更新光照采集间隔: ");
        Serial.print(intervalSeconds);
        Serial.print("秒 (");
        Serial.print(lightCollectInterval);
        Serial.println("毫秒)");
    }
    
    if (doc.containsKey("temp.humidity.collect.interval")) {
        int intervalSeconds = doc["temp.humidity.collect.interval"];
        tempHumidityCollectInterval = intervalSeconds * 1000; // 转换为毫秒
        
        Serial.print("更新温湿度采集间隔: ");
        Serial.print(intervalSeconds);
        Serial.print("秒 (");
        Serial.print(tempHumidityCollectInterval);
        Serial.println("毫秒)");
    }
    
    if (doc.containsKey("flame.detect.interval")) {
        int intervalSeconds = doc["flame.detect.interval"];
        flameDetectInterval = intervalSeconds * 1000; // 转换为毫秒
        
        Serial.print("更新火焰检测间隔: ");
        Serial.print(intervalSeconds);
        Serial.print("秒 (");
        Serial.print(flameDetectInterval);
        Serial.println("毫秒)");
    }
    
    // 可以在这里添加其他配置项的处理
}

// ==================== 传感器数据读取 ====================
void readSensors() {
    // 读取温湿度
    temperature = dht.readTemperature();
    humidity = dht.readHumidity();
    
    // 检查读取是否成功
    if (isnan(temperature) || isnan(humidity)) {
        Serial.println("温湿度传感器读取失败!");
        temperature = 25.0; // 默认值
        humidity = 50.0;    // 默认值
    }
    
    // 读取光照强度（转换为lux）
    int lightRaw = analogRead(LIGHT_SENSOR_PIN);
    lightLevel = map(lightRaw, 0, 1023, 0, 1000); // 简单映射到0-1000 lux
    
    // 读取火焰传感器
    flameDetected = digitalRead(FLAME_SENSOR_PIN) == HIGH;
    
    // 打印传感器数据
    Serial.println("=== 传感器数据 ===");
    Serial.print("温度: ");
    Serial.print(temperature);
    Serial.println("°C");
    Serial.print("湿度: ");
    Serial.print(humidity);
    Serial.println("%");
    Serial.print("光照: ");
    Serial.print(lightLevel);
    Serial.println(" lux");
    Serial.print("火焰: ");
    Serial.println(flameDetected ? "检测到" : "正常");
}

// ==================== 发布设备状态 ====================
void publishDeviceStatus(String status) {
    DynamicJsonDocument doc(512);
    doc["deviceId"] = DEVICE_ID;
    doc["status"] = status;
    doc["timestamp"] = millis();
    doc["ip"] = WiFi.localIP().toString();
    doc["rssi"] = WiFi.RSSI();
    
    String jsonString;
    serializeJson(doc, jsonString);
    
    String topic = "office/device/status";
    if (mqttClient.publish(topic.c_str(), jsonString.c_str())) {
        Serial.print("设备状态发送成功: ");
        Serial.println(status);
    } else {
        Serial.println("设备状态发送失败!");
    }
}

// ==================== 发布传感器数据 ====================
void publishSensorData() {
    // 检查MQTT连接
    if (!mqttClient.connected()) {
        Serial.println("MQTT未连接，跳过数据发送");
        return;
    }
    
    // 创建JSON数据
    DynamicJsonDocument doc(1024);
    doc["deviceId"] = DEVICE_ID;
    doc["light"] = round(lightLevel * 100) / 100.0; // 保留2位小数
    doc["temperature"] = round(temperature * 100) / 100.0;
    doc["humidity"] = round(humidity * 100) / 100.0;
    doc["flame"] = flameDetected;
    doc["rgbStatus"] = rgbStatus;
    doc["buzzerStatus"] = buzzerStatus;
    doc["timestamp"] = millis();
    
    // 序列化JSON
    String jsonString;
    serializeJson(doc, jsonString);
    
    // 发布到MQTT（QoS 1确保消息送达）
    if (mqttClient.publish(TOPIC_SENSOR_DATA, jsonString.c_str(), true)) {
        Serial.println("传感器数据发送成功:");
        Serial.println(jsonString);
    } else {
        Serial.println("传感器数据发送失败!");
        Serial.print("MQTT状态: ");
        Serial.println(mqttClient.state());
    }
}

// ==================== 发布告警消息 ====================
void publishAlarm(String alarmType, String message) {
    DynamicJsonDocument doc(1024);
    doc["deviceId"] = DEVICE_ID;
    doc["alarmType"] = alarmType;
    doc["message"] = message;
    doc["level"] = "HIGH";
    doc["timestamp"] = millis();
    doc["location"] = "办公区域";
    
    String jsonString;
    serializeJson(doc, jsonString);
    
    // 发布告警消息（QoS 1确保重要消息送达）
    if (mqttClient.publish(TOPIC_ALARM, jsonString.c_str(), true)) {
        Serial.println("告警消息发送成功:");
        Serial.println(jsonString);
    } else {
        Serial.println("告警消息发送失败!");
    }
}

// ==================== 设备控制函数 ====================
void setRGBOn() {
    rgbStatus = true;
    setRGBColor(255, 255, 255); // 白光
    Serial.println("💡 RGB灯已开启");
}

void setRGBOff() {
    rgbStatus = false;
    setRGBColor(0, 0, 0);
    Serial.println("🌑 RGB灯已关闭");
}

void setRGBColor(int red, int green, int blue) {
    analogWrite(RGB_RED_PIN, red);
    analogWrite(RGB_GREEN_PIN, green);
    analogWrite(RGB_BLUE_PIN, blue);
}

void setBuzzerOn() {
    buzzerStatus = true;
    digitalWrite(BUZZER_PIN, HIGH);
    Serial.println("🔔 蜂鸣器已开启");
}

void setBuzzerOff() {
    buzzerStatus = false;
    digitalWrite(BUZZER_PIN, LOW);
    Serial.println("🔕 蜂鸣器已关闭");
}

// ==================== 自动控制逻辑 ====================
// 注意：自动控制逻辑主要在服务器端实现
// 硬件端主要负责响应服务器发送的控制命令
// 但也可以在硬件端实现一些紧急响应逻辑，如火焰检测立即开启蜂鸣器

// ==================== 火焰告警检测 ====================
void checkFlameAlarm() {
    static bool lastFlameState = false;
    
    if (flameDetected && !lastFlameState) {
        // 检测到火焰，立即响应
        Serial.println("🚨 检测到火焰！立即触发告警");
        
        // 立即开启蜂鸣器（本地紧急响应）
        setBuzzerOn();
        
        // 发送告警消息到服务器
        publishAlarm("FIRE", "🚨 检测到火焰！立即疏散！");
        
        // 立即闪烁RGB灯作为视觉警告（减少延时）
        for (int i = 0; i < 3; i++) {
            setRGBColor(255, 0, 0); // 红色
            delay(100);  // 减少延时，更快响应
            setRGBColor(0, 0, 0);   // 关闭
            delay(100);  // 减少延时
        }
        setRGBColor(255, 0, 0); // 保持红色警告状态
    } else if (!flameDetected && lastFlameState) {
        // 火焰消失，发送恢复消息
        Serial.println("✅ 火焰告警解除");
        publishAlarm("FIRE_CLEAR", "火焰告警已解除");
        setBuzzerOff(); // 确保蜂鸣器关闭
        setRGBColor(0, 0, 0); // 关闭RGB灯
    }
    
    lastFlameState = flameDetected;
}

// ==================== 智能照明控制 ====================
void autoLightingControl() {
    static bool autoLightEnabled = true;
    static unsigned long lastLightCheck = 0;
    
    // 每5秒检查一次光照条件
    if (millis() - lastLightCheck < 5000) {
        return;
    }
    lastLightCheck = millis();
    
    if (!autoLightEnabled) {
        return;
    }
    
    // 智能照明逻辑
    if (lightLevel < 300 && !rgbStatus) {
        // 光照不足且灯未开启，自动开灯
        Serial.println("💡 光照不足，自动开启照明");
        setRGBOn();
        
        // 发送自动控制消息
        publishAutoControl("AUTO_LIGHT_ON", "光照不足，自动开启照明");
        
    } else if (lightLevel > 350 && rgbStatus) {
        // 光照充足且灯已开启，自动关灯
        Serial.println("🌞 光照充足，自动关闭照明");
        setRGBOff();
        
        // 发送自动控制消息
        publishAutoControl("AUTO_LIGHT_OFF", "光照充足，自动关闭照明");
    }
}

// ==================== 发布自动控制消息 ====================
void publishAutoControl(String action, String message) {
    DynamicJsonDocument doc(1024);
    doc["deviceId"] = DEVICE_ID;
    doc["action"] = action;
    doc["message"] = message;
    doc["lightLevel"] = lightLevel;
    doc["rgbStatus"] = rgbStatus;
    doc["timestamp"] = millis();
    
    String jsonString;
    serializeJson(doc, jsonString);
    
    String topic = "office/auto/control";
    if (mqttClient.publish(topic.c_str(), jsonString.c_str())) {
        Serial.println("自动控制消息发送成功:");
        Serial.println(jsonString);
    } else {
        Serial.println("自动控制消息发送失败!");
    }
}

// ==================== 设备自检功能 ====================
void performSelfCheck() {
    Serial.println("=== 设备自检开始 ===");
    
    // 检查传感器
    Serial.println("检查传感器...");
    readSensors();
    
    if (isnan(temperature) || isnan(humidity)) {
        Serial.println("❌ 温湿度传感器异常");
    } else {
        Serial.println("✅ 温湿度传感器正常");
    }
    
    // 检查RGB灯
    Serial.println("检查RGB灯...");
    setRGBColor(255, 0, 0); delay(500);
    setRGBColor(0, 255, 0); delay(500);
    setRGBColor(0, 0, 255); delay(500);
    setRGBColor(0, 0, 0);
    Serial.println("✅ RGB灯测试完成");
    
    // 检查蜂鸣器
    Serial.println("检查蜂鸣器...");
    digitalWrite(BUZZER_PIN, HIGH);
    delay(200);
    digitalWrite(BUZZER_PIN, LOW);
    Serial.println("✅ 蜂鸣器测试完成");
    
    Serial.println("=== 设备自检完成 ===");
}

// ==================== 系统状态监控 ====================
void monitorSystemStatus() {
    static unsigned long lastStatusCheck = 0;
    
    // 每分钟检查一次系统状态
    if (millis() - lastStatusCheck < 60000) {
        return;
    }
    lastStatusCheck = millis();
    
    Serial.println("=== 系统状态监控 ===");
    Serial.print("运行时间: ");
    Serial.print(millis() / 1000);
    Serial.println(" 秒");
    
    Serial.print("WiFi状态: ");
    Serial.println(WiFi.status() == WL_CONNECTED ? "已连接" : "未连接");
    
    Serial.print("MQTT状态: ");
    Serial.println(mqttClient.connected() ? "已连接" : "未连接");
    
    Serial.print("可用内存: ");
    Serial.print(ESP.getFreeHeap());
    Serial.println(" bytes");
    
    // 发送心跳消息
    publishDeviceStatus("heartbeat");
}