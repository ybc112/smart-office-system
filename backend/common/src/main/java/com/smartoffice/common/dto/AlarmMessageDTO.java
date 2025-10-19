package com.smartoffice.common.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 告警消息DTO（MQTT消息格式）
 */
@Data
public class AlarmMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 告警类型：FIRE/TEMP/HUMIDITY/LIGHT
     */
    private String alarmType;

    /**
     * 告警级别：INFO/WARNING/CRITICAL
     */
    private String level;

    /**
     * 告警消息
     */
    private String message;

    /**
     * 时间戳
     */
    private Long timestamp;

    public AlarmMessageDTO() {
    }

    public AlarmMessageDTO(String deviceId, String alarmType, String level, String message) {
        this.deviceId = deviceId;
        this.alarmType = alarmType;
        this.level = level;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}
