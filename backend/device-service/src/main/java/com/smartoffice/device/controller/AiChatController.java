package com.smartoffice.device.controller;

import com.smartoffice.common.vo.Result;
import com.smartoffice.device.dto.AiChatRequestDTO;
import com.smartoffice.device.dto.AiChatResponseDTO;
import com.smartoffice.device.service.AiChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI聊天对话控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@CrossOrigin
public class AiChatController {
    
    @Autowired
    private AiChatService aiChatService;
    
    /**
     * AI聊天对话
     */
    @PostMapping("/chat")
    public Result<AiChatResponseDTO> chat(@RequestBody AiChatRequestDTO request) {
        try {
            log.info("收到AI聊天请求: {}", request.getMessage());
            AiChatResponseDTO response = aiChatService.chat(request);
            return Result.success("AI回复成功", response);
        } catch (Exception e) {
            log.error("AI聊天失败", e);
            return Result.fail("AI助手暂时无法回复，请稍后重试");
        }
    }
    
    /**
     * 获取聊天历史（可选功能）
     */
    @GetMapping("/chat/history")
    public Result<Object> getChatHistory() {
        try {
            // 这里可以实现聊天历史功能，暂时返回空
            return Result.success("获取成功", null);
        } catch (Exception e) {
            log.error("获取聊天历史失败", e);
            return Result.fail("获取聊天历史失败");
        }
    }
    
    /**
     * 清空聊天历史（可选功能）
     */
    @DeleteMapping("/chat/clear")
    public Result<Object> clearChatHistory() {
        try {
            // 这里可以实现清空聊天历史功能
            return Result.success("清空成功", null);
        } catch (Exception e) {
            log.error("清空聊天历史失败", e);
            return Result.fail("清空聊天历史失败");
        }
    }
}