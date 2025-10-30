package com.smartoffice.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartoffice.common.entity.Office;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 办公室Mapper接口
 */
@Mapper
public interface OfficeMapper extends BaseMapper<Office> {

    /**
     * 根据楼层查询办公室
     */
    @Select("SELECT * FROM office WHERE floor = #{floor} AND status = 'ACTIVE' ORDER BY office_code")
    List<Office> selectByFloor(Integer floor);

    /**
     * 查询所有启用的办公室
     */
    @Select("SELECT * FROM office WHERE status = 'ACTIVE' ORDER BY floor, office_code")
    List<Office> selectAllActive();
}