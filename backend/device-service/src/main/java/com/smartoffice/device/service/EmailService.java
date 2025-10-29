package com.smartoffice.device.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 邮件发送服务
 */
@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送简单文本邮件
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("简单邮件发送成功: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("简单邮件发送失败: to={}, subject={}", to, subject, e);
        }
    }

    /**
     * 发送HTML邮件
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML邮件发送成功: to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败: to={}, subject={}", to, subject, e);
        }
    }

    /**
     * 发送告警邮件
     */
    public void sendAlarmEmail(String to, String deviceId, String alarmType, String alarmMessage, String alarmLevel) {
        try {
            String subject = "【智慧办公楼系统】告警通知 - " + alarmType;
            
            String htmlContent = buildAlarmEmailContent(deviceId, alarmType, alarmMessage, alarmLevel);
            
            sendHtmlEmail(to, subject, htmlContent);
            log.info("告警邮件发送成功: to={}, deviceId={}, alarmType={}", to, deviceId, alarmType);
        } catch (Exception e) {
            log.error("告警邮件发送失败: to={}, deviceId={}, alarmType={}", to, deviceId, alarmType, e);
        }
    }

    /**
     * 构建告警邮件HTML内容
     */
    private String buildAlarmEmailContent(String deviceId, String alarmType, String alarmMessage, String alarmLevel) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        String levelColor = getLevelColor(alarmLevel);
        String levelText = getLevelText(alarmLevel);
        
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<meta charset=\"UTF-8\">")
                .append("<title>告警通知</title>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }")
                .append(".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }")
                .append(".header { background-color: #ff4757; color: white; padding: 20px; border-radius: 8px 8px 0 0; text-align: center; }")
                .append(".content { padding: 30px; }")
                .append(".alarm-info { background-color: #f8f9fa; padding: 20px; border-radius: 6px; margin: 20px 0; }")
                .append(".info-row { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #eee; }")
                .append(".label { font-weight: bold; color: #333; }")
                .append(".value { color: #666; }")
                .append(".level { padding: 4px 12px; border-radius: 20px; color: white; font-weight: bold; }")
                .append(".footer { text-align: center; padding: 20px; color: #999; font-size: 12px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class=\"container\">")
                .append("<div class=\"header\">")
                .append("<h1>🚨 智慧办公楼系统告警通知</h1>")
                .append("</div>")
                .append("<div class=\"content\">")
                .append("<p>您好，</p>")
                .append("<p>智慧办公楼系统检测到以下告警信息，请及时处理：</p>")
                .append("<div class=\"alarm-info\">")
                .append("<div class=\"info-row\">")
                .append("<span class=\"label\">设备ID：</span>")
                .append("<span class=\"value\">").append(deviceId).append("</span>")
                .append("</div>")
                .append("<div class=\"info-row\">")
                .append("<span class=\"label\">告警类型：</span>")
                .append("<span class=\"value\">").append(alarmType).append("</span>")
                .append("</div>")
                .append("<div class=\"info-row\">")
                .append("<span class=\"label\">告警级别：</span>")
                .append("<span class=\"level\" style=\"background-color: ").append(levelColor).append(";\">").append(levelText).append("</span>")
                .append("</div>")
                .append("<div class=\"info-row\">")
                .append("<span class=\"label\">告警信息：</span>")
                .append("<span class=\"value\">").append(alarmMessage).append("</span>")
                .append("</div>")
                .append("<div class=\"info-row\">")
                .append("<span class=\"label\">告警时间：</span>")
                .append("<span class=\"value\">").append(currentTime).append("</span>")
                .append("</div>")
                .append("</div>")
                .append("<p><strong>请及时登录系统查看详细信息并处理相关告警。</strong></p>")
                .append("<p>如有疑问，请联系系统管理员。</p>")
                .append("</div>")
                .append("<div class=\"footer\">")
                .append("<p>此邮件由智慧办公楼系统自动发送，请勿回复。</p>")
                .append("<p>© 2024 智慧办公楼管理系统</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");
        
        return htmlBuilder.toString();
    }

    /**
     * 获取告警级别对应的颜色
     */
    private String getLevelColor(String level) {
        if (level == null) return "#747d8c";
        
        switch (level.toUpperCase()) {
            case "CRITICAL":
                return "#ff4757";  // 红色
            case "HIGH":
                return "#ff6b35";  // 橙红色
            case "MEDIUM":
                return "#ffa502";  // 橙色
            case "LOW":
                return "#2ed573";  // 绿色
            default:
                return "#747d8c";  // 灰色
        }
    }

    /**
     * 获取告警级别对应的中文文本
     */
    private String getLevelText(String level) {
        if (level == null) return "未知";
        
        switch (level.toUpperCase()) {
            case "CRITICAL":
                return "严重";
            case "HIGH":
                return "高";
            case "MEDIUM":
                return "中";
            case "LOW":
                return "低";
            default:
                return "未知";
        }
    }
}