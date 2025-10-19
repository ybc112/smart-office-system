<template>
  <div class="alarms">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>告警管理</span>
          <div>
            <el-button type="primary" size="small" @click="refreshAlarms">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="告警类型">
          <el-select v-model="queryForm.alarmType" placeholder="全部" clearable style="width: 150px">
            <el-option label="火灾告警" value="FIRE" />
            <el-option label="温度异常" value="TEMP" />
            <el-option label="湿度异常" value="HUMIDITY" />
            <el-option label="光照异常" value="LIGHT" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警级别">
          <el-select v-model="queryForm.alarmLevel" placeholder="全部" clearable style="width: 150px">
            <el-option label="严重" value="CRITICAL" />
            <el-option label="警告" value="WARNING" />
            <el-option label="提示" value="INFO" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 150px">
            <el-option label="未处理" value="UNHANDLED" />
            <el-option label="处理中" value="HANDLING" />
            <el-option label="已处理" value="HANDLED" />
            <el-option label="已忽略" value="IGNORED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 告警列表 -->
      <el-table
        :data="alarmList"
        border
        style="width: 100%"
        :row-class-name="getRowClassName"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="deviceId" label="设备编号" width="150" />
        <el-table-column prop="alarmType" label="告警类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getAlarmTypeColor(row.alarmType)" size="small">
              {{ getAlarmTypeText(row.alarmType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmLevel" label="告警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlarmLevelType(row.alarmLevel)" size="small">
              {{ getAlarmLevelText(row.alarmLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmMessage" label="告警消息" min-width="200" />
        <el-table-column prop="alarmTime" label="告警时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.alarmTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'UNHANDLED'"
              size="small"
              type="primary"
              @click="handleAlarm(row)"
            >
              处理
            </el-button>
            <el-button
              v-if="row.status === 'UNHANDLED'"
              size="small"
              type="warning"
              @click="ignoreAlarm(row)"
            >
              忽略
            </el-button>
            <el-button size="small" @click="viewAlarm(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 告警详情对话框 -->
    <el-dialog v-model="detailVisible" title="告警详情" width="50%">
      <div v-if="selectedAlarm">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="告警ID">
            {{ selectedAlarm.id }}
          </el-descriptions-item>
          <el-descriptions-item label="设备编号">
            {{ selectedAlarm.deviceId }}
          </el-descriptions-item>
          <el-descriptions-item label="告警类型">
            <el-tag :type="getAlarmTypeColor(selectedAlarm.alarmType)">
              {{ getAlarmTypeText(selectedAlarm.alarmType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警级别">
            <el-tag :type="getAlarmLevelType(selectedAlarm.alarmLevel)">
              {{ getAlarmLevelText(selectedAlarm.alarmLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警消息" :span="2">
            {{ selectedAlarm.alarmMessage }}
          </el-descriptions-item>
          <el-descriptions-item label="告警值">
            {{ selectedAlarm.alarmValue || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="阈值">
            {{ selectedAlarm.thresholdValue || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="告警时间" :span="2">
            {{ formatTime(selectedAlarm.alarmTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusType(selectedAlarm.status)">
              {{ getStatusText(selectedAlarm.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理时间">
            {{ formatTime(selectedAlarm.handleTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理备注" :span="2">
            {{ selectedAlarm.handleRemark || '--' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 处理告警对话框 -->
    <el-dialog v-model="handleVisible" title="处理告警" width="40%">
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="告警ID">
          <el-input v-model="handleForm.id" disabled />
        </el-form-item>
        <el-form-item label="处理备注" required>
          <el-input
            v-model="handleForm.handleRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="submitting">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getAlarmList, updateAlarmStatus } from '@/api/alarm'

// 查询表单
const queryForm = reactive({
  alarmType: '',
  alarmLevel: '',
  status: ''
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

// 告警列表
const alarmList = ref([])

// 详情对话框
const detailVisible = ref(false)
const selectedAlarm = ref(null)

// 处理对话框
const handleVisible = ref(false)
const handleForm = reactive({
  id: null,
  handleRemark: ''
})
const submitting = ref(false)

// 加载告警列表
const loadAlarms = async () => {
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...queryForm
    }

    const res = await getAlarmList(params)
    if (res && res.code === 200) {
      alarmList.value = res.data.records || []
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res?.message || '获取告警列表失败')
    }
  } catch (error) {
    console.error('获取告警列表失败:', error)
    ElMessage.error('获取告警列表失败')
  }
}

// 刷新
const refreshAlarms = () => {
  loadAlarms()
  ElMessage.success('刷新成功')
}

// 查询
const handleQuery = () => {
  pagination.pageNum = 1
  loadAlarms()
}

// 重置
const handleReset = () => {
  queryForm.alarmType = ''
  queryForm.alarmLevel = ''
  queryForm.status = ''
  handleQuery()
}

// 分页事件
const handleSizeChange = (val) => {
  pagination.pageSize = val
  loadAlarms()
}

const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadAlarms()
}

// 查看详情
const viewAlarm = (alarm) => {
  selectedAlarm.value = alarm
  detailVisible.value = true
}

// 处理告警
const handleAlarm = (alarm) => {
  handleForm.id = alarm.id
  handleForm.handleRemark = ''
  handleVisible.value = true
}

// 提交处理
const submitHandle = async () => {
  if (!handleForm.handleRemark) {
    ElMessage.warning('请输入处理备注')
    return
  }

  submitting.value = true
  try {
    const res = await updateAlarmStatus({
      id: handleForm.id,
      status: 'HANDLED',
      handleRemark: handleForm.handleRemark
    })

    if (res && res.code === 200) {
      ElMessage.success('处理成功')
      handleVisible.value = false
      loadAlarms()
    } else {
      ElMessage.error(res?.message || '处理失败')
    }
  } catch (error) {
    console.error('处理告警失败:', error)
    ElMessage.error('处理失败')
  } finally {
    submitting.value = false
  }
}

// 忽略告警
const ignoreAlarm = (alarm) => {
  ElMessageBox.confirm('确定要忽略此告警吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await updateAlarmStatus({
        id: alarm.id,
        status: 'IGNORED',
        handleRemark: '已忽略'
      })

      if (res && res.code === 200) {
        ElMessage.success('已忽略')
        loadAlarms()
      } else {
        ElMessage.error(res?.message || '操作失败')
      }
    } catch (error) {
      console.error('忽略告警失败:', error)
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '--'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 告警类型
const getAlarmTypeText = (type) => {
  const map = {
    'FIRE': '火灾',
    'TEMP': '温度异常',
    'HUMIDITY': '湿度异常',
    'LIGHT': '光照异常'
  }
  return map[type] || type
}

const getAlarmTypeColor = (type) => {
  const map = {
    'FIRE': 'danger',
    'TEMP': 'warning',
    'HUMIDITY': 'warning',
    'LIGHT': 'info'
  }
  return map[type] || 'info'
}

// 告警级别
const getAlarmLevelText = (level) => {
  const map = {
    'CRITICAL': '严重',
    'WARNING': '警告',
    'INFO': '提示'
  }
  return map[level] || level
}

const getAlarmLevelType = (level) => {
  const map = {
    'CRITICAL': 'danger',
    'WARNING': 'warning',
    'INFO': 'info'
  }
  return map[level] || 'info'
}

// 状态
const getStatusText = (status) => {
  const map = {
    'UNHANDLED': '未处理',
    'HANDLING': '处理中',
    'HANDLED': '已处理',
    'IGNORED': '已忽略'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    'UNHANDLED': 'danger',
    'HANDLING': 'warning',
    'HANDLED': 'success',
    'IGNORED': 'info'
  }
  return map[status] || 'info'
}

// 行样式
const getRowClassName = ({ row }) => {
  if (row.status === 'UNHANDLED' && row.alarmLevel === 'CRITICAL') {
    return 'critical-row'
  }
  return ''
}

onMounted(() => {
  loadAlarms()
})
</script>

<style scoped>
.alarms {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
}

:deep(.critical-row) {
  background-color: #fef0f0 !important;
}

:deep(.el-table .critical-row:hover > td) {
  background-color: #fde2e2 !important;
}
</style>
