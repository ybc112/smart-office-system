package com.smartoffice.device.dto;

import lombok.Data;
import java.util.List;

/**
 * AI分析结果DTO
 */
@Data
public class AiAnalysisDTO {
    
    private String analysisType; // 分析类型：设备配置总结、防火设备评估、火灾风险评估
    private String summary; // 总结
    private List<String> recommendations; // 建议
    private String riskLevel; // 风险等级：低、中、高
    private List<Issue> issues; // 发现的问题
    
    @Data
    public static class Issue {
        private String type; // 问题类型
        private String description; // 问题描述
        private String severity; // 严重程度：低、中、高
        private String suggestion; // 解决建议
    }
}