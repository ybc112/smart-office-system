package com.smartoffice.device.service;

import com.smartoffice.common.entity.Office;
import com.smartoffice.common.entity.WorkArea;

import java.util.List;

/**
 * 办公室服务接口
 */
public interface OfficeService {

    /**
     * 获取所有办公室
     */
    List<Office> getAllOffices();

    /**
     * 根据ID获取办公室
     */
    Office getOfficeById(Long id);

    /**
     * 根据楼层获取办公室
     */
    List<Office> getOfficesByFloor(Integer floor);

    /**
     * 添加办公室
     */
    boolean addOffice(Office office);

    /**
     * 更新办公室
     */
    boolean updateOffice(Office office);

    /**
     * 删除办公室
     */
    boolean deleteOffice(Long id);

    /**
     * 根据办公室ID获取办公区
     */
    List<WorkArea> getWorkAreasByOfficeId(Long officeId);

    /**
     * 获取所有办公区
     */
    List<WorkArea> getAllWorkAreas();

    /**
     * 添加办公区
     */
    boolean addWorkArea(WorkArea workArea);

    /**
     * 更新办公区
     */
    boolean updateWorkArea(WorkArea workArea);

    /**
     * 删除办公区
     */
    boolean deleteWorkArea(Long id);

    /**
     * 获取办公室及其办公区的树形结构
     */
    List<Office> getOfficeTreeStructure();
}