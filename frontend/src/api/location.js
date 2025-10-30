import request from '@/utils/request'

// 获取办公室位置选项
export function getLocationOptions() {
  return request({
    url: '/api/locations/options',
    method: 'get'
  })
}

// 获取办公室和办公区信息
export function getOfficeLocations() {
  return request({
    url: '/api/locations/offices',
    method: 'get'
  })
}