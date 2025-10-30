package com.smartoffice.device.controller;

import com.smartoffice.common.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 位置管理控制器
 */
@RestController
@RequestMapping("/locations")
@CrossOrigin
public class LocationController {

    /**
     * 获取所有办公室和办公区信息
     */
    @GetMapping("/offices")
    public Result<List<Map<String, Object>>> getOfficeLocations() {
        try {
            List<Map<String, Object>> offices = new ArrayList<>();
            
            // 1楼办公区
            Map<String, Object> floor1 = new HashMap<>();
            floor1.put("floor", "1楼");
            floor1.put("areas", Arrays.asList("前台", "接待区", "会议室101", "会议室102", "办公区A", "办公区B", "茶水间", "卫生间"));
            offices.add(floor1);
            
            // 2楼办公区
            Map<String, Object> floor2 = new HashMap<>();
            floor2.put("floor", "2楼");
            floor2.put("areas", Arrays.asList("办公区201", "办公区202", "办公区203", "会议室201", "会议室202", "经理办公室", "茶水间", "卫生间"));
            offices.add(floor2);
            
            // 3楼办公区
            Map<String, Object> floor3 = new HashMap<>();
            floor3.put("floor", "3楼");
            floor3.put("areas", Arrays.asList("办公区301", "办公区302", "办公区303", "会议室301", "总经理办公室", "财务室", "机房", "茶水间", "卫生间"));
            offices.add(floor3);
            
            // 4楼办公区
            Map<String, Object> floor4 = new HashMap<>();
            floor4.put("floor", "4楼");
            floor4.put("areas", Arrays.asList("办公区401", "办公区402", "培训室", "会议室401", "休息室", "档案室", "茶水间", "卫生间"));
            offices.add(floor4);
            
            return Result.success(offices);
        } catch (Exception e) {
            return Result.fail("获取办公室位置信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取格式化的位置选项（用于下拉选择）
     */
    @GetMapping("/options")
    public Result<List<Map<String, String>>> getLocationOptions() {
        try {
            List<Map<String, String>> options = new ArrayList<>();
            
            // 生成所有位置选项
            String[] floors = {"1楼", "2楼", "3楼", "4楼"};
            Map<String, String[]> floorAreas = new HashMap<>();
            floorAreas.put("1楼", new String[]{"前台", "接待区", "会议室101", "会议室102", "办公区A", "办公区B", "茶水间", "卫生间"});
            floorAreas.put("2楼", new String[]{"办公区201", "办公区202", "办公区203", "会议室201", "会议室202", "经理办公室", "茶水间", "卫生间"});
            floorAreas.put("3楼", new String[]{"办公区301", "办公区302", "办公区303", "会议室301", "总经理办公室", "财务室", "机房", "茶水间", "卫生间"});
            floorAreas.put("4楼", new String[]{"办公区401", "办公区402", "培训室", "会议室401", "休息室", "档案室", "茶水间", "卫生间"});
            
            for (String floor : floors) {
                String[] areas = floorAreas.get(floor);
                for (String area : areas) {
                    Map<String, String> option = new HashMap<>();
                    option.put("value", floor + "-" + area);
                    option.put("label", floor + "-" + area);
                    option.put("floor", floor);
                    option.put("area", area);
                    options.add(option);
                }
            }
            
            return Result.success(options);
        } catch (Exception e) {
            return Result.fail("获取位置选项失败：" + e.getMessage());
        }
    }
}