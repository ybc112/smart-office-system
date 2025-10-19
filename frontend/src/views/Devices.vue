<template>
  <div class="devices">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备列表</span>
          <el-button type="primary" size="small" @click="refreshDevices">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <el-table :data="deviceList" border style="width: 100%">
        <el-table-column prop="deviceId" label="设备编号" width="150" />
        <el-table-column prop="deviceName" label="设备名称" width="180" />
        <el-table-column prop="deviceType" label="设备类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.deviceType === 'SENSOR' ? 'success' : 'warning'" size="small">
              {{ row.deviceType === 'SENSOR' ? '传感器' : '执行器' }}
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
    <el-dialog v-model="detailVisible" title="设备详情" width="60%">
      <div v-if="selectedDevice">
        <el-descriptions :column="2" border>
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
          <el-descriptions-item label="描述" :span="2">
            {{ selectedDevice.description || '--' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>最新传感器数据</el-divider>

        <div v-if="deviceSensorData" class="sensor-data-grid">
          <el-card class="data-item">
            <div class="data-content">
              <el-icon :size="30" color="#409EFF"><Sunny /></el-icon>
              <div>
                <div class="data-label">光照强度</div>
                <div class="data-value">{{ deviceSensorData.light || '--' }} lux</div>
              </div>
            </div>
          </el-card>

          <el-card class="data-item">
            <div class="data-content">
              <el-icon :size="30" color="#F56C6C"><Orange /></el-icon>
              <div>
                <div class="data-label">温度</div>
                <div class="data-value">{{ deviceSensorData.temperature || '--' }} ℃</div>
              </div>
            </div>
          </el-card>

          <el-card class="data-item">
            <div class="data-content">
              <el-icon :size="30" color="#67C23A"><Coffee /></el-icon>
              <div>
                <div class="data-label">湿度</div>
                <div class="data-value">{{ deviceSensorData.humidity || '--' }} %</div>
              </div>
            </div>
          </el-card>

          <el-card class="data-item">
            <div class="data-content">
              <el-icon :size="30" :color="deviceSensorData.flame ? '#F56C6C' : '#67C23A'">
                <Warning />
              </el-icon>
              <div>
                <div class="data-label">火焰检测</div>
                <div class="data-value">
                  <el-tag :type="deviceSensorData.flame ? 'danger' : 'success'">
                    {{ deviceSensorData.flame ? '检测到火焰' : '正常' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>

          <el-card class="data-item">
            <div class="data-content">
              <el-icon :size="30" :color="deviceSensorData.rgbStatus ? '#E6A23C' : '#909399'">
                <Sunrise />
              </el-icon>
              <div>
                <div class="data-label">RGB灯</div>
                <div class="data-value">
                  <el-tag :type="deviceSensorData.rgbStatus ? 'warning' : 'info'">
                    {{ deviceSensorData.rgbStatus ? '开启' : '关闭' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>

          <el-card class="data-item">
            <div class="data-content">
              <el-icon :size="30" color="#909399"><Clock /></el-icon>
              <div>
                <div class="data-label">更新时间</div>
                <div class="data-value small">{{ formatTime(deviceSensorData.dataTime) }}</div>
              </div>
            </div>
          </el-card>
        </div>
        <el-empty v-else description="暂无传感器数据" />
      </div>
    </el-dialog>

    <!-- 设备控制对话框 -->
    <el-dialog v-model="controlVisible" title="设备控制" width="40%">
      <div v-if="selectedDevice">
        <el-form label-width="100px">
          <el-form-item label="设备编号">
            <el-input v-model="selectedDevice.deviceId" disabled />
          </el-form-item>
          <el-form-item label="控制命令">
            <el-select v-model="controlCommand" placeholder="请选择控制命令" style="width: 100%">
              <el-option label="开启RGB灯" value="rgb_on" />
              <el-option label="关闭RGB灯" value="rgb_off" />
              <el-option label="开启蜂鸣器" value="buzzer_on" />
              <el-option label="关闭蜂鸣器" value="buzzer_off" />
              <el-option label="开启加湿器" value="humidifier_on" />
              <el-option label="关闭加湿器" value="humidifier_off" />
              <el-option label="空调制热" value="ac_heat" />
              <el-option label="空调制冷" value="ac_cool" />
              <el-option label="关闭空调" value="ac_off" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="controlVisible = false">取消</el-button>
        <el-button type="primary" @click="sendControlCommand" :loading="sending">
          发送命令
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh, Sunny, Orange, Coffee,
  Warning, Sunrise, Clock
} from '@element-plus/icons-vue'
import { getDeviceList, getLatestSensorData, controlDevice as controlDeviceApi } from '@/api/device'

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

// 加载设备列表
const loadDevices = async () => {
  try {
    const res = await getDeviceList()
    if (res.code === 200 && res.data) {
      deviceList.value = res.data
    } else {
      ElMessage.error(res.message || '获取设备列表失败')
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('获取设备列表失败')
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
})
</script>

<style scoped>
.devices {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sensor-data-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-top: 15px;
}

.data-item {
  cursor: default;
}

.data-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.data-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.data-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.data-value.small {
  font-size: 14px;
}

@media (max-width: 1200px) {
  .sensor-data-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .sensor-data-grid {
    grid-template-columns: 1fr;
  }
}
</style>
