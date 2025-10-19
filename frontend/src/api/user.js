import request from '@/utils/request'

// 用户登录
export function login(username, password) {
  return request({
    url: '/user-api/user/login',
    method: 'post',
    data: { username, password }
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user-api/user/info',
    method: 'get'
  })
}

// 用户登出
export function logout() {
  return request({
    url: '/user-api/user/logout',
    method: 'post'
  })
}

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/user-api/user/list',
    method: 'get',
    params
  })
}

// 添加用户
export function addUser(data) {
  return request({
    url: '/user-api/user/add',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(id, data) {
  return request({
    url: `/user-api/user/update/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/user-api/user/delete/${id}`,
    method: 'delete'
  })
}

// 切换用户状态
export function toggleUserStatus(id) {
  return request({
    url: `/user-api/user/toggle-status/${id}`,
    method: 'put'
  })
}
