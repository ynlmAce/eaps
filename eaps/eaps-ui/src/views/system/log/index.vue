<template>
  <div class="log-management-container">
    <!-- 搜索过滤区域 -->
    <el-card class="filter-card">
      <div class="filter-header">
        <span class="filter-title">查询条件</span>
        <div class="filter-buttons">
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </div>
      </div>
      <el-form :model="queryParams" ref="queryFormRef" :inline="true">
        <el-form-item label="操作人员" prop="operName">
          <el-input v-model="queryParams.operName" clearable placeholder="请输入操作人员" style="width: 220px" />
        </el-form-item>
        <el-form-item label="操作类型" prop="operType">
          <el-select v-model="queryParams.operType" clearable placeholder="请选择操作类型" style="width: 220px">
            <el-option
              v-for="(dict, index) in operTypeOptions"
              :key="index"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择操作状态" style="width: 220px">
            <el-option label="成功" value="0" />
            <el-option label="失败" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间" prop="operTime">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 380px"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表区域 -->
    <el-card class="log-list-card">
      <div class="log-list-header">
        <span class="list-title">操作日志列表</span>
        <div class="list-buttons">
          <el-button type="danger" @click="handleClear">
            <el-icon><Delete /></el-icon>清空日志
          </el-button>
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>导出日志
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="logList"
        style="width: 100%"
      >
        <el-table-column prop="operId" label="日志编号" width="80" />
        <el-table-column prop="title" label="操作模块" width="150" show-overflow-tooltip />
        <el-table-column prop="businessType" label="操作类型" width="100">
          <template #default="scope">
            <el-tag :type="getOperTypeTag(scope.row.businessType)">{{ getOperTypeText(scope.row.businessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="请求方式" width="100" />
        <el-table-column prop="operName" label="操作人员" width="120" />
        <el-table-column prop="operIp" label="操作地址" width="130" show-overflow-tooltip />
        <el-table-column prop="operTime" label="操作时间" width="170" />
        <el-table-column prop="status" label="操作状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
              {{ scope.row.status === '0' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="90" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="scope">
            <el-button 
              link
              type="primary"
              @click="handleDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="操作日志详情"
      width="800px"
      append-to-body
    >
      <el-form label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="操作模块:">
              <span>{{ currentLog.title }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作类型:">
              <span>{{ getOperTypeText(currentLog.businessType) }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="操作人员:">
              <span>{{ currentLog.operName }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="请求方式:">
              <span>{{ currentLog.requestMethod }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="操作状态:">
              <span>{{ currentLog.status === '0' ? '成功' : '失败' }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作时间:">
              <span>{{ currentLog.operTime }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="操作IP:">
              <span>{{ currentLog.operIp }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作地点:">
              <span>{{ currentLog.operLocation }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="请求地址:">
              <span>{{ currentLog.operUrl }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="请求参数:">
              <el-input
                type="textarea"
                :model-value="currentLog.operParam"
                readonly
                :rows="7"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="currentLog.status === '1'">
          <el-col :span="24">
            <el-form-item label="错误信息:">
              <el-input
                type="textarea"
                v-model="currentLog.errorMsg"
                readonly
                :rows="7"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Delete, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLogList, getLogDetail, clearLogs, exportLogs } from '@/api/system'

// 操作类型选项
const operTypeOptions = [
  { label: '新增', value: '1' },
  { label: '修改', value: '2' },
  { label: '删除', value: '3' },
  { label: '查询', value: '4' },
  { label: '导出', value: '5' },
  { label: '导入', value: '6' },
  { label: '登录', value: '7' },
  { label: '退出', value: '8' },
  { label: '其他', value: '9' }
]

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  operName: undefined,
  operType: undefined,
  status: undefined,
  beginTime: undefined,
  endTime: undefined
})

// 日期范围
const dateRange = ref([])

// 操作日志表格数据
const loading = ref(false)
const logList = ref([])
const total = ref(0)
const queryFormRef = ref(null)

// 查看详情变量
const dialogVisible = ref(false)
const currentLog = ref({})

// 获取操作日志列表数据
const getList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.beginTime = dateRange.value[0]
      queryParams.endTime = dateRange.value[1]
    } else {
      queryParams.beginTime = undefined
      queryParams.endTime = undefined
    }

    const response = await getLogList(queryParams)
    logList.value = response.data.rows
    total.value = response.data.total
  } catch (error) {
    console.error('获取操作日志列表失败', error)
    ElMessage.error('获取操作日志列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索按钮操作
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置按钮操作
const resetQuery = () => {
  dateRange.value = []
  queryFormRef.value.resetFields()
  handleQuery()
}

// 查看日志详情
const handleDetail = async (row) => {
  dialogVisible.value = true
  try {
    const response = await getLogDetail(row.operId)
    currentLog.value = response.data
  } catch (error) {
    console.error('获取日志详情失败', error)
    ElMessage.error('获取日志详情失败')
  }
}

// 清空操作日志
const handleClear = () => {
  ElMessageBox.confirm('是否确认清空所有操作日志?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await clearLogs()
      ElMessage.success('清空操作日志成功')
      getList()
    } catch (error) {
      console.error('清空操作日志失败', error)
      ElMessage.error('清空操作日志失败')
    }
  }).catch(() => {})
}

// 导出操作日志
const handleExport = () => {
  ElMessageBox.confirm('是否确认导出所有操作日志?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await exportLogs(queryParams)
      ElMessage.success('导出操作日志成功')
    } catch (error) {
      console.error('导出操作日志失败', error)
      ElMessage.error('导出操作日志失败')
    }
  }).catch(() => {})
}

// 获取操作类型标签样式
const getOperTypeTag = (type) => {
  const types = {
    '1': 'success',
    '2': 'info',
    '3': 'danger',
    '4': '',
    '5': 'warning',
    '6': 'warning',
    '7': 'success',
    '8': 'info',
    '9': ''
  }
  return types[type] || ''
}

// 获取操作类型文字
const getOperTypeText = (type) => {
  const texts = {
    '1': '新增',
    '2': '修改',
    '3': '删除',
    '4': '查询',
    '5': '导出',
    '6': '导入',
    '7': '登录',
    '8': '退出',
    '9': '其他'
  }
  return texts[type] || '未知'
}

// 格式化JSON数据
const formatJsonData = (jsonData) => {
  if (!jsonData) return ''
  try {
    const parsed = typeof jsonData === 'object' ? jsonData : JSON.parse(jsonData)
    return JSON.stringify(parsed, null, 2)
  } catch (error) {
    return jsonData
  }
}

// 页面加载时获取数据
onMounted(() => {
  getList()
})
</script>

<style scoped>
.log-management-container {
  padding: 20px;
}

.filter-card, .log-list-card {
  margin-bottom: 20px;
}

.filter-header, .log-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-title, .list-title {
  font-size: 16px;
  font-weight: 600;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 