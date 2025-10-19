<template>
  <div class="dashboard">
    <div class="stats-row">
      <el-card class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon" color="#409EFF" :size="40">
            <Monitor />
          </el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ deviceStats.total }}</div>
            <div class="stat-label">设备总数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon" color="#67C23A" :size="40">
            <CircleCheck />
          </el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ deviceStats.online }}</div>
            <div class="stat-label">在线设备</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon" color="#F56C6C" :size="40">
            <Bell />
          </el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ alarmStats.unhandled }}</div>
            <div class="stat-label">未处理告警</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon" color="#E6A23C" :size="40">
            <Warning />
          </el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ alarmStats.today }}</div>
            <div class="stat-label">今日告警</div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="data-row">
      <el-card class="device-card">
        <template #header>
          <div class="card-header">
            <span>实时传感器数据</span>
            <el-tag :type="wsConnected ? 'success' : 'danger'" size="small">
              {{ wsConnected ? '已连接' : '未连接' }}
            </el-tag>
          </div>
        </template>
        <div v-if="latestData" class="sensor-data">
          <div class="sensor-item">
            <el-icon :size="30" color="#409EFF"><Sunny /></el-icon>
            <div class="sensor-info">
              <div class="sensor-label">光照强度</div>
              <div class="sensor-value">{{ latestData.light || '--' }} <span class="unit">lux</span></div>
            </div>
          </div>

          <div class="sensor-item">
            <el-icon :size="30" color="#F56C6C"><Orange /></el-icon>
            <div class="sensor-info">
              <div class="sensor-label">温度</div>
              <div class="sensor-value">{{ latestData.temperature || '--' }} <span class="unit">℃</span></div>
            </div>
          </div>

          <div class="sensor-item">
            <el-icon :size="30" color="#67C23A"><Coffee /></el-icon>
            <div class="sensor-info">
              <div class="sensor-label">湿度</div>
              <div class="sensor-value">{{ latestData.humidity || '--' }} <span class="unit">%</span></div>
            </div>
          </div>

          <div class="sensor-item">
            <el-icon :size="30" :color="latestData.flame ? '#F56C6C' : '#909399'">
              <Warning />
            </el-icon>
            <div class="sensor-info">
              <div class="sensor-label">火焰检测</div>
              <div class="sensor-value">
                <el-tag :type="latestData.flame ? 'danger' : 'success'" size="large">
                  {{ latestData.flame ? '检测到火焰' : '正常' }}
                </el-tag>
              </div>
            </div>
          </div>

          <div class="sensor-item">
            <el-icon :size="30" :color="latestData.rgbStatus ? '#E6A23C' : '#909399'">
              <Sunrise />
            </el-icon>
            <div class="sensor-info">
              <div class="sensor-label">灯光状态</div>
              <div class="sensor-value">
                <el-tag :type="latestData.rgbStatus ? 'warning' : 'info'" size="large">
                  {{ latestData.rgbStatus ? '已开启' : '已关闭' }}
                </el-tag>
              </div>
            </div>
          </div>

          <div class="sensor-item">
            <el-icon :size="30" color="#909399"><Clock /></el-icon>
            <div class="sensor-info">
              <div class="sensor-label">更新时间</div>
              <div class="sensor-value small">{{ formatTime(latestData.dataTime) }}</div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无数据" />
      </el-card>

      <el-card class="alarm-card">
        <template #header>
          <div class="card-header">
            <span>最新告警</span>
            <el-link type="primary" @click="goToAlarms">查看全部</el-link>
          </div>
        </template>
        <div class="alarm-list" v-if="recentAlarms.length > 0">
          <div
            v-for="alarm in recentAlarms"
            :key="alarm.id"
            class="alarm-item"
          >
            <el-tag
              :type="getAlarmLevelType(alarm.alarmLevel)"
              size="small"
              style="margin-right: 10px"
            >
              {{ getAlarmLevelText(alarm.alarmLevel) }}
            </el-tag>
            <span class="alarm-message">{{ alarm.alarmMessage }}</span>
            <span class="alarm-time">{{ formatTime(alarm.alarmTime) }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无告警" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Monitor, CircleCheck, Bell, Warning, Sunny,
  Orange, Coffee, Clock, Sunrise  // Orange代替Thermometer（温度），Coffee代替Drizzling（湿度）
} from '@element-plus/icons-vue'
import { getDeviceList, getLatestSensorData } from '@/api/device'
import { getAlarmList } from '@/api/alarm'
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'

const router = useRouter()

// 统计数据
const deviceStats = ref({
  total: 0,
  online: 0
})

