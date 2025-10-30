package com.smartoffice.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartoffice.common.entity.Office;
import com.smartoffice.common.entity.WorkArea;
import com.smartoffice.device.mapper.OfficeMapper;
import com.smartoffice.device.mapper.WorkAreaMapper;
import com.smartoffice.device.service.OfficeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 办公室服务实现类
 */
@Slf4j
@Service
public class OfficeServiceImpl implements OfficeService {

    @Autowired
    private OfficeMapper officeMapper;

    @Autowired
    private WorkAreaMapper workAreaMapper;

    @Override
    public List<Office> getAllOffices() {
        return officeMapper.selectAllActive();
    }

    @Override
    public Office getOfficeById(Long id) {
        return officeMapper.selectById(id);
    }

    @Override
    public List<Office> getOfficesByFloor(Integer floor) {
        return officeMapper.selectByFloor(floor);
    }

    @Override
    @Transactional
    public boolean addOffice(Office office) {
        try {
            // 检查办公室编号是否已存在
            Office existing = officeMapper.selectOne(
                    new LambdaQueryWrapper<Office>()
                            .eq(Office::getOfficeCode, office.getOfficeCode())
            );
            if (existing != null) {
                log.warn("办公室编号已存在: {}", office.getOfficeCode());
                return false;
            }

            office.setStatus("ACTIVE");
            return officeMapper.insert(office) > 0;
        } catch (Exception e) {
            log.error("添加办公室失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateOffice(Office office) {
        try {
            return officeMapper.updateById(office) > 0;
        } catch (Exception e) {
            log.error("更新办公室失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteOffice(Long id) {
        try {
            // 检查是否有关联的办公区
            List<WorkArea> workAreas = workAreaMapper.selectByOfficeId(id);
            if (!workAreas.isEmpty()) {
                log.warn("办公室下还有办公区，无法删除: {}", id);
                return false;
            }

            return officeMapper.deleteById(id) > 0;
        } catch (Exception e) {
            log.error("删除办公室失败", e);
            return false;
        }
    }

    @Override
    public List<WorkArea> getWorkAreasByOfficeId(Long officeId) {
        return workAreaMapper.selectByOfficeId(officeId);
    }

    @Override
    public List<WorkArea> getAllWorkAreas() {
        return workAreaMapper.selectAllActive();
    }

    @Override
    @Transactional
    public boolean addWorkArea(WorkArea workArea) {
        try {
            workArea.setStatus("ACTIVE");
            return workAreaMapper.insert(workArea) > 0;
        } catch (Exception e) {
            log.error("添加办公区失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateWorkArea(WorkArea workArea) {
        try {
            return workAreaMapper.updateById(workArea) > 0;
        } catch (Exception e) {
            log.error("更新办公区失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteWorkArea(Long id) {
        try {
            return workAreaMapper.deleteById(id) > 0;
        } catch (Exception e) {
            log.error("删除办公区失败", e);
            return false;
        }
    }

    @Override
    public List<Office> getOfficeTreeStructure() {
        List<Office> offices = getAllOffices();
        // 为每个办公室加载其办公区
        for (Office office : offices) {
            List<WorkArea> workAreas = getWorkAreasByOfficeId(office.getId());
            // 这里可以设置一个临时字段来存储办公区，或者创建一个DTO
            // 暂时使用description字段来标识这是树形结构数据
        }
        return offices;
    }
}