package com.smartoffice.device.service.impl;

import com.smartoffice.device.service.AiFallbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI服务降级策略实现
 */
@Service
@Slf4j
public class AiFallbackServiceImpl implements AiFallbackService {

    private final AtomicInteger fallbackCount = new AtomicInteger(0);
    private final Map<String, AtomicInteger> errorTypeCount = new HashMap<>();
    
    @Override
    public String getFallbackResponse(String userMessage, String errorType) {
        recordFallbackUsage(errorType);
        
        // 根据用户消息内容提供智能降级响应
        String message = userMessage.toLowerCase();
        
        if (message.contains("设备") || message.contains("监控")) {
            return "抱歉，AI助手暂时无法响应。关于设备监控，您可以：\n" +
                   "1. 查看设备管理页面了解设备状态\n" +
                   "2. 检查设备告警信息\n" +
                   "3. 联系系统管理员获取技术支持\n" +
                   "4. 稍后重试AI助手功能";
        }
        
        if (message.contains("温度") || message.contains("湿度") || message.contains("环境")) {
            return "抱歉，AI助手暂时无法响应。关于环境监控，您可以：\n" +
                   "1. 直接查看环境监控仪表板\n" +
                   "2. 查看历史环境数据报表\n" +
                   "3. 设置环境告警阈值\n" +
                   "4. 稍后重试AI助手功能";
        }
        
        if (message.contains("能耗") || message.contains("电力") || message.contains("用电")) {
            return "抱歉，AI助手暂时无法响应。关于能耗管理，您可以：\n" +
                   "1. 查看能耗统计报表\n" +
                   "2. 分析用电趋势图表\n" +
                   "3. 查看节能建议\n" +
                   "4. 稍后重试AI助手功能";
        }
        
        if (message.contains("安全") || message.contains("门禁") || message.contains("访客")) {
            return "抱歉，AI助手暂时无法响应。关于安全管理，您可以：\n" +
                   "1. 查看门禁记录\n" +
                   "2. 管理访客信息\n" +
                   "3. 查看安全告警\n" +
                   "4. 稍后重试AI助手功能";
        }
        
        // 默认降级响应
        return "抱歉，AI助手暂时无法正常服务。可能的原因：\n" +
               "• 网络连接问题\n" +
               "• AI服务暂时不可用\n" +
               "• 系统负载过高\n\n" +
               "建议您：\n" +
               "1. 稍后重试\n" +
               "2. 使用系统其他功能模块\n" +
               "3. 联系技术支持：400-xxx-xxxx\n\n" +
               "感谢您的理解！";
    }
    
    @Override
    public boolean shouldUseFallback() {
        // 如果连续失败次数过多，启用降级
        return fallbackCount.get() > 5;
    }
    
    @Override
    public void recordFallbackUsage(String errorType) {
        fallbackCount.incrementAndGet();
        errorTypeCount.computeIfAbsent(errorType, k -> new AtomicInteger(0)).incrementAndGet();
        
        log.warn("AI服务降级 - 错误类型: {}, 总降级次数: {}, 该类型错误次数: {}", 
                errorType, fallbackCount.get(), errorTypeCount.get(errorType).get());
        
        // 每10次降级记录一次统计信息
        if (fallbackCount.get() % 10 == 0) {
            log.error("AI服务降级统计 - 总次数: {}, 错误分布: {}", fallbackCount.get(), errorTypeCount);
        }
    }
    
    /**
     * 重置降级计数器（用于系统恢复后）
     */
    public void resetFallbackCount() {
        int previousCount = fallbackCount.getAndSet(0);
        errorTypeCount.clear();
        log.info("AI服务降级计数器已重置，之前总计: {}", previousCount);
    }
    
    /**
     * 获取降级统计信息
     */
    public Map<String, Object> getFallbackStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFallbackCount", fallbackCount.get());
        stats.put("errorTypeDistribution", errorTypeCount);
        stats.put("shouldUseFallback", shouldUseFallback());
        return stats;
    }
}