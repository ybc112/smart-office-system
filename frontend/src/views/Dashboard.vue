<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card" v-for="stat in stats" :key="stat.title">
        <div class="stat-content">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-title">{{ stat.title }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 传感器数据图表 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>传感器数据趋势</span>
            <el-button-group class="chart-controls" size="small">
              <el-button 
                v-for="type in sensorTypes" 
                :key="type.key"
                :type="activeChart === type.key ? 'primary' : ''"
                @click="switchChart(type.key)"
                size="small"
              >
                {{ type.label }}
              </el-button>
            </el-button-group>
          </div>
        </template>
        <div class="chart-container" ref="chartContainer">
          <div id="sensorChart" style="width: 100%; height: 300px;"></div>
        </div>
      </el-card>

      <!-- 设备状态分布 -->
      <el-card class="chart-card">
        <template #header>
          <span>设备状态分布</span>
        </template>
        <div class="chart-container">
          <div id="deviceChart" style="width: 100%; height: 300px;"></div>
        </div>
      </el-card>
    </div>

    <!-- 告警列表 -->
    <el-card class="alarm-card">
      <template #header>
        <div class="card-header">
          <span>最新告警</span>
          <el-button type="text" @click="$router.push('/alarms')" size="small">
            查看全部
          </el-button>
        </div>
      </template>
      <div class="alarm-list">
        <div 
          v-for="alarm in recentAlarms" 
          :key="alarm.id" 
          class="alarm-item"
          :class="`alarm-${alarm.level?.toLowerCase()}`"
        >
          <div class="alarm-content">
            <div class="alarm-header">
              <span class="alarm-type">{{ alarm.alarmType }}</span>
              <span class="alarm-time">{{ formatTime(alarm.createTime) }}</span>
            </div>
            <div class="alarm-message">{{ alarm.message }}</div>
            <div class="alarm-location" v-if="alarm.deviceId">
              设备ID: {{ alarm.deviceId }}
            </div>
          </div>
          <div class="alarm-level">
            <el-tag 
              :type="getAlarmTagType(alarm.level)" 
              size="small"
            >
              {{ alarm.level }}
            </el-tag>
          </div>
        </div>
        <div v-if="recentAlarms.length === 0" class="no-data">
          暂无告警信息
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import {
  Monitor, CircleCheck, Bell, Warning, Sunny,
  Orange, Coffee, Clock, Sunrise  // Orange代替Thermometer（温度），Coffee代替Drizzling（湿度）
} from '@element-plus/icons-vue'
import { getDeviceList, getLatestSensorData } from '@/api/device'
import { getAlarmList } from '@/api/alarm'
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'
import * as echarts from 'echarts'

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

// 图表相关
let sensorChart = null
let deviceChart = null
const activeChart = ref('temperature')
const sensorTypes = ref([
  { key: 'temperature', label: '温度' },
  { key: 'humidity', label: '湿度' },
  { key: 'light', label: '光照' }
])

// 图表数据
const chartData = ref({
  temperature: [],
  humidity: [],
  light: []
})

// 统计卡片数据
const stats = computed(() => [
  {
    title: '设备总数',
    value: deviceStats.value.total,
    icon: Monitor,
    color: '#409eff'
  },
  {
    title: '在线设备',
    value: deviceStats.value.online,
    icon: CircleCheck,
    color: '#67c23a'
  },
  {
    title: '未处理告警',
    value: alarmStats.value.unhandled,
    icon: Warning,
    color: '#f56c6c'
  },
  {
    title: '今日告警',
    value: alarmStats.value.today,
    icon: Bell,
    color: '#e6a23c'
  },
  {
    title: '温度',
    value: latestData.value ? `${latestData.value.temperature}°C` : '--',
    icon: Orange,
    color: '#ff7875'
  },
  {
    title: '湿度',
    value: latestData.value ? `${latestData.value.humidity}%` : '--',
    icon: Coffee,
    color: '#40a9ff'
  },
  {
    title: '光照',
    value: latestData.value ? `${latestData.value.light}lux` : '--',
    icon: Sunny,
    color: '#faad14'
  },
  {
    title: '最后更新',
    value: latestData.value ? formatTime(latestData.value.timestamp) : '--',
    icon: Clock,
    color: '#722ed1'
  }
])

// 获取设备统计
const loadDeviceStats = async () => {
  try {
    const res = await getDeviceList()
    if (res.code === 200 && res.data) {
      deviceStats.value.total = res.data.length
      deviceStats.value.online = res.data.filter(d => d.status === 'ONLINE').length
      // 更新设备状态图表
      updateDeviceChart()
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
      // 添加数据到图表
      addSensorDataToChart(res.data)
    }
  } catch (error) {
    console.error('获取传感器数据失败:', error)
  }
}

// 获取告警统计和最新告警
const loadAlarmData = async () => {
  try {
    const res = await getAlarmList({ pageNum: 1, pageSize: 5 })
    if (res && res.code === 200 && res.data) {
      // 确保recentAlarms始终是数组 - 多层防御
      let alarmData = res.data.records || res.data.list || res.data || []
      if (!Array.isArray(alarmData)) {
        alarmData = []
      }
      recentAlarms.value = alarmData

      // 统计未处理告警
      const allAlarmsRes = await getAlarmList({ pageNum: 1, pageSize: 1000 })
      if (allAlarmsRes && allAlarmsRes.code === 200 && allAlarmsRes.data) {
        let allAlarms = allAlarmsRes.data.records || allAlarmsRes.data.list || allAlarmsRes.data || []
        if (!Array.isArray(allAlarms)) {
          allAlarms = []
        }
        alarmStats.value.unhandled = allAlarms.filter(a => a.status === 'UNHANDLED').length

        // 统计今日告警
        const today = new Date().toISOString().split('T')[0]
        alarmStats.value.today = allAlarms.filter(a =>
          a.alarmTime && a.alarmTime.startsWith(today)
        ).length
      }
    }
  } catch (error) {
    console.error('获取告警数据失败:', error)
    recentAlarms.value = []
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
        // 添加数据到图表
        addSensorDataToChart(data)
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
const getAlarmTagType = (level) => {
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

// 初始化传感器数据图表
const initSensorChart = () => {
  const chartDom = document.getElementById('sensorChart')
  if (!chartDom) return
  
  sensorChart = echarts.init(chartDom)
  
  const option = {
    title: {
      text: '传感器数据趋势',
      left: 'center',
      textStyle: {
        fontSize: 14,
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['数值'],
      bottom: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '数值',
      type: 'line',
      smooth: true,
      data: []
    }]
  }
  
  sensorChart.setOption(option)
}

// 初始化设备状态分布图表
const initDeviceChart = () => {
  const chartDom = document.getElementById('deviceChart')
  if (!chartDom) return
  
  deviceChart = echarts.init(chartDom)
  
  const option = {
    title: {
      text: '设备状态分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      name: '设备状态',
      type: 'pie',
      radius: '50%',
      data: [
        { value: 0, name: '在线' },
        { value: 0, name: '离线' },
        { value: 0, name: '故障' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  deviceChart.setOption(option)
}

// 切换图表类型
const switchChart = (type) => {
  activeChart.value = type
  updateSensorChart()
}

// 更新传感器数据图表
const updateSensorChart = () => {
  if (!sensorChart) return
  
  const data = chartData.value[activeChart.value] || []
  const times = data.map(item => item.time)
  const values = data.map(item => item.value)
  
  let unit = ''
  switch (activeChart.value) {
    case 'temperature':
      unit = '°C'
      break
    case 'humidity':
      unit = '%'
      break
    case 'light':
      unit = 'lux'
      break
  }
  
  sensorChart.setOption({
    title: {
      text: `${sensorTypes.value.find(t => t.key === activeChart.value)?.label}趋势`
    },
    xAxis: {
      data: times
    },
    yAxis: {
      name: unit
    },
    series: [{
      data: values
    }]
  })
}

// 更新设备状态图表
const updateDeviceChart = () => {
  if (!deviceChart) return
  
  const online = deviceStats.value.online
  const offline = deviceStats.value.total - deviceStats.value.online
  
  deviceChart.setOption({
    series: [{
      data: [
        { value: online, name: '在线', itemStyle: { color: '#67c23a' } },
        { value: offline, name: '离线', itemStyle: { color: '#909399' } }
      ]
    }]
  })
}

// 添加传感器数据到图表
const addSensorDataToChart = (data) => {
  const time = new Date(data.timestamp || Date.now()).toLocaleTimeString()
  
  // 添加温度数据
  if (data.temperature !== undefined) {
    chartData.value.temperature.push({
      time,
      value: data.temperature
    })
    // 保持最近20个数据点
    if (chartData.value.temperature.length > 20) {
      chartData.value.temperature.shift()
    }
  }
  
  // 添加湿度数据
  if (data.humidity !== undefined) {
    chartData.value.humidity.push({
      time,
      value: data.humidity
    })
    if (chartData.value.humidity.length > 20) {
      chartData.value.humidity.shift()
    }
  }
  
  // 添加光照数据
  if (data.light !== undefined) {
    chartData.value.light.push({
      time,
      value: data.light
    })
    if (chartData.value.light.length > 20) {
      chartData.value.light.shift()
    }
  }
  
  // 更新当前显示的图表
  updateSensorChart()
}

onMounted(() => {
  loadDeviceStats()
  loadLatestData()
  loadAlarmData()
  connectWebSocket()

  // 等待DOM渲染完成后初始化图表
  nextTick(() => {
    initSensorChart()
    initDeviceChart()
  })

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
    // 销毁图表实例
    if (sensorChart) {
      sensorChart.dispose()
      sensorChart = null
    }
    if (deviceChart) {
      deviceChart.dispose()
      deviceChart = null
    }
  })
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1;
  margin-bottom: 5px;
}

.stat-title {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.chart-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.chart-container {
  position: relative;
  width: 100%;
  overflow: hidden;
}

.alarm-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.alarm-list {
  max-height: 400px;
  overflow-y: auto;
}

.alarm-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 15px;
  border-left: 4px solid #e0e0e0;
  margin-bottom: 10px;
  background: #fafafa;
  border-radius: 0 6px 6px 0;
  transition: all 0.2s ease;
  gap: 15px;
}

.alarm-item:hover {
  background: #f0f0f0;
}

.alarm-item:last-child {
  margin-bottom: 0;
}

.alarm-critical {
  border-left-color: #f56c6c;
}

.alarm-high {
  border-left-color: #e6a23c;
}

.alarm-medium {
  border-left-color: #409eff;
}

.alarm-low {
  border-left-color: #67c23a;
}

.alarm-content {
  flex: 1;
  min-width: 0;
}

.alarm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 10px;
}

.alarm-type {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.alarm-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

.alarm-message {
  color: #666;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 5px;
  word-break: break-word;
}

.alarm-location {
  font-size: 12px;
  color: #999;
}

.alarm-level {
  flex-shrink: 0;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 40px 20px;
  font-size: 14px;
}

/* 移动端适配 */
@media (max-width: 1200px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 15px;
    margin-bottom: 15px;
  }
  
  .charts-grid {
    gap: 15px;
    margin-bottom: 15px;
  }
  
  .stat-content {
    gap: 12px;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .stat-title {
    font-size: 13px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .chart-controls {
    width: 100%;
    justify-content: flex-start;
  }
  
  .chart-container {
    overflow-x: auto;
  }
  
  #sensorChart,
  #deviceChart {
    min-width: 300px;
  }
  
  .alarm-item {
    padding: 12px;
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .alarm-header {
    margin-bottom: 5px;
  }
  
  .alarm-level {
    align-self: flex-end;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 10px;
    margin-bottom: 10px;
  }
  
  .charts-grid {
    gap: 10px;
    margin-bottom: 10px;
  }
  
  .stat-content {
    gap: 10px;
  }
  
  .stat-icon {
    width: 45px;
    height: 45px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .stat-title {
    font-size: 12px;
  }
  
  .chart-controls .el-button {
    padding: 5px 8px;
    font-size: 12px;
  }
  
  .alarm-item {
    padding: 10px;
  }
  
  .alarm-type {
    font-size: 13px;
  }
  
  .alarm-message {
    font-size: 12px;
  }
  
  .alarm-time,
  .alarm-location {
    font-size: 11px;
  }
}

/* 横屏适配 */
@media (max-width: 768px) and (orientation: landscape) {
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  }
  
  .charts-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
