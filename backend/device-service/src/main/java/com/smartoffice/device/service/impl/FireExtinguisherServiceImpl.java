package com.smartoffice.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartoffice.device.dto.FireExtinguisherDTO;
import com.smartoffice.device.entity.FireExtinguisher;
import com.smartoffice.device.mapper.FireExtinguisherMapper;
import com.smartoffice.device.service.FireExtinguisherService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 灭火器服务实现类
 */
@Service
public class FireExtinguisherServiceImpl extends ServiceImpl<FireExtinguisherMapper, FireExtinguisher> implements FireExtinguisherService {

    @Override
    public List<FireExtinguisherDTO> getAllFireExtinguishers() {
        List<FireExtinguisher> fireExtinguishers = list();
        return fireExtinguishers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public FireExtinguisherDTO getFireExtinguisherById(Long id) {
        FireExtinguisher fireExtinguisher = getById(id);
        return fireExtinguisher != null ? convertToDTO(fireExtinguisher) : null;
    }

    @Override
    public boolean addFireExtinguisher(FireExtinguisherDTO fireExtinguisherDTO) {
        FireExtinguisher fireExtinguisher = new FireExtinguisher();
        BeanUtils.copyProperties(fireExtinguisherDTO, fireExtinguisher);
        
        // 自动生成设备编号
        fireExtinguisher.setCode(generateFireExtinguisherCode());
        
        fireExtinguisher.setCreateTime(LocalDateTime.now());
        fireExtinguisher.setUpdateTime(LocalDateTime.now());
        return save(fireExtinguisher);
    }

    @Override
    public boolean updateFireExtinguisher(FireExtinguisherDTO fireExtinguisherDTO) {
        FireExtinguisher fireExtinguisher = new FireExtinguisher();
        BeanUtils.copyProperties(fireExtinguisherDTO, fireExtinguisher);
        fireExtinguisher.setUpdateTime(LocalDateTime.now());
        return updateById(fireExtinguisher);
    }

    @Override
    public boolean deleteFireExtinguisher(Long id) {
        return removeById(id);
    }

    /**
     * 生成灭火器编号
     */
    private String generateFireExtinguisherCode() {
        // 查询当前最大编号
        QueryWrapper<FireExtinguisher> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("code").last("LIMIT 1");
        FireExtinguisher lastFireExtinguisher = getOne(queryWrapper);
        
        int nextNumber = 1;
        if (lastFireExtinguisher != null && lastFireExtinguisher.getCode() != null) {
            String lastCode = lastFireExtinguisher.getCode();
            // 提取编号中的数字部分 (FE001 -> 001 -> 1)
            if (lastCode.startsWith("FE") && lastCode.length() > 2) {
                try {
                    String numberPart = lastCode.substring(2);
                    nextNumber = Integer.parseInt(numberPart) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = 1;
                }
            }
        }
        
        // 格式化为 FE001, FE002, ... 的格式
        return String.format("FE%03d", nextNumber);
    }

    /**
     * 实体转DTO
     */
    private FireExtinguisherDTO convertToDTO(FireExtinguisher fireExtinguisher) {
        FireExtinguisherDTO dto = new FireExtinguisherDTO();
        BeanUtils.copyProperties(fireExtinguisher, dto);
        
        // 计算是否超期未检查（超过一个月）
        if (fireExtinguisher.getLastPressureCheckTime() != null) {
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            dto.setOverdue(fireExtinguisher.getLastPressureCheckTime().isBefore(oneMonthAgo));
        } else {
            dto.setOverdue(true); // 没有检查记录视为超期
        }
        
        return dto;
    }
}