package com.smartoffice.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartoffice.common.constants.SystemConstants;
import com.smartoffice.common.entity.AlarmLog;
import com.smartoffice.common.vo.Result;
import com.smartoffice.device.mapper.AlarmLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 告警管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/alarm")
@CrossOrigin
public class AlarmController {

    @Autowired
    private AlarmLogMapper alarmLogMapper;

    /**
     * 分页查询告警列表
     */
    @GetMapping("/list")
    public Result<Page<AlarmLog>> getAlarmList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String alarmType,
            @RequestParam(required = false) String alarmLevel) {
        try {
            Page<AlarmLog> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<AlarmLog> wrapper = new LambdaQueryWrapper<>();

            if (status != null && !status.isEmpty()) {
                wrapper.eq(AlarmLog::getStatus, status);
            }
            if (alarmType != null && !alarmType.isEmpty()) {
                wrapper.eq(AlarmLog::getAlarmType, alarmType);
            }
            if (alarmLevel != null && !alarmLevel.isEmpty()) {
                wrapper.eq(AlarmLog::getAlarmLevel, alarmLevel);
            }

            wrapper.orderByDesc(AlarmLog::getAlarmTime);
            alarmLogMapper.selectPage(page, wrapper);

            return Result.success(page);
        } catch (Exception e) {
            log.error("查询告警列表失败", e);
            return Result.fail("查询告警列表失败");
        }
    }

    /**
     * 获取告警统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 未处理告警数量
            Long unhandledCount = alarmLogMapper.selectCount(
                    new LambdaQueryWrapper<AlarmLog>()
                            .eq(AlarmLog::getStatus, SystemConstants.ALARM_STATUS_UNHANDLED)
            );
            stats.put("unhandledCount", unhandledCount);

            // 今日告警数量
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            Long todayCount = alarmLogMapper.selectCount(
                    new LambdaQueryWrapper<AlarmLog>()
                            .ge(AlarmLog::getAlarmTime, todayStart)
            );
            stats.put("todayCount", todayCount);

            // 按类型统计
            Map<String, Long> typeStats = new HashMap<>();
            for (String type : new String[]{"FIRE", "TEMP", "HUMIDITY", "LIGHT"}) {
                Long count = alarmLogMapper.selectCount(
                        new LambdaQueryWrapper<AlarmLog>()
                                .eq(AlarmLog::getAlarmType, type)
                                .ge(AlarmLog::getAlarmTime, todayStart)
                );
                typeStats.put(type, count);
            }
            stats.put("typeStats", typeStats);

            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取告警统计失败", e);
            return Result.fail("获取告警统计失败");
        }
    }

    /**
     * 处理告警
     */
    @PutMapping("/{id}/handle")
    public Result<String> handleAlarm(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        try {
            AlarmLog alarm = alarmLogMapper.selectById(id);
            if (alarm == null) {
                return Result.fail("告警记录不存在");
            }

            alarm.setStatus((String) params.get("status"));
            alarm.setHandleRemark((String) params.get("handleRemark"));
            alarm.setHandleTime(LocalDateTime.now());
            // 这里应该从Token中获取用户ID，暂时写死
            alarm.setHandlerId(1L);

            alarmLogMapper.updateById(alarm);
            log.info("告警已处理: id={}", id);

            return Result.success("告警已处理");
        } catch (Exception e) {
            log.error("处理告警失败", e);
            return Result.fail("处理告警失败");
        }
    }

    /**
     * 更新告警状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateAlarmStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        try {
            AlarmLog alarm = alarmLogMapper.selectById(id);
            if (alarm == null) {
                return Result.fail("告警记录不存在");
            }

            alarm.setStatus((String) params.get("status"));
            alarm.setHandleRemark((String) params.get("handleRemark"));
            alarm.setHandleTime(LocalDateTime.now());
            // 这里应该从Token中获取用户ID，暂时写死
            alarm.setHandlerId(1L);

            alarmLogMapper.updateById(alarm);
            log.info("告警状态已更新: id={}, status={}", id, params.get("status"));

            return Result.success("告警状态已更新");
        } catch (Exception e) {
            log.error("更新告警状态失败", e);
            return Result.fail("更新告警状态失败");
        }
    }

    /**
     * 获取最新告警
     */
    @GetMapping("/latest")
    public Result<List<AlarmLog>> getLatestAlarms(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<AlarmLog> alarms = alarmLogMapper.selectList(
                    new LambdaQueryWrapper<AlarmLog>()
                            .orderByDesc(AlarmLog::getAlarmTime)
                            .last("LIMIT " + limit)
            );
            return Result.success(alarms);
        } catch (Exception e) {
            log.error("获取最新告警失败", e);
            return Result.fail("获取最新告警失败");
        }
    }

    /**
     * 删除单个告警
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteAlarm(@PathVariable Long id) {
        try {
            AlarmLog alarm = alarmLogMapper.selectById(id);
            if (alarm == null) {
                return Result.fail("告警记录不存在");
            }

            int result = alarmLogMapper.deleteById(id);
            if (result > 0) {
                log.info("告警已删除: id={}", id);
                return Result.success("删除成功");
            } else {
                return Result.fail("删除失败");
            }
        } catch (Exception e) {
            log.error("删除告警失败: id={}", id, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有告警
     */
    @DeleteMapping("/clear")
    public Result<String> clearAllAlarms() {
        try {
            // 删除所有告警记录
            int result = alarmLogMapper.delete(new LambdaQueryWrapper<>());
            log.info("已清空所有告警, 共删除{}条记录", result);
            return Result.success("已清空所有告警，共删除" + result + "条记录");
        } catch (Exception e) {
            log.error("清空告警失败", e);
            return Result.fail("清空失败: " + e.getMessage());
        }
    }
}
