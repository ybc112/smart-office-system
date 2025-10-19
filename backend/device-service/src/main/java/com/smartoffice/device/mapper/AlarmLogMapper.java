package com.smartoffice.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartoffice.common.entity.AlarmLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警日志Mapper
 */
@Mapper
public interface AlarmLogMapper extends BaseMapper<AlarmLog> {
}
