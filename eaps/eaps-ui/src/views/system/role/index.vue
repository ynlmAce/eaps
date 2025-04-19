<template>
  <div class="role-manage-container">
    <!-- 搜索筛选区域 -->
    <el-card class="filter-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="queryParams.name" placeholder="请输入角色名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="角色状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="角色状态" clearable>
            <el-option label="正常" value="active" />
            <el-option label="停用" value="inactive" />
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
    <el-card class="role-list-card">
      <template #header>
        <div class="card-header">
          <span>角色列表</span>
          <div class="card-header-operations">
            <el-button type="primary" icon="Plus" @click="handleAdd">新增角色</el-button>
            <el-button type="danger" icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
          </div>
        </div>
      </template>

      <!-- 角色表格 -->
      <el-table
        v-loading="loading"
        :data="roleList"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="角色ID" prop="id" width="80" />
        <el-table-column label="角色名称" prop="name" min-width="120" />
        <el-table-column label="角色编码" prop="code" min-width="120" />
        <el-table-column label="显示顺序" prop="sort" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="'active'"
              :inactive-value="'inactive'"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="200" :show-overflow-tooltip="true" />
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" fixed="right" width="260">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button link type="primary" icon="Key" @click="handleAssignAuth(scope.row)">分配权限</el-button>
            <el-button link type="primary" icon="User" @click="handleViewUsers(scope.row)">用户列表</el-button>
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

    <!-- 添加或修改角色对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="600px"
      append-to-body
    >
      <el-form ref="roleForm" :model="roleForm" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="roleForm.code" placeholder="请输入角色编码，如ROLE_ADMIN" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="sort">
          <el-input-number v-model="roleForm.sort" :min="0" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roleForm.status">
            <el-radio label="active">正常</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="roleForm.remark" type="textarea" placeholder="请输入角色备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog
      title="分配权限"
      v-model="permissionDialog.visible"
      width="600px"
      append-to-body
    >
      <el-form label-width="100px">
        <el-form-item label="角色名称">
          <el-input v-model="permissionDialog.roleName" disabled />
        </el-form-item>
        <el-form-item label="权限列表">
          <el-tree
            ref="permissionTree"
            :data="permissionDialog.permissionTree"
            :props="{ label: 'name', children: 'children' }"
            show-checkbox
            node-key="id"
            :default-checked-keys="permissionDialog.selectedPermissions"
            :check-strictly="false"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="permissionDialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitPermissions">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 角色用户列表对话框 -->
    <el-dialog
      title="角色用户列表"
      v-model="userDialog.visible"
      width="900px"
      append-to-body
    >
      <el-table
        v-loading="userDialog.loading"
        :data="userDialog.userList"
        border
      >
        <el-table-column label="用户ID" prop="id" width="80" />
        <el-table-column label="用户名称" prop="username" min-width="100" />
        <el-table-column label="用户昵称" prop="nickname" min-width="100" />
        <el-table-column label="用户类型" width="120">
          <template #default="scope">
            <el-tag :type="getUserTypeTag(scope.row.role)">{{ getUserTypeText(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="手机号码" prop="phone" width="120" />
        <el-table-column label="邮箱" prop="email" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="userDialog.pageNum"
          v-model:page-size="userDialog.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="userDialog.total"
          @size-change="handleUserDialogSizeChange"
          @current-change="handleUserDialogCurrentChange"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getRoleList,
  getRoleDetail,
  addRole,
  updateRole,
  deleteRole,
  assignRolePermissions
} from '@/api/system'
import { getPermissionTree } from '@/api/system'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  status: ''
})

// 日期范围
const dateRange = ref([])

// 角色表格数据
const loading = ref(false)
const roleList = ref([])
const total = ref(0)
const selectedIds = ref([])

// 弹窗控制
const dialog = reactive({
  visible: false,
  title: ''
})

// 表单参数
const roleForm = reactive({
  id: undefined,
  name: '',
  code: '',
  sort: 0,
  status: 'active',
  remark: ''
})

// 表单校验规则
const rules = reactive({
  name: [
    { required: true, message: '角色名称不能为空', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '角色编码不能为空', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '显示顺序不能为空', trigger: 'blur' }
  ]
})

// 分配权限对话框
const permissionDialog = reactive({
  visible: false,
  roleId: undefined,
  roleName: '',
  permissionTree: [],
  selectedPermissions: []
})

// 获取权限树的引用
const permissionTree = ref(null)

// 角色用户列表对话框
const userDialog = reactive({
  visible: false,
  loading: false,
  roleId: undefined,
  userList: [],
  total: 0,
  pageNum: 1,
  pageSize: 10
})

// 获取角色列表
const getList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    const params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.beginTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    
    const response = await getRoleList(params)
    roleList.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('获取角色列表失败', error)
    ElMessage.error('获取角色列表失败')
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
  dateRange.value = []
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    name: '',
    status: ''
  })
  handleQuery()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 角色状态修改
const handleStatusChange = async (row) => {
  try {
    await updateRole(row.id, { status: row.status })
    ElMessage.success(`修改${row.name}状态成功`)
  } catch (error) {
    row.status = row.status === 'active' ? 'inactive' : 'active' // 失败时状态回退
    ElMessage.error('修改角色状态失败')
  }
}

