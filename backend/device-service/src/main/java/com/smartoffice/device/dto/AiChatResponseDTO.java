package com.smartoffice.device.dto;

import lombok.Data;

/**
 * AI聊天响应DTO
 */
@Data
public class AiChatResponseDTO {
    
    /**
     * AI回复内容
     */
    private String message;
    
    /**
     * 回复时间戳
     */
    private String timestamp;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 是否成功
     */
    private boolean success = true;
}