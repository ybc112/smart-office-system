package com.smartoffice.common.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 传感器数据DTO（MQTT消息格式）
 */
@Data
public class SensorDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 光照值（lux）
     */
    private Double light;

    /**
     * 温度（℃）
     */
    private Double temperature;

    /**
     * 湿度（%）
     */
    private Double humidity;

    /**
     * 火焰检测（true/false）
     */
    private Boolean flame;

    /**
     * RGB灯状态
     */
    private Boolean rgbStatus;

    /**
     * 蜂鸣器状态
     */
    private Boolean buzzerStatus;

    /**
     * 时间戳
     */
    private Long timestamp;
}
