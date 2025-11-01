package com.smartoffice.device.controller;

import com.smartoffice.device.config.AiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * AI服务健康检查控制器
 */
@RestController
@RequestMapping("/api/ai")
@Slf4j
public class AiHealthController {

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private WebClient aiWebClient;

    /**
     * AI服务健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 简单的连通性测试
            String testResponse = testAiConnection();
            
            long duration = System.currentTimeMillis() - startTime;
            
            health.put("status", "UP");
            health.put("service", "DeepSeek AI");
            health.put("apiUrl", aiConfig.getApiUrl());
            health.put("timeout", aiConfig.getTimeout() + "ms");
            health.put("retryCount", aiConfig.getRetryCount());
            health.put("retryDelay", aiConfig.getRetryDelay() + "ms");
            health.put("responseTime", duration + "ms");
            health.put("apiKeyConfigured", isApiKeyConfigured());
            health.put("timestamp", System.currentTimeMillis());
            
            if (testResponse != null) {
                health.put("connectionTest", "SUCCESS");
            } else {
                health.put("connectionTest", "FAILED");
                health.put("status", "DOWN");
            }
            
        } catch (Exception e) {
            log.error("AI健康检查失败", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
        }
        
        return ResponseEntity.ok(health);
    }

    /**
     * 获取AI服务配置信息
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("apiUrl", aiConfig.getApiUrl());
        config.put("timeout", aiConfig.getTimeout());
        config.put("retryCount", aiConfig.getRetryCount());
        config.put("retryDelay", aiConfig.getRetryDelay());
        config.put("apiKeyConfigured", isApiKeyConfigured());
        
        return ResponseEntity.ok(config);
    }

    /**
     * 测试AI连接
     */
    private String testAiConnection() {
        try {
            if (!isApiKeyConfigured()) {
                return null;
            }

            // 发送一个简单的测试请求
            Map<String, Object> testRequest = new HashMap<>();
            testRequest.put("model", "deepseek-chat");
            testRequest.put("messages", new Object[]{
                Map.of("role", "user", "content", "test")
            });
            testRequest.put("max_tokens", 1);

            Mono<String> response = aiWebClient
                .post()
                .bodyValue(testRequest)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorReturn("CONNECTION_ERROR");

            return response.block();
            
        } catch (Exception e) {
            log.warn("AI连接测试失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查API密钥是否配置
     */
    private boolean isApiKeyConfigured() {
        return aiConfig.getApiKey() != null && 
               !aiConfig.getApiKey().isEmpty() && 
               !aiConfig.getApiKey().equals("sk-your-api-key-here");
    }
}