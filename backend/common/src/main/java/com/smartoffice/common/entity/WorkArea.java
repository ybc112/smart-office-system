package com.smartoffice.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 办公区实体类
 */
@Data
@TableName("work_area")
public class WorkArea implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 办公区ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 办公区编号（如A区、B区、C区）
     */
    private String areaCode;

    /**
     * 办公区名称
     */
    private String areaName;

    /**
     * 所属办公室ID
     */
    private Long officeId;

    /**
     * 办公区描述
     */
    private String description;

    /**
     * 办公区状态：ACTIVE-启用, INACTIVE-禁用
     */
    private String status;

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