<template>
  <div class="device-management">
    <el-card class="management-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">设备管理</span>
          <div class="header-actions">
            <!-- 筛选器 -->
            <div class="filters">
              <el-select 
                v-model="selectedOfficeId" 
                placeholder="选择办公室" 
                clearable 
                @change="onOfficeChange"
                style="width: 150px"
              >
                <el-option 
                  v-for="office in offices" 
                  :key="office.id" 
                  :label="office.officeName" 
                  :value="office.id"
                />
              </el-select>
              
              <el-select 
                v-model="selectedWorkAreaId" 
                placeholder="选择办公区" 
                clearable 
                @change="filterDevices"
                style="width: 150px"
                :disabled="!selectedOfficeId"
              >
                <el-option 
                  v-for="workArea in workAreas" 
                  :key="workArea.id" 
                  :label="workArea.areaName" 
                  :value="workArea.id"
                />
              </el-select>
              
              <el-select 
                v-model="selectedDeviceType" 
                placeholder="设备类型" 
                clearable 
                @change="filterDevices"
                style="width: 120px"
              >
                <el-option label="空调" value="AIR_CONDITIONER" />
                <el-option label="加湿器" value="HUMIDIFIER" />
                <el-option label="灯光" value="LIGHT" />
                <el-option label="传感器" value="SENSOR" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </div>
            
            <div class="action-buttons">
              <el-button type="primary" @click="showAddDialog" :icon="Plus">
                添加设备
              </el-button>
              <el-button @click="refreshDevices" :icon="Refresh">
                刷新
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <!-- 设备列表 -->
      <el-table :data="filteredDeviceList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="deviceId" label="设备编号" width="150" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="deviceType" label="设备类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getDeviceTypeTag(row.deviceType)" size="small">
              {{ getDeviceTypeText(row.deviceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" width="150" />
        <el-table-column label="办公室" width="120">
          <template #default="{ row }">
            {{ getOfficeName(row.officeId) }}
          </template>
        </el-table-column>
        <el-table-column label="办公区" width="120">
          <template #default="{ row }">
            {{ getWorkAreaName(row.workAreaId) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="editDevice(row)">
              编辑
            </el-button>
            <el-button size="small" type="success" @click="controlDevice(row)" 
                       :disabled="row.status !== 'ONLINE'">
              控制
            </el-button>
            <el-button size="small" type="danger" @click="deleteDeviceHandler(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑设备对话框 -->
    <el-dialog 
      :title="isEdit ? '编辑设备' : '添加设备'" 
      v-model="dialogVisible" 
      width="600px"
      @close="resetForm"
    >
      <el-form :model="deviceForm" :rules="deviceRules" ref="deviceFormRef" label-width="100px">
        <el-form-item label="设备编号" prop="deviceId">
          <el-input v-model="deviceForm.deviceId" :disabled="isEdit" placeholder="请输入设备编号" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="deviceForm.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="deviceForm.deviceType" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="空调" value="AIR_CONDITIONER" />
            <el-option label="加湿器" value="HUMIDIFIER" />
            <el-option label="灯光" value="LIGHT" />
            <el-option label="传感器" value="SENSOR" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="办公室" prop="officeId">
          <el-select v-model="deviceForm.officeId" placeholder="请选择办公室" style="width: 100%" @change="onFormOfficeChange">
            <el-option 
              v-for="office in offices" 
              :key="office.id" 
              :label="office.officeName" 
              :value="office.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="办公区" prop="workAreaId">
          <el-select v-model="deviceForm.workAreaId" placeholder="请选择办公区" style="width: 100%" :disabled="!deviceForm.officeId">
            <el-option 
              v-for="workArea in formWorkAreas" 
              :key="workArea.id" 
              :label="workArea.areaName" 
              :value="workArea.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="deviceForm.location" placeholder="请输入设备位置" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="deviceForm.status" placeholder="请选择设备状态" style="width: 100%">
            <el-option label="在线" value="ONLINE" />
            <el-option label="离线" value="OFFLINE" />
            <el-option label="故障" value="FAULT" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="deviceForm.description" type="textarea" :rows="3" placeholder="请输入设备描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveDevice" :loading="saving">
            {{ isEdit ? '更新' : '添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 设备控制对话框 -->
    <el-dialog 
      title="设备控制" 
      v-model="controlDialogVisible" 
      width="500px"
    >
      <div class="control-content" v-if="selectedDevice">
        <div class="device-info">
          <h4>{{ selectedDevice.deviceName }}</h4>
          <p>设备编号: {{ selectedDevice.deviceId }}</p>
          <p>设备类型: {{ getDeviceTypeText(selectedDevice.deviceType) }}</p>
        </div>
        
        <div class="control-actions">
          <template v-if="selectedDevice.deviceType === 'AIR_CONDITIONER'">
            <h5>空调控制</h5>
            <div class="action-buttons">
              <el-button type="success" @click="sendControlCommand('ac_heat')" :loading="controlLoading">
                制热
              </el-button>
              <el-button type="primary" @click="sendControlCommand('ac_cool')" :loading="controlLoading">
                制冷
              </el-button>
              <el-button type="warning" @click="sendControlCommand('ac_off')" :loading="controlLoading">
                关闭
              </el-button>
            </div>
          </template>
          
          <template v-else-if="selectedDevice.deviceType === 'HUMIDIFIER'">
            <h5>加湿器控制</h5>
            <div class="action-buttons">
              <el-button type="success" @click="sendControlCommand('humidifier_on')" :loading="controlLoading">
                开启
              </el-button>
              <el-button type="warning" @click="sendControlCommand('humidifier_off')" :loading="controlLoading">
                关闭
              </el-button>
            </div>
          </template>
          
          <template v-else-if="selectedDevice.deviceType === 'LIGHT'">
            <h5>灯光控制</h5>
            <div class="action-buttons">
              <el-button type="success" @click="sendControlCommand('rgb_on')" :loading="controlLoading">
                开启
              </el-button>
              <el-button type="warning" @click="sendControlCommand('rgb_off')" :loading="controlLoading">
                关闭
              </el-button>
            </div>
          </template>
          
          <template v-else>
            <p>该设备类型暂不支持控制</p>
          </template>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { getDeviceList, controlDevice as controlDeviceApi, addDevice, updateDevice, deleteDevice } from '@/api/device'
import axios from 'axios'

// 设备列表
const deviceList = ref([])
const loading = ref(false)

// 办公室和办公区数据
const offices = ref([])
const workAreas = ref([])
const formWorkAreas = ref([])
const selectedOfficeId = ref('')
const selectedWorkAreaId = ref('')
const selectedDeviceType = ref('')

// 对话框状态
const dialogVisible = ref(false)
const controlDialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const controlLoading = ref(false)

// 选中的设备
const selectedDevice = ref(null)

// 设备表单
const deviceForm = reactive({
  deviceId: '',
  deviceName: '',
  deviceType: '',
  officeId: '',
  workAreaId: '',
  location: '',
  status: 'ONLINE',
  description: ''
})

// 表单引用
const deviceFormRef = ref()

// 表单验证规则
const deviceRules = {
  deviceId: [
    { required: true, message: '请输入设备编号', trigger: 'blur' }
  ],
  deviceName: [
    { required: true, message: '请输入设备名称', trigger: 'blur' }
  ],
  deviceType: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  officeId: [
    { required: true, message: '请选择办公室', trigger: 'change' }
  ],
  workAreaId: [
    { required: true, message: '请选择办公区', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入设备位置', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择设备状态', trigger: 'change' }
  ]
}

// 计算属性 - 过滤后的设备列表
const filteredDeviceList = computed(() => {
  let filtered = deviceList.value

  if (selectedOfficeId.value) {
    filtered = filtered.filter(device => device.officeId === selectedOfficeId.value)
  }

  if (selectedWorkAreaId.value) {
    filtered = filtered.filter(device => device.workAreaId === selectedWorkAreaId.value)
  }

  if (selectedDeviceType.value) {
    filtered = filtered.filter(device => device.deviceType === selectedDeviceType.value)
  }

  return filtered
})

// 加载办公室列表
const loadOffices = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/offices')
    if (response.data.code === 200) {
      offices.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载办公室列表失败:', error)
  }
}

// 加载办公区列表
const loadWorkAreas = async (officeId) => {
  try {
    const response = await axios.get(`http://localhost:8080/api/offices/${officeId}/work-areas`)
    if (response.data.code === 200) {
      return response.data.data || []
    }
  } catch (error) {
    console.error('加载办公区列表失败:', error)
    return []
  }
}

// 办公室变化处理
const onOfficeChange = async () => {
  selectedWorkAreaId.value = ''
  if (selectedOfficeId.value) {
    workAreas.value = await loadWorkAreas(selectedOfficeId.value)
  } else {
    workAreas.value = []
  }
  filterDevices()
}

// 表单中办公室变化处理
const onFormOfficeChange = async () => {
  deviceForm.workAreaId = ''
  if (deviceForm.officeId) {
    formWorkAreas.value = await loadWorkAreas(deviceForm.officeId)
  } else {
    formWorkAreas.value = []
  }
}

// 筛选设备
const filterDevices = () => {
  // 计算属性会自动更新
}

// 获取办公室名称
const getOfficeName = (officeId) => {
  const office = offices.value.find(o => o.id === officeId)
  return office ? office.officeName : '-'
}

// 获取办公区名称
const getWorkAreaName = (workAreaId) => {
  const allWorkAreas = [...workAreas.value, ...formWorkAreas.value]
  const workArea = allWorkAreas.find(w => w.id === workAreaId)
  return workArea ? workArea.areaName : '-'
}

// 加载设备列表
const loadDevices = async () => {
  loading.value = true
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
  } finally {
    loading.value = false
  }
}

// 刷新设备列表
const refreshDevices = () => {
  loadDevices()
  ElMessage.success('刷新成功')
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑设备
const editDevice = async (device) => {
  isEdit.value = true
  Object.assign(deviceForm, {
    id: device.id,
    deviceId: device.deviceId,
    deviceName: device.deviceName,
    deviceType: device.deviceType,
    officeId: device.officeId,
    workAreaId: device.workAreaId,
    location: device.location,
    status: device.status,
    description: device.description
  })
  
  // 如果有办公室ID，加载对应的办公区
  if (device.officeId) {
    formWorkAreas.value = await loadWorkAreas(device.officeId)
  }
  
  dialogVisible.value = true
}

// 保存设备
const saveDevice = async () => {
  const valid = await deviceFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value) {
      console.log(`[前端] 更新设备: ${deviceForm.deviceName}`)
      const res = await updateDevice(deviceForm)
      if (res.code === 200) {
        ElMessage.success('设备更新成功')
        console.log(`[前端] 设备更新成功: ${deviceForm.deviceName}`)
      } else {
        ElMessage.error(res.message || '设备更新失败')
      }
    } else {
      console.log(`[前端] 添加设备: ${deviceForm.deviceName}`)
      const res = await addDevice(deviceForm)
      if (res.code === 200) {
        ElMessage.success('设备添加成功')
        console.log(`[前端] 设备添加成功: ${deviceForm.deviceName}`)
      } else {
        ElMessage.error(res.message || '设备添加失败')
      }
    }
    
    dialogVisible.value = false
    loadDevices()
  } catch (error) {
    console.error('保存设备失败:', error)
    ElMessage.error('保存设备失败')
  } finally {
    saving.value = false
  }
}

// 删除设备
const deleteDeviceHandler = async (device) => {
  try {
    await ElMessageBox.confirm(`确定要删除设备 "${device.deviceName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    console.log(`[前端] 删除设备: ${device.deviceName}`)
    const res = await deleteDevice(device.deviceId)
    
    if (res.code === 200) {
      ElMessage.success('设备删除成功')
      console.log(`[前端] 设备删除成功: ${device.deviceName}`)
      loadDevices()
    } else {
      ElMessage.error(res.message || '设备删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除设备失败:', error)
      ElMessage.error('删除设备失败')
    }
  }
}

// 控制设备
const controlDevice = (device) => {
  selectedDevice.value = device
  controlDialogVisible.value = true
}

// 发送控制命令
const sendControlCommand = async (action) => {
  controlLoading.value = true
  try {
    const res = await controlDeviceApi({
      deviceId: selectedDevice.value.deviceId,
      action: action
    })

    if (res.code === 200) {
      ElMessage.success(`设备控制成功: ${getActionText(action)}`)
      console.log(`[前端] 设备控制成功: ${selectedDevice.value.deviceName} - ${getActionText(action)}`)
    } else {
      ElMessage.error(res.message || '控制失败')
    }
  } catch (error) {
    console.error('设备控制失败:', error)
    ElMessage.error('控制失败')
  } finally {
    controlLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(deviceForm, {
    deviceId: '',
    deviceName: '',
    deviceType: '',
    officeId: '',
    workAreaId: '',
    location: '',
    status: 'ONLINE',
    description: ''
  })
  formWorkAreas.value = []
  if (deviceFormRef.value) {
    deviceFormRef.value.clearValidate()
  }
}

// 获取设备类型标签
const getDeviceTypeTag = (type) => {
  const tagMap = {
    'AIR_CONDITIONER': 'primary',
    'HUMIDIFIER': 'success',
    'LIGHT': 'warning',
    'SENSOR': 'info',
    'OTHER': ''
  }
  return tagMap[type] || ''
}

// 获取设备类型文本
const getDeviceTypeText = (type) => {
  const textMap = {
    'AIR_CONDITIONER': '空调',
    'HUMIDIFIER': '加湿器',
    'LIGHT': '灯光',
    'SENSOR': '传感器',
    'OTHER': '其他'
  }
  return textMap[type] || type
}

// 获取状态标签
const getStatusTag = (status) => {
  const tagMap = {
    'ONLINE': 'success',
    'OFFLINE': 'danger',
    'FAULT': 'warning'
  }
  return tagMap[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'ONLINE': '在线',
    'OFFLINE': '离线',
    'FAULT': '故障'
  }
  return textMap[status] || status
}

// 获取操作文本
const getActionText = (action) => {
  const textMap = {
    'ac_heat': '空调制热',
    'ac_cool': '空调制冷',
    'ac_off': '空调关闭',
    'humidifier_on': '加湿器开启',
    'humidifier_off': '加湿器关闭',
    'rgb_on': '灯光开启',
    'rgb_off': '灯光关闭'
  }
  return textMap[action] || action
}

onMounted(() => {
  loadDevices()
  loadOffices()
})
</script>

<style scoped>
.device-management {
  padding: 20px;
}

.management-card {
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.control-content {
  padding: 10px 0;
}

.device-info {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 6px;
}

.device-info h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.device-info p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.control-actions h5 {
  margin: 0 0 15px 0;
  color: #333;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  flex: 1;
  min-width: 80px;
}

@media (max-width: 768px) {
  .device-management {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .filters {
    justify-content: center;
  }
  
  .filters .el-select {
    width: 120px !important;
  }
  
  .action-buttons {
    justify-content: center;
  }
}
</style>