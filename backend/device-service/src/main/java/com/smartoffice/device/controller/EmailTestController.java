package com.smartoffice.device.controller;

import com.smartoffice.common.vo.Result;
import com.smartoffice.device.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 邮件测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/email")
@CrossOrigin
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    /**
     * 测试发送简单邮件
     */
    @PostMapping("/test/simple")
    public Result<String> testSimpleEmail(@RequestBody Map<String, String> params) {
        try {
            String to = params.get("to");
            String subject = params.getOrDefault("subject", "智慧办公楼系统测试邮件");
            String content = params.getOrDefault("content", "这是一封测试邮件，用于验证邮件发送功能是否正常。");

            if (to == null || to.isEmpty()) {
                return Result.fail("收件人邮箱不能为空");
            }

            emailService.sendSimpleEmail(to, subject, content);
            log.info("测试邮件发送成功: to={}", to);
            return Result.success("测试邮件发送成功");
        } catch (Exception e) {
            log.error("测试邮件发送失败", e);
            return Result.fail("测试邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * 测试发送告警邮件
     */
    @PostMapping("/test/alarm")
    public Result<String> testAlarmEmail(@RequestBody Map<String, String> params) {
        try {
            String to = params.get("to");
            String deviceId = params.getOrDefault("deviceId", "TEST_DEVICE_001");
            String alarmType = params.getOrDefault("alarmType", "FIRE");
            String alarmMessage = params.getOrDefault("alarmMessage", "这是一个测试告警消息");
            String alarmLevel = params.getOrDefault("alarmLevel", "HIGH");

            if (to == null || to.isEmpty()) {
                return Result.fail("收件人邮箱不能为空");
            }

            emailService.sendAlarmEmail(to, deviceId, alarmType, alarmMessage, alarmLevel);
            log.info("测试告警邮件发送成功: to={}, deviceId={}", to, deviceId);
            return Result.success("测试告警邮件发送成功");
        } catch (Exception e) {
            log.error("测试告警邮件发送失败", e);
            return Result.fail("测试告警邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * 测试发送HTML邮件
     */
    @PostMapping("/test/html")
    public Result<String> testHtmlEmail(@RequestBody Map<String, String> params) {
        try {
            String to = params.get("to");
            String subject = params.getOrDefault("subject", "智慧办公楼系统HTML测试邮件");
            String htmlContent = params.getOrDefault("htmlContent", 
                "<h1>智慧办公楼系统</h1>" +
                "<p>这是一封HTML格式的测试邮件。</p>" +
                "<p style='color: blue;'>邮件发送功能正常！</p>");

            if (to == null || to.isEmpty()) {
                return Result.fail("收件人邮箱不能为空");
            }

            emailService.sendHtmlEmail(to, subject, htmlContent);
            log.info("测试HTML邮件发送成功: to={}", to);
            return Result.success("测试HTML邮件发送成功");
        } catch (Exception e) {
            log.error("测试HTML邮件发送失败", e);
            return Result.fail("测试HTML邮件发送失败: " + e.getMessage());
        }
    }
}