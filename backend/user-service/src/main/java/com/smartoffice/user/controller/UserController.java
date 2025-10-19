package com.smartoffice.user.controller;

import com.smartoffice.common.entity.UserInfo;
import com.smartoffice.common.vo.Result;
import com.smartoffice.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户Controller
 */
@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");

            if (username == null || password == null) {
                return Result.fail("用户名或密码不能为空");
            }

            Map<String, Object> result = userService.login(username, password);

            if ((Boolean) result.get("success")) {
                return Result.success((String) result.get("message"), result);
            } else {
                return Result.fail((String) result.get("message"));
            }
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.fail("登录失败");
        }
    }

    /**
     * 验证Token
     */
    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            boolean valid = userService.validateToken(token);
            return Result.success(valid);
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return Result.success(false);
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserInfo> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = userService.getUserIdFromToken(token);
            UserInfo user = userService.getUserById(userId);
            // 清除密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.fail("获取用户信息失败");
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = userService.getUserIdFromToken(token);
            userService.logout(userId);
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("登出失败", e);
            return Result.fail("登出失败");
        }
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public Result<java.util.List<UserInfo>> getUserList() {
        try {
            java.util.List<UserInfo> users = userService.getAllUsers();
            return Result.success(users);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.fail("获取用户列表失败");
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("User Service is running");
    }
}
