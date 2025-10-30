package com.smartoffice.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 灭火器实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("fire_extinguisher")
public class FireExtinguisher {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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