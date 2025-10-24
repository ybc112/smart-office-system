<template>
  <div class="alarms">
    <el-card class="alarms-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">告警管理</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="refreshAlarms" class="refresh-btn">
              <el-icon><Refresh /></el-icon>
              <span class="btn-text">刷新</span>
            </el-button>
            <el-button type="warning" size="small" @click="clearAllAlarms" class="clear-btn">
              <el-icon><Delete /></el-icon>
              <span class="btn-text">清空</span>
            </el-button>
          </div>
        </div>
      </template>

      <!-- 移动端卡片视图 -->
      <div class="mobile-alarm-list" v-if="isMobile">
        <div 
          v-for="alarm in alarmList" 
          :key="alarm.id" 
          class="alarm-card-mobile"
          :class="getAlarmCardClass(alarm.level)"
        >
          <div class="alarm-header">
            <div class="alarm-info">
              <h4 class="alarm-title">{{ alarm.alarmType }}</h4>
              <p class="alarm-device">{{ alarm.deviceId }}</p>
            </div>
            <div class="alarm-level">
              <el-tag :type="getAlarmTagType(alarm.level)" size="small">
                {{ getAlarmLevelText(alarm.level) }}
              </el-tag>
            </div>
          </div>
          <div class="alarm-content">
            <p class="alarm-message">{{ alarm.message }}</p>
            <div class="alarm-meta">
              <div class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(alarm.alarmTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Location /></el-icon>
                <span>{{ alarm.location || '未知位置' }}</span>
              </div>
            </div>
          </div>
          <div class="alarm-actions">
            <el-button 
              size="small" 
              type="success" 
              @click="handleAlarm(alarm)"
              :disabled="alarm.status === 'HANDLED'"
            >
              {{ alarm.status === 'HANDLED' ? '已处理' : '处理' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteAlarm(alarm.id)">
              删除
            </el-button>
          </div>
        </div>
        
        <el-empty v-if="alarmList.length === 0" description="暂无告警信息" />
      </div>

      <!-- 桌面端表格视图 -->
      <el-table :data="alarmList" border style="width: 100%" v-else class="alarms-table">
        <el-table-column prop="id" label="告警ID" width="80" />
        <el-table-column prop="alarmType" label="告警类型" width="120" />
        <el-table-column prop="deviceId" label="设备编号" width="150" />
        <el-table-column prop="message" label="告警信息" min-width="200" />
        <el-table-column prop="level" label="告警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlarmTagType(row.level)" size="small">
              {{ getAlarmLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" width="120" />
        <el-table-column prop="alarmTime" label="告警时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.alarmTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'HANDLED' ? 'success' : 'warning'" size="small">
              {{ row.status === 'HANDLED' ? '已处理' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="success" 
              @click="handleAlarm(row)"
              :disabled="row.status === 'HANDLED'"
            >
              {{ row.status === 'HANDLED' ? '已处理' : '处理告警' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteAlarm(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 处理告警对话框 -->
    <el-dialog 
      v-model="handleVisible" 
      title="处理告警" 
      :width="isMobile ? '95%' : '50%'"
      class="handle-alarm-dialog"
    >
      <div v-if="selectedAlarm" class="handle-content">
        <el-alert
          title="告警详情"
          :description="`${selectedAlarm.alarmType}: ${selectedAlarm.message}`"
          :type="getAlarmAlertType(selectedAlarm.level)"
          show-icon
          :closable="false"
          class="alarm-alert"
        />

        <el-form :model="handleForm" label-width="80px" class="handle-form">
          <el-form-item label="处理方式">
            <el-radio-group v-model="handleForm.handleType">
              <el-radio label="manual">手动处理</el-radio>
              <el-radio label="auto">自动处理</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="处理备注">
            <el-input
              v-model="handleForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入处理备注..."
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmHandle" :loading="handling">
            确认处理
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Delete, Clock, Location } from '@element-plus/icons-vue'
import { getAlarmList, handleAlarm as handleAlarmApi, deleteAlarm as deleteAlarmApi, clearAllAlarms as clearAllAlarmsApi } from '@/api/alarm'

// 移动端检测
const isMobile = ref(false)

// 告警列表
const alarmList = ref([])

// 处理告警对话框
const handleVisible = ref(false)
const selectedAlarm = ref(null)
const handling = ref(false)
const handleForm = ref({
  handleType: 'manual',
  remark: ''
})

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 监听窗口大小变化
const handleResize = () => {
  checkMobile()
}

// 加载告警列表
const loadAlarms = async () => {
  try {
    const res = await getAlarmList()
    if (res && res.code === 200) {
      alarmList.value = res.data || []
    } else {
      ElMessage.error(res?.message || '获取告警列表失败')
    }
  } catch (error) {
    console.error('获取告警列表失败:', error)
    ElMessage.error('获取告警列表失败')
  }
}

// 刷新告警列表
const refreshAlarms = () => {
  loadAlarms()
  ElMessage.success('刷新成功')
}

// 清空所有告警
const clearAllAlarms = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有告警吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await clearAllAlarmsApi()
    if (res && res.code === 200) {
      ElMessage.success('清空成功')
      loadAlarms()
    } else {
      ElMessage.error(res?.message || '清空失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空告警失败:', error)
      ElMessage.error('清空失败')
    }
  }
}

// 处理告警
const handleAlarm = (alarm) => {
  selectedAlarm.value = alarm
  handleForm.value = {
    handleType: 'manual',
    remark: ''
  }
  handleVisible.value = true
}

// 确认处理告警
const confirmHandle = async () => {
  if (!handleForm.value.remark.trim()) {
    ElMessage.warning('请输入处理备注')
    return
  }

  handling.value = true
  try {
    const res = await handleAlarmApi({
      id: selectedAlarm.value.id,
      handleType: handleForm.value.handleType,
      remark: handleForm.value.remark
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
    handling.value = false
  }
}

// 删除告警
const deleteAlarm = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条告警吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteAlarmApi(id)
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      loadAlarms()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除告警失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 获取告警卡片样式类
const getAlarmCardClass = (level) => {
  const levelMap = {
    'CRITICAL': 'high-level',
    'WARNING': 'medium-level',
    'INFO': 'low-level'
  }
  return levelMap[level] || 'low-level'
}

// 获取告警标签类型
const getAlarmTagType = (level) => {
  const typeMap = {
    'CRITICAL': 'danger',
    'WARNING': 'warning',
    'INFO': 'success'
  }
  return typeMap[level] || 'info'
}

// 获取告警级别文本
const getAlarmLevelText = (level) => {
  const textMap = {
    'CRITICAL': '严重',
    'WARNING': '警告',
    'INFO': '提示'
  }
  return textMap[level] || level
}

// 获取告警提示框类型
const getAlarmAlertType = (level) => {
  const typeMap = {
    'CRITICAL': 'error',
    'WARNING': 'warning',
    'INFO': 'info'
  }
  return typeMap[level] || 'info'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '--'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadAlarms()
  checkMobile()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.alarms {
  padding: 0;
}

.alarms-card {
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
  gap: 10px;
  align-items: center;
}

.refresh-btn, .clear-btn {
  display: flex;
  align-items: center;
  gap: 5px;
}

.btn-text {
  display: inline;
}

/* 移动端告警卡片 */
.mobile-alarm-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.alarm-card-mobile {
  border-radius: 8px;
  padding: 15px;
  background: #fff;
  transition: all 0.3s ease;
  border-left: 4px solid #e4e7ed;
}

.alarm-card-mobile.high-level {
  border-left-color: #f56c6c;
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
}

.alarm-card-mobile.medium-level {
  border-left-color: #e6a23c;
  background: linear-gradient(135deg, #fdf6ec 0%, #fff 100%);
}

.alarm-card-mobile.low-level {
  border-left-color: #67c23a;
  background: linear-gradient(135deg, #f0f9ff 0%, #fff 100%);
}

.alarm-card-mobile:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.alarm-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.alarm-info {
  flex: 1;
  min-width: 0;
}

.alarm-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 5px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.alarm-device {
  font-size: 13px;
  color: #666;
  margin: 0;
  font-family: 'Courier New', monospace;
}

.alarm-level {
  flex-shrink: 0;
  margin-left: 10px;
}

.alarm-content {
  margin-bottom: 15px;
}

.alarm-message {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin: 0 0 12px 0;
  word-break: break-word;
}

.alarm-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
}

.meta-item .el-icon {
  font-size: 14px;
}

.alarm-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* 桌面端表格 */
.alarms-table {
  border-radius: 8px;
  overflow: hidden;
}

/* 对话框样式 */
.handle-alarm-dialog .el-dialog__body {
  padding: 20px;
}

.handle-content {
  padding: 10px 0;
}

.alarm-alert {
  margin-bottom: 20px;
}

.handle-form {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .refresh-btn .btn-text,
  .clear-btn .btn-text {
    display: none;
  }
  
  .alarm-meta {
    flex-direction: column;
    gap: 6px;
  }
  
  .meta-item {
    font-size: 11px;
  }
  
  .handle-alarm-dialog .el-dialog__body {
    padding: 15px;
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 10px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .alarm-card-mobile {
    padding: 12px;
  }
  
  .alarm-title {
    font-size: 15px;
  }
  
  .alarm-device {
    font-size: 12px;
  }
  
  .alarm-message {
    font-size: 13px;
  }
  
  .alarm-actions {
    gap: 8px;
  }
  
  .alarm-actions .el-button {
    padding: 5px 10px;
    font-size: 12px;
  }
  
  .meta-item {
    font-size: 10px;
  }
  
  .handle-alarm-dialog .el-dialog__body {
    padding: 10px;
  }
}

/* 横屏适配 */
@media (max-width: 768px) and (orientation: landscape) {
  .alarm-meta {
    flex-direction: row;
    justify-content: space-between;
  }
  
  .dialog-footer {
    flex-direction: row;
    justify-content: flex-end;
  }
  
  .dialog-footer .el-button {
    width: auto;
  }
}
</style>
