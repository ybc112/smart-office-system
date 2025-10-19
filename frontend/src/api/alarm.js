import request from '@/utils/request'

// 获取告警列表
export function getAlarmList(params) {
  return request({
    url: '/api/alarm/list',
    method: 'get',
    params
  })
}

// 获取告警统计
export function getAlarmStatistics() {
  return request({
    url: '/api/alarm/statistics',
    method: 'get'
  })
}

// 更新告警状态
export function updateAlarmStatus(data) {
  return request({
    url: `/api/alarm/${data.id}/status`,
    method: 'put',
    data
  })
}

// 处理告警（旧方法，保留兼容性）
export function handleAlarm(id, status, handleRemark) {
  return request({
    url: `/api/alarm/${id}/handle`,
    method: 'put',
    data: { status, handleRemark }
  })
}

// 获取最新告警
export function getLatestAlarms(limit = 10) {
  return request({
    url: '/api/alarm/latest',
    method: 'get',
    params: { limit }
  })
}
