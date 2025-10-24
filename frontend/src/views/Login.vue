<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <el-icon :size="40" color="#409EFF"><OfficeBuilding /></el-icon>
          </div>
          <h2 class="title">智慧办公楼系统</h2>
          <p class="subtitle">Smart Office Building System</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="login-btn"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <div class="demo-accounts">
            <h4>测试账号</h4>
            <div class="account-list">
              <div class="account-item" @click="fillAccount('admin', 'admin123')">
                <el-tag type="danger" size="small">管理员</el-tag>
                <span>admin / admin123</span>
              </div>
              <div class="account-item" @click="fillAccount('user', 'user123')">
                <el-tag type="info" size="small">普通用户</el-tag>
                <span>user / user123</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, OfficeBuilding } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    const result = await userStore.login(loginForm.username, loginForm.password)
    if (result.success) {
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

const fillAccount = (username, password) => {
  loginForm.username = username
  loginForm.password = password
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="75" cy="75" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="50" cy="10" r="0.5" fill="rgba(255,255,255,0.05)"/><circle cx="10" cy="60" r="0.5" fill="rgba(255,255,255,0.05)"/><circle cx="90" cy="40" r="0.5" fill="rgba(255,255,255,0.05)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
}

.login-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  margin-bottom: 20px;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 10px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
  font-weight: 300;
  letter-spacing: 1px;
}

.login-form {
  margin-bottom: 30px;
}

.login-form .el-form-item {
  margin-bottom: 25px;
}

.login-form .el-input {
  border-radius: 12px;
}

.login-form .el-input__wrapper {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.login-form .el-input__wrapper:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.login-form .el-input.is-focus .el-input__wrapper {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.login-btn {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.login-footer {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.demo-accounts h4 {
  font-size: 14px;
  color: #666;
  margin: 0 0 15px 0;
  text-align: center;
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.account-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 15px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
}

.account-item:hover {
  background: #e9ecef;
  transform: translateX(5px);
}

.account-item span {
  color: #666;
  font-family: 'Courier New', monospace;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .login-container {
    padding: 15px;
  }
  
  .login-wrapper {
    max-width: 100%;
  }
  
  .login-card {
    padding: 30px 25px;
    border-radius: 16px;
  }
  
  .title {
    font-size: 24px;
  }
  
  .subtitle {
    font-size: 13px;
  }
  
  .login-header {
    margin-bottom: 30px;
  }
  
  .login-form {
    margin-bottom: 25px;
  }
  
  .login-form .el-form-item {
    margin-bottom: 20px;
  }
  
  .login-btn {
    height: 45px;
    font-size: 15px;
  }
  
  .account-list {
    gap: 8px;
  }
  
  .account-item {
    padding: 8px 12px;
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 10px;
  }
  
  .login-card {
    padding: 25px 20px;
    border-radius: 12px;
  }
  
  .title {
    font-size: 22px;
  }
  
  .subtitle {
    font-size: 12px;
  }
  
  .login-header {
    margin-bottom: 25px;
  }
  
  .logo .el-icon {
    font-size: 35px !important;
  }
  
  .login-btn {
    height: 42px;
    font-size: 14px;
  }
  
  .demo-accounts h4 {
    font-size: 13px;
  }
  
  .account-item {
    padding: 6px 10px;
    font-size: 11px;
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .account-item span {
    font-size: 10px;
  }
}

/* 横屏适配 */
@media (max-width: 768px) and (orientation: landscape) {
  .login-container {
    padding: 10px;
  }
  
  .login-card {
    padding: 20px;
    max-height: 90vh;
    overflow-y: auto;
  }
  
  .login-header {
    margin-bottom: 20px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .login-form {
    margin-bottom: 15px;
  }
  
  .login-form .el-form-item {
    margin-bottom: 15px;
  }
  
  .demo-accounts {
    margin-top: 10px;
  }
}

/* 超小屏幕适配 */
@media (max-width: 320px) {
  .login-card {
    padding: 20px 15px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .login-btn {
    height: 40px;
    font-size: 13px;
  }
}
</style>
