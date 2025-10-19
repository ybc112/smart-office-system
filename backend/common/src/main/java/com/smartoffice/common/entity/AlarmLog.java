package com.smartoffice.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 告警日志实体类
 */
@Data
@TableName("alarm_log")
public class AlarmLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 告警ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 设备编号
     */
    private String deviceId;

    /**
     * 告警类型：FIRE-火灾, TEMP-温度异常, HUMIDITY-湿度异常, LIGHT-光照异常
     */
    private String alarmType;

    /**
     * 告警级别：INFO-提示, WARNING-警告, CRITICAL-严重
     */
    private String alarmLevel;

    /**
     * 告警消息
     */
    private String alarmMessage;

    /**
     * 告警触发值
     */
    private String alarmValue;

    /**
     * 阈值
     */
    private String thresholdValue;

    /**
     * 处理状态：UNHANDLED-未处理, HANDLING-处理中, HANDLED-已处理, IGNORED-已忽略
     */
    private String status;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 告警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 记录创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
