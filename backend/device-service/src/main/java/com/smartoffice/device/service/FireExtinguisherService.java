package com.smartoffice.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartoffice.device.dto.FireExtinguisherDTO;
import com.smartoffice.device.entity.FireExtinguisher;

import java.util.List;

/**
 * 灭火器服务接口
 */
public interface FireExtinguisherService extends IService<FireExtinguisher> {

    /**
     * 获取所有灭火器列表
     */
    List<FireExtinguisherDTO> getAllFireExtinguishers();

    /**
     * 根据ID获取灭火器
     */
    FireExtinguisherDTO getFireExtinguisherById(Long id);

    /**
     * 添加灭火器
     */
    boolean addFireExtinguisher(FireExtinguisherDTO fireExtinguisherDTO);

    /**
     * 更新灭火器
     */
    boolean updateFireExtinguisher(FireExtinguisherDTO fireExtinguisherDTO);

    /**
     * 删除灭火器
     */
    boolean deleteFireExtinguisher(Long id);
}