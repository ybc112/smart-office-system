package com.smartoffice.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartoffice.common.entity.DeviceInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备信息Mapper
 */
@Mapper
public interface DeviceInfoMapper extends BaseMapper<DeviceInfo> {
}