// 新增按钮操作
const handleAdd = () => {
  dialog.visible = true
  dialog.title = '添加角色'
  resetForm()
}

// 修改按钮操作
const handleUpdate = async (row) => {
  try {
    const response = await getRoleDetail(row.id)
    dialog.visible = true
    dialog.title = '修改角色'
    Object.assign(roleForm, response.data)
  } catch (error) {
    console.error('获取角色详情失败', error)
    ElMessage.error('获取角色详情失败')
  }
}

// 分配权限按钮操作
const handleAssignAuth = async (row) => {
  try {
    const roleResponse = await getRoleDetail(row.id)
    const permissionResponse = await getPermissionTree()
    
    permissionDialog.visible = true
    permissionDialog.roleId = row.id
    permissionDialog.roleName = row.name
    permissionDialog.permissionTree = permissionResponse.data || []
    
    // 设置当前角色已有的权限
    permissionDialog.selectedPermissions = roleResponse.data.permissions.map(item => item.id)
    
    nextTick(() => {
      // 确保DOM更新后再设置选中状态
      if (permissionTree.value) {
        permissionTree.value.setCheckedKeys(permissionDialog.selectedPermissions)
      }
    })
  } catch (error) {
    console.error('获取权限信息失败', error)
    ElMessage.error('获取权限信息失败')
  }
}

// 查看角色用户列表按钮操作
const handleViewUsers = async (row) => {
  userDialog.visible = true
  userDialog.roleId = row.id
  userDialog.pageNum = 1
  userDialog.pageSize = 10
  await loadRoleUsers()
}

// 加载角色用户列表
const loadRoleUsers = async () => {
  userDialog.loading = true
  try {
    // 需要实现根据角色ID查询用户的接口
    const response = await getUsersByRoleId(userDialog.roleId, {
      pageNum: userDialog.pageNum,
      pageSize: userDialog.pageSize
    })
    userDialog.userList = response.data.content || []
    userDialog.total = response.data.totalElements || 0
  } catch (error) {
    console.error('获取角色用户列表失败', error)
    ElMessage.error('获取角色用户列表失败')
  } finally {
    userDialog.loading = false
  }
}

// 通过角色ID查询用户列表（模拟实现，实际项目中应该从API导入）
const getUsersByRoleId = async (roleId, params) => {
  // 这里模拟接口返回，实际开发中应该调用真实接口
  return new Promise(resolve => {
    setTimeout(() => {
      resolve({
        data: {
          content: [
            {
              id: 1,
              username: 'admin',
              nickname: '管理员',
              role: 'admin',
              phone: '13800138000',
              email: 'admin@example.com',
              status: 'active',
              createTime: '2023-01-01 00:00:00'
            }
          ],
          totalElements: 1
        }
      })
    }, 300)
  })
}

// 角色用户列表分页大小改变
const handleUserDialogSizeChange = (val) => {
  userDialog.pageSize = val
  loadRoleUsers()
}

// 角色用户列表当前页改变
const handleUserDialogCurrentChange = (val) => {
  userDialog.pageNum = val
  loadRoleUsers()
}

// 重置表单
const resetForm = () => {
  Object.assign(roleForm, {
    id: undefined,
    name: '',
    code: '',
    sort: 0,
    status: 'active',
    remark: ''
  })
}
// 提交表单
const submitForm = async () => {
  try {
    await roleForm.value.validate()
    const formData = { ...roleForm }
    
    if (formData.id) {
      // 更新角色
      await updateRole(formData.id, formData)
      ElMessage.success('修改角色成功')
    } else {
      // 新增角色
      await addRole(formData)
      ElMessage.success('新增角色成功')
    }
    
    dialog.visible = false
    getList()
  } catch (error) {
    console.error('表单提交失败', error)
  }
}

// 提交权限分配
const submitPermissions = async () => {
  try {
    // 获取选中的权限ID列表
    const selectedKeys = permissionTree.value.getCheckedKeys()
    // 获取半选中的节点 ID 列表（父节点）
    const halfCheckedKeys = permissionTree.value.getHalfCheckedKeys()
    // 合并所有选中的权限ID
    const permissionIds = [...selectedKeys, ...halfCheckedKeys]
    
    await assignRolePermissions(permissionDialog.roleId, permissionIds)
    ElMessage.success('权限分配成功')
    permissionDialog.visible = false
  } catch (error) {
    console.error('权限分配失败', error)
    ElMessage.error('权限分配失败')
  }
}

// 删除按钮操作
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除角色"${row.name}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除角色失败', error)
      ElMessage.error('删除角色失败')
    }
  }).catch(() => {})
}

// 批量删除操作
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的${selectedIds.value.length}个角色吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 这里需要实现批量删除接口
      for (const id of selectedIds.value) {
        await deleteRole(id)
      }
      ElMessage.success('批量删除成功')
      getList()
    } catch (error) {
      console.error('批量删除失败', error)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {})
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
})
</script>

<style lang="scss" scoped>
.role-manage-container {
  padding: 20px;
  
  .filter-card {
    margin-bottom: 20px;
  }
  
  .role-list-card {
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