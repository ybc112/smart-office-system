package com.smartoffice.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartoffice.device.dto.FireHoseDTO;
import com.smartoffice.device.entity.FireHose;

import java.util.List;

/**
 * 消防水带服务接口
 */
public interface FireHoseService extends IService<FireHose> {

    /**
     * 获取所有消防水带列表
     */
    List<FireHoseDTO> getAllFireHoses();

    /**
     * 根据ID获取消防水带
     */
    FireHoseDTO getFireHoseById(Long id);

    /**
     * 添加消防水带
     */
    boolean addFireHose(FireHoseDTO fireHoseDTO);

    /**
     * 更新消防水带
     */
    boolean updateFireHose(FireHoseDTO fireHoseDTO);

    /**
     * 删除消防水带
     */
    boolean deleteFireHose(Long id);
}