package com.smartoffice.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartoffice.common.entity.SensorData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 传感器数据Mapper
 */
@Mapper
public interface SensorDataMapper extends BaseMapper<SensorData> {
}
