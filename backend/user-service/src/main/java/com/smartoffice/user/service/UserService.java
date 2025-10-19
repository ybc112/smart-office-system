package com.smartoffice.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartoffice.common.entity.UserInfo;
import com.smartoffice.user.mapper.UserInfoMapper;
import com.smartoffice.user.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired(required = false)  // 暂时设为非必需，简化测试
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询用户
            UserInfo user = userInfoMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
            );

            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            // 验证密码（明文比较）
            if (!password.equals(user.getPassword())) {
                result.put("success", false);
                result.put("message", "密码错误");
                return result;
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                result.put("success", false);
                result.put("message", "账号已被禁用");
                return result;
            }

            // 生成Token
            String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

            // 缓存Token到Redis（暂时注释掉，简化测试）
            if (redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set(
                            "token:" + user.getId(),
                            token,
                            24, TimeUnit.HOURS
                    );
                } catch (Exception e) {
                    log.warn("Redis缓存失败，继续登录流程: {}", e.getMessage());
                }
            }

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userInfoMapper.updateById(user);

            // 返回结果
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("token", token);
            result.put("userInfo", getUserInfo(user));

            log.info("用户登录成功: {}", username);
            return result;

        } catch (Exception e) {
            log.error("登录失败", e);
            result.put("success", false);
            result.put("message", "登录失败");
            return result;
        }
    }

    /**
     * 获取用户信息（不含密码）
     */
    private Map<String, Object> getUserInfo(UserInfo user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole());
        return userInfo;
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        return jwtUtils.validateToken(token);
    }

    /**
     * 从Token获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        return jwtUtils.getUserIdFromToken(token);
    }

    /**
     * 根据ID获取用户信息
     */
    public UserInfo getUserById(Long userId) {
        return userInfoMapper.selectById(userId);
    }

    /**
     * 用户登出
     */
    public void logout(Long userId) {
        if (redisTemplate != null) {
            try {
                redisTemplate.delete("token:" + userId);
                log.info("用户登出: userId={}", userId);
            } catch (Exception e) {
                log.warn("Redis删除失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 获取所有用户列表
     */
    public java.util.List<UserInfo> getAllUsers() {
        try {
            java.util.List<UserInfo> users = userInfoMapper.selectList(null);
            // 清除所有密码
            users.forEach(user -> user.setPassword(null));
            return users;
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return new java.util.ArrayList<>();
        }
    }
}
