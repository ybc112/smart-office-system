package com.smartoffice.device.service;

/**
 * AI服务降级策略接口
 */
public interface AiFallbackService {
    
    /**
     * 获取降级响应
     * @param userMessage 用户消息
     * @param errorType 错误类型
     * @return 降级响应
     */
    String getFallbackResponse(String userMessage, String errorType);
    
    /**
     * 检查是否应该启用降级
     * @return 是否启用降级
     */
    boolean shouldUseFallback();
    
    /**
     * 记录降级使用情况
     * @param errorType 错误类型
     */
    void recordFallbackUsage(String errorType);
}