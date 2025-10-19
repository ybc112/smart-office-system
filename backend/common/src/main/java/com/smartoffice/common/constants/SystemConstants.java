package com.smartoffice.common.constants;

/**
 * 系统常量
 */
public class SystemConstants {

    /**
     * 设备状态
     */
    public static final String DEVICE_STATUS_ONLINE = "ONLINE";
    public static final String DEVICE_STATUS_OFFLINE = "OFFLINE";
    public static final String DEVICE_STATUS_FAULT = "FAULT";

    /**
     * 告警类型
     */
    public static final String ALARM_TYPE_FIRE = "FIRE";
    public static final String ALARM_TYPE_TEMP = "TEMP";
    public static final String ALARM_TYPE_HUMIDITY = "HUMIDITY";
    public static final String ALARM_TYPE_LIGHT = "LIGHT";

    /**
     * 告警级别
     */
    public static final String ALARM_LEVEL_INFO = "INFO";
    public static final String ALARM_LEVEL_WARNING = "WARNING";
    public static final String ALARM_LEVEL_CRITICAL = "CRITICAL";

    /**
     * 告警状态
     */
    public static final String ALARM_STATUS_UNHANDLED = "UNHANDLED";
    public static final String ALARM_STATUS_HANDLING = "HANDLING";
    public static final String ALARM_STATUS_HANDLED = "HANDLED";
    public static final String ALARM_STATUS_IGNORED = "IGNORED";

    /**
     * 用户角色
     */
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    /**
     * 控制动作
     */
    public static final String ACTION_RGB_ON = "rgb_on";
    public static final String ACTION_RGB_OFF = "rgb_off";
    public static final String ACTION_BUZZER_ON = "buzzer_on";
    public static final String ACTION_BUZZER_OFF = "buzzer_off";
    public static final String ACTION_HUMIDIFIER_ON = "humidifier_on";
    public static final String ACTION_HUMIDIFIER_OFF = "humidifier_off";
    public static final String ACTION_AC_HEAT = "ac_heat";
    public static final String ACTION_AC_COOL = "ac_cool";
    public static final String ACTION_AC_OFF = "ac_off";
    public static final String ACTION_SET_THRESHOLD = "set_threshold";

    /**
     * 触发方式
     */
    public static final String TRIGGER_TYPE_AUTO = "AUTO";
    public static final String TRIGGER_TYPE_MANUAL = "MANUAL";

    /**
     * 执行结果
     */
    public static final String RESULT_SUCCESS = "SUCCESS";
    public static final String RESULT_FAILED = "FAILED";
}