const alarmStats = ref({
  unhandled: 0,
  today: 0
})

// 最新传感器数据
const latestData = ref(null)

// 最新告警列表
const recentAlarms = ref([])

// WebSocket连接状态
const wsConnected = ref(false)
let stompClient = null

// 获取设备统计
const loadDeviceStats = async () => {
  try {
    const res = await getDeviceList()
    if (res.code === 200 && res.data) {
      deviceStats.value.total = res.data.length
      deviceStats.value.online = res.data.filter(d => d.status === 'ONLINE').length
    }
  } catch (error) {
    console.error('获取设备统计失败:', error)
  }
}

// 获取最新传感器数据
const loadLatestData = async () => {
  try {
    // 默认获取W601_001的数据
    const res = await getLatestSensorData('W601_001')
    if (res.code === 200 && res.data) {
      latestData.value = res.data
    }
  } catch (error) {
    console.error('获取传感器数据失败:', error)
  }
}

// 获取告警统计和最新告警
const loadAlarmData = async () => {
  try {
    const res = await getAlarmList({ pageNum: 1, pageSize: 5 })
    if (res.code === 200 && res.data) {
      recentAlarms.value = res.data.records || []

      // 统计未处理告警
      const allAlarmsRes = await getAlarmList({ pageNum: 1, pageSize: 1000 })
      if (allAlarmsRes.code === 200 && allAlarmsRes.data) {
        const alarms = allAlarmsRes.data.records || []
        alarmStats.value.unhandled = alarms.filter(a => a.status === 'UNHANDLED').length

        // 统计今日告警
        const today = new Date().toISOString().split('T')[0]
        alarmStats.value.today = alarms.filter(a =>
          a.alarmTime && a.alarmTime.startsWith(today)
        ).length
      }
    }
  } catch (error) {
    console.error('获取告警数据失败:', error)
  }
}

// 建立WebSocket连接
const connectWebSocket = () => {
  try {
    const socket = new SockJS('http://localhost:8081/ws')
    stompClient = Stomp.over(socket)

    stompClient.connect({}, () => {
      wsConnected.value = true
      console.log('WebSocket连接成功')

      // 订阅传感器数据
      stompClient.subscribe('/topic/sensor-data', (message) => {
        const data = JSON.parse(message.body)
        latestData.value = data
        loadDeviceStats() // 更新设备统计
      })

      // 订阅告警消息
      stompClient.subscribe('/topic/alarm', (message) => {
        const alarm = JSON.parse(message.body)
        recentAlarms.value.unshift(alarm)
        if (recentAlarms.value.length > 5) {
          recentAlarms.value.pop()
        }
        loadAlarmData() // 更新告警统计
      })
    }, (error) => {
      wsConnected.value = false
      console.error('WebSocket连接失败:', error)
    })
  } catch (error) {
    console.error('WebSocket初始化失败:', error)
  }
}

// 断开WebSocket
const disconnectWebSocket = () => {
  if (stompClient) {
    stompClient.disconnect()
    wsConnected.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '--'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取告警级别类型
const getAlarmLevelType = (level) => {
  const types = {
    'CRITICAL': 'danger',
    'WARNING': 'warning',
    'INFO': 'info'
  }
  return types[level] || 'info'
}

// 获取告警级别文本
const getAlarmLevelText = (level) => {
  const texts = {
    'CRITICAL': '严重',
    'WARNING': '警告',
    'INFO': '提示'
  }
  return texts[level] || level
}

// 跳转到告警页面
const goToAlarms = () => {
  router.push('/alarms')
}

onMounted(() => {
  loadDeviceStats()
  loadLatestData()
  loadAlarmData()
  connectWebSocket()

  // 定时刷新数据（作为WebSocket的备份）
  const interval = setInterval(() => {
    if (!wsConnected.value) {
      loadLatestData()
      loadDeviceStats()
      loadAlarmData()
    }
  }, 5000)

  onUnmounted(() => {
    clearInterval(interval)
    disconnectWebSocket()
  })
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.data-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.sensor-data {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.sensor-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.sensor-info {
  flex: 1;
}

.sensor-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.sensor-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.sensor-value.small {
  font-size: 14px;
}

.unit {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
}

.alarm-list {
  max-height: 400px;
  overflow-y: auto;
}

.alarm-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #EBEEF5;
}

.alarm-item:last-child {
  border-bottom: none;
}

.alarm-message {
  flex: 1;
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
}

.alarm-time {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .data-row {
    grid-template-columns: 1fr;
  }

  .sensor-data {
    grid-template-columns: 1fr;
  }
}
</style>
