<template>
  <div class="permission-manage-container">
    <!-- 操作按钮区域 -->
    <el-card class="permission-card">
      <template #header>
        <div class="card-header">
          <span>权限列表</span>
          <div class="card-header-operations">
            <el-button type="primary" icon="Plus" @click="handleAdd(null)">新增顶级权限</el-button>
            <el-button type="success" icon="Refresh" @click="getList">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 权限表格 -->
      <el-table
        v-loading="loading"
        :data="permissionList"
        row-key="id"
        border
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column label="权限名称" prop="name" min-width="180">
          <template #default="scope">
            <div class="permission-name">
              <span>{{ scope.row.name }}</span>
              <div class="action-btns">
                <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)">新增子权限</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="权限标识" prop="code" min-width="200" />
        <el-table-column label="权限类型" width="120">
          <template #default="scope">
            <el-tag :type="getPermissionTypeTag(scope.row.type)">{{ getPermissionTypeText(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="图标" width="80">
          <template #default="scope">
            <i v-if="scope.row.icon" :class="scope.row.icon"></i>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="显示顺序" prop="sort" width="100" />
        <el-table-column label="URL路径" prop="path" min-width="200" :show-overflow-tooltip="true" />
        <el-table-column label="状态" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="'active'"
              :inactive-value="'inactive'"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" fixed="right" width="160">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加或修改权限对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="600px"
      append-to-body
    >
      <el-form ref="permissionForm" :model="permissionForm" :rules="rules" label-width="100px">
        <el-form-item label="上级权限">
          <el-tree-select
            v-model="permissionForm.parentId"
            placeholder="请选择上级权限"
            :data="permissionOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            clearable
          />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="permissionForm.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限标识" prop="code">
          <el-input v-model="permissionForm.code" placeholder="请输入权限标识，如system:user:list" />
        </el-form-item>
        <el-form-item label="权限类型" prop="type">
          <el-select v-model="permissionForm.type" placeholder="请选择权限类型">
            <el-option label="目录" value="directory" />
            <el-option label="菜单" value="menu" />
            <el-option label="按钮" value="button" />
          </el-select>
        </el-form-item>
        <el-form-item label="图标" prop="icon" v-if="permissionForm.type !== 'button'">
          <el-input v-model="permissionForm.icon" placeholder="请输入图标类名" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="sort">
          <el-input-number v-model="permissionForm.sort" :min="0" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="URL路径" prop="path" v-if="permissionForm.type !== 'button'">
          <el-input v-model="permissionForm.path" placeholder="请输入URL路径" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component" v-if="permissionForm.type === 'menu'">
          <el-input v-model="permissionForm.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="是否外链" v-if="permissionForm.type !== 'button'">
          <el-radio-group v-model="permissionForm.isExternal">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否显示" v-if="permissionForm.type !== 'button'">
          <el-radio-group v-model="permissionForm.isVisible">
            <el-radio :label="true">显示</el-radio>
            <el-radio :label="false">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="permissionForm.status">
            <el-radio label="active">正常</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPermissionList,
  getPermissionTree,
  addPermission,
  updatePermission,
  deletePermission
} from '@/api/system'

// 权限列表数据
const loading = ref(false)
const permissionList = ref([])

// 权限选项树（用于选择上级权限）
const permissionOptions = ref([])

// 弹窗控制
const dialog = reactive({
  visible: false,
  title: ''
})
// 表单参数
const permissionForm = reactive({
  id: undefined,
  parentId: null,
  name: '',
  code: '',
  type: 'menu',
  icon: '',
  sort: 0,
  path: '',
  component: '',
  isExternal: false,
  isVisible: true,
  status: 'active'
})

// 表单校验规则
const rules = reactive({
  name: [
    { required: true, message: '权限名称不能为空', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '权限标识不能为空', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '权限类型不能为空', trigger: 'change' }
  ],
  sort: [
    { required: true, message: '显示顺序不能为空', trigger: 'blur' }
  ]
})

// 获取权限列表数据
const getList = async () => {
  loading.value = true
  try {
    const response = await getPermissionTree()
    permissionList.value = response.data || []
    // 构建权限选项树
    await buildPermissionOptions()
  } catch (error) {
    console.error('获取权限列表失败', error)
    ElMessage.error('获取权限列表失败')
  } finally {
    loading.value = false
  }
}

// 构建权限选项树
const buildPermissionOptions = async () => {
  try {
    const response = await getPermissionTree()
    // 添加一个顶级虚拟节点
    permissionOptions.value = [
      {
        id: 0,
        name: '顶级权限',
        children: response.data || []
      }
    ]
  } catch (error) {
    console.error('获取权限树失败', error)
  }
}

// 新增按钮操作
const handleAdd = (row) => {
  resetForm()
  dialog.visible = true
  dialog.title = '添加权限'
  
  if (row) {
    // 如果有父级，设置父级ID
    permissionForm.parentId = row.id
  } else {
    // 顶级权限
    permissionForm.parentId = null
  }
}

// 修改按钮操作
const handleUpdate = (row) => {
  resetForm()
  dialog.visible = true
  dialog.title = '修改权限'
  
  // 将行数据赋值给表单
  Object.assign(permissionForm, {
    id: row.id,
    parentId: row.parentId || null,
    name: row.name,
    code: row.code,
    type: row.type,
    icon: row.icon,
    sort: row.sort,
    path: row.path,
    component: row.component,
    isExternal: row.isExternal,
    isVisible: row.isVisible,
    status: row.status
  })
}

// 权限状态修改
const handleStatusChange = async (row) => {
  try {
    await updatePermission(row.id, { status: row.status })
    ElMessage.success(`修改${row.name}状态成功`)
  } catch (error) {
    row.status = row.status === 'active' ? 'inactive' : 'active' // 失败时状态回退
    ElMessage.error('修改权限状态失败')
  }
}

// 删除按钮操作
const handleDelete = (row) => {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该权限有子权限，不能删除')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除权限"${row.name}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deletePermission(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除权限失败', error)
      ElMessage.error('删除权限失败')
    }
  }).catch(() => {})
}

// 重置表单
const resetForm = () => {
  Object.assign(permissionForm, {
    id: undefined,
    parentId: null,
    name: '',
    code: '',
    type: 'menu',
    icon: '',
    sort: 0,
    path: '',
    component: '',
    isExternal: false,
    isVisible: true,
    status: 'active'
  })
}
// 提交表单
const submitForm = async () => {
  try {
    await permissionForm.value.validate()
    const formData = { ...permissionForm }
    
    if (formData.id) {
      // 更新权限
      await updatePermission(formData.id, formData)
      ElMessage.success('修改权限成功')
    } else {
      // 新增权限
      await addPermission(formData)
      ElMessage.success('新增权限成功')
    }
    
    dialog.visible = false
    getList()
  } catch (error) {
    console.error('表单提交失败', error)
  }
}

// 获取权限类型对应的Tag类型
const getPermissionTypeTag = (type) => {
  const typeMap = {
    'directory': 'warning',
    'menu': 'success',
    'button': 'info'
  }
  return typeMap[type] || ''
}

// 获取权限类型文本
const getPermissionTypeText = (type) => {
  const typeMap = {
    'directory': '目录',
    'menu': '菜单',
    'button': '按钮'
  }
  return typeMap[type] || type
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.permission-manage-container {
  padding: 20px;
  
  .permission-card {
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
    
    .permission-name {
      display: flex;
      align-items: center;
      justify-content: space-between;
      
      .action-btns {
        opacity: 0;
        transition: opacity 0.3s;
      }
      
      &:hover {
        .action-btns {
          opacity: 1;
        }
      }
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