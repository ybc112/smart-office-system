# Docker部署说明

## 服务列表

| 服务 | 端口 | 用户名 | 密码 | 说明 |
|------|------|--------|------|------|
| EMQ X (MQTT) | 1883, 18083 | admin | public | MQTT消息代理 |
| MySQL | 3306 | root | 123456 | 关系数据库 |
| Redis | 6379 | - | redis123 | 缓存数据库 |

## 快速启动

### 1. 启动所有服务
```bash
cd docker
docker-compose up -d
```

### 2. 查看服务状态
```bash
docker-compose ps
```

### 3. 查看日志
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f emqx
docker-compose logs -f mysql
docker-compose logs -f redis
```

### 4. 停止服务
```bash
docker-compose down
```

### 5. 停止并删除数据
```bash
docker-compose down -v
```

## EMQ X 管理界面

访问地址：http://localhost:18083

默认账号：
- 用户名: admin
- 密码: public

### 常用功能
- **Clients**: 查看已连接的MQTT客户端
- **Subscriptions**: 查看主题订阅情况
- **Topics**: 查看活跃的主题
- **WebSocket**: 在线测试MQTT连接

## 测试MQTT连接

### 使用MQTTX工具
1. 下载MQTTX：https://mqttx.app/
2. 创建新连接：
   - Name: Smart Office
   - Host: localhost
   - Port: 1883
   - Username: (留空)
   - Password: (留空)

3. 测试订阅主题：
   - Topic: office/sensor/data

4. 测试发布消息：
   - Topic: office/sensor/data
   - Payload:
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

## 数据库连接

### MySQL
```bash
# 使用Docker命令行连接
docker exec -it smart-office-mysql mysql -uroot -p123456

# 使用客户端工具连接
Host: localhost
Port: 3306
Username: root
Password: 123456
Database: smart_office
```

### Redis
```bash
# 使用Docker命令行连接
docker exec -it smart-office-redis redis-cli -a redis123

# 测试
127.0.0.1:6379> PING
PONG
```

## 故障排查

### 端口被占用
```bash
# 查看端口占用
netstat -ano | findstr :1883
netstat -ano | findstr :3306
netstat -ano | findstr :6379

# 修改docker-compose.yml中的端口映射
```

### 容器启动失败
```bash
# 查看详细日志
docker-compose logs [service-name]

# 重启服务
docker-compose restart [service-name]
```

### 数据持久化
数据存储在 `docker/` 目录下：
- `mysql/data/` - MySQL数据文件
- `redis/data/` - Redis数据文件
- `emqx/data/` - EMQ X数据文件
- `emqx/log/` - EMQ X日志文件

## 注意事项

1. **首次启动**：MySQL会自动执行 `database/init.sql` 初始化脚本
2. **数据备份**：定期备份 `docker/` 目录下的数据文件
3. **安全性**：生产环境请修改默认密码
4. **网络**：所有服务在 `smart-office-network` 网络中，可以通过服务名互相访问
