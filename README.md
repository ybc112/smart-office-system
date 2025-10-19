# 智慧办公楼系统

## 项目简介
基于 W601 开发板 + SpringCloud 微服务架构 + Vue 前端技术实现的智慧办公楼管理系统。

## 项目结构

```
智慧办公楼系统/
├── backend/                    # 后端微服务
│   ├── smart-office-parent/    # Maven父工程，统一管理依赖版本
│   ├── common/                 # 公共模块（实体类、工具类、常量等）
│   ├── gateway/                # 网关服务（端口8080）
│   ├── device-service/         # 设备管理服务（端口8081）
│   ├── user-service/           # 用户服务（端口8082）
│   └── alarm-service/          # 告警服务（端口8083）
├── frontend/                   # Vue3前端项目
├── device-simulator/           # Python虚拟设备模拟器
├── database/                   # 数据库初始化脚本
├── docker/                     # Docker配置文件
└── docs/                       # 项目文档
```

## 技术栈

### 后端
- SpringBoot 2.7.x
- SpringCloud 2021.x
- SpringCloud Gateway
- MyBatis-Plus
- MySQL 8.0
- Redis
- MQTT (Eclipse Paho)

### 前端
- Vue 3
- Element Plus
- Axios
- ECharts

### 设备通信
- MQTT 协议
- EMQ X (MQTT Broker)

## 快速开始

### 1. 环境准备
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0
- Redis
- Node.js 16+
- Python 3.8+ (用于虚拟设备)

### 2. 启动MQTT服务器
```bash
cd docker
docker-compose up -d emqx
```

访问管理界面：http://localhost:18083
默认账号：admin / public

### 3. 初始化数据库
```bash
mysql -u root -p < database/init.sql
```

### 4. 启动后端服务
```bash
cd backend
# 按顺序启动各个服务
cd gateway && mvn spring-boot:run
cd device-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd alarm-service && mvn spring-boot:run
```

### 5. 启动虚拟设备
```bash
cd device-simulator
pip install -r requirements.txt
python device_simulator.py
```

### 6. 启动前端
```bash
cd frontend
npm install
npm run dev
```

访问地址：http://localhost:5173

## 核心功能

### 1. 智慧照明管理
- 光照传感器监测亮度
- 自动开关灯（RGB灯模拟）
- 阈值可配置

### 2. 智慧环境调控
- 温湿度监测
- 自动控制加湿器、空调
- 环境数据可视化

### 3. 智慧消防安全
- 火焰传感器检测
- 自动触发蜂鸣器
- 告警记录与推送

### 4. 设备状态监控
- 实时设备状态
- 在线/离线监测
- 历史数据查询

### 5. 用户权限管理
- JWT身份认证
- 角色权限控制
- 操作日志记录

## MQTT通信协议

### 设备上报数据
**Topic:** `office/sensor/data`

```json
{
  "deviceId": "W601_001",
  "light": 280,
  "temperature": 25.5,
  "humidity": 55.0,
  "flame": false,
  "rgbStatus": true,
  "timestamp": 1697520000000
}
```

### 服务器控制设备
**Topic:** `office/control/cmd`

```json
{
  "deviceId": "W601_001",
  "action": "rgb_on",
  "params": {
    "brightness": 80
  }
}
```

### 告警消息
**Topic:** `office/alarm`

```json
{
  "deviceId": "W601_001",
  "alarmType": "FIRE",
  "level": "CRITICAL",
  "message": "检测到火焰！",
  "timestamp": 1697520000000
}
```

## 接口文档

详见 `docs/API.md`

## W601硬件对接说明

详见 `docs/W601接入指南.md`

## 开发进度

- [x] 项目结构搭建
- [ ] 后端微服务开发
- [ ] 前端界面开发
- [ ] 虚拟设备模拟器
- [ ] 系统联调测试
- [ ] W601硬件对接

## 许可证
MIT License
