package com.smartoffice.device.dto;

import lombok.Data;
import java.util.List;

/**
 * AI请求DTO
 */
@Data
public class AiRequestDTO {
    
    private String model = "deepseek-chat";
    private List<Message> messages;
    private double temperature = 0.7;
    private int maxTokens = 2000;
    
    @Data
    public static class Message {
        private String role;
        private String content;
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}