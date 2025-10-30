import request from '@/utils/request'

// ==================== 消防水带管理 ====================

/**
 * 获取所有消防水带
 */
export function getFireHoses() {
  return request({
    url: '/api/fire-safety/fire-hoses',
    method: 'get'
  })
}

/**
 * 根据ID获取消防水带
 */
export function getFireHoseById(id) {
  return request({
    url: `/api/fire-safety/fire-hoses/${id}`,
    method: 'get'
  })
}

/**
 * 添加消防水带
 */
export function addFireHose(data) {
  return request({
    url: '/api/fire-safety/fire-hoses',
    method: 'post',
    data
  })
}

/**
 * 更新消防水带
 */
export function updateFireHose(data) {
  return request({
    url: '/api/fire-safety/fire-hoses',
    method: 'put',
    data
  })
}

/**
 * 删除消防水带
 */
export function deleteFireHose(id) {
  return request({
    url: `/api/fire-safety/fire-hoses/${id}`,
    method: 'delete'
  })
}

// ==================== 灭火器管理 ====================

/**
 * 获取所有灭火器
 */
export function getFireExtinguishers() {
  return request({
    url: '/api/fire-safety/fire-extinguishers',
    method: 'get'
  })
}

/**
 * 根据ID获取灭火器
 */
export function getFireExtinguisherById(id) {
  return request({
    url: `/api/fire-safety/fire-extinguishers/${id}`,
    method: 'get'
  })
}

/**
 * 添加灭火器
 */
export function addFireExtinguisher(data) {
  return request({
    url: '/api/fire-safety/fire-extinguishers',
    method: 'post',
    data
  })
}

/**
 * 更新灭火器
 */
export function updateFireExtinguisher(data) {
  return request({
    url: '/api/fire-safety/fire-extinguishers',
    method: 'put',
    data
  })
}

/**
 * 删除灭火器
 */
export function deleteFireExtinguisher(id) {
  return request({
    url: `/api/fire-safety/fire-extinguishers/${id}`,
    method: 'delete'
  })
}