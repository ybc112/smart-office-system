import request from '@/utils/request'

/**
 * AI助手相关API
 */

// AI对话接口
export function sendChatMessage(message) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data: {
      message: message,
      timestamp: new Date().toISOString()
    }
  })
}

// 获取对话历史
export function getChatHistory() {
  return request({
    url: '/ai/chat/history',
    method: 'get'
  })
}

// 清空对话历史
export function clearChatHistory() {
  return request({
    url: '/ai/chat/clear',
    method: 'delete'
  })
}