<template>
  <div class="fire-safety-container">
    <div class="header">
      <h2>消防安全管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showEvacuationMap">查看疏散图</el-button>
      </div>
    </div>

    <!-- 消防水带管理 -->
    <div class="section">
      <div class="section-header">
        <h3>消防水带管理</h3>
        <el-button type="primary" @click="showAddFireHoseDialog">添加消防水带</el-button>
      </div>
      
      <el-table :data="fireHoses" style="width: 100%" stripe>
        <el-table-column prop="code" label="编号" width="120"></el-table-column>
        <el-table-column prop="name" label="名称" width="150"></el-table-column>
        <el-table-column prop="location" label="位置" width="200"></el-table-column>
        <el-table-column prop="lastCheckTime" label="上次检查时间" width="180">
          <template #default="scope">
            <span :class="{ 'overdue-text': scope.row.overdue }">
              {{ formatDateTime(scope.row.lastCheckTime) }}
            </span>
            <el-tag v-if="scope.row.overdue" type="danger" size="small" style="margin-left: 8px">
              超期未检查
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="editFireHose(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteFireHose(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 灭火器管理 -->
    <div class="section">
      <div class="section-header">
        <h3>灭火器管理</h3>
        <el-button type="primary" @click="showAddFireExtinguisherDialog">添加灭火器</el-button>
      </div>
      
      <el-table :data="fireExtinguishers" style="width: 100%" stripe>
        <el-table-column prop="code" label="编号" width="120"></el-table-column>
        <el-table-column prop="name" label="名称" width="150"></el-table-column>
        <el-table-column prop="location" label="位置" width="200"></el-table-column>
        <el-table-column prop="lastPressureCheckTime" label="上次压力检查时间" width="180">
          <template #default="scope">
            <span :class="{ 'overdue-text': scope.row.overdue }">
              {{ formatDateTime(scope.row.lastPressureCheckTime) }}
            </span>
            <el-tag v-if="scope.row.overdue" type="danger" size="small" style="margin-left: 8px">
              超期未检查
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="editFireExtinguisher(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteFireExtinguisher(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 消防水带编辑对话框 -->
    <el-dialog v-model="fireHoseDialogVisible" :title="fireHoseDialogTitle" width="500px">
      <el-form :model="currentFireHose" :rules="fireHoseRules" ref="fireHoseFormRef" label-width="120px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="currentFireHose.name" placeholder="请输入消防水带名称"></el-input>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-select v-model="currentFireHose.location" placeholder="请选择安装位置" filterable>
            <el-option
              v-for="option in locationOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="上次检查时间">
          <el-date-picker
            v-model="currentFireHose.lastCheckTime"
            type="datetime"
            placeholder="选择检查时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="currentFireHose.status" placeholder="请选择状态">
            <el-option label="正常" :value="1"></el-option>
            <el-option label="停用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="currentFireHose.remark" type="textarea" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="fireHoseDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveFireHose">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 灭火器编辑对话框 -->
    <el-dialog v-model="fireExtinguisherDialogVisible" :title="fireExtinguisherDialogTitle" width="500px">
      <el-form :model="currentFireExtinguisher" :rules="fireExtinguisherRules" ref="fireExtinguisherFormRef" label-width="140px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="currentFireExtinguisher.name" placeholder="请输入灭火器名称"></el-input>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-select v-model="currentFireExtinguisher.location" placeholder="请选择安装位置" filterable>
            <el-option
              v-for="option in locationOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="上次压力检查时间">
          <el-date-picker
            v-model="currentFireExtinguisher.lastPressureCheckTime"
            type="datetime"
            placeholder="选择压力检查时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="currentFireExtinguisher.status" placeholder="请选择状态">
            <el-option label="正常" :value="1"></el-option>
            <el-option label="停用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="currentFireExtinguisher.remark" type="textarea" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="fireExtinguisherDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveFireExtinguisher">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 疏散图对话框 -->
    <el-dialog v-model="evacuationMapVisible" title="消防疏散图" width="80%" center>
      <div class="evacuation-map">
        <img src="/evacuation-map.png" alt="消防疏散图" style="width: 100%; height: auto;" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLocationOptions } from '@/api/location'
import { 
  getFireHoses, 
  addFireHose, 
  updateFireHose, 
  deleteFireHose,
  getFireExtinguishers,
  addFireExtinguisher,
  updateFireExtinguisher,
  deleteFireExtinguisher
} from '@/api/fireSafety'

