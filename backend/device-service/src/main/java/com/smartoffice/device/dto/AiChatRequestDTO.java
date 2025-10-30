package com.smartoffice.device.dto;

import lombok.Data;

/**
 * AI聊天请求DTO
 */
@Data
public class AiChatRequestDTO {
    
    /**
     * 用户消息
     */
    private String message;
    
    /**
     * 时间戳
     */
    private String timestamp;
    
    /**
     * 会话ID（可选，用于上下文管理）
     */
    private String sessionId;
}