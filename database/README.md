# 数据库设计文档

## 数据库概览

**数据库名称**: smart_office
**字符集**: utf8mb4
**排序规则**: utf8mb4_unicode_ci

## 数据表清单

| 序号 | 表名 | 说明 | 主要用途 |
|------|------|------|----------|
| 1 | user_info | 用户信息表 | 存储用户账号、角色权限 |
| 2 | device_info | 设备信息表 | 管理所有物联网设备 |
| 3 | sensor_data | 传感器数据表 | 存储传感器上报的数据 |
| 4 | alarm_log | 告警日志表 | 记录所有告警事件 |
| 5 | control_log | 设备控制日志表 | 记录设备控制操作 |
| 6 | system_config | 系统配置表 | 存储系统参数和阈值 |
| 7 | operation_log | 操作日志表 | 记录用户操作日志 |

## 详细表结构

### 1. user_info（用户信息表）

```sql
字段名             类型           说明
id                BIGINT         用户ID（主键）
username          VARCHAR(50)    用户名（唯一）
password          VARCHAR(255)   密码（BCrypt加密）
real_name         VARCHAR(50)    真实姓名
email             VARCHAR(100)   邮箱
phone             VARCHAR(20)    手机号
role              VARCHAR(20)    角色：ADMIN/USER
status            TINYINT        状态：0-禁用, 1-启用
create_time       DATETIME       创建时间
update_time       DATETIME       更新时间
last_login_time   DATETIME       最后登录时间
```

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (username)
- INDEX (role)
- INDEX (status)

**默认数据**:
- admin / admin123（管理员）
- user / user123（普通用户）

### 2. device_info（设备信息表）

```sql
字段名             类型           说明
id                BIGINT         设备ID（主键）
device_id         VARCHAR(50)    设备编号（唯一，如W601_001）
device_name       VARCHAR(100)   设备名称
device_type       VARCHAR(50)    设备类型：SENSOR/ACTUATOR
location          VARCHAR(100)   设备位置
status            VARCHAR(20)    设备状态：ONLINE/OFFLINE/FAULT
online_status     TINYINT        在线状态：0-离线, 1-在线
last_online_time  DATETIME       最后在线时间
firmware_version  VARCHAR(50)    固件版本
description       TEXT           设备描述
create_time       DATETIME       创建时间
update_time       DATETIME       更新时间
```

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (device_id)
- INDEX (device_type)
- INDEX (status)

### 3. sensor_data（传感器数据表）

```sql
字段名         类型             说明
id            BIGINT           数据ID（主键）
device_id     VARCHAR(50)      设备编号
light         DECIMAL(10,2)    光照强度（lux）
temperature   DECIMAL(5,2)     温度（℃）
humidity      DECIMAL(5,2)     湿度（%）
flame         TINYINT          火焰检测：0-无, 1-有
rgb_status    TINYINT          RGB灯状态：0-关, 1-开
data_time     DATETIME         数据采集时间
create_time   DATETIME         记录创建时间
```

**索引**:
- PRIMARY KEY (id)
- INDEX (device_id)
- INDEX (data_time)

**数据保留策略**: 建议定期清理30天前的历史数据

### 4. alarm_log（告警日志表）

```sql
字段名             类型           说明
id                BIGINT         告警ID（主键）
device_id         VARCHAR(50)    设备编号
alarm_type        VARCHAR(50)    告警类型：FIRE/TEMP/HUMIDITY/LIGHT
alarm_level       VARCHAR(20)    告警级别：INFO/WARNING/CRITICAL
alarm_message     TEXT           告警消息
alarm_value       VARCHAR(50)    告警触发值
threshold_value   VARCHAR(50)    阈值
status            VARCHAR(20)    处理状态：UNHANDLED/HANDLING/HANDLED/IGNORED
handler_id        BIGINT         处理人ID
handle_time       DATETIME       处理时间
handle_remark     TEXT           处理备注
alarm_time        DATETIME       告警时间
create_time       DATETIME       记录创建时间
```

**索引**:
- PRIMARY KEY (id)
- INDEX (device_id)
- INDEX (alarm_type)
- INDEX (alarm_level)
- INDEX (status)

### 5. control_log（设备控制日志表）

```sql
字段名            类型           说明
id               BIGINT         日志ID（主键）
device_id        VARCHAR(50)    设备编号
action           VARCHAR(50)    控制动作（rgb_on/rgb_off等）
params           TEXT           控制参数（JSON格式）
trigger_type     VARCHAR(20)    触发方式：AUTO/MANUAL
operator_id      BIGINT         操作人ID
result           VARCHAR(20)    执行结果：SUCCESS/FAILED
error_message    TEXT           错误信息
control_time     DATETIME       控制时间
create_time      DATETIME       记录创建时间
```

**索引**:
- PRIMARY KEY (id)
- INDEX (device_id)
- INDEX (action)
- INDEX (trigger_type)

### 6. system_config（系统配置表）

```sql
字段名         类型            说明
id            BIGINT          配置ID（主键）
config_key    VARCHAR(100)    配置键（唯一）
config_value  TEXT            配置值
config_type   VARCHAR(50)     配置类型：THRESHOLD/SYSTEM
description   VARCHAR(255)    配置描述
create_time   DATETIME        创建时间
update_time   DATETIME        更新时间
```

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (config_key)
- INDEX (config_type)

**默认配置项**:
```
light.threshold.low = 300       // 光照下限
light.threshold.high = 350      // 光照上限
temperature.threshold.low = 18  // 温度下限
temperature.threshold.high = 28 // 温度上限
humidity.threshold.low = 40     // 湿度下限
humidity.threshold.high = 70    // 湿度上限
```

### 7. operation_log（操作日志表）

```sql
字段名           类型            说明
id              BIGINT          日志ID（主键）
user_id         BIGINT          用户ID
username        VARCHAR(50)     用户名
operation       VARCHAR(100)    操作类型
method          VARCHAR(200)    方法名
params          TEXT            请求参数
result          TEXT            返回结果
ip              VARCHAR(50)     IP地址
user_agent      VARCHAR(500)    用户代理
status          TINYINT         操作状态：0-失败, 1-成功
error_message   TEXT            错误信息
execute_time    BIGINT          执行时长（毫秒）
create_time     DATETIME        创建时间
```

**索引**:
- PRIMARY KEY (id)
- INDEX (user_id)
- INDEX (operation)
- INDEX (create_time)

## 视图

### v_latest_sensor_data（最新传感器数据视图）

显示每个设备的最新传感器数据。

### v_alarm_statistics（告警统计视图）

统计最近7天的告警数据，按类型和级别分组。

## 数据关系

```
user_info (1) -----> (*) operation_log
user_info (1) -----> (*) alarm_log (处理人)
user_info (1) -----> (*) control_log (操作人)

device_info (1) -----> (*) sensor_data
device_info (1) -----> (*) alarm_log
device_info (1) -----> (*) control_log
```

## 初始化

运行 `database/init.sql` 脚本自动创建所有表结构和初始数据。

```bash
mysql -u root -p < database/init.sql
```

或通过Docker Compose自动初始化：

```bash
cd docker
docker-compose up -d
```

## 备份建议

- **每日备份**: sensor_data, alarm_log, control_log
- **每周备份**: 全库备份
- **定期清理**: 30天前的sensor_data记录

## 性能优化建议

1. sensor_data表数据量大，建议按月份分表
2. 为常用查询字段添加索引
3. 定期执行 `OPTIMIZE TABLE` 优化表结构
4. 使用Redis缓存热点数据（最新传感器数据、系统配置等）
