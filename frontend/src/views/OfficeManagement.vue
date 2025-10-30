<template>
  <div class="office-management">
    <div class="header">
      <h2>办公室管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showAddOfficeDialog">
          <el-icon><Plus /></el-icon>
          添加办公室
        </el-button>
      </div>
    </div>

    <!-- 办公室列表 -->
    <div class="office-list">
      <el-card v-for="office in offices" :key="office.id" class="office-card">
        <template #header>
          <div class="office-header">
            <div class="office-info">
              <h3>{{ office.officeName }}</h3>
              <span class="office-code">{{ office.officeCode }}</span>
              <el-tag :type="office.status === 'ACTIVE' ? 'success' : 'danger'">
                {{ office.status === 'ACTIVE' ? '启用' : '禁用' }}
              </el-tag>
            </div>
            <div class="office-actions">
              <el-button size="small" @click="showAddWorkAreaDialog(office.id)">
                <el-icon><Plus /></el-icon>
                添加办公区
              </el-button>
              <el-button size="small" type="primary" @click="editOffice(office)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="deleteOffice(office.id)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="office-content">
          <div class="office-details">
            <p><strong>楼层：</strong>{{ office.floor }}层</p>
            <p><strong>描述：</strong>{{ office.description || '暂无描述' }}</p>
          </div>
          
          <!-- 办公区列表 -->
          <div class="work-areas">
            <h4>办公区列表</h4>
            <div v-if="office.workAreas && office.workAreas.length > 0" class="work-area-grid">
              <div v-for="workArea in office.workAreas" :key="workArea.id" class="work-area-item">
                <div class="work-area-info">
                  <span class="work-area-name">{{ workArea.areaName }}</span>
                  <span class="work-area-code">{{ workArea.areaCode }}</span>
                  <el-tag size="small" :type="workArea.status === 'ACTIVE' ? 'success' : 'danger'">
                    {{ workArea.status === 'ACTIVE' ? '启用' : '禁用' }}
                  </el-tag>
                </div>
                <div class="work-area-actions">
                  <el-button size="small" type="primary" @click="editWorkArea(workArea)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button size="small" type="danger" @click="deleteWorkArea(workArea.id)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
            <div v-else class="no-work-areas">
              <p>暂无办公区</p>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 添加/编辑办公室对话框 -->
    <el-dialog
      v-model="officeDialogVisible"
      :title="isEditingOffice ? '编辑办公室' : '添加办公室'"
      width="500px"
    >
      <el-form :model="officeForm" :rules="officeRules" ref="officeFormRef" label-width="80px">
        <el-form-item label="办公室编号" prop="officeCode">
          <el-input v-model="officeForm.officeCode" placeholder="请输入办公室编号" />
        </el-form-item>
        <el-form-item label="办公室名称" prop="officeName">
          <el-input v-model="officeForm.officeName" placeholder="请输入办公室名称" />
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input-number v-model="officeForm.floor" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="officeForm.status" placeholder="请选择状态">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="officeForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入办公室描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="officeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveOffice">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加/编辑办公区对话框 -->
    <el-dialog
      v-model="workAreaDialogVisible"
      :title="isEditingWorkArea ? '编辑办公区' : '添加办公区'"
      width="500px"
    >
      <el-form :model="workAreaForm" :rules="workAreaRules" ref="workAreaFormRef" label-width="80px">
        <el-form-item label="办公区编号" prop="areaCode">
          <el-input v-model="workAreaForm.areaCode" placeholder="请输入办公区编号" />
        </el-form-item>
        <el-form-item label="办公区名称" prop="areaName">
          <el-input v-model="workAreaForm.areaName" placeholder="请输入办公区名称" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="workAreaForm.status" placeholder="请选择状态">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="workAreaForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入办公区描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="workAreaDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveWorkArea">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import axios from 'axios'

export default {
  name: 'OfficeManagement',
  components: {
    Plus,
    Edit,
    Delete
  },
  setup() {
    const offices = ref([])
    const officeDialogVisible = ref(false)
    const workAreaDialogVisible = ref(false)
    const isEditingOffice = ref(false)
    const isEditingWorkArea = ref(false)
    const officeFormRef = ref()
    const workAreaFormRef = ref()

    // 办公室表单
    const officeForm = reactive({
      id: null,
      officeCode: '',
      officeName: '',
      floor: 1,
      status: 'ACTIVE',
      description: ''
    })

    // 办公区表单
    const workAreaForm = reactive({
      id: null,
      areaCode: '',
      areaName: '',
      officeId: null,
      status: 'ACTIVE',
      description: ''
    })

    // 表单验证规则
    const officeRules = {
      officeCode: [
        { required: true, message: '请输入办公室编号', trigger: 'blur' }
      ],
      officeName: [
        { required: true, message: '请输入办公室名称', trigger: 'blur' }
      ],
      floor: [
        { required: true, message: '请选择楼层', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
      ]
    }

    const workAreaRules = {
      areaCode: [
        { required: true, message: '请输入办公区编号', trigger: 'blur' }
      ],
      areaName: [
        { required: true, message: '请输入办公区名称', trigger: 'blur' }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
      ]
    }

    // 获取办公室列表
    const loadOffices = async () => {
      try {
        const response = await axios.get('/api/office/list')
        if (response.data.code === 200) {
          offices.value = response.data.data
          // 为每个办公室加载办公区
          for (const office of offices.value) {
            await loadWorkAreas(office.id, office)
          }
        }
      } catch (error) {
        console.error('获取办公室列表失败:', error)
        ElMessage.error('获取办公室列表失败')
      }
    }

    // 获取办公区列表
    const loadWorkAreas = async (officeId, office) => {
      try {
        const response = await axios.get(`/api/office/${officeId}/work-areas`)
        if (response.data.code === 200) {
          office.workAreas = response.data.data
        }
      } catch (error) {
        console.error('获取办公区列表失败:', error)
      }
    }

    // 显示添加办公室对话框
    const showAddOfficeDialog = () => {
      isEditingOffice.value = false
      resetOfficeForm()
      officeDialogVisible.value = true
    }

    // 显示添加办公区对话框
    const showAddWorkAreaDialog = (officeId) => {
      isEditingWorkArea.value = false
      resetWorkAreaForm()
      workAreaForm.officeId = officeId
      workAreaDialogVisible.value = true
    }

    // 编辑办公室
    const editOffice = (office) => {
      isEditingOffice.value = true
      Object.assign(officeForm, office)
      officeDialogVisible.value = true
    }

    // 编辑办公区
    const editWorkArea = (workArea) => {
      isEditingWorkArea.value = true
      Object.assign(workAreaForm, workArea)
      workAreaDialogVisible.value = true
    }

    // 保存办公室
    const saveOffice = async () => {
      try {
        await officeFormRef.value.validate()
        
        const url = isEditingOffice.value 
          ? '/api/office/update'
          : '/api/office/add'
        
        const method = isEditingOffice.value ? 'put' : 'post'
        
        const response = await axios[method](url, officeForm)
        
        if (response.data.code === 200) {
          ElMessage.success(response.data.message || '保存成功')
          officeDialogVisible.value = false
          resetOfficeForm()
          loadOffices()
        } else {
          ElMessage.error(response.data.message || '保存失败')
        }
      } catch (error) {
        console.error('保存办公室失败:', error)
        ElMessage.error('保存办公室失败')
      }
    }

    // 保存办公区
    const saveWorkArea = async () => {
      try {
        await workAreaFormRef.value.validate()
        
        const workAreaData = {
          ...workAreaForm,
          officeId: selectedOfficeId.value
        }
        
        const url = isEditingWorkArea.value 
          ? '/api/office/work-area/update'
          : '/api/office/work-area/add'
        
        const method = isEditingWorkArea.value ? 'put' : 'post'
        
        const response = await axios[method](url, workAreaData)
        
        if (response.data.code === 200) {
          ElMessage.success(response.data.message || '保存成功')
          workAreaDialogVisible.value = false
          resetWorkAreaForm()
          loadOffices()
        } else {
          ElMessage.error(response.data.message || '保存失败')
        }
      } catch (error) {
        console.error('保存办公区失败:', error)
        ElMessage.error('保存办公区失败')
      }
    }

    // 删除办公室
    const deleteOffice = async (officeId) => {
      try {
        await ElMessageBox.confirm('确定要删除这个办公室吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await axios.delete(`/api/office/${officeId}`)
        
        if (response.data.code === 200) {
          ElMessage.success(response.data.message || '删除成功')
          loadOffices()
        } else {
          ElMessage.error(response.data.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除办公室失败:', error)
          ElMessage.error('删除办公室失败')
        }
      }
    }

    // 删除办公区
    const deleteWorkArea = async (workAreaId) => {
      try {
        await ElMessageBox.confirm('确定要删除这个办公区吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await axios.delete(`/api/office/work-area/${workAreaId}`)
        
        if (response.data.code === 200) {
          ElMessage.success(response.data.message || '删除成功')
          loadOffices()
        } else {
          ElMessage.error(response.data.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除办公区失败:', error)
          ElMessage.error('删除办公区失败')
        }
      }
    }

    // 重置办公室表单
    const resetOfficeForm = () => {
      Object.assign(officeForm, {
        id: null,
        officeCode: '',
        officeName: '',
        floor: 1,
        status: 'ACTIVE',
        description: ''
      })
    }

    // 重置办公区表单
    const resetWorkAreaForm = () => {
      Object.assign(workAreaForm, {
        id: null,
        areaCode: '',
        areaName: '',
        officeId: null,
        status: 'ACTIVE',
        description: ''
      })
    }

    onMounted(() => {
      loadOffices()
    })

    return {
      offices,
      officeDialogVisible,
      workAreaDialogVisible,
      isEditingOffice,
      isEditingWorkArea,
      officeForm,
      workAreaForm,
      officeRules,
      workAreaRules,
      officeFormRef,
      workAreaFormRef,
      showAddOfficeDialog,
      showAddWorkAreaDialog,
      editOffice,
      editWorkArea,
      saveOffice,
      saveWorkArea,
      deleteOffice,
      deleteWorkArea
    }
  }
}
</script>

<style scoped>
.office-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #303133;
}

.office-list {
  display: grid;
  gap: 20px;
}

.office-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.office-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.office-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.office-info h3 {
  margin: 0;
  color: #303133;
}

.office-code {
  background: #f0f2f5;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
}

.office-actions {
  display: flex;
  gap: 8px;
}

.office-content {
  margin-top: 15px;
}

.office-details {
  margin-bottom: 20px;
}

.office-details p {
  margin: 5px 0;
  color: #606266;
}

.work-areas h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.work-area-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.work-area-item {
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.work-area-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.work-area-name {
  font-weight: 500;
  color: #303133;
}

.work-area-code {
  background: #e4e7ed;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
  color: #909399;
}

.work-area-actions {
  display: flex;
  gap: 5px;
}

.no-work-areas {
  text-align: center;
  color: #909399;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 6px;
}

.no-work-areas p {
  margin: 0;
}

/* 表单样式 */
.el-dialog .el-form-item {
  margin-bottom: 20px;
}

.el-dialog .el-input,
.el-dialog .el-select,
.el-dialog .el-textarea {
  width: 100%;
}

.el-dialog .el-input__inner {
  min-width: 200px;
}

/* 确保办公室编号输入框有足够的宽度 */
.el-form-item .el-input {
  width: 100%;
}

.el-form-item .el-input .el-input__inner {
  width: 100%;
  min-width: 250px;
}

@media (max-width: 768px) {
  .office-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .office-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .work-area-grid {
    grid-template-columns: 1fr;
  }

  .work-area-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .work-area-actions {
    align-self: flex-end;
  }
}
</style>