import request from '@/utils/request'

// 获取设备列表
export function getDeviceList() {
  return request({
    url: '/api/device/list',
    method: 'get'
  })
}

// 获取设备详情
export function getDeviceInfo(deviceId) {
  return request({
    url: `/api/device/${deviceId}`,
    method: 'get'
  })
}

// 获取最新传感器数据
export function getLatestSensorData(deviceId) {
  return request({
    url: `/api/device/${deviceId}/latest`,
    method: 'get'
  })
}

// 获取传感器历史数据
export function getSensorDataHistory(deviceId, pageNum = 1, pageSize = 20) {
  return request({
    url: `/api/device/${deviceId}/history`,
    method: 'get',
    params: { pageNum, pageSize }
  })
}

// 控制设备
export function controlDevice(data) {
  return request({
    url: '/api/device/control',
    method: 'post',
    data
  })
}
