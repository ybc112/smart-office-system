package com.smartoffice.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备信息实体类
 */
@Data
@TableName("device_info")
public class DeviceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 设备编号（如W601_001）
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型：SENSOR-传感器, ACTUATOR-执行器
     */
    private String deviceType;

    /**
     * 设备位置
     */
    private String location;

    /**
     * 所属办公室ID
     */
    private Long officeId;

    /**
     * 所属办公区ID
     */
    private Long workAreaId;

    /**
     * 设备状态：ONLINE-在线, OFFLINE-离线, FAULT-故障
     */
    private String status;

    /**
     * 在线状态：0-离线, 1-在线
     */
    private Integer onlineStatus;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 设备描述
     */
    private String description;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
