package com.smartoffice.device.controller;

import com.smartoffice.common.vo.Result;
import com.smartoffice.device.dto.AiAnalysisDTO;
import com.smartoffice.device.service.AiAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@CrossOrigin
public class AiAnalysisController {
    
    @Autowired
    private AiAnalysisService aiAnalysisService;
    
    /**
     * 设备配置总结分析
     */
    @GetMapping("/analyze/device-config")
    public Result<AiAnalysisDTO> analyzeDeviceConfiguration() {
        try {
            log.info("开始设备配置分析");
            AiAnalysisDTO result = aiAnalysisService.analyzeDeviceConfiguration();
            return Result.success("设备配置分析完成", result);
        } catch (Exception e) {
            log.error("设备配置分析失败", e);
            return Result.fail("设备配置分析失败");
        }
    }
    
    /**
     * 防火设备评估
     */
    @GetMapping("/analyze/fire-safety")
    public Result<AiAnalysisDTO> analyzeFireSafetyDevices() {
        try {
            log.info("开始防火设备评估");
            AiAnalysisDTO result = aiAnalysisService.analyzeFireSafetyDevices();
            return Result.success("防火设备评估完成", result);
        } catch (Exception e) {
            log.error("防火设备评估失败", e);
            return Result.fail("防火设备评估失败");
        }
    }
    
    /**
     * 火灾风险评估
     */
    @GetMapping("/analyze/fire-risk")
    public Result<AiAnalysisDTO> analyzeFireRisk() {
        try {
            log.info("开始火灾风险评估");
            AiAnalysisDTO result = aiAnalysisService.analyzeFireRisk();
            return Result.success("火灾风险评估完成", result);
        } catch (Exception e) {
            log.error("火灾风险评估失败", e);
            return Result.fail("火灾风险评估失败");
        }
    }
}