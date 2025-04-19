<template>
  <div class="user-manage-container">
    <!-- 搜索筛选区域 -->
    <el-card class="filter-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="用户名称" prop="username">
          <el-input v-model="queryParams.username" placeholder="请输入用户名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号码" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="用户状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="用户状态" clearable>
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户角色" prop="roleId">
          <el-select v-model="queryParams.roleId" placeholder="用户角色" clearable>
            <el-option 
              v-for="role in roleOptions" 
              :key="role.id" 
              :label="role.name" 
              :value="role.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮区域 -->
    <el-card class="user-list-card">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div class="card-header-operations">
            <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
            <el-button type="danger" icon="Delete" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
            <el-button icon="Download" @click="handleExport">导出</el-button>
          </div>
        </div>
      </template>

      <!-- 用户表格 -->
      <el-table
        v-loading="loading"
        :data="userList"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="用户ID" prop="id" width="80" />
        <el-table-column label="用户名" prop="username" min-width="100" :show-overflow-tooltip="true" />
        <el-table-column label="用户昵称" prop="nickname" min-width="100" :show-overflow-tooltip="true" />
        <el-table-column label="用户类型" width="120">
          <template #default="scope">
            <el-tag :type="getUserTypeTag(scope.row.role)">{{ getUserTypeText(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="手机号码" prop="phone" width="120" />
        <el-table-column label="邮箱" prop="email" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column label="状态" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="'active'"
              :inactive-value="'disabled'"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" fixed="right" width="240">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button link type="primary" icon="Key" @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-button link type="primary" icon="User" @click="handleAssignRole(scope.row)">分配角色</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 添加/修改用户对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="600px"
      append-to-body
    >
      <el-form ref="userForm" :model="userForm" :rules="rules" label-width="100px">
        <el-form-item label="用户名称" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名称" :disabled="userForm.id !== undefined" />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入用户昵称" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="用户类型" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择用户类型">
            <el-option label="学生" value="student" />
            <el-option label="企业" value="enterprise" />
            <el-option label="辅导员" value="counselor" />
            <el-option label="管理员" value="admin" />
            <el-option label="超级管理员" value="superAdmin" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!userForm.id">
          <el-input v-model="userForm.password" placeholder="请输入密码" type="password" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!userForm.id">
          <el-input v-model="userForm.confirmPassword" placeholder="请再次输入密码" type="password" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="userForm.status">
            <el-radio label="active">正常</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="userForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      title="重置密码"
      v-model="resetPwd.visible"
      width="500px"
      append-to-body
    >
      <el-form ref="resetPwdForm" :model="resetPwd.form" :rules="resetPwd.rules" label-width="100px">
        <el-form-item label="用户名称">
          <el-input v-model="resetPwd.form.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPwd.form.password" placeholder="请输入新密码" type="password" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPwd.form.confirmPassword" placeholder="请再次输入新密码" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetPwd.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitResetPwd">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog
      title="分配角色"
      v-model="assignRole.visible"
      width="500px"
      append-to-body
    >
      <el-form label-width="100px">
        <el-form-item label="用户名称">
          <el-input v-model="assignRole.form.username" disabled />
        </el-form-item>
        <el-form-item label="角色列表">
          <el-transfer
            v-model="assignRole.form.selectedRoles"
            :data="assignRole.roleOptions"
            :titles="['可选角色', '已选角色']"
            :props="{
              key: 'id',
              label: 'name'
            }"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="assignRole.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitAssignRole">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserList,
  getUserDetail,
  addUser,
  updateUser,
  deleteUser,
  resetUserPassword,
  changeUserStatus,
  assignUserRole
} from '@/api/system'
import { getRoleList } from '@/api/system'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  phone: '',
  status: '',
  roleId: ''
})

// 日期范围
const dateRange = ref([])

// 用户表格数据
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const selectedRows = ref([])
const roleOptions = ref([])

// 弹窗控制
const dialog = reactive({
  visible: false,
  title: ''
})
// 表单参数
const form = reactive({
  id: undefined,
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  role: '',
  status: 'active',
  remark: ''
})

// 表单校验规则
const rules = reactive({
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度必须在3到20个字符之间', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '用户昵称不能为空', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6到20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== userForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ]
})

// 重置密码相关
const resetPwd = reactive({
  visible: false,
  form: {
    userId: undefined,
    username: '',
    password: '',
    confirmPassword: ''
  },
  rules: {
    password: [
      { required: true, message: '密码不能为空', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度必须在6到20个字符之间', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '确认密码不能为空', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== resetPwd.form.password) {
            callback(new Error('两次输入的密码不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }
})

// 分配角色相关
const assignRole = reactive({
  visible: false,
  form: {
    userId: undefined,
    username: '',
    selectedRoles: []
  },
  roleOptions: []
})

// 获取用户列表
const getList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    const params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.beginTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    
    const response = await getUserList(params)
    userList.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('获取用户列表失败', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 查询角色列表
const getRoles = async () => {
  try {
    const response = await getRoleList()
    roleOptions.value = response.data || []
    assignRole.roleOptions = response.data.map(item => ({
      id: item.id,
      name: item.name
    }))
  } catch (error) {
    console.error('获取角色列表失败', error)
  }
}

// 查询按钮操作
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置按钮操作
const resetQuery = () => {
  dateRange.value = []
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    username: '',
    phone: '',
    status: '',
    roleId: ''
  })
  handleQuery()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 用户状态修改
const handleStatusChange = async (row) => {
  try {
    await changeUserStatus(row.id, row.status)
    ElMessage.success(`修改${row.username}状态成功`)
  } catch (error) {
    row.status = row.status === 'active' ? 'disabled' : 'active' // 失败时状态回退
    ElMessage.error('修改用户状态失败')
  }
}

// 新增按钮操作
const handleAdd = () => {
  dialog.visible = true
  dialog.title = '添加用户'
  resetForm()
}

// 修改按钮操作
const handleUpdate = async (row) => {
  try {
    const response = await getUserDetail(row.id)
    dialog.visible = true
    dialog.title = '修改用户'
    Object.assign(userForm, response.data)
  } catch (error) {
    console.error('获取用户详情失败', error)
    ElMessage.error('获取用户详情失败')
  }
}

// 重置密码按钮操作
const handleResetPwd = (row) => {
  resetPwd.visible = true
  resetPwd.form.userId = row.id
  resetPwd.form.username = row.username
  resetPwd.form.password = ''
  resetPwd.form.confirmPassword = ''
}

// 分配角色按钮操作
const handleAssignRole = async (row) => {
  try {
    const response = await getUserDetail(row.id)
    assignRole.visible = true
    assignRole.form.userId = row.id
    assignRole.form.username = row.username
    assignRole.form.selectedRoles = response.data.roles.map(role => role.id)
  } catch (error) {
    console.error('获取用户角色信息失败', error)
    ElMessage.error('获取用户角色信息失败')
  }
}

// 提交表单
const userForm = ref(null)
const submitForm = async () => {
  try {
    await userForm.value.validate()
    
    const formData = { ...userForm }
    
    if (formData.id) {
      // 更新用户
      await updateUser(formData.id, formData)
      ElMessage.success('修改用户成功')
    } else {
      // 新增用户
      await addUser(formData)
      ElMessage.success('新增用户成功')
    }
    
    dialog.visible = false
    getList()
  } catch (error) {
    console.error('表单提交失败', error)
  }
}

// 提交重置密码
const resetPwdForm = ref(null)
const submitResetPwd = async () => {
  try {
    await resetPwdForm.value.validate()
    
    await resetUserPassword(resetPwd.form.userId, {
      password: resetPwd.form.password
    })
    
    ElMessage.success('重置密码成功')
    resetPwd.visible = false
  } catch (error) {
    console.error('重置密码失败', error)
  }
}

// 提交角色分配
const submitAssignRole = async () => {
  try {
    await assignUserRole(assignRole.form.userId, assignRole.form.selectedRoles)
    ElMessage.success('分配角色成功')
    assignRole.visible = false
    getList()
  } catch (error) {
    console.error('分配角色失败', error)
    ElMessage.error('分配角色失败')
  }
}

// 删除按钮操作
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户"${row.username}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除用户失败', error)
      ElMessage.error('删除用户失败')
    }
  }).catch(() => {})
}

// 批量删除操作
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  const usernames = selectedRows.value.map(item => item.username).join('、')
  ElMessageBox.confirm(
    `确定要删除选中的${selectedRows.value.length}个用户(${usernames})吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = selectedRows.value.map(item => item.id)
      // 这里需要实现批量删除接口
      for (const id of ids) {
        await deleteUser(id)
      }
      ElMessage.success('批量删除成功')
      getList()
    } catch (error) {
      console.error('批量删除失败', error)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {})
}

// 导出按钮操作
const handleExport = () => {
  // 这里实现导出逻辑
  ElMessage.info('导出功能待实现')
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

// 重置表单
const resetForm = () => {
  Object.assign(userForm, {
    id: undefined,
    username: '',
    nickname: '',
    password: '',
    confirmPassword: '',
    phone: '',
    email: '',
    role: '',
    status: 'active',
    remark: ''
  })
}

// 获取用户类型对应的Tag类型
const getUserTypeTag = (role) => {
  const typeMap = {
    'student': 'info',
    'enterprise': 'success',
    'counselor': 'warning',
    'admin': 'danger',
    'superAdmin': 'danger'
  }
  return typeMap[role] || ''
}

// 获取用户类型文本
const getUserTypeText = (role) => {
  const typeMap = {
    'student': '学生',
    'enterprise': '企业',
    'counselor': '辅导员',
    'admin': '管理员',
    'superAdmin': '超级管理员'
  }
  return typeMap[role] || role
}

onMounted(() => {
  getList()
  getRoles()
})
</script>

<style lang="scss" scoped>
.user-manage-container {
  padding: 20px;
  
  .filter-card {
    margin-bottom: 20px;
  }
  
  .user-list-card {
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
}
</style> 