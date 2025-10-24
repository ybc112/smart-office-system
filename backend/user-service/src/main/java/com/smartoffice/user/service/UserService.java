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

    /**
     * 分页获取用户列表
     */
    public Map<String, Object> getUserListWithPagination(Integer pageNum, Integer pageSize, String username, String role) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
            if (username != null && !username.trim().isEmpty()) {
                queryWrapper.like(UserInfo::getUsername, username.trim());
            }
            if (role != null && !role.trim().isEmpty()) {
                queryWrapper.eq(UserInfo::getRole, role.trim());
            }
            
            // 计算总数
            Long total = userInfoMapper.selectCount(queryWrapper);
            
            // 分页查询
            int offset = (pageNum - 1) * pageSize;
            queryWrapper.last("LIMIT " + offset + ", " + pageSize);
            java.util.List<UserInfo> users = userInfoMapper.selectList(queryWrapper);
            
            // 清除所有密码
            users.forEach(user -> user.setPassword(null));
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", users);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            
            return result;
        } catch (Exception e) {
            log.error("分页获取用户列表失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("list", new java.util.ArrayList<>());
            result.put("total", 0L);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return result;
        }
    }

    /**
     * 添加用户
     */
    public boolean addUser(UserInfo userInfo) {
        try {
            // 检查用户名是否已存在
            UserInfo existUser = userInfoMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, userInfo.getUsername())
            );
            if (existUser != null) {
                log.warn("用户名已存在: {}", userInfo.getUsername());
                return false;
            }
            
            // 设置默认值
            userInfo.setCreateTime(LocalDateTime.now());
            userInfo.setUpdateTime(LocalDateTime.now());
            if (userInfo.getStatus() == null) {
                userInfo.setStatus(1); // 默认启用
            }
            if (userInfo.getRole() == null || userInfo.getRole().trim().isEmpty()) {
                userInfo.setRole("USER"); // 默认普通用户
            }
            
            int result = userInfoMapper.insert(userInfo);
            log.info("添加用户成功: {}", userInfo.getUsername());
            return result > 0;
        } catch (Exception e) {
            log.error("添加用户失败", e);
            return false;
        }
    }

    /**
     * 更新用户信息
     */
    public boolean updateUser(Long id, UserInfo userInfo) {
        try {
            // 检查用户是否存在
            UserInfo existUser = userInfoMapper.selectById(id);
            if (existUser == null) {
                log.warn("用户不存在: {}", id);
                return false;
            }
            
            // 如果更新用户名，检查是否重复
            if (userInfo.getUsername() != null && !userInfo.getUsername().equals(existUser.getUsername())) {
                UserInfo duplicateUser = userInfoMapper.selectOne(
                        new LambdaQueryWrapper<UserInfo>()
                                .eq(UserInfo::getUsername, userInfo.getUsername())
                                .ne(UserInfo::getId, id)
                );
                if (duplicateUser != null) {
                    log.warn("用户名已存在: {}", userInfo.getUsername());
                    return false;
                }
            }
            
            // 设置更新时间和ID
            userInfo.setId(id);
            userInfo.setUpdateTime(LocalDateTime.now());
            
            int result = userInfoMapper.updateById(userInfo);
            log.info("更新用户成功: {}", id);
            return result > 0;
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return false;
        }
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(Long id) {
        try {
            // 检查用户是否存在
            UserInfo existUser = userInfoMapper.selectById(id);
            if (existUser == null) {
                log.warn("用户不存在: {}", id);
                return false;
            }
            
            // 不能删除管理员账户
            if ("ADMIN".equals(existUser.getRole())) {
                log.warn("不能删除管理员账户: {}", id);
                return false;
            }
            
            int result = userInfoMapper.deleteById(id);
            log.info("删除用户成功: {}", id);
            return result > 0;
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return false;
        }
    }

    /**
     * 切换用户状态
     */
    public boolean toggleUserStatus(Long id) {
        try {
            UserInfo user = userInfoMapper.selectById(id);
            if (user == null) {
                log.warn("用户不存在: {}", id);
                return false;
            }
            
            // 不能禁用管理员账户
            if ("ADMIN".equals(user.getRole()) && user.getStatus() == 1) {
                log.warn("不能禁用管理员账户: {}", id);
                return false;
            }
            
            // 切换状态
            user.setStatus(user.getStatus() == 1 ? 0 : 1);
            user.setUpdateTime(LocalDateTime.now());
            
            int result = userInfoMapper.updateById(user);
            log.info("切换用户状态成功: {} -> {}", id, user.getStatus());
            return result > 0;
        } catch (Exception e) {
            log.error("切换用户状态失败", e);
            return false;
        }
    }
}
