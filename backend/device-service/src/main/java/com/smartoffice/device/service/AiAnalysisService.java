package com.smartoffice.device.service;

import com.smartoffice.device.dto.AiAnalysisDTO;

/**
 * AI分析服务接口
 */
public interface AiAnalysisService {
    
    /**
     * 设备配置总结分析
     */
    AiAnalysisDTO analyzeDeviceConfiguration();
    
    /**
     * 防火设备评估
     */
    AiAnalysisDTO analyzeFireSafetyDevices();
    
    /**
     * 火灾风险评估
     */
    AiAnalysisDTO analyzeFireRisk();
}