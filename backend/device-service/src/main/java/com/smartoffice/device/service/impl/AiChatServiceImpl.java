package com.smartoffice.device.service.impl;

import com.smartoffice.device.config.AiConfig;
import com.smartoffice.device.dto.AiChatRequestDTO;
import com.smartoffice.device.dto.AiChatResponseDTO;
import com.smartoffice.device.dto.AiRequestDTO;
import com.smartoffice.device.dto.AiResponseDTO;
import com.smartoffice.device.service.AiChatService;
import com.smartoffice.device.service.AiFallbackService;
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
    
    @Autowired
    private AiConfig aiConfig;
    
    @Autowired
    private AiFallbackService aiFallbackService;
    
    @Override
    public AiChatResponseDTO chat(AiChatRequestDTO request) {
        log.info("收到AI聊天请求: {}", request.getMessage());
        
        try {
            // 检查是否应该直接使用降级策略
            if (aiFallbackService.shouldUseFallback()) {
                log.warn("AI服务处于降级模式，直接返回降级响应");
                String fallbackResponse = aiFallbackService.getFallbackResponse(request.getMessage(), "CIRCUIT_BREAKER");
                
                AiChatResponseDTO response = new AiChatResponseDTO();
                response.setMessage(fallbackResponse);
                response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                response.setSessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString());
                response.setSuccess(true);
                response.setFallback(true);
                
                return response;
            }
            
            // 构建系统提示词
            String systemPrompt = buildSystemPrompt();
            
            // 调用AI API
            String aiResponse = callAiApi(systemPrompt, request.getMessage());
            
            if (aiResponse != null && !aiResponse.isEmpty()) {
                // 构建响应
                AiChatResponseDTO response = new AiChatResponseDTO();
                response.setMessage(aiResponse);
                response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                response.setSessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString());
                response.setSuccess(true);
                response.setFallback(false);
                
                return response;
            } else {
                // AI返回空响应，使用降级
                String fallbackResponse = aiFallbackService.getFallbackResponse(request.getMessage(), "EMPTY_RESPONSE");
                
                AiChatResponseDTO response = new AiChatResponseDTO();
                response.setMessage(fallbackResponse);
                response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                response.setSessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString());
                response.setSuccess(true);
                response.setFallback(true);
                
                return response;
            }
            
        } catch (Exception e) {
            log.error("AI聊天服务失败", e);
            
            // 根据异常类型确定降级策略
            String errorType = determineErrorType(e);
            String fallbackResponse = aiFallbackService.getFallbackResponse(request.getMessage(), errorType);
            
            // 返回降级响应
            AiChatResponseDTO response = new AiChatResponseDTO();
            response.setMessage(fallbackResponse);
            response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            response.setSessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString());
            response.setSuccess(true);
            response.setFallback(true);
            response.setErrorMessage(e.getMessage());
            
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
            // 检查API密钥是否配置
            if (isApiKeyConfigured()) {
                log.info("开始调用AI API - 用户消息: {}", userMessage);
                long startTime = System.currentTimeMillis();
                
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
                    .onStatus(status -> !status.is2xxSuccessful(), 
                        clientResponse -> {
                            log.error("AI API返回错误状态: {}", clientResponse.statusCode());
                            return clientResponse.bodyToMono(String.class)
                                .doOnNext(body -> log.error("错误响应体: {}", body))
                                .then(Mono.error(new RuntimeException("AI API调用失败: " + clientResponse.statusCode())));
                        })
                    .bodyToMono(AiResponseDTO.class)
                    .timeout(Duration.ofSeconds(aiConfig.getTimeout() / 1000))
                    .doOnError(throwable -> {
                        long duration = System.currentTimeMillis() - startTime;
                        log.error("AI API调用失败 - 耗时: {}ms, 错误: {}", duration, throwable.getMessage());
                    })
                    .retryWhen(reactor.util.retry.Retry.backoff(aiConfig.getRetryCount(), Duration.ofMillis(aiConfig.getRetryDelay()))
                        .filter(throwable -> !(throwable instanceof java.util.concurrent.TimeoutException))
                        .doBeforeRetry(retrySignal -> 
                            log.warn("AI API调用重试 - 第{}次重试, 错误: {}", retrySignal.totalRetries() + 1, retrySignal.failure().getMessage())));
                
                AiResponseDTO response = responseMono.block();
                
                long duration = System.currentTimeMillis() - startTime;
                log.info("AI API调用成功 - 耗时: {}ms", duration);
                
                if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                    String aiReply = response.getChoices().get(0).getMessage().getContent();
                    log.info("AI回复: {}", aiReply);
                    return aiReply;
                } else {
                    log.warn("AI API返回空响应，使用本地智能回复");
                }
            } else {
                log.info("API密钥未配置，使用本地智能回复");
            }
            
            // 如果API密钥未配置或AI服务不可用，返回基于规则的智能回复
            return generateIntelligentResponse(userMessage);
            
        } catch (Exception e) {
            log.error("调用AI API失败", e);
            
            // 记录详细的错误信息用于监控
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                log.error("AI API超时 - 超时时间: {}秒", aiConfig.getTimeout() / 1000);
            } else if (e.getMessage().contains("Connection refused")) {
                log.error("AI API连接被拒绝 - 请检查网络连接和API服务状态");
            } else if (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized")) {
                log.error("AI API认证失败 - 请检查API密钥是否正确");
            }
            
            // 如果AI服务不可用，返回基于规则的智能回复
            return generateIntelligentResponse(userMessage);
        }
    }
    
    /**
     * 检查API密钥是否已配置
     */
    private boolean isApiKeyConfigured() {
        return aiConfig.getApiKey() != null && 
               !aiConfig.getApiKey().equals("sk-your-api-key-here") &&
               !aiConfig.getApiKey().trim().isEmpty();
    }
    
    /**
     * 生成智能回复（增强版本地回复）
     */
    private String generateIntelligentResponse(String userMessage) {
        String message = userMessage.toLowerCase();
        
        // 问候语处理
        if (message.contains("你好") || message.contains("hello") || message.contains("hi")) {
            return "您好！我是智慧办公楼系统的AI助手。我可以帮助您了解系统功能、设备管理、告警处理等方面的问题。请问有什么可以帮助您的吗？";
        }
        
        // 介绍相关
        if (message.contains("介绍") || message.contains("是什么") || message.contains("功能")) {
            return "智慧办公楼系统是一个集成了物联网传感器、设备监控、告警管理和数据分析的综合管理平台。主要功能包括：\n\n" +
                   "🏢 **设备监控**：实时监控温湿度、光照、火焰等环境参数\n" +
                   "🚨 **告警管理**：智能告警检测和处理\n" +
                   "👥 **用户管理**：多角色权限管理\n" +
                   "📊 **数据分析**：历史数据统计和趋势分析\n" +
                   "⚙️ **系统配置**：灵活的参数配置和阈值设置\n\n" +
                   "如需了解具体功能，请告诉我您感兴趣的模块！";
        }
        
        // 设备相关
        if (message.contains("设备") || message.contains("传感器") || message.contains("监控")) {
            return "关于设备管理，系统提供以下功能：\n\n" +
                   "📱 **设备监控**：在\"设备监控\"页面可以查看所有设备的实时状态和数据\n" +
                   "🔧 **设备管理**：在\"设备管理\"页面可以添加、编辑和配置设备\n" +
                   "📊 **数据展示**：支持实时数据图表和历史趋势分析\n" +
                   "🎛️ **远程控制**：可以远程控制RGB灯、蜂鸣器等设备\n\n" +
                   "目前支持的传感器类型：温湿度传感器、光照传感器、火焰传感器等。";
        }
        
        // 告警相关
        if (message.contains("告警") || message.contains("报警") || message.contains("警报")) {
            return "告警管理功能帮助您及时发现和处理异常情况：\n\n" +
                   "🚨 **实时告警**：当传感器数据超出设定阈值时自动触发告警\n" +
                   "📋 **告警列表**：在\"告警管理\"页面查看所有告警记录\n" +
                   "⚙️ **阈值配置**：在\"系统配置\"页面设置各类传感器的告警阈值\n" +
                   "🔔 **通知机制**：支持声光告警和系统通知\n\n" +
                   "如遇紧急情况，请及时联系相关管理人员处理！";
        }
        
        // 用户管理相关
        if (message.contains("用户") || message.contains("权限") || message.contains("登录")) {
            return "用户管理功能提供完整的权限控制：\n\n" +
                   "👤 **用户管理**：管理员可以在\"用户管理\"页面添加、编辑用户信息\n" +
                   "🔐 **权限控制**：支持多角色权限管理，确保系统安全\n" +
                   "🔑 **登录认证**：安全的用户登录和会话管理\n" +
                   "📝 **操作日志**：记录用户操作历史\n\n" +
                   "如需修改权限或遇到登录问题，请联系系统管理员。";
        }
        
        // 数据和统计相关
        if (message.contains("数据") || message.contains("统计") || message.contains("报表") || message.contains("分析")) {
            return "系统提供丰富的数据分析功能：\n\n" +
                   "📊 **实时数据**：首页仪表板显示关键指标和实时状态\n" +
                   "📈 **历史趋势**：查看各类传感器数据的历史变化趋势\n" +
                   "📋 **数据报表**：生成详细的数据统计报表\n" +
                   "🔍 **数据查询**：支持按时间范围和设备筛选数据\n\n" +
                   "所有数据都会自动保存，您可以随时查看历史记录。";
        }
        
        // 配置相关
        if (message.contains("配置") || message.contains("设置") || message.contains("参数")) {
            return "系统配置功能让您灵活调整系统参数：\n\n" +
                   "⚙️ **阈值设置**：配置温度、湿度、光照等传感器的告警阈值\n" +
                   "⏱️ **采集间隔**：设置数据采集的时间间隔\n" +
                   "🔧 **设备参数**：配置设备的工作参数\n" +
                   "💾 **配置保存**：所有配置会自动同步到硬件设备\n\n" +
                   "修改配置后，系统会自动推送到相关设备。";
        }
        
        // 帮助和支持
        if (message.contains("帮助") || message.contains("支持") || message.contains("问题")) {
            return "如需更多帮助，您可以：\n\n" +
                   "📖 **查看文档**：系统提供详细的使用说明文档\n" +
                   "🎯 **功能导航**：使用左侧菜单导航到各个功能模块\n" +
                   "💬 **在线咨询**：通过AI助手获取即时帮助\n" +
                   "📞 **技术支持**：联系系统管理员获取技术支持\n\n" +
                   "我会尽力为您解答各种使用问题！";
        }
        
        // 默认回复
        return "感谢您的提问！我是智慧办公楼系统的AI助手。\n\n" +
               "我可以帮助您了解：\n" +
               "• 🏢 系统功能介绍\n" +
               "• 📱 设备监控和管理\n" +
               "• 🚨 告警处理\n" +
               "• 👥 用户权限管理\n" +
               "• 📊 数据分析和报表\n" +
               "• ⚙️ 系统配置\n\n" +
               "请告诉我您想了解哪个方面，我会为您详细介绍！";
    }

    /**
     * 根据异常类型确定错误类型
     */
    private String determineErrorType(Exception e) {
        if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
            return "TIMEOUT";
        } else if (e.getMessage().contains("Connection refused")) {
            return "CONNECTION_ERROR";
        } else if (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized")) {
            return "AUTH_ERROR";
        } else if (e.getMessage().contains("429") || e.getMessage().contains("Rate limit")) {
            return "RATE_LIMIT";
        } else {
            return "UNKNOWN_ERROR";
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