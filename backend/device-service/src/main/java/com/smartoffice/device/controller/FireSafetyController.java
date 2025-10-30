package com.smartoffice.device.controller;

import com.smartoffice.common.vo.Result;
import com.smartoffice.device.dto.FireExtinguisherDTO;
import com.smartoffice.device.dto.FireHoseDTO;
import com.smartoffice.device.service.FireExtinguisherService;
import com.smartoffice.device.service.FireHoseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消防安全管理控制器
 */
@RestController
@RequestMapping("/fire-safety")
@CrossOrigin
public class FireSafetyController {

    @Autowired
    private FireHoseService fireHoseService;

    @Autowired
    private FireExtinguisherService fireExtinguisherService;

    // ==================== 消防水带管理 ====================

    /**
     * 获取所有消防水带
     */
    @GetMapping("/fire-hoses")
    public Result<List<FireHoseDTO>> getAllFireHoses() {
        try {
            List<FireHoseDTO> fireHoses = fireHoseService.getAllFireHoses();
            return Result.success(fireHoses);
        } catch (Exception e) {
            return Result.fail("获取消防水带列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取消防水带
     */
    @GetMapping("/fire-hoses/{id}")
    public Result<FireHoseDTO> getFireHoseById(@PathVariable Long id) {
        try {
            FireHoseDTO fireHose = fireHoseService.getFireHoseById(id);
            if (fireHose != null) {
                return Result.success(fireHose);
            } else {
                return Result.fail("消防水带不存在");
            }
        } catch (Exception e) {
            return Result.fail("获取消防水带失败：" + e.getMessage());
        }
    }

    /**
     * 添加消防水带
     */
    @PostMapping("/fire-hoses")
    public Result<String> addFireHose(@RequestBody FireHoseDTO fireHoseDTO) {
        try {
            boolean success = fireHoseService.addFireHose(fireHoseDTO);
            if (success) {
                return Result.success("消防水带添加成功");
            } else {
                return Result.fail("消防水带添加失败");
            }
        } catch (Exception e) {
            return Result.fail("消防水带添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新消防水带
     */
    @PutMapping("/fire-hoses")
    public Result<String> updateFireHose(@RequestBody FireHoseDTO fireHoseDTO) {
        try {
            boolean success = fireHoseService.updateFireHose(fireHoseDTO);
            if (success) {
                return Result.success("消防水带更新成功");
            } else {
                return Result.fail("消防水带更新失败");
            }
        } catch (Exception e) {
            return Result.fail("消防水带更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除消防水带
     */
    @DeleteMapping("/fire-hoses/{id}")
    public Result<String> deleteFireHose(@PathVariable Long id) {
        try {
            boolean success = fireHoseService.deleteFireHose(id);
            if (success) {
                return Result.success("消防水带删除成功");
            } else {
                return Result.fail("消防水带删除失败");
            }
        } catch (Exception e) {
            return Result.fail("消防水带删除失败：" + e.getMessage());
        }
    }

    // ==================== 灭火器管理 ====================

    /**
     * 获取所有灭火器
     */
    @GetMapping("/fire-extinguishers")
    public Result<List<FireExtinguisherDTO>> getAllFireExtinguishers() {
        try {
            List<FireExtinguisherDTO> fireExtinguishers = fireExtinguisherService.getAllFireExtinguishers();
            return Result.success(fireExtinguishers);
        } catch (Exception e) {
            return Result.fail("获取灭火器列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取灭火器
     */
    @GetMapping("/fire-extinguishers/{id}")
    public Result<FireExtinguisherDTO> getFireExtinguisherById(@PathVariable Long id) {
        try {
            FireExtinguisherDTO fireExtinguisher = fireExtinguisherService.getFireExtinguisherById(id);
            if (fireExtinguisher != null) {
                return Result.success(fireExtinguisher);
            } else {
                return Result.fail("灭火器不存在");
            }
        } catch (Exception e) {
            return Result.fail("获取灭火器失败：" + e.getMessage());
        }
    }

    /**
     * 添加灭火器
     */
    @PostMapping("/fire-extinguishers")
    public Result<String> addFireExtinguisher(@RequestBody FireExtinguisherDTO fireExtinguisherDTO) {
        try {
            boolean success = fireExtinguisherService.addFireExtinguisher(fireExtinguisherDTO);
            if (success) {
                return Result.success("灭火器添加成功");
            } else {
                return Result.fail("灭火器添加失败");
            }
        } catch (Exception e) {
            return Result.fail("灭火器添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新灭火器
     */
    @PutMapping("/fire-extinguishers")
    public Result<String> updateFireExtinguisher(@RequestBody FireExtinguisherDTO fireExtinguisherDTO) {
        try {
            boolean success = fireExtinguisherService.updateFireExtinguisher(fireExtinguisherDTO);
            if (success) {
                return Result.success("灭火器更新成功");
            } else {
                return Result.fail("灭火器更新失败");
            }
        } catch (Exception e) {
            return Result.fail("灭火器更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除灭火器
     */
    @DeleteMapping("/fire-extinguishers/{id}")
    public Result<String> deleteFireExtinguisher(@PathVariable Long id) {
        try {
            boolean success = fireExtinguisherService.deleteFireExtinguisher(id);
            if (success) {
                return Result.success("灭火器删除成功");
            } else {
                return Result.fail("灭火器删除失败");
            }
        } catch (Exception e) {
            return Result.fail("灭火器删除失败：" + e.getMessage());
        }
    }
}