# 🏢 智慧办公楼系统

## 📋 项目简介
基于 **W601 开发板** + **SpringCloud 微服务架构** + **Vue3 前端技术** 实现的智慧办公楼管理系统。

### 🎯 核心功能
- **智慧照明管理**: 光照传感器自动控制RGB灯
- **智慧环境调控**: 温湿度监测与自动控制
- **智慧消防安全**: 火焰检测与自动报警
- **设备状态监控**: 实时监控与历史数据分析
- **用户权限管理**: JWT认证与角色权限控制

### 🏗️ 系统架构
```
W601硬件设备 ←→ MQTT服务器 ←→ SpringCloud后端 ←→ Vue3前端
     ↓              ↓              ↓              ↓
   传感器数据      消息代理        业务处理        数据展示
```

## 🚀 快速开始

> **📖 完整指南**: 详细的运行步骤请参考 [项目完整运行指南.md](项目完整运行指南.md)

### 方式一：完整体验（推荐）
如果你有W601开发板和传感器，可以体验完整的物联网功能：
1. 按照 [硬件连接指南](hardware/Hardware_Connection_Guide.md) 连接硬件
2. 烧录 `W601_SmartOffice/` 目录下的MicroPython代码
3. 启动后端服务和前端界面

### 方式二：软件演示
如果没有硬件，系统仍可正常运行展示功能：
1. 启动数据库和MQTT服务
2. 启动后端微服务
3. 启动前端界面

## 📦 项目结构

```
智慧办公楼系统/
├── W601_SmartOffice/           # W601硬件代码（MicroPython）
│   ├── main.py                 # 主程序入口
│   ├── config.py               # 配置文件
│   ├── sensors/                # 传感器模块
│   ├── devices/                # 设备控制模块
│   └── modules/                # 功能模块
├── backend/                    # 后端微服务
│   ├── smart-office-parent/    # Maven父工程
│   ├── common/                 # 公共模块
│   ├── gateway/                # 网关服务（端口8080）
│   ├── device-service/         # 设备管理服务（端口8081）
│   ├── user-service/           # 用户服务（端口8082）
│   └── alarm-service/          # 告警服务（端口8083）
├── frontend/                   # Vue3前端项目
├── database/                   # 数据库初始化脚本
├── docker/                     # Docker配置文件
├── hardware/                   # 硬件连接指南
├── docs/                       # 项目文档
└── 项目完整运行指南.md          # 完整运行指南
```

## 🛠️ 技术栈

### 硬件层
- **W601 IoT开发板**: 主控芯片，支持WiFi和MicroPython
- **传感器**: DHT22温湿度、光敏电阻、火焰传感器
- **执行器**: RGB LED、蜂鸣器

### 后端技术
- **SpringBoot 2.7.x**: 微服务框架
- **SpringCloud 2021.x**: 微服务治理
- **SpringCloud Gateway**: API网关
- **MyBatis-Plus**: ORM框架
- **MySQL 8.0**: 关系型数据库
- **Redis**: 缓存数据库
- **MQTT (Eclipse Paho)**: 物联网通信协议

### 前端技术
- **Vue 3**: 前端框架
- **Element Plus**: UI组件库
- **Axios**: HTTP客户端
- **ECharts**: 数据可视化
- **WebSocket**: 实时通信

### 通信协议
- **MQTT**: 设备与服务器通信
- **WebSocket**: 前后端实时通信
- **RESTful API**: 标准HTTP接口

## ⚡ 环境要求

### 开发环境
- **JDK 1.8+**
- **Maven 3.6+**
- **Node.js 16+**
- **MySQL 8.0**
- **Redis**

### 硬件环境（可选）
- **W601开发板** × 1
- **传感器模块** × 若干
- **连接线材** × 若干

## 🔧 快速部署

### 1. 环境准备
```bash
# 检查Java环境
java -version
mvn -version

# 检查Node.js环境
node --version
npm --version

# 启动数据库服务
# MySQL和Redis需要预先安装并启动
```

### 2. 数据库初始化
```bash
# 导入数据库结构和初始数据
mysql -u root -p < database/init.sql
```

### 3. 启动MQTT服务
```bash
# 使用Docker启动EMQX
cd docker
docker-compose up -d emqx

# 或使用本地Mosquitto
# Windows: net start mosquitto
```

### 4. 启动后端服务
```bash
# 启动Device Service
cd backend/device-service
mvn spring-boot:run

# 启动User Service（新开终端）
cd backend/user-service
mvn spring-boot:run
```

### 5. 启动前端服务
```bash
cd frontend
npm install
npm run dev
```

### 6. 访问系统
- **前端界面**: http://localhost:5173
- **默认账号**: admin / 123456
- **EMQX管理**: http://localhost:18083 (admin / public)

## 🔌 硬件接入（可选）

如果你有W601开发板，可以按照以下步骤接入真实硬件：

### 1. 硬件连接
参考 [硬件连接指南](hardware/Hardware_Connection_Guide.md) 完成传感器连接

### 2. 代码烧录
1. 安装Thonny IDE
2. 配置 `W601_SmartOffice/config.py` 中的WiFi和MQTT参数
3. 上传所有代码文件到W601

### 3. 系统配置
确保后端MQTT配置与W601设备配置一致：
```yaml
# backend/device-service/src/main/resources/application.yml
mqtt:
  broker-url: tcp://你的服务器IP:1883
```

## 🎯 核心功能展示

### 1. 智慧照明管理
- **自动控制**: 光照传感器检测环境亮度，自动开关RGB灯
- **手动控制**: 支持远程手动控制灯光开关
- **阈值配置**: 可调节光照阈值，适应不同环境需求
- **状态监控**: 实时显示灯光状态和光照强度

### 2. 智慧环境调控
- **温湿度监测**: DHT22传感器实时采集环境数据
- **自动调控**: 根据设定阈值自动控制空调、加湿器等设备
- **数据可视化**: 实时图表展示温湿度变化趋势
- **历史数据**: 支持历史数据查询和分析

### 3. 智慧消防安全
- **火焰检测**: 火焰传感器实时监测火灾风险
- **自动报警**: 检测到火焰立即触发蜂鸣器报警
- **告警推送**: 系统自动记录告警信息并推送到前端
- **应急响应**: 支持远程控制蜂鸣器开关

### 4. 设备状态监控
- **实时监控**: 显示所有设备的在线状态和工作状态
- **心跳检测**: 定期检测设备连接状态
- **故障告警**: 设备离线或异常时自动告警
- **远程控制**: 支持远程控制设备开关和参数设置

### 5. 用户权限管理
- **身份认证**: JWT令牌认证，确保系统安全
- **角色管理**: 支持管理员、普通用户等不同角色
- **权限控制**: 不同角色拥有不同的操作权限
- **操作日志**: 记录用户操作历史，便于审计

## 📊 系统特色

### 🔄 实时性
- **WebSocket通信**: 前后端实时数据同步
- **MQTT协议**: 设备数据毫秒级传输
- **即时响应**: 告警信息实时推送

### 🛡️ 可靠性
- **微服务架构**: 服务独立部署，故障隔离
- **数据持久化**: MySQL存储历史数据
- **Redis缓存**: 提高系统响应速度

### 🔧 可扩展性
- **模块化设计**: 传感器和设备模块可独立扩展
- **标准协议**: 基于MQTT标准，支持多种设备接入
- **配置灵活**: 支持动态配置阈值和参数

### 📱 易用性
- **响应式设计**: 支持PC和移动端访问
- **直观界面**: 清晰的数据展示和操作界面
- **一键部署**: 提供完整的部署脚本和文档

## 📡 MQTT通信协议

### 设备上报数据
**Topic:** `office/sensor/data`

```json
{
  "deviceId": "W601_001",
  "light": 280.5,
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

## 📚 文档导航

### 📖 使用指南
- **[项目完整运行指南](项目完整运行指南.md)** - 详细的部署和运行步骤
- **[硬件连接指南](hardware/Hardware_Connection_Guide.md)** - W601硬件连接详解
- **[W601接入指南](docs/W601接入指南.md)** - 设备接入和配置说明

### 🔧 开发文档
- **[API接口文档](docs/API.md)** - 后端接口说明
- **[MicroPython测试指南](hardware/MicroPython_Test_Guide.md)** - 硬件测试方法
- **[RT-Thread配置指南](hardware/RT-Thread_Setup_Guide.md)** - RT-Thread开发环境

### 🚀 快速开始
- **[快速启动指南](docs/快速启动指南.md)** - 系统快速启动方法
- **[启动指南](启动指南.md)** - 详细启动步骤

## 🐛 常见问题

### 后端服务问题
- **端口被占用**: 使用 `netstat -ano | findstr 8081` 检查端口
- **数据库连接失败**: 检查MySQL服务状态和连接配置
- **MQTT连接失败**: 验证MQTT服务器地址和端口

### 前端访问问题
- **页面无法加载**: 检查Node.js版本和依赖安装
- **接口调用失败**: 确认后端服务正常启动
- **数据不显示**: 检查WebSocket连接状态

### 硬件连接问题
- **设备无法连接WiFi**: 检查WiFi名称、密码和信号强度
- **传感器数据异常**: 验证传感器连接和引脚配置
- **MQTT数据不上报**: 检查网络连通性和MQTT配置

## 🔄 更新日志

### v1.0.0 (2024-01-XX)
- ✅ 完成基础系统架构搭建
- ✅ 实现W601硬件设备接入
- ✅ 完成前后端功能开发
- ✅ 添加完整文档和部署指南
- ✅ 支持实时数据监控和设备控制
- ✅ 实现智能告警和自动控制功能

## 🤝 贡献指南

欢迎提交Issue和Pull Request来改进项目！

### 开发环境搭建
1. Fork本项目
2. 创建功能分支
3. 提交代码更改
4. 创建Pull Request

### 代码规范
- 后端：遵循阿里巴巴Java开发手册
- 前端：使用ESLint和Prettier格式化代码
- 硬件：遵循MicroPython编码规范

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 技术支持

如果在使用过程中遇到问题，请：

1. 查看相关文档和FAQ
2. 检查系统日志和错误信息
3. 在GitHub Issues中提交问题
4. 参考项目Wiki获取更多帮助

---

**🎉 感谢使用智慧办公楼系统！希望这个项目能为你的学习和开发带来帮助！**
