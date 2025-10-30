package com.smartoffice.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 办公室实体类
 */
@Data
@TableName("office")
public class Office implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 办公室ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 办公室编号（如201、202、203）
     */
    private String officeCode;

    /**
     * 办公室名称
     */
    private String officeName;

    /**
     * 办公室描述
     */
    private String description;

    /**
     * 楼层
     */
    private Integer floor;

    /**
     * 办公室状态：ACTIVE-启用, INACTIVE-禁用
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