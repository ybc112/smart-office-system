package com.smartoffice.device.controller;

import com.smartoffice.common.entity.Office;
import com.smartoffice.common.entity.WorkArea;
import com.smartoffice.common.vo.Result;
import com.smartoffice.device.service.OfficeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 办公室管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/office")
@CrossOrigin
public class OfficeController {

    @Autowired
    private OfficeService officeService;

    /**
     * 获取所有办公室
     */
    @GetMapping("/list")
    public Result<List<Office>> getOfficeList() {
        try {
            List<Office> offices = officeService.getAllOffices();
            return Result.success(offices);
        } catch (Exception e) {
            log.error("获取办公室列表失败", e);
            return Result.fail("获取办公室列表失败");
        }
    }

    /**
     * 根据ID获取办公室
     */
    @GetMapping("/{id}")
    public Result<Office> getOfficeById(@PathVariable Long id) {
        try {
            Office office = officeService.getOfficeById(id);
            return Result.success(office);
        } catch (Exception e) {
            log.error("获取办公室信息失败", e);
            return Result.fail("获取办公室信息失败");
        }
    }

    /**
     * 根据楼层获取办公室
     */
    @GetMapping("/floor/{floor}")
    public Result<List<Office>> getOfficesByFloor(@PathVariable Integer floor) {
        try {
            List<Office> offices = officeService.getOfficesByFloor(floor);
            return Result.success(offices);
        } catch (Exception e) {
            log.error("获取楼层办公室失败", e);
            return Result.fail("获取楼层办公室失败");
        }
    }

    /**
     * 添加办公室
     */
    @PostMapping("/add")
    public Result<String> addOffice(@RequestBody Office office) {
        try {
            log.info("添加办公室请求: {}", office.getOfficeName());
            boolean success = officeService.addOffice(office);
            if (success) {
                return Result.success("办公室添加成功");
            } else {
                return Result.fail("办公室编号已存在");
            }
        } catch (Exception e) {
            log.error("添加办公室失败", e);
            return Result.fail("添加办公室失败");
        }
    }

    /**
     * 更新办公室
     */
    @PutMapping("/update")
    public Result<String> updateOffice(@RequestBody Office office) {
        try {
            log.info("更新办公室请求: {}", office.getOfficeName());
            boolean success = officeService.updateOffice(office);
            if (success) {
                return Result.success("办公室更新成功");
            } else {
                return Result.fail("办公室更新失败");
            }
        } catch (Exception e) {
            log.error("更新办公室失败", e);
            return Result.fail("更新办公室失败");
        }
    }

    /**
     * 删除办公室
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteOffice(@PathVariable Long id) {
        try {
            log.info("删除办公室请求: {}", id);
            boolean success = officeService.deleteOffice(id);
            if (success) {
                return Result.success("办公室删除成功");
            } else {
                return Result.fail("办公室下还有办公区，无法删除");
            }
        } catch (Exception e) {
            log.error("删除办公室失败", e);
            return Result.fail("删除办公室失败");
        }
    }

    /**
     * 获取办公室的办公区列表
     */
    @GetMapping("/{officeId}/work-areas")
    public Result<List<WorkArea>> getWorkAreasByOfficeId(@PathVariable Long officeId) {
        try {
            List<WorkArea> workAreas = officeService.getWorkAreasByOfficeId(officeId);
            return Result.success(workAreas);
        } catch (Exception e) {
            log.error("获取办公区列表失败", e);
            return Result.fail("获取办公区列表失败");
        }
    }

    /**
     * 获取所有办公区
     */
    @GetMapping("/work-areas/list")
    public Result<List<WorkArea>> getAllWorkAreas() {
        try {
            List<WorkArea> workAreas = officeService.getAllWorkAreas();
            return Result.success(workAreas);
        } catch (Exception e) {
            log.error("获取办公区列表失败", e);
            return Result.fail("获取办公区列表失败");
        }
    }

    /**
     * 添加办公区
     */
    @PostMapping("/work-area/add")
    public Result<String> addWorkArea(@RequestBody WorkArea workArea) {
        try {
            log.info("添加办公区请求: {}", workArea.getAreaName());
            boolean success = officeService.addWorkArea(workArea);
            if (success) {
                return Result.success("办公区添加成功");
            } else {
                return Result.fail("办公区添加失败");
            }
        } catch (Exception e) {
            log.error("添加办公区失败", e);
            return Result.fail("添加办公区失败");
        }
    }

    /**
     * 更新办公区
     */
    @PutMapping("/work-area/update")
    public Result<String> updateWorkArea(@RequestBody WorkArea workArea) {
        try {
            log.info("更新办公区请求: {}", workArea.getAreaName());
            boolean success = officeService.updateWorkArea(workArea);
            if (success) {
                return Result.success("办公区更新成功");
            } else {
                return Result.fail("办公区更新失败");
            }
        } catch (Exception e) {
            log.error("更新办公区失败", e);
            return Result.fail("更新办公区失败");
        }
    }

    /**
     * 删除办公区
     */
    @DeleteMapping("/work-area/{id}")
    public Result<String> deleteWorkArea(@PathVariable Long id) {
        try {
            log.info("删除办公区请求: {}", id);
            boolean success = officeService.deleteWorkArea(id);
            if (success) {
                return Result.success("办公区删除成功");
            } else {
                return Result.fail("办公区删除失败");
            }
        } catch (Exception e) {
            log.error("删除办公区失败", e);
            return Result.fail("删除办公区失败");
        }
    }

    /**
     * 获取办公室树形结构（包含办公区）
     */
    @GetMapping("/tree")
    public Result<Map<String, Object>> getOfficeTree() {
        try {
            List<Office> offices = officeService.getAllOffices();
            Map<String, Object> result = new HashMap<>();
            
            for (Office office : offices) {
                List<WorkArea> workAreas = officeService.getWorkAreasByOfficeId(office.getId());
                Map<String, Object> officeData = new HashMap<>();
                officeData.put("office", office);
                officeData.put("workAreas", workAreas);
                result.put(office.getOfficeCode(), officeData);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取办公室树形结构失败", e);
            return Result.fail("获取办公室树形结构失败");
        }
    }
}