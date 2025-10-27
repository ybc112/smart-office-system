<template>
  <div class="devices">
    <el-card class="devices-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">设备列表</span>
          <el-button type="primary" size="small" @click="refreshDevices" class="refresh-btn">
            <el-icon><Refresh /></el-icon>
            <span class="btn-text">刷新</span>
          </el-button>
        </div>
      </template>

      <!-- 移动端卡片视图 -->
      <div class="mobile-device-list" v-if="isMobile">
        <div 
          v-for="device in deviceList" 
          :key="device.deviceId" 
          class="device-card-mobile"
          @click="viewDevice(device)"
        >
          <div class="device-header">
            <div class="device-info">
              <h4 class="device-name">{{ device.deviceName }}</h4>
              <p class="device-id">{{ device.deviceId }}</p>
            </div>
            <div class="device-status">
              <el-tag
                :type="device.status === 'ONLINE' ? 'success' : device.status === 'FAULT' ? 'danger' : 'info'"
                size="small"
              >
                {{ getStatusText(device.status) }}
              </el-tag>
            </div>
          </div>
          <div class="device-details">
            <div class="detail-item">
              <span class="label">类型:</span>
              <el-tag :type="device.deviceType === 'SENSOR' ? 'success' : 'warning'" size="small">
                {{ device.deviceType === 'SENSOR' ? '传感器' : '执行器' }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">位置:</span>
              <span class="value">{{ device.location }}</span>
            </div>
            <div class="detail-item">
              <span class="label">最后在线:</span>
              <span class="value">{{ formatTime(device.lastOnlineTime) }}</span>
            </div>
          </div>
          <div class="device-actions">
            <el-button size="small" type="primary" @click.stop="viewDevice(device)">
              详情
            </el-button>
            <el-button size="small" type="success" @click.stop="controlDevice(device)">
              控制
            </el-button>
          </div>
        </div>
      </div>

      <!-- 桌面端表格视图 -->
      <el-table :data="deviceList" border style="width: 100%" v-else class="devices-table">
        <el-table-column prop="deviceId" label="设备编号" width="150" />
        <el-table-column prop="deviceName" label="设备名称" width="180" />
        <el-table-column prop="deviceType" label="设备类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.deviceType === 'SENSOR' ? 'success' : 'warning'" size="small">
              {{ row.deviceType === '传感器' ? '传感器' : '执行器' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'ONLINE' ? 'success' : row.status === 'FAULT' ? 'danger' : 'info'"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastOnlineTime" label="最后在线时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastOnlineTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewDevice(row)">
              查看详情
            </el-button>
            <el-button size="small" type="success" @click="controlDevice(row)">
              设备控制
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 设备详情对话框 -->
    <el-dialog 
      v-model="detailVisible" 
      title="设备详情" 
      :width="isMobile ? '95%' : '60%'"
      :fullscreen="isMobile"
      class="device-detail-dialog"
    >
      <div v-if="selectedDevice" class="device-detail-content">
        <el-descriptions :column="isMobile ? 1 : 2" border class="device-descriptions">
          <el-descriptions-item label="设备编号">
            {{ selectedDevice.deviceId }}
          </el-descriptions-item>
          <el-descriptions-item label="设备名称">
            {{ selectedDevice.deviceName }}
          </el-descriptions-item>
          <el-descriptions-item label="设备类型">
            {{ selectedDevice.deviceType }}
          </el-descriptions-item>
          <el-descriptions-item label="位置">
            {{ selectedDevice.location }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedDevice.status === 'ONLINE' ? 'success' : 'info'">
              {{ getStatusText(selectedDevice.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="固件版本">
            {{ selectedDevice.firmwareVersion || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="isMobile ? 1 : 2">
            {{ selectedDevice.description || '--' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>最新传感器数据</el-divider>

        <div v-if="deviceSensorData" class="sensor-data-grid">
          <el-card class="data-item">
            <div class="data-content">
              <el-icon :size="isMobile ? 24 : 30" color="#409EFF"><Sunny /></el-icon>
              <div class="data-info">
                <div class="data-label">光照强度</div>
                <div class="data-value">{{ deviceSensorData.light || '--' }} lux</div>
              </div>
            </div>
          </el-card>

          <el-card class="data-item">
            <div class="data-content">
              <el-icon :size="isMobile ? 24 : 30" color="#F56C6C"><Orange /></el-icon>
              <div class="data-info">
                <div class="data-label">温度</div>
                <div class="data-value">{{ deviceSensorData.temperature || '--' }} ℃</div>
              </div>
            </div>
          </el-card>

          <el-card class="data-item">
            <div class="data-content">
              <el-icon :size="isMobile ? 24 : 30" color="#67C23A"><Coffee /></el-icon>
              <div class="data-info">
                <div class="data-label">湿度</div>
                <div class="data-value">{{ deviceSensorData.humidity || '--' }} %</div>
              </div>
            </div>
          </el-card>

          <el-card class="data-item">
            <div class="data-content">
              <el-icon :size="isMobile ? 24 : 30" :color="deviceSensorData.flame ? '#F56C6C' : '#909399'">
                <Warning />
              </el-icon>
              <div class="data-info">
                <div class="data-label">火焰检测</div>
                <div class="data-value">
                  <el-tag :type="deviceSensorData.flame ? 'danger' : 'success'" size="small">
                    {{ deviceSensorData.flame ? '检测到火焰' : '正常' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </div>
        <el-empty v-else description="暂无传感器数据" />
      </div>
    </el-dialog>

    <!-- 设备控制对话框 -->
    <el-dialog 
      v-model="controlVisible" 
      title="设备控制" 
      :width="isMobile ? '95%' : '50%'"
      class="device-control-dialog"
    >
      <div v-if="selectedDevice" class="control-content">
        <el-alert
          title="设备控制"
          :description="`正在控制设备: ${selectedDevice.deviceName} (${selectedDevice.deviceId})`"
          type="info"
          show-icon
          :closable="false"
          class="control-alert"
        />

        <div class="control-panel">
          <div class="control-item">
            <label class="control-label">RGB灯光控制</label>
            <div class="control-actions">
              <el-button 
                type="success" 
                @click="controlRGB(true)"
                :loading="controlLoading"
                size="default"
              >
                开启
              </el-button>
              <el-button 
                type="danger" 
                @click="controlRGB(false)"
                :loading="controlLoading"
                size="default"
              >
                关闭
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh, Sunny, Orange, Coffee,
  Warning, Sunrise, Clock
} from '@element-plus/icons-vue'
import { getDeviceList, getLatestSensorData, controlDevice as controlDeviceApi } from '@/api/device'

// 移动端检测
const isMobile = ref(false)

// 设备列表
const deviceList = ref([])

// 详情对话框
const detailVisible = ref(false)
const selectedDevice = ref(null)
const deviceSensorData = ref(null)

// 控制对话框
const controlVisible = ref(false)
const controlCommand = ref('')
const sending = ref(false)
const controlLoading = ref(false)

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 监听窗口大小变化
const handleResize = () => {
  checkMobile()
}

// 加载设备列表
const loadDevices = async () => {
  try {
    const res = await getDeviceList()
    if (res && res.code === 200) {
      // Ensure deviceList is always an array to satisfy ElTable
      deviceList.value = Array.isArray(res.data) ? res.data : []
    } else {
      deviceList.value = []
      ElMessage.error(res?.message || '获取设备列表失败')
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('获取设备列表失败')
    deviceList.value = []
  }
}

// 刷新设备列表
const refreshDevices = () => {
  loadDevices()
  ElMessage.success('刷新成功')
}

// 查看设备详情
const viewDevice = async (device) => {
  selectedDevice.value = device
  detailVisible.value = true

  // 加载传感器数据
  try {
    const res = await getLatestSensorData(device.deviceId)
    if (res.code === 200 && res.data) {
      deviceSensorData.value = res.data
    }
  } catch (error) {
    console.error('获取传感器数据失败:', error)
  }
}

// 打开控制对话框
const controlDevice = (device) => {
  selectedDevice.value = device
  controlCommand.value = ''
  controlVisible.value = true
}

// 发送控制命令
const sendControlCommand = async () => {
  if (!controlCommand.value) {
    ElMessage.warning('请选择控制命令')
    return
  }

  sending.value = true
  try {
    const res = await controlDeviceApi({
      deviceId: selectedDevice.value.deviceId,
      action: controlCommand.value
    })

    if (res.code === 200) {
      ElMessage.success(res.message || '控制命令已发送')
      controlVisible.value = false
    } else {
      ElMessage.error(res.message || '发送命令失败')
    }
  } catch (error) {
    console.error('发送控制命令失败:', error)
    ElMessage.error('发送命令失败')
  } finally {
    sending.value = false
  }
}

// RGB控制功能
const controlRGB = async (isOn) => {
  controlLoading.value = true
  try {
    const res = await controlDeviceApi({
      deviceId: selectedDevice.value.deviceId,
      action: isOn ? 'rgb_on' : 'rgb_off'
    })

    if (res.code === 200) {
      ElMessage.success(res.message || `RGB灯已${isOn ? '开启' : '关闭'}`)
      controlVisible.value = false
    } else {
      ElMessage.error(res.message || '控制失败')
    }
  } catch (error) {
    console.error('RGB控制失败:', error)
    ElMessage.error('控制失败')
  } finally {
    controlLoading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '--'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'ONLINE': '在线',
    'OFFLINE': '离线',
    'FAULT': '故障'
  }
  return statusMap[status] || status
}

onMounted(() => {
  loadDevices()
  checkMobile()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.devices {
  padding: 0;
}

.devices-card {
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

.header-title {
  font-weight: 600;
  color: #333;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 5px;
}

.btn-text {
  display: inline;
}

/* 移动端设备卡片 */
.mobile-device-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.device-card-mobile {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.device-card-mobile:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.device-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.device-info {
  flex: 1;
  min-width: 0;
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 5px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-id {
  font-size: 13px;
  color: #666;
  margin: 0;
  font-family: 'Courier New', monospace;
}

.device-status {
  flex-shrink: 0;
  margin-left: 10px;
}

.device-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.detail-item .label {
  color: #666;
  font-weight: 500;
  min-width: 60px;
}

.detail-item .value {
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* 桌面端表格 */
.devices-table {
  border-radius: 8px;
  overflow: hidden;
}

/* 对话框样式 */
.device-detail-dialog .el-dialog__body {
  padding: 20px;
}

.device-detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.device-descriptions {
  margin-bottom: 20px;
}

.sensor-data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.data-item {
  border-radius: 8px;
  transition: transform 0.2s ease;
}

.data-item:hover {
  transform: translateY(-2px);
}

.data-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.data-info {
  flex: 1;
  min-width: 0;
}

.data-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 5px;
}

.data-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.control-content {
  padding: 10px 0;
}

.control-alert {
  margin-bottom: 20px;
}

.control-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.control-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
}

.control-label {
  font-weight: 500;
  color: #333;
}

.control-actions {
  display: flex;
  gap: 10px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .refresh-btn .btn-text {
    display: none;
  }
  
  .sensor-data-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .data-content {
    gap: 10px;
  }
  
  .data-value {
    font-size: 16px;
  }
  
  .control-item {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
    text-align: center;
  }
  
  .control-actions {
    justify-content: center;
  }
  
  .device-detail-dialog .el-dialog__body {
    padding: 15px;
  }
}

@media (max-width: 480px) {
  .device-card-mobile {
    padding: 12px;
  }
  
  .device-name {
    font-size: 15px;
  }
  
  .device-id {
    font-size: 12px;
  }
  
  .detail-item {
    font-size: 12px;
  }
  
  .detail-item .label {
    min-width: 50px;
  }
  
  .device-actions {
    gap: 8px;
  }
  
  .device-actions .el-button {
    padding: 5px 10px;
    font-size: 12px;
  }
  
  .sensor-data-grid {
    gap: 10px;
  }
  
  .data-content {
    gap: 8px;
  }
  
  .data-label {
    font-size: 12px;
  }
  
  .data-value {
    font-size: 14px;
  }
  
  .control-item {
    padding: 12px;
  }
  
  .device-detail-dialog .el-dialog__body {
    padding: 10px;
  }
}

/* 横屏适配 */
@media (max-width: 768px) and (orientation: landscape) {
  .sensor-data-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .device-detail-content {
    max-height: 60vh;
  }
}
</style>
