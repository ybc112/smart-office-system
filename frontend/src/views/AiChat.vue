<template>
  <div class="ai-chat">
    <el-card class="page-header">
      <div class="header-content">
        <h2>
          <el-icon><ChatDotRound /></el-icon>
          AI智能助手
        </h2>
        <p>与AI助手对话，获取智慧办公楼系统的帮助和建议</p>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="chat-container">
          <div class="chat-messages" ref="messagesContainer">
            <div v-if="messages.length === 0" class="welcome-message">
              <el-empty description="开始与AI助手对话吧！">
                <template #image>
                  <el-icon size="60" color="#409eff">
                    <ChatDotRound />
                  </el-icon>
                </template>
              </el-empty>
              <div class="quick-questions">
                <h4>快速提问：</h4>
                <el-button 
                  v-for="question in quickQuestions" 
                  :key="question"
                  type="primary" 
                  plain 
                  size="small"
                  @click="sendQuickQuestion(question)"
                  class="quick-question-btn"
                >
                  {{ question }}
                </el-button>
              </div>
            </div>

            <div 
              v-for="(message, index) in messages" 
              :key="index"
              class="message-item"
              :class="{ 'user-message': message.type === 'user', 'ai-message': message.type === 'ai' }"
            >
              <div class="message-avatar">
                <el-avatar 
                  :size="40" 
                  :src="message.type === 'user' ? null : null"
                  :icon="message.type === 'user' ? User : Monitor"
                  :style="{ backgroundColor: message.type === 'user' ? '#409eff' : '#67c23a' }"
                />
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="message-sender">{{ message.type === 'user' ? '您' : 'AI助手' }}</span>
                  <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                </div>
                <div class="message-text" v-html="formatMessage(message.content)"></div>
              </div>
            </div>

            <div v-if="isTyping" class="message-item ai-message typing">
              <div class="message-avatar">
                <el-avatar 
                  :size="40" 
                  :icon="Monitor"
                  :style="{ backgroundColor: '#67c23a' }"
                />
              </div>
              <div class="message-content">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>

          <div class="chat-input">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="请输入您的问题..."
              @keydown.ctrl.enter="sendMessage"
              :disabled="isTyping"
              resize="none"
            />
            <div class="input-actions">
              <el-button 
                type="primary" 
                @click="sendMessage"
                :loading="isTyping"
                :disabled="!inputMessage.trim()"
                :icon="Promotion"
              >
                {{ isTyping ? '发送中...' : '发送 (Ctrl+Enter)' }}
              </el-button>
              <el-button @click="clearChat" :icon="Delete">清空对话</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User, Monitor, Promotion, Delete } from '@element-plus/icons-vue'
import { sendChatMessage } from '@/api/ai'

const messages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)
const messagesContainer = ref(null)

// 快速提问选项
const quickQuestions = ref([
  '如何查看设备状态？',
  '告警信息在哪里查看？',
  '如何添加新设备？',
  '系统有哪些功能？'
])

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value) return

  const userMessage = {
    type: 'user',
    content: inputMessage.value.trim(),
    timestamp: new Date()
  }

  messages.value.push(userMessage)
  const question = inputMessage.value.trim()
  inputMessage.value = ''

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 显示AI正在输入
  isTyping.value = true

  try {
    // 调用真实的AI API
    const response = await sendChatMessage(question)
    
    const aiMessage = {
      type: 'ai',
      content: response.data.message || response.data,
      timestamp: new Date()
    }

    messages.value.push(aiMessage)
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
    
  } catch (error) {
    console.error('AI回复错误:', error)
    
    // 如果API调用失败，使用备用回复
    const fallbackMessage = {
      type: 'ai',
      content: '抱歉，AI服务暂时不可用。您可以尝试以下操作：<ul><li>检查网络连接</li><li>稍后重试</li><li>联系系统管理员</li></ul>',
      timestamp: new Date()
    }
    
    messages.value.push(fallbackMessage)
    await nextTick()
    scrollToBottom()
    
    ElMessage.error('AI助手暂时无法回复，请稍后重试')
  } finally {
    isTyping.value = false
  }
}

// 发送快速提问
const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 清空对话
const clearChat = () => {
  messages.value = []
  ElMessage.success('对话已清空')
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化消息内容
const formatMessage = (content) => {
  return content.replace(/\n/g, '<br>')
}

onMounted(() => {
  // 添加欢迎消息
  setTimeout(() => {
    const welcomeMessage = {
      type: 'ai',
      content: '您好！我是智慧办公楼系统的AI助手。我可以帮助您了解系统功能、解答使用问题。请问有什么可以帮助您的吗？',
      timestamp: new Date()
    }
    messages.value.push(welcomeMessage)
    nextTick(() => scrollToBottom())
  }, 500)
})
</script>

<style scoped>
.ai-chat {
  padding: 20px;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.page-header {
  margin-bottom: 20px;
  flex-shrink: 0;
}

.header-content h2 {
  margin: 0 0 10px 0;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-content p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 220px);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.welcome-message {
  text-align: center;
  padding: 40px 20px;
}

.quick-questions {
  margin-top: 30px;
}

.quick-questions h4 {
  margin-bottom: 15px;
  color: #303133;
}

.quick-question-btn {
  margin: 5px;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease-in;
}

.user-message {
  flex-direction: row-reverse;
}

.user-message .message-content {
  margin-right: 12px;
  margin-left: 0;
}

.ai-message .message-content {
  margin-left: 12px;
}

.message-content {
  max-width: 70%;
  background: white;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-message .message-content {
  background: #409eff;
  color: white;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
}

.user-message .message-header {
  color: rgba(255, 255, 255, 0.8);
}

.ai-message .message-header {
  color: #909399;
}

.message-sender {
  font-weight: 600;
}

.message-text {
  line-height: 1.6;
  word-wrap: break-word;
}

.message-text :deep(ul), .message-text :deep(ol) {
  margin: 10px 0;
  padding-left: 20px;
}

.message-text :deep(li) {
  margin: 5px 0;
}

.message-text :deep(p) {
  margin: 8px 0;
}

.message-text :deep(strong) {
  font-weight: 600;
}

.typing {
  opacity: 0.8;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
  padding: 8px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

.chat-input {
  flex-shrink: 0;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>