package com.smartoffice.device.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 灭火器数据传输对象
 */
@Data
public class FireExtinguisherDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 灭火器编号
     */
    private String code;

    /**
     * 灭火器名称
     */
    private String name;

    /**
     * 安装位置
     */
    private String location;

    /**
     * 上次压力检查时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastPressureCheckTime;

    /**
     * 状态：1-正常，0-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否超期未检查（前端计算字段）
     */
    private Boolean overdue;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}