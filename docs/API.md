# API接口文档

## 基础信息

- **Base URL**: `http://localhost:8081`
- **数据格式**: JSON
- **字符编码**: UTF-8

## 通用响应格式

所有接口返回统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1697520000000
}
```

**字段说明**:
- `code`: 状态码，200表示成功，其他表示失败
- `message`: 响应消息
- `data`: 响应数据
- `timestamp`: 响应时间戳

## 设备管理接口

### 1. 获取设备列表

**请求**:
```
GET /device/list
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "deviceId": "W601_001",
      "deviceName": "W601智能开发板",
      "deviceType": "SENSOR",
      "location": "办公室A区",
      "status": "ONLINE",
      "onlineStatus": 1,
      "lastOnlineTime": "2024-01-01 10:00:00"
    }
  ]
}
```

### 2. 获取设备详情

**请求**:
```
GET /device/{deviceId}
```

**路径参数**:
- `deviceId`: 设备ID

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "deviceId": "W601_001",
    "deviceName": "W601智能开发板",
    "deviceType": "SENSOR",
    "location": "办公室A区",
    "status": "ONLINE",
    "onlineStatus": 1,
    "lastOnlineTime": "2024-01-01 10:00:00",
    "firmwareVersion": "1.0.0",
    "description": "集成光照、温湿度传感器及RGB灯、蜂鸣器"
  }
}
```

### 3. 获取最新传感器数据

**请求**:
```
GET /device/{deviceId}/latest
```

**路径参数**:
- `deviceId`: 设备ID

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "deviceId": "W601_001",
    "light": 456.78,
    "temperature": 26.3,
    "humidity": 58.2,
    "flame": false,
    "rgbStatus": true,
    "timestamp": 1697520000000
  }
}
```

### 4. 查询传感器历史数据

**请求**:
```
GET /device/{deviceId}/history?pageNum=1&pageSize=20
```

**路径参数**:
- `deviceId`: 设备ID

**查询参数**:
- `pageNum`: 页码，默认1
- `pageSize`: 每页数量，默认20

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "deviceId": "W601_001",
        "light": 456.78,
        "temperature": 26.3,
        "humidity": 58.2,
        "flame": 0,
        "rgbStatus": 1,
        "dataTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 100,
    "size": 20,
    "current": 1,
    "pages": 5
  }
}
```

### 5. 手动控制设备

**请求**:
```
POST /device/control
Content-Type: application/json
```

**请求体**:
```json
{
  "deviceId": "W601_001",
  "action": "rgb_on",
  "params": {
    "brightness": 80
  }
}
```

**支持的action**:
- `rgb_on`: 开灯
- `rgb_off`: 关灯
- `buzzer_on`: 开蜂鸣器
- `buzzer_off`: 关蜂鸣器

**响应示例**:
```json
{
  "code": 200,
  "message": "控制命令已发送",
  "data": null
}
```

## 告警管理接口

### 6. 获取告警列表

**请求**:
```
GET /alarm/list?pageNum=1&pageSize=20&status=UNHANDLED
```

**查询参数**:
- `pageNum`: 页码，默认1
- `pageSize`: 每页数量，默认20
- `status`: 告警状态（可选）
  - `UNHANDLED`: 未处理
  - `HANDLING`: 处理中
  - `HANDLED`: 已处理
  - `IGNORED`: 已忽略

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "deviceId": "W601_001",
        "alarmType": "FIRE",
        "alarmLevel": "CRITICAL",
        "alarmMessage": "检测到火焰！",
        "status": "UNHANDLED",
        "alarmTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 10,
    "size": 20,
    "current": 1,
    "pages": 1
  }
}
```

### 7. 处理告警

**请求**:
```
PUT /alarm/{id}/handle
Content-Type: application/json
```

**路径参数**:
- `id`: 告警ID

**请求体**:
```json
{
  "status": "HANDLED",
  "handleRemark": "已确认，虚惊一场"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "告警已处理",
  "data": null
}
```

## 系统配置接口

### 8. 获取阈值配置

**请求**:
```
GET /config/thresholds
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "light.threshold.low": "300",
    "light.threshold.high": "350",
    "temperature.threshold.low": "18",
    "temperature.threshold.high": "28",
    "humidity.threshold.low": "40",
    "humidity.threshold.high": "70"
  }
}
```

### 9. 更新阈值配置

**请求**:
```
PUT /config/threshold
Content-Type: application/json
```

**请求体**:
```json
{
  "configKey": "light.threshold.low",
  "configValue": "280"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "配置已更新",
  "data": null
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 测试示例

### 使用curl测试

```bash
# 1. 获取设备列表
curl http://localhost:8081/device/list

# 2. 获取最新传感器数据
curl http://localhost:8081/device/W601_001/latest

# 3. 控制设备
curl -X POST http://localhost:8081/device/control \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"W601_001","action":"rgb_on"}'
```

### 使用Postman测试

1. 导入接口到Postman
2. 设置Base URL为 `http://localhost:8081`
3. 测试各个接口

## WebSocket实时推送

### 连接地址
```
ws://localhost:8081/ws/sensor
```

### 消息格式

**服务器推送的消息**:
```json
{
  "type": "sensor_data",
  "deviceId": "W601_001",
  "data": {
    "light": 456.78,
    "temperature": 26.3,
    "humidity": 58.2,
    "flame": false,
    "rgbStatus": true
  },
  "timestamp": 1697520000000
}
```

### 前端接入示例

```javascript
// 创建WebSocket连接
const ws = new WebSocket('ws://localhost:8081/ws/sensor');

// 连接成功
ws.onopen = () => {
  console.log('WebSocket连接成功');
};

// 接收消息
ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('收到传感器数据:', data);
  // 更新界面
};

// 连接关闭
ws.onclose = () => {
  console.log('WebSocket连接关闭');
};
```

## 注意事项

1. 所有时间格式统一使用 `yyyy-MM-dd HH:mm:ss`
2. 所有接口支持跨域访问（CORS）
3. 分页查询默认按时间倒序排列
4. Boolean类型字段：数据库存储为 0/1，JSON返回为 true/false
