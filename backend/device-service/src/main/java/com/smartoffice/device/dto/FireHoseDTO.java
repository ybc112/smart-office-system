package com.smartoffice.device.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消防水带数据传输对象
 */
@Data
public class FireHoseDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 消防水带编号
     */
    private String code;

    /**
     * 消防水带名称
     */
    private String name;

    /**
     * 安装位置
     */
    private String location;

    /**
     * 上次检查时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCheckTime;

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