<template>
  <div class="users">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="showAddDialog">
              <el-icon><Plus /></el-icon>
              <span class="button-text">添加用户</span>
            </el-button>
            <el-button type="primary" size="small" @click="refreshUsers">
              <el-icon><Refresh /></el-icon>
              <span class="button-text">刷新</span>
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryForm.role" placeholder="全部" clearable>
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable>
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item class="query-actions">
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 用户列表 - 桌面端表格 -->
      <el-table 
        v-if="!isMobile" 
        :data="userList" 
        border 
        style="width: 100%"
        class="desktop-table"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="editUser(row)">
              编辑
            </el-button>
            <el-button
              size="small"
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="toggleUserStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteUser(row)"
              :disabled="row.username === 'admin'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 用户列表 - 移动端卡片 -->
      <div v-else class="mobile-user-list">
        <div v-for="user in userList" :key="user.id" class="user-card">
          <div class="user-header">
            <div class="user-info">
              <h4>{{ user.realName || user.username }}</h4>
              <p class="username">@{{ user.username }}</p>
            </div>
            <div class="user-status">
              <el-tag :type="user.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
                {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-tag>
              <el-tag :type="user.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
                {{ user.status === 'ACTIVE' ? '启用' : '禁用' }}
              </el-tag>
            </div>
          </div>
          
          <div class="user-details">
            <div class="detail-item">
              <span class="label">邮箱:</span>
              <span class="value">{{ user.email || '--' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">手机:</span>
              <span class="value">{{ user.phone || '--' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">创建时间:</span>
              <span class="value">{{ formatTime(user.createTime) }}</span>
            </div>
          </div>
          
          <div class="user-actions">
            <el-button size="small" type="primary" @click="editUser(user)">
              编辑
            </el-button>
            <el-button
              size="small"
              :type="user.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="toggleUserStatus(user)"
            >
              {{ user.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteUser(user)"
              :disabled="user.username === 'admin'"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          :small="isMobile"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      :width="isMobile ? '95%' : '500px'"
      :fullscreen="isMobile"
      @close="resetForm"
    >
      <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="修改密码">
          <el-checkbox v-model="changePassword" @change="handleChangePasswordToggle">
            修改密码
          </el-checkbox>
        </el-form-item>
        <el-form-item v-if="isEdit && changePassword" label="新密码" prop="newPassword">
          <el-input v-model="userForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item v-if="isEdit && changePassword" label="确认密码" prop="confirmPassword">
          <el-input v-model="userForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio label="ACTIVE">启用</el-radio>
            <el-radio label="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser" :loading="saving">
          {{ saving ? '保存中...' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser as deleteUserApi, toggleUserStatus as toggleUserStatusApi } from '@/api/user'

// 移动端检测
const isMobile = ref(false)

// 检测是否为移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 窗口大小改变处理
const handleResize = () => {
  checkMobile()
}

// 用户列表
const userList = ref([])

// 查询表单
const queryForm = reactive({
  username: '',
  role: '',
  status: ''
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const userFormRef = ref(null)
const changePassword = ref(false)

// 用户表单
const userForm = reactive({
  id: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  role: 'USER',
  password: '',
  newPassword: '',
  confirmPassword: '',
  status: 'ACTIVE'
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== userForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 加载用户列表
const loadUsers = async () => {
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      ...queryForm
    }
    const res = await getUserList(params)
    if (res && res.code === 200 && res.data) {
      // 确保userList始终是数组 - 多层防御
      let userData = res.data.records || res.data.list || res.data || []
      if (!Array.isArray(userData)) {
        userData = []
      }
      // 转换后端数字状态为前端字符串状态
      userList.value = userData.map(user => ({
        ...user,
        status: user.status === 1 ? 'ACTIVE' : 'DISABLED'  // 转换状态：1 -> ACTIVE, 0 -> DISABLED
      }))
      pagination.total = (res.data && res.data.total) || 0
    } else {
      userList.value = []
      ElMessage.error((res && res.message) || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    userList.value = []
    ElMessage.error('获取用户列表失败: ' + (error.message || error))
  }
}

// 刷新用户列表
const refreshUsers = () => {
  loadUsers()
  ElMessage.success('刷新成功')
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  loadUsers()
}

// 重置查询条件
const handleReset = () => {
  Object.assign(queryForm, {
    username: '',
    role: '',
    status: ''
  })
  pagination.current = 1
  loadUsers()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadUsers()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadUsers()
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑用户
const editUser = (user) => {
  isEdit.value = true
  changePassword.value = false
  Object.assign(userForm, {
    id: user.id,
    username: user.username,
    realName: user.realName,
    email: user.email,
    phone: user.phone,
    role: user.role,
    status: user.status,  // 这里已经是字符串状态了（ACTIVE/DISABLED）
    password: '',
    newPassword: '',
    confirmPassword: ''
  })
  dialogVisible.value = true
}

// 处理密码修改切换
const handleChangePasswordToggle = (value) => {
  if (!value) {
    userForm.newPassword = ''
    userForm.confirmPassword = ''
    // 清除密码字段的验证错误
    userFormRef.value?.clearValidate(['newPassword', 'confirmPassword'])
  }
}

// 保存用户
const saveUser = async () => {
  const valid = await userFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    // 准备提交的数据，将前端的字符串状态转换为后端需要的数字状态
    const submitData = {
      ...userForm,
      status: userForm.status === 'ACTIVE' ? 1 : 0  // 转换状态：ACTIVE -> 1, DISABLED -> 0
    }
    
    // 如果是编辑模式且选择了修改密码，则使用新密码
    if (isEdit.value && changePassword.value) {
      submitData.password = userForm.newPassword
    }
    
    // 清理不需要的字段
    delete submitData.newPassword
    delete submitData.confirmPassword
    
    let res
    if (isEdit.value) {
      res = await updateUser(submitData.id, submitData)
    } else {
      res = await addUser(submitData)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadUsers()
    } else {
      ElMessage.error(res.message || (isEdit.value ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    console.error('保存用户失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  } finally {
    saving.value = false
  }
}

// 切换用户状态
const toggleUserStatus = async (user) => {
  const action = user.status === 'ACTIVE' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 "${user.username}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await toggleUserStatusApi(user.id)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      loadUsers()
    } else {
      ElMessage.error(res.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换用户状态失败:', error)
      ElMessage.error(`${action}失败`)
    }
  }
}

// 删除用户
const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${user.username}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteUserApi(user.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  changePassword.value = false
  Object.assign(userForm, {
    id: null,
    username: '',
    realName: '',
    email: '',
    phone: '',
    role: 'USER',
    password: '',
    newPassword: '',
    confirmPassword: '',
    status: 'ACTIVE'
  })
  userFormRef.value?.resetFields()
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '--'
  return new Date(time).toLocaleString('zh-CN')
}

// 初始化
onMounted(() => {
  loadUsers()
  checkMobile()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.users {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.query-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

/* 移动端用户卡片样式 */
.mobile-user-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.user-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.user-info .username {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.user-status {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-end;
}

.user-details {
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.detail-item .label {
  color: #606266;
  font-weight: 500;
}

.detail-item .value {
  color: #303133;
  text-align: right;
  flex: 1;
  margin-left: 16px;
}

.user-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.user-actions .el-button {
  flex: 1;
  min-width: 60px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .users {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .header-actions .button-text {
    display: none;
  }
  
  .query-form {
    padding: 16px;
  }
  
  .query-form .el-form-item {
    margin-bottom: 16px;
    width: 100%;
  }
  
  .query-form .el-input,
  .query-form .el-select {
    width: 100% !important;
  }
  
  .query-actions {
    text-align: center;
  }
  
  .query-actions .el-button {
    width: 100px;
  }
  
  .pagination-container {
    text-align: center;
    margin-top: 16px;
  }
}

@media (max-width: 480px) {
  .users {
    padding: 8px;
  }
  
  .user-card {
    padding: 12px;
  }
  
  .user-header {
    flex-direction: column;
    gap: 8px;
  }
  
  .user-status {
    flex-direction: row;
    align-items: center;
    justify-content: flex-start;
  }
  
  .user-actions .el-button {
    font-size: 12px;
    padding: 4px 8px;
  }
  
  .detail-item {
    flex-direction: column;
    gap: 4px;
  }
  
  .detail-item .value {
    text-align: left;
    margin-left: 0;
  }
}

@media (orientation: landscape) and (max-height: 500px) {
  .user-card {
    padding: 8px;
  }
  
  .user-header {
    margin-bottom: 8px;
  }
  
  .user-details {
    margin-bottom: 8px;
  }
  
  .detail-item {
    margin-bottom: 4px;
  }
}
</style>