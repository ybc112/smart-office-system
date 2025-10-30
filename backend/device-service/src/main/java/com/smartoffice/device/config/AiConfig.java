package com.smartoffice.device.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.Data;

/**
 * AI服务配置
 */
@Configuration
@ConfigurationProperties(prefix = "ai.deepseek")
@Data
public class AiConfig {
    
    private String apiUrl = "https://api.deepseek.com/v1/chat/completions";
    private String apiKey = "sk-your-api-key-here"; // 需要配置实际的API Key
    private int timeout = 30000; // 30秒超时
    
    @Bean
    public WebClient aiWebClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}