package com.smartoffice.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartoffice.common.entity.WorkArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 办公区Mapper接口
 */
@Mapper
public interface WorkAreaMapper extends BaseMapper<WorkArea> {

    /**
     * 根据办公室ID查询办公区
     */
    @Select("SELECT * FROM work_area WHERE office_id = #{officeId} AND status = 'ACTIVE' ORDER BY area_code")
    List<WorkArea> selectByOfficeId(Long officeId);

    /**
     * 查询所有启用的办公区
     */
    @Select("SELECT * FROM work_area WHERE status = 'ACTIVE' ORDER BY office_id, area_code")
    List<WorkArea> selectAllActive();

    /**
     * 查询办公区及其关联的办公室信息
     */
    @Select("SELECT wa.*, o.office_name, o.office_code FROM work_area wa " +
            "LEFT JOIN office o ON wa.office_id = o.id " +
            "WHERE wa.status = 'ACTIVE' ORDER BY o.floor, o.office_code, wa.area_code")
    List<WorkArea> selectWithOfficeInfo();
}