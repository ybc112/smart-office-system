<template>
  <div class="config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
          <el-button type="primary" size="small" @click="saveConfig" :loading="saving">
            <el-icon><Check /></el-icon> 保存配置
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 阈值配置 -->
        <el-tab-pane label="阈值配置" name="threshold">
          <el-form :model="thresholdForm" label-width="200px" style="max-width: 600px">
            <el-divider content-position="left">光照控制</el-divider>
            <el-form-item label="光照阈值下限 (lux)">
              <el-input-number
                v-model="thresholdForm.lightLow"
                :min="0"
                :max="1000"
                :step="10"
              />
              <span class="form-tip">低于此值自动开灯</span>
            </el-form-item>
            <el-form-item label="光照阈值上限 (lux)">
              <el-input-number
                v-model="thresholdForm.lightHigh"
                :min="0"
                :max="1000"
                :step="10"
              />
              <span class="form-tip">高于此值自动关灯</span>
            </el-form-item>

            <el-divider content-position="left">温度控制</el-divider>
            <el-form-item label="温度阈值下限 (℃)">
              <el-input-number
                v-model="thresholdForm.tempLow"
                :min="-10"
                :max="50"
                :step="1"
              />
              <span class="form-tip">低于此值开启空调制热</span>
            </el-form-item>
            <el-form-item label="温度阈值上限 (℃)">
              <el-input-number
                v-model="thresholdForm.tempHigh"
                :min="-10"
                :max="50"
                :step="1"
              />
              <span class="form-tip">高于此值开启空调制冷</span>
            </el-form-item>

            <el-divider content-position="left">湿度控制</el-divider>
            <el-form-item label="湿度阈值下限 (%)">
              <el-input-number
                v-model="thresholdForm.humidityLow"
                :min="0"
                :max="100"
                :step="5"
              />
              <span class="form-tip">低于此值开启加湿器</span>
            </el-form-item>
            <el-form-item label="湿度阈值上限 (%)">
              <el-input-number
                v-model="thresholdForm.humidityHigh"
                :min="0"
                :max="100"
                :step="5"
              />
              <span class="form-tip">高于此值关闭加湿器</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 系统设置 -->
        <el-tab-pane label="系统设置" name="system">
          <el-form :model="systemForm" label-width="200px" style="max-width: 600px">
            <el-divider content-position="left">MQTT配置</el-divider>
            <el-form-item label="MQTT服务器地址">
              <el-input v-model="systemForm.mqttBrokerUrl" placeholder="tcp://localhost:1883" />
            </el-form-item>
            <el-form-item label="MQTT客户端ID">
              <el-input v-model="systemForm.mqttClientId" placeholder="smart-office-server" />
            </el-form-item>

            <el-divider content-position="left">告警通知</el-divider>
            <el-form-item label="邮件告警">
              <el-switch v-model="systemForm.emailAlarm" />
              <span class="form-tip">开启后将通过邮件发送告警通知</span>
            </el-form-item>
            <el-form-item label="短信告警">
              <el-switch v-model="systemForm.smsAlarm" />
              <span class="form-tip">开启后将通过短信发送告警通知</span>
            </el-form-item>

            <el-divider content-position="left">数据采集</el-divider>
            <el-form-item label="数据采集间隔 (秒)">
              <el-input-number
                v-model="systemForm.dataCollectInterval"
                :min="1"
                :max="3600"
                :step="1"
              />
              <span class="form-tip">设备上报数据的时间间隔</span>
            </el-form-item>
            <el-form-item label="数据保留时长 (天)">
              <el-input-number
                v-model="systemForm.dataRetentionDays"
                :min="1"
                :max="365"
                :step="1"
              />
              <span class="form-tip">历史数据保留的天数</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 配置列表 -->
        <el-tab-pane label="配置列表" name="list">
          <el-table :data="configList" border style="width: 100%">
            <el-table-column prop="configKey" label="配置键" width="250" />
            <el-table-column prop="configValue" label="配置值" width="200" />
            <el-table-column prop="configType" label="配置类型" width="150">
              <template #default="{ row }">
                <el-tag :type="row.configType === 'THRESHOLD' ? 'success' : 'primary'" size="small">
                  {{ row.configType === 'THRESHOLD' ? '阈值配置' : '系统配置' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="200" />
            <el-table-column prop="updateTime" label="更新时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.updateTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { getConfigList, updateConfig } from '@/api/config'

const activeTab = ref('threshold')
const saving = ref(false)

// 阈值配置表单
const thresholdForm = reactive({
  lightLow: 300,
  lightHigh: 350,
  tempLow: 18,
  tempHigh: 28,
  humidityLow: 40,
  humidityHigh: 70
})

// 系统配置表单
const systemForm = reactive({
  mqttBrokerUrl: 'tcp://localhost:1883',
  mqttClientId: 'smart-office-server',
  emailAlarm: false,
  smsAlarm: false,
  dataCollectInterval: 5,
  dataRetentionDays: 30
})

// 配置列表
const configList = ref([])

// 加载配置
const loadConfig = async () => {
  try {
    const res = await getConfigList()
    if (res.success && res.data) {
      configList.value = res.data

      // 解析配置到表单
      res.data.forEach(config => {
        const key = config.configKey
        const value = config.configValue

        // 阈值配置
        if (key === 'light.threshold.low') thresholdForm.lightLow = Number(value)
        if (key === 'light.threshold.high') thresholdForm.lightHigh = Number(value)
        if (key === 'temperature.threshold.low') thresholdForm.tempLow = Number(value)
        if (key === 'temperature.threshold.high') thresholdForm.tempHigh = Number(value)
        if (key === 'humidity.threshold.low') thresholdForm.humidityLow = Number(value)
        if (key === 'humidity.threshold.high') thresholdForm.humidityHigh = Number(value)

        // 系统配置
        if (key === 'mqtt.broker.url') systemForm.mqttBrokerUrl = value
        if (key === 'mqtt.client.id') systemForm.mqttClientId = value
        if (key === 'alarm.email.enable') systemForm.emailAlarm = value === 'true'
        if (key === 'alarm.sms.enable') systemForm.smsAlarm = value === 'true'
        if (key === 'data.collect.interval') systemForm.dataCollectInterval = Number(value)
        if (key === 'data.retention.days') systemForm.dataRetentionDays = Number(value)
      })
    }
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error('加载配置失败')
  }
}

// 保存配置
const saveConfig = async () => {
  saving.value = true
  try {
    // 构建配置数据
    const configs = []

    // 阈值配置
    if (activeTab.value === 'threshold' || activeTab.value === 'list') {
      configs.push(
        { configKey: 'light.threshold.low', configValue: String(thresholdForm.lightLow), configType: 'THRESHOLD' },
        { configKey: 'light.threshold.high', configValue: String(thresholdForm.lightHigh), configType: 'THRESHOLD' },
        { configKey: 'temperature.threshold.low', configValue: String(thresholdForm.tempLow), configType: 'THRESHOLD' },
        { configKey: 'temperature.threshold.high', configValue: String(thresholdForm.tempHigh), configType: 'THRESHOLD' },
        { configKey: 'humidity.threshold.low', configValue: String(thresholdForm.humidityLow), configType: 'THRESHOLD' },
        { configKey: 'humidity.threshold.high', configValue: String(thresholdForm.humidityHigh), configType: 'THRESHOLD' }
      )
    }

    // 系统配置
    if (activeTab.value === 'system' || activeTab.value === 'list') {
      configs.push(
        { configKey: 'mqtt.broker.url', configValue: systemForm.mqttBrokerUrl, configType: 'SYSTEM' },
        { configKey: 'mqtt.client.id', configValue: systemForm.mqttClientId, configType: 'SYSTEM' },
        { configKey: 'alarm.email.enable', configValue: String(systemForm.emailAlarm), configType: 'SYSTEM' },
        { configKey: 'alarm.sms.enable', configValue: String(systemForm.smsAlarm), configType: 'SYSTEM' },
        { configKey: 'data.collect.interval', configValue: String(systemForm.dataCollectInterval), configType: 'SYSTEM' },
        { configKey: 'data.retention.days', configValue: String(systemForm.dataRetentionDays), configType: 'SYSTEM' }
      )
    }

    // 批量更新配置
    const promises = configs.map(config => updateConfig(config))
    const results = await Promise.all(promises)

    const allSuccess = results.every(res => res.success)
    if (allSuccess) {
      ElMessage.success('配置保存成功')
      loadConfig() // 重新加载配置
    } else {
      ElMessage.error('部分配置保存失败')
    }
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '--'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

:deep(.el-tabs__content) {
  padding: 20px 0;
}

:deep(.el-input-number) {
  width: 200px;
}
</style>
