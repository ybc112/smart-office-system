package com.smartoffice.device.service;

import com.smartoffice.device.dto.AiChatRequestDTO;
import com.smartoffice.device.dto.AiChatResponseDTO;

/**
 * AI聊天服务接口
 */
public interface AiChatService {
    
    /**
     * AI聊天对话
     * @param request 聊天请求
     * @return 聊天响应
     */
    AiChatResponseDTO chat(AiChatRequestDTO request);
}