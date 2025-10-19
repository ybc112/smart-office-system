package com.smartoffice.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 设备控制命令DTO（MQTT消息格式）
 */
@Data
public class ControlCommandDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 控制动作
     */
    private String action;

    /**
     * 控制参数
     */
    private Map<String, Object> params;

    public ControlCommandDTO() {
    }

    public ControlCommandDTO(String deviceId, String action) {
        this.deviceId = deviceId;
        this.action = action;
    }

    public ControlCommandDTO(String deviceId, String action, Map<String, Object> params) {
        this.deviceId = deviceId;
        this.action = action;
        this.params = params;
    }
}
