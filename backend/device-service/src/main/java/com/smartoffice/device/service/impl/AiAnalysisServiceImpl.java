package com.smartoffice.device.service.impl;

import com.alibaba.fastjson2.JSON;
import com.smartoffice.device.dto.*;
import com.smartoffice.device.entity.FireExtinguisher;
import com.smartoffice.device.entity.FireHose;
import com.smartoffice.device.mapper.DeviceInfoMapper;
import com.smartoffice.device.mapper.FireExtinguisherMapper;
import com.smartoffice.device.mapper.FireHoseMapper;
import com.smartoffice.device.service.AiAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AI分析服务实现类
 */
@Slf4j
@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {
    
    @Autowired
    private WebClient aiWebClient;
    
    @Autowired
    private FireExtinguisherMapper fireExtinguisherMapper;
    
    @Autowired
    private FireHoseMapper fireHoseMapper;
    
    @Autowired
    private DeviceInfoMapper deviceInfoMapper;
    
    @Override
    public AiAnalysisDTO analyzeDeviceConfiguration() {
        try {
            // 获取设备数据
            List<FireExtinguisher> extinguishers = fireExtinguisherMapper.selectList(null);
            List<FireHose> hoses = fireHoseMapper.selectList(null);
            
            // 构建分析提示
            String prompt = buildDeviceConfigPrompt(extinguishers, hoses);
            
            // 调用AI分析
            String aiResponse = callAiApi(prompt);
            
            // 解析AI响应并构建结果
            return parseDeviceConfigResponse(aiResponse);
            
        } catch (Exception e) {
            log.error("设备配置分析失败", e);
            return createErrorResponse("设备配置分析", "分析过程中发生错误");
        }
    }
    
    @Override
    public AiAnalysisDTO analyzeFireSafetyDevices() {
        try {
            // 获取防火设备数据
            List<FireExtinguisher> extinguishers = fireExtinguisherMapper.selectList(null);
            List<FireHose> hoses = fireHoseMapper.selectList(null);
            
            // 构建分析提示
            String prompt = buildFireSafetyPrompt(extinguishers, hoses);
            
            // 调用AI分析
            String aiResponse = callAiApi(prompt);
            
            // 解析AI响应并构建结果
            return parseFireSafetyResponse(aiResponse);
            
        } catch (Exception e) {
            log.error("防火设备评估失败", e);
            return createErrorResponse("防火设备评估", "评估过程中发生错误");
        }
    }
    
    @Override
    public AiAnalysisDTO analyzeFireRisk() {
        try {
            // 获取所有相关数据
            List<FireExtinguisher> extinguishers = fireExtinguisherMapper.selectList(null);
            List<FireHose> hoses = fireHoseMapper.selectList(null);
            
            // 构建分析提示
            String prompt = buildFireRiskPrompt(extinguishers, hoses);
            
            // 调用AI分析
            String aiResponse = callAiApi(prompt);
            
            // 解析AI响应并构建结果
            return parseFireRiskResponse(aiResponse);
            
        } catch (Exception e) {
            log.error("火灾风险评估失败", e);
            return createErrorResponse("火灾风险评估", "评估过程中发生错误");
        }
    }
    
    /**
     * 调用AI API
     */
    private String callAiApi(String prompt) {
        try {
            AiRequestDTO request = new AiRequestDTO();
            request.setMessages(Arrays.asList(
                new AiRequestDTO.Message("system", "你是一个专业的智能办公楼安全管理专家，请根据提供的数据进行专业分析。"),
                new AiRequestDTO.Message("user", prompt)
            ));
            
            Mono<AiResponseDTO> responseMono = aiWebClient
                .post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiResponseDTO.class)
                .timeout(Duration.ofSeconds(30));
            
            AiResponseDTO response = responseMono.block();
            
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
            
            return "AI服务暂时不可用，请稍后重试。";
            
        } catch (Exception e) {
            log.error("调用AI API失败", e);
            return "AI服务调用失败，请检查网络连接。";
        }
    }
    
    /**
     * 构建设备配置分析提示
     */
    private String buildDeviceConfigPrompt(List<FireExtinguisher> extinguishers, List<FireHose> hoses) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下智能办公楼的设备配置情况：\n\n");
        
        prompt.append("灭火器设备（共").append(extinguishers.size()).append("台）：\n");
        for (FireExtinguisher ext : extinguishers) {
            prompt.append("- 编号：").append(ext.getCode())
                  .append("，位置：").append(ext.getLocation())
                  .append("，状态：").append(ext.getStatus())
                  .append("，最后检查：").append(ext.getLastPressureCheckTime()).append("\n");
        }
        
        prompt.append("\n消防水带设备（共").append(hoses.size()).append("套）：\n");
        for (FireHose hose : hoses) {
            prompt.append("- 编号：").append(hose.getCode())
                  .append("，位置：").append(hose.getLocation())
                  .append("，状态：").append(hose.getStatus())
                  .append("，最后检查：").append(hose.getLastCheckTime()).append("\n");
        }
        
        prompt.append("\n请从以下角度进行分析：\n");
        prompt.append("1. 设备数量是否充足\n");
        prompt.append("2. 设备分布是否合理\n");
        prompt.append("3. 设备状态是否良好\n");
        prompt.append("4. 维护检查是否及时\n");
        prompt.append("5. 提出改进建议\n");
        
        return prompt.toString();
    }
    
    /**
     * 构建防火设备评估提示
     */
    private String buildFireSafetyPrompt(List<FireExtinguisher> extinguishers, List<FireHose> hoses) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请对以下防火安全设备进行专业评估：\n\n");
        
        // 统计设备状态
        long normalExtinguishers = extinguishers.stream().filter(e -> "正常".equals(e.getStatus())).count();
        long normalHoses = hoses.stream().filter(h -> "正常".equals(h.getStatus())).count();
        
        prompt.append("设备概况：\n");
        prompt.append("- 灭火器总数：").append(extinguishers.size()).append("台，正常：").append(normalExtinguishers).append("台\n");
        prompt.append("- 消防水带总数：").append(hoses.size()).append("套，正常：").append(normalHoses).append("套\n\n");
        
        prompt.append("详细设备信息：\n");
        prompt.append(buildDeviceDetails(extinguishers, hoses));
        
        prompt.append("\n请从消防安全角度评估：\n");
        prompt.append("1. 设备配置是否符合消防规范\n");
        prompt.append("2. 设备覆盖范围是否充分\n");
        prompt.append("3. 设备维护状况评价\n");
        prompt.append("4. 潜在安全隐患识别\n");
        prompt.append("5. 消防安全改进建议\n");
        
        return prompt.toString();
    }
    
    /**
     * 构建火灾风险评估提示
     */
    private String buildFireRiskPrompt(List<FireExtinguisher> extinguishers, List<FireHose> hoses) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于以下数据进行火灾风险评估：\n\n");
        
        prompt.append(buildDeviceDetails(extinguishers, hoses));
        
        prompt.append("\n请进行综合火灾风险评估：\n");
        prompt.append("1. 当前火灾风险等级（低/中/高）\n");
        prompt.append("2. 主要风险因素分析\n");
        prompt.append("3. 应急响应能力评估\n");
        prompt.append("4. 风险控制措施建议\n");
        prompt.append("5. 紧急情况处理预案\n");
        
        return prompt.toString();
    }
    
    /**
     * 构建设备详细信息
     */
    private String buildDeviceDetails(List<FireExtinguisher> extinguishers, List<FireHose> hoses) {
        StringBuilder details = new StringBuilder();
        
        details.append("灭火器详情：\n");
        for (FireExtinguisher ext : extinguishers) {
            details.append("  ").append(ext.getCode()).append(" - ")
                   .append(ext.getLocation()).append(" - ")
                   .append(ext.getStatus()).append("\n");
        }
        
        details.append("\n消防水带详情：\n");
        for (FireHose hose : hoses) {
            details.append("  ").append(hose.getCode()).append(" - ")
                   .append(hose.getLocation()).append(" - ")
                   .append(hose.getStatus()).append("\n");
        }
        
        return details.toString();
    }
    
    /**
     * 解析设备配置响应
     */
    private AiAnalysisDTO parseDeviceConfigResponse(String aiResponse) {
        AiAnalysisDTO result = new AiAnalysisDTO();
        result.setAnalysisType("设备配置总结");
        result.setSummary(aiResponse);
        result.setRiskLevel("中");
        result.setRecommendations(Arrays.asList(
            "定期检查设备状态",
            "优化设备布局",
            "加强维护管理"
        ));
        result.setIssues(new ArrayList<>());
        
        return result;
    }
    
    /**
     * 解析防火设备响应
     */
    private AiAnalysisDTO parseFireSafetyResponse(String aiResponse) {
        AiAnalysisDTO result = new AiAnalysisDTO();
        result.setAnalysisType("防火设备评估");
        result.setSummary(aiResponse);
        result.setRiskLevel("中");
        result.setRecommendations(Arrays.asList(
            "增加设备巡检频率",
            "完善应急预案",
            "加强人员培训"
        ));
        result.setIssues(new ArrayList<>());
        
        return result;
    }
    
    /**
     * 解析火灾风险响应
     */
    private AiAnalysisDTO parseFireRiskResponse(String aiResponse) {
        AiAnalysisDTO result = new AiAnalysisDTO();
        result.setAnalysisType("火灾风险评估");
        result.setSummary(aiResponse);
        result.setRiskLevel("中");
        result.setRecommendations(Arrays.asList(
            "建立风险监控机制",
            "制定应急响应流程",
            "定期进行安全演练"
        ));
        result.setIssues(new ArrayList<>());
        
        return result;
    }
    
    /**
     * 创建错误响应
     */
    private AiAnalysisDTO createErrorResponse(String analysisType, String errorMessage) {
        AiAnalysisDTO result = new AiAnalysisDTO();
        result.setAnalysisType(analysisType);
        result.setSummary(errorMessage);
        result.setRiskLevel("未知");
        result.setRecommendations(Arrays.asList("请稍后重试"));
        result.setIssues(new ArrayList<>());
        
        return result;
    }
}