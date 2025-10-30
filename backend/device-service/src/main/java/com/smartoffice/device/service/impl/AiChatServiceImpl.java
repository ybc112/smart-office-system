package com.smartoffice.device.service.impl;

import com.smartoffice.device.dto.AiChatRequestDTO;
import com.smartoffice.device.dto.AiChatResponseDTO;
import com.smartoffice.device.dto.AiRequestDTO;
import com.smartoffice.device.dto.AiResponseDTO;
import com.smartoffice.device.service.AiChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * AI聊天服务实现类
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {
    
    @Autowired
    private WebClient aiWebClient;
    
    @Override
    public AiChatResponseDTO chat(AiChatRequestDTO request) {
        try {
            // 构建系统提示词
            String systemPrompt = buildSystemPrompt();
            
            // 调用AI API
            String aiResponse = callAiApi(systemPrompt, request.getMessage());
            
            // 构建响应
            AiChatResponseDTO response = new AiChatResponseDTO();
            response.setMessage(aiResponse);
            response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            response.setSessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString());
            response.setSuccess(true);
            
            return response;
            
        } catch (Exception e) {
            log.error("AI聊天服务失败", e);
            
            // 返回错误响应
            AiChatResponseDTO response = new AiChatResponseDTO();
            response.setMessage("抱歉，我暂时无法回复您的问题。请稍后重试或联系系统管理员。");
            response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            response.setSessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString());
            response.setSuccess(false);
            
            return response;
        }
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return "你是智慧办公楼系统的AI助手，专门为用户提供关于智慧办公楼管理系统的帮助和指导。" +
               "你的主要职责包括：\n" +
               "1. 解答用户关于系统功能的问题\n" +
               "2. 指导用户如何使用各个功能模块\n" +
               "3. 提供设备管理、告警处理、用户管理等方面的建议\n" +
               "4. 解释系统数据和报告的含义\n" +
               "5. 协助用户解决使用过程中遇到的问题\n\n" +
               "系统主要功能模块包括：\n" +
               "- 首页仪表板：显示系统概览和实时数据\n" +
               "- 设备监控：监控温湿度、光照、火焰等传感器数据\n" +
               "- 设备管理：管理W601智能设备的配置和状态\n" +
               "- 告警管理：处理和查看系统告警信息\n" +
               "- 办公室管理：管理办公室信息和设备分配\n" +
               "- 用户管理：管理系统用户和权限\n" +
               "- 系统配置：配置系统参数和阈值\n\n" +
               "请用友好、专业的语气回答用户问题，并尽可能提供具体的操作指导。如果遇到不确定的问题，请建议用户联系系统管理员。";
    }
    
    /**
     * 调用AI API
     */
    private String callAiApi(String systemPrompt, String userMessage) {
        try {
            AiRequestDTO request = new AiRequestDTO();
            request.setModel("deepseek-chat");
            request.setMessages(Arrays.asList(
                new AiRequestDTO.Message("system", systemPrompt),
                new AiRequestDTO.Message("user", userMessage)
            ));
            request.setTemperature(0.7);
            request.setMaxTokens(1000);
            
            log.info("发送AI请求: {}", userMessage);
            
            Mono<AiResponseDTO> responseMono = aiWebClient
                .post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiResponseDTO.class)
                .timeout(Duration.ofSeconds(30));
            
            AiResponseDTO response = responseMono.block();
            
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String aiReply = response.getChoices().get(0).getMessage().getContent();
                log.info("AI回复: {}", aiReply);
                return aiReply;
            }
            
            return "AI服务暂时不可用，请稍后重试。";
            
        } catch (Exception e) {
            log.error("调用AI API失败", e);
            
            // 如果AI服务不可用，返回基于规则的回复
            return generateFallbackResponse(userMessage);
        }
    }
    
    /**
     * 生成备用回复（当AI服务不可用时）
     */
    private String generateFallbackResponse(String userMessage) {
        String message = userMessage.toLowerCase();
        
        if (message.contains("设备") || message.contains("传感器")) {
            return "关于设备管理，您可以在\"设备监控\"页面查看所有设备的实时状态，在\"设备管理\"页面进行设备配置。如需更多帮助，请查看系统帮助文档。";
        } else if (message.contains("告警") || message.contains("报警")) {
            return "告警管理功能可以帮您查看和处理系统告警。您可以在\"告警管理\"页面查看告警历史，设置告警规则。如有紧急情况，请及时联系相关人员。";
        } else if (message.contains("用户") || message.contains("权限")) {
            return "用户管理功能允许管理员添加、编辑用户信息和设置权限。请在\"用户管理\"页面进行相关操作。如需修改权限，请联系系统管理员。";
        } else if (message.contains("数据") || message.contains("统计")) {
            return "系统会实时收集和展示各种数据，您可以在首页仪表板查看概览信息，在各个功能模块查看详细数据和统计报告。";
        } else {
            return "感谢您的提问！我是智慧办公楼系统的AI助手。您可以询问关于系统功能、设备管理、告警处理等方面的问题。如需更详细的帮助，请查看系统帮助文档或联系管理员。";
        }
    }
}