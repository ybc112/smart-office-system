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
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role) {
        try {
            Map<String, Object> result = userService.getUserListWithPagination(pageNum, pageSize, username, role);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.fail("获取用户列表失败");
        }
    }

    /**
     * 添加用户
     */
    @PostMapping("/add")
    public Result<String> addUser(@RequestBody UserInfo userInfo) {
        try {
            if (userInfo.getUsername() == null || userInfo.getUsername().trim().isEmpty()) {
                return Result.fail("用户名不能为空");
            }
            if (userInfo.getPassword() == null || userInfo.getPassword().trim().isEmpty()) {
                return Result.fail("密码不能为空");
            }
            
            boolean success = userService.addUser(userInfo);
            if (success) {
                return Result.success("添加用户成功");
            } else {
                return Result.fail("用户名已存在");
            }
        } catch (Exception e) {
            log.error("添加用户失败", e);
            return Result.fail("添加用户失败");
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update/{id}")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody UserInfo userInfo) {
        try {
            boolean success = userService.updateUser(id, userInfo);
            if (success) {
                return Result.success("更新用户成功");
            } else {
                return Result.fail("用户不存在或用户名已被使用");
            }
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.fail("更新用户失败");
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        try {
            boolean success = userService.deleteUser(id);
            if (success) {
                return Result.success("删除用户成功");
            } else {
                return Result.fail("用户不存在或不能删除管理员账户");
            }
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.fail("删除用户失败");
        }
    }

    /**
     * 切换用户状态
     */
    @PutMapping("/toggle-status/{id}")
    public Result<String> toggleUserStatus(@PathVariable Long id) {
        try {
            boolean success = userService.toggleUserStatus(id);
            if (success) {
                return Result.success("切换用户状态成功");
            } else {
                return Result.fail("用户不存在或不能禁用管理员账户");
            }
        } catch (Exception e) {
            log.error("切换用户状态失败", e);
            return Result.fail("切换用户状态失败");
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