export default {
  name: 'FireSafety',
  data() {
    return {
      // 位置选项
      locationOptions: [],
      
      // 消防水带数据
      fireHoses: [],
      fireHoseDialogVisible: false,
      fireHoseDialogTitle: '',
      currentFireHose: {
        id: null,
        name: '',
        location: '',
        lastCheckTime: null,
        status: 1,
        remark: ''
      },
      fireHoseRules: {
        name: [{ required: true, message: '请输入消防水带名称', trigger: 'blur' }],
        location: [{ required: true, message: '请选择安装位置', trigger: 'change' }]
      },

      // 灭火器数据
      fireExtinguishers: [],
      fireExtinguisherDialogVisible: false,
      fireExtinguisherDialogTitle: '',
      currentFireExtinguisher: {
        id: null,
        name: '',
        location: '',
        lastPressureCheckTime: null,
        status: 1,
        remark: ''
      },
      fireExtinguisherRules: {
        name: [{ required: true, message: '请输入灭火器名称', trigger: 'blur' }],
        location: [{ required: true, message: '请选择安装位置', trigger: 'change' }]
      },

      // 疏散图
      evacuationMapVisible: false
    }
  },
  mounted() {
    this.loadLocationOptions()
    this.loadFireHoses()
    this.loadFireExtinguishers()
  },
  methods: {
    // ==================== 位置管理 ====================
    async loadLocationOptions() {
      try {
        const response = await getLocationOptions()
        if (response.code === 200) {
          this.locationOptions = response.data
        } else {
          ElMessage.error(response.message || '获取位置选项失败')
        }
      } catch (error) {
        console.error('获取位置选项失败:', error)
        ElMessage.error('获取位置选项失败：' + (error.message || '网络错误'))
      }
    },
    // ==================== 消防水带管理 ====================
    async loadFireHoses() {
      try {
        const response = await getFireHoses()
        if (response.code === 200) {
          this.fireHoses = response.data
        } else {
          ElMessage.error(response.message || '获取消防水带列表失败')
        }
      } catch (error) {
        console.error('获取消防水带列表失败:', error)
        ElMessage.error('获取消防水带列表失败：' + (error.message || '网络错误'))
      }
    },

    showAddFireHoseDialog() {
      this.fireHoseDialogTitle = '添加消防水带'
      this.currentFireHose = {
        id: null,
        name: '',
        location: '',
        lastCheckTime: null,
        status: 1,
        remark: ''
      }
      this.fireHoseDialogVisible = true
    },

    editFireHose(fireHose) {
      this.fireHoseDialogTitle = '编辑消防水带'
      this.currentFireHose = { ...fireHose }
      this.fireHoseDialogVisible = true
    },

    async saveFireHose() {
      try {
        await this.$refs.fireHoseFormRef.validate()
        
        let response
        if (this.currentFireHose.id) {
          response = await updateFireHose(this.currentFireHose)
        } else {
          response = await addFireHose(this.currentFireHose)
        }

        if (response.code === 200) {
          ElMessage.success(response.message)
          this.fireHoseDialogVisible = false
          this.loadFireHoses()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('保存消防水带失败:', error)
        ElMessage.error('操作失败：' + (error.message || '网络错误'))
      }
    },

    async deleteFireHose(id) {
      try {
        await ElMessageBox.confirm('确定要删除这个消防水带吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await deleteFireHose(id)
        
        if (response.code === 200) {
          ElMessage.success('删除成功')
          this.loadFireHoses()
        } else {
          ElMessage.error(response.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除消防水带失败:', error)
          ElMessage.error('删除失败：' + (error.message || '网络错误'))
        }
      }
    },

    // ==================== 灭火器管理 ====================
    async loadFireExtinguishers() {
      try {
        const response = await getFireExtinguishers()
        if (response.code === 200) {
          this.fireExtinguishers = response.data
        } else {
          ElMessage.error(response.message || '获取灭火器列表失败')
        }
      } catch (error) {
        console.error('获取灭火器列表失败:', error)
        ElMessage.error('获取灭火器列表失败：' + (error.message || '网络错误'))
      }
    },

    showAddFireExtinguisherDialog() {
      this.fireExtinguisherDialogTitle = '添加灭火器'
      this.currentFireExtinguisher = {
        id: null,
        name: '',
        location: '',
        lastPressureCheckTime: null,
        status: 1,
        remark: ''
      }
      this.fireExtinguisherDialogVisible = true
    },

    editFireExtinguisher(fireExtinguisher) {
      this.fireExtinguisherDialogTitle = '编辑灭火器'
      this.currentFireExtinguisher = { ...fireExtinguisher }
      this.fireExtinguisherDialogVisible = true
    },

    async saveFireExtinguisher() {
      try {
        await this.$refs.fireExtinguisherFormRef.validate()
        
        let response
        if (this.currentFireExtinguisher.id) {
          response = await updateFireExtinguisher(this.currentFireExtinguisher)
        } else {
          response = await addFireExtinguisher(this.currentFireExtinguisher)
        }

        if (response.code === 200) {
          ElMessage.success(response.message)
          this.fireExtinguisherDialogVisible = false
          this.loadFireExtinguishers()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('保存灭火器失败:', error)
        ElMessage.error('操作失败：' + (error.message || '网络错误'))
      }
    },

    async deleteFireExtinguisher(id) {
      try {
        await ElMessageBox.confirm('确定要删除这个灭火器吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await deleteFireExtinguisher(id)
        
        if (response.code === 200) {
          ElMessage.success('删除成功')
          this.loadFireExtinguishers()
        } else {
          ElMessage.error(response.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除灭火器失败:', error)
          ElMessage.error('删除失败：' + (error.message || '网络错误'))
        }
      }
    },

    // ==================== 通用方法 ====================
    formatDateTime(dateTime) {
      if (!dateTime) return '未设置'
      return new Date(dateTime).toLocaleString('zh-CN')
    },

    showEvacuationMap() {
      this.evacuationMapVisible = true
    }
  }
}
</script>

<style scoped>
.fire-safety-container {
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
  color: #333;
}

.section {
  margin-bottom: 30px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  color: #333;
}

.overdue-text {
  color: #f56c6c;
  font-weight: bold;
}

.evacuation-map {
  text-align: center;
}

.evacuation-map img {
  max-width: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
</style>