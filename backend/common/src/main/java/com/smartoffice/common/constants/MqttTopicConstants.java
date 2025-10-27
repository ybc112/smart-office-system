package com.smartoffice.common.constants;

/**
 * MQTT主题常量
 */
public class MqttTopicConstants {

    /**
     * 传感器数据上报主题
     */
    public static final String TOPIC_SENSOR_DATA = "office/sensor/data";

    /**
     * 设备控制命令主题
     */
    public static final String TOPIC_CONTROL_CMD = "office/control/cmd";

    /**
     * 告警消息主题
     */
    public static final String TOPIC_ALARM = "office/alarm";

    /**
     * 设备状态主题
     */
    public static final String TOPIC_DEVICE_STATUS = "office/device/status";

    /**
     * 配置更新主题
     */
    public static final String TOPIC_CONFIG_UPDATE = "office/config/update";
}
