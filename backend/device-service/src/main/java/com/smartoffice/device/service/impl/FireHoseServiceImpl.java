package com.smartoffice.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartoffice.device.dto.FireHoseDTO;
import com.smartoffice.device.entity.FireHose;
import com.smartoffice.device.mapper.FireHoseMapper;
import com.smartoffice.device.service.FireHoseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消防水带服务实现类
 */
@Service
public class FireHoseServiceImpl extends ServiceImpl<FireHoseMapper, FireHose> implements FireHoseService {

    @Override
    public List<FireHoseDTO> getAllFireHoses() {
        List<FireHose> fireHoses = list();
        return fireHoses.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public FireHoseDTO getFireHoseById(Long id) {
        FireHose fireHose = getById(id);
        return fireHose != null ? convertToDTO(fireHose) : null;
    }

    @Override
    public boolean addFireHose(FireHoseDTO fireHoseDTO) {
        FireHose fireHose = new FireHose();
        BeanUtils.copyProperties(fireHoseDTO, fireHose);
        
        // 自动生成设备编号
        fireHose.setCode(generateFireHoseCode());
        
        fireHose.setCreateTime(LocalDateTime.now());
        fireHose.setUpdateTime(LocalDateTime.now());
        return save(fireHose);
    }

    @Override
    public boolean updateFireHose(FireHoseDTO fireHoseDTO) {
        FireHose fireHose = new FireHose();
        BeanUtils.copyProperties(fireHoseDTO, fireHose);
        fireHose.setUpdateTime(LocalDateTime.now());
        return updateById(fireHose);
    }

    @Override
    public boolean deleteFireHose(Long id) {
        return removeById(id);
    }

    /**
     * 生成消防水带编号
     */
    private String generateFireHoseCode() {
        // 查询当前最大编号
        QueryWrapper<FireHose> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("code").last("LIMIT 1");
        FireHose lastFireHose = getOne(queryWrapper);
        
        int nextNumber = 1;
        if (lastFireHose != null && lastFireHose.getCode() != null) {
            String lastCode = lastFireHose.getCode();
            // 提取编号中的数字部分 (FH001 -> 001 -> 1)
            if (lastCode.startsWith("FH") && lastCode.length() > 2) {
                try {
                    String numberPart = lastCode.substring(2);
                    nextNumber = Integer.parseInt(numberPart) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = 1;
                }
            }
        }
        
        // 格式化为 FH001, FH002, ... 的格式
        return String.format("FH%03d", nextNumber);
    }

    /**
     * 实体转DTO
     */
    private FireHoseDTO convertToDTO(FireHose fireHose) {
        FireHoseDTO dto = new FireHoseDTO();
        BeanUtils.copyProperties(fireHose, dto);
        
        // 计算是否超期未检查（超过一个月）
        if (fireHose.getLastCheckTime() != null) {
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            dto.setOverdue(fireHose.getLastCheckTime().isBefore(oneMonthAgo));
        } else {
            dto.setOverdue(true); // 没有检查记录视为超期
        }
        
        return dto;
    }
}