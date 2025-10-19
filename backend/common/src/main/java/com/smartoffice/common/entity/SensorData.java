package com.smartoffice.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 传感器数据实体类
 */
@Data
@TableName("sensor_data")
public class SensorData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 设备编号
     */
    private String deviceId;

    /**
     * 光照强度（lux）
     */
    private BigDecimal light;

    /**
     * 温度（℃）
     */
    private BigDecimal temperature;

    /**
     * 湿度（%）
     */
    private BigDecimal humidity;

    /**
     * 火焰检测：0-无火焰, 1-检测到火焰
     */
    private Integer flame;

    /**
     * RGB灯状态：0-关闭, 1-开启
     */
    private Integer rgbStatus;

    /**
     * 数据采集时间
     */
    private LocalDateTime dataTime;

    /**
     * 记录创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
