import request from '@/utils/request'

// 获取配置列表
export function getConfigList(params) {
  return request({
    url: '/api/config/list',
    method: 'get',
    params
  })
}

// 获取阈值配置
export function getThresholds() {
  return request({
    url: '/api/config/thresholds',
    method: 'get'
  })
}

// 更新配置
export function updateConfig(data) {
  return request({
    url: '/api/config/update',
    method: 'put',
    data
  })
}

// 更新阈值配置（旧方法，保留兼容性）
export function updateThreshold(configKey, configValue) {
  return request({
    url: '/api/config/threshold',
    method: 'put',
    data: { configKey, configValue }
  })
}

// 获取所有系统配置
export function getAllConfigs() {
  return request({
    url: '/api/config/all',
    method: 'get'
  })
}
