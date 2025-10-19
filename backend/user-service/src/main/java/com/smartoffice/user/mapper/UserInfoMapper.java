package com.smartoffice.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartoffice.common.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
