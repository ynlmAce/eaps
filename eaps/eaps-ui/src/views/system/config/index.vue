<template>
  <div class="config-manage-container">
    <!-- 搜索筛选区域 -->
    <el-card class="filter-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="参数名称" prop="name">
          <el-input v-model="queryParams.name" placeholder="请输入参数名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input v-model="queryParams.configKey" placeholder="请输入参数键名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="参数类型" prop="type">
          <el-select v-model="queryParams.type" placeholder="参数类型" clearable>
            <el-option label="系统参数" value="system" />
            <el-option label="业务参数" value="business" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮区域 -->
    <el-card class="config-list-card">
      <template #header>
        <div class="card-header">
          <span>参数列表</span>
          <div class="card-header-operations">
            <el-button type="primary" icon="Plus" @click="handleAdd">新增参数</el-button>
            <el-button type="danger" icon="Delete" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="success" icon="Upload" @click="handleImport">导入</el-button>
            <el-button icon="Download" @click="handleExport">导出</el-button>
          </div>
        </div>
      </template>

      <!-- 参数表格 -->
      <el-table
        v-loading="loading"
        :data="configList"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="参数ID" prop="id" width="80" />
        <el-table-column label="参数名称" prop="name" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column label="参数键名" prop="configKey" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column label="参数键值" prop="configValue" min-width="200" :show-overflow-tooltip="true" />
        <el-table-column label="参数类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'system' ? 'danger' : 'success'">
              {{ scope.row.type === 'system' ? '系统参数' : '业务参数' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否内置" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isBuiltin ? 'danger' : 'info'">
              {{ scope.row.isBuiltin ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="scope">
            <el-button v-if="!scope.row.isBuiltin" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button v-if="!scope.row.isBuiltin" link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
            <el-button v-if="scope.row.isBuiltin" link type="info" icon="View" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加或修改参数对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="600px"
      append-to-body
    >
      <el-form ref="configForm" :model="configForm" :rules="rules" label-width="100px">
        <el-form-item label="参数名称" prop="name">
          <el-input v-model="configForm.name" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input v-model="configForm.configKey" placeholder="请输入参数键名" :disabled="dialog.type === 'edit'" />
        </el-form-item>
        <el-form-item label="参数键值" prop="configValue">
          <el-input v-model="configForm.configValue" type="textarea" :rows="4" placeholder="请输入参数键值" />
        </el-form-item>
        <el-form-item label="参数类型" prop="type">
          <el-radio-group v-model="configForm.type">
            <el-radio label="system">系统参数</el-radio>
            <el-radio label="business">业务参数</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="configForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 参数详情对话框 -->
    <el-dialog
      title="参数详情"
      v-model="viewDialog.visible"
      width="600px"
      append-to-body
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="参数名称">{{ viewDialog.data.name }}</el-descriptions-item>
        <el-descriptions-item label="参数键名">{{ viewDialog.data.configKey }}</el-descriptions-item>
        <el-descriptions-item label="参数键值">{{ viewDialog.data.configValue }}</el-descriptions-item>
        <el-descriptions-item label="参数类型">
          {{ viewDialog.data.type === 'system' ? '系统参数' : '业务参数' }}
        </el-descriptions-item>
        <el-descriptions-item label="是否内置">
          {{ viewDialog.data.isBuiltin ? '是' : '否' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ viewDialog.data.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewDialog.data.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ viewDialog.data.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      title="导入参数"
      v-model="importDialog.visible"
      width="400px"
      append-to-body
    >
      <el-upload
        class="upload-demo"
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        :limit="1"
        :file-list="importDialog.fileList"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传JSON文件，且不超过500kb
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="importDialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitImport" :disabled="!importDialog.file">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getConfigList, getConfigByKey, updateConfig } from '@/api/system'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  configKey: '',
  type: ''
})

// 配置表格数据
const loading = ref(false)
const configList = ref([])
const total = ref(0)
const selectedRows = ref([])

// 弹窗控制
const dialog = reactive({
  visible: false,
  title: '',
  type: 'add' // add 或 edit
})

// 表单参数
const configForm = reactive({
  id: undefined,
  name: '',
  configKey: '',
  configValue: '',
  type: 'business',
  isBuiltin: false,
  remark: ''
})

// 表单校验规则
const rules = reactive({
  name: [
    { required: true, message: '参数名称不能为空', trigger: 'blur' }
  ],
  configKey: [
    { required: true, message: '参数键名不能为空', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_\.]+$/, message: '参数键名只能包含字母、数字、下划线和点', trigger: 'blur' }
  ],
  configValue: [
    { required: true, message: '参数键值不能为空', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '参数类型不能为空', trigger: 'change' }
  ]
})

// 查看详情对话框
const viewDialog = reactive({
  visible: false,
  data: {}
})

// 导入对话框
const importDialog = reactive({
  visible: false,
  file: null,
  fileList: []
})

// 获取参数列表数据
const getList = async () => {
  loading.value = true
  try {
    const response = await getConfigList(queryParams)
    configList.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('获取参数列表失败', error)
    ElMessage.error('获取参数列表失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮操作
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置按钮操作
const resetQuery = () => {
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    name: '',
    configKey: '',
    type: ''
  })
  handleQuery()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 新增按钮操作
const handleAdd = () => {
  resetForm()
  dialog.visible = true
  dialog.title = '添加参数'
  dialog.type = 'add'
}

// 修改按钮操作
const handleUpdate = (row) => {
  resetForm()
  dialog.visible = true
  dialog.title = '修改参数'
  dialog.type = 'edit'
  Object.assign(configForm, row)
}

// 查看按钮操作
const handleView = (row) => {
  viewDialog.visible = true
  viewDialog.data = row
}

// 删除按钮操作
const handleDelete = (row) => {
  if (row.isBuiltin) {
    ElMessage.warning('内置参数不能删除')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除参数"${row.name}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 实现删除接口调用
      // await deleteConfig(row.id)
      
      // 这里模拟成功响应
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除参数失败', error)
      ElMessage.error('删除参数失败')
    }
  }).catch(() => {})
}

// 批量删除操作
const handleBatchDelete = () => {
  // 过滤掉内置参数
  const nonBuiltinRows = selectedRows.value.filter(row => !row.isBuiltin)
  
  if (nonBuiltinRows.length === 0) {
    ElMessage.warning('所选参数中不存在可删除的非内置参数')
    return
  }
  
  if (nonBuiltinRows.length < selectedRows.value.length) {
    ElMessageBox.confirm(
      `所选中的${selectedRows.value.length}个参数中有${selectedRows.value.length - nonBuiltinRows.length}个内置参数不能删除，是否继续删除其余${nonBuiltinRows.length}个参数？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      deleteMultiple(nonBuiltinRows)
    }).catch(() => {})
  } else {
    ElMessageBox.confirm(
      `确定要删除选中的${nonBuiltinRows.length}个参数吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      deleteMultiple(nonBuiltinRows)
    }).catch(() => {})
  }
}

// 执行批量删除
const deleteMultiple = async (rows) => {
  try {
    // 实现批量删除接口调用
    // await batchDeleteConfig(rows.map(item => item.id))
    
    // 这里模拟成功响应
    ElMessage.success('批量删除成功')
    getList()
  } catch (error) {
    console.error('批量删除失败', error)
    ElMessage.error('批量删除失败')
  }
}

// 导入操作
const handleImport = () => {
  importDialog.visible = true
  importDialog.file = null
  importDialog.fileList = []
}

// 文件选择变更
const handleFileChange = (file) => {
  importDialog.file = file.raw
}

// 提交导入
const submitImport = async () => {
  if (!importDialog.file) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  
  if (importDialog.file.type !== 'application/json') {
    ElMessage.warning('只能导入JSON格式的文件')
    return
  }
  
  // 这里实现文件上传和导入逻辑
  // 模拟上传成功
  ElMessage.success('导入成功')
  importDialog.visible = false
  getList()
}

// 导出操作
const handleExport = () => {
  // 实现导出逻辑
  ElMessage.info('导出功能待实现')
}

// 重置表单
const resetForm = () => {
  Object.assign(configForm, {
    id: undefined,
    name: '',
    configKey: '',
    configValue: '',
    type: 'business',
    isBuiltin: false,
    remark: ''
  })
}
// 提交表单
const formRef = ref(null)
const submitForm = async () => {
  try {
    await formRef.value.validate()
    const formData = { ...configForm }
    
    if (dialog.type === 'edit') {
      // 更新参数
      await updateConfig(formData.id, formData)
      ElMessage.success('修改参数成功')
    } else {
      // 新增参数
      // 实现新增逻辑
      // await addConfig(formData)
      
      // 这里模拟成功响应
      ElMessage.success('新增参数成功')
    }
    
    dialog.visible = false
    getList()
  } catch (error) {
    console.error('表单提交失败', error)
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.config-manage-container {
  padding: 20px;
  
  .filter-card {
    margin-bottom: 20px;
  }
  
  .config-list-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .card-header-operations {
        .el-button {
          margin-left: 8px;
        }
      }
    }
    
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    
    .el-button {
      margin-left: 10px;
    }
  }
  
  :deep(.el-upload-dragger) {
    width: 100%;
  }
}
</style> 