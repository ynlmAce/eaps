<template>
  <div class="message-container">
    <div class="page-header">
      <h2>消息中心</h2>
    </div>
    
    <el-tabs v-model="activeTab" class="message-tabs" @tab-click="handleTabClick">
      <el-tab-pane label="私聊" name="private">
        <div class="search-bar">
          <el-input
            v-model="searchText"
            placeholder="搜索会话"
            prefix-icon="Search"
            clearable
            @input="filterSessions"
          />
        </div>
        
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="3" animated />
          <el-divider />
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="filteredPrivateSessions.length === 0" class="empty-container">
          <el-empty description="暂无聊天会话" />
          <el-button type="primary" @click="startNewChat">发起新的会话</el-button>
        </div>
        
        <div v-else class="session-list">
          <div
            v-for="session in filteredPrivateSessions"
            :key="session.id"
            class="session-item"
            :class="{ 'session-active': currentSessionId === session.id }"
            @click="openSession(session)"
          >
            <div class="session-avatar">
              <el-avatar :size="50" :src="session.avatar || defaultAvatar">
                {{ session.name ? session.name.substr(0, 1) : 'U' }}
              </el-avatar>
              <el-badge v-if="session.unreadCount" :value="session.unreadCount" class="unread-badge" />
            </div>
            <div class="session-info">
              <div class="session-header">
                <div class="session-name">{{ session.name }}</div>
                <div class="session-time">{{ formatTime(session.lastMessageTime) }}</div>
              </div>
              <div class="session-message" :title="session.lastMessage">
                {{ session.lastMessage }}
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="群聊" name="group">
        <div class="search-bar">
          <el-input
            v-model="searchText"
            placeholder="搜索群组"
            prefix-icon="Search"
            clearable
            @input="filterSessions"
          />
          <el-button type="primary" @click="createGroup" class="create-group-btn">
            <el-icon><Plus /></el-icon>
            创建群组
          </el-button>
        </div>
        
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="3" animated />
          <el-divider />
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="filteredGroupSessions.length === 0" class="empty-container">
          <el-empty description="暂无群聊" />
          <div class="empty-actions">
            <el-button type="primary" @click="createGroup">创建群组</el-button>
            <el-button @click="findGroups">查找群组</el-button>
          </div>
        </div>
        
        <div v-else class="session-list">
          <div
            v-for="group in filteredGroupSessions"
            :key="group.id"
            class="session-item"
            :class="{ 'session-active': currentSessionId === group.id }"
            @click="openGroupSession(group)"
          >
            <div class="session-avatar">
              <el-avatar :size="50" :src="group.avatar || defaultGroupAvatar">
                {{ group.name ? group.name.substr(0, 1) : 'G' }}
              </el-avatar>
              <el-badge v-if="group.unreadCount" :value="group.unreadCount" class="unread-badge" />
            </div>
            <div class="session-info">
              <div class="session-header">
                <div class="session-name">{{ group.name }}</div>
                <div class="session-time">{{ formatTime(group.lastMessageTime) }}</div>
              </div>
              <div class="session-message">
                <span v-if="group.lastSenderName" class="sender-name">{{ group.lastSenderName }}：</span>
                {{ group.lastMessage }}
              </div>
              <div class="group-info">
                <el-icon><User /></el-icon>
                <span>{{ group.memberCount }}人</span>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 创建群组对话框 -->
    <el-dialog
      v-model="groupDialogVisible"
      title="创建群组"
      width="500px"
    >
      <el-form :model="groupForm" :rules="groupRules" ref="groupFormRef" label-width="100px">
        <el-form-item label="群组名称" prop="name">
          <el-input v-model="groupForm.name" placeholder="请输入群组名称" />
        </el-form-item>
        <el-form-item label="群组简介" prop="description">
          <el-input 
            v-model="groupForm.description" 
            placeholder="请输入群组简介" 
            type="textarea" 
            :rows="3" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="groupDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGroupForm" :loading="submitLoading">创建</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 发起新会话对话框 -->
    <el-dialog
      v-model="newChatDialogVisible"
      title="发起会话"
      width="500px"
    >
      <el-input
        v-model="searchUserText"
        placeholder="搜索用户或企业"
        prefix-icon="Search"
        clearable
        @input="searchUsers"
      />
      
      <div v-if="searchingUsers" class="searching-users">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="userSearchResult.length === 0" class="empty-search-result">
        <el-empty description="未找到用户" />
      </div>
      
      <div v-else class="user-list">
        <div
          v-for="user in userSearchResult"
          :key="user.id"
          class="user-item"
          @click="startChatWithUser(user)"
        >
          <el-avatar :size="40" :src="user.avatar">
            {{ user.name ? user.name.substr(0, 1) : 'U' }}
          </el-avatar>
          <div class="user-info">
            <div class="user-name">{{ user.name }}</div>
            <div class="user-type">{{ user.type === 'enterprise' ? '企业' : '用户' }}</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User, Search } from '@element-plus/icons-vue'
import { getChatSessionList, getChatGroupList, createChatGroup, createChatSession } from '@/api/message'
import { formatTimeAgo } from '@/utils/format'

const router = useRouter()
const activeTab = ref('private')
const searchText = ref('')
const searchUserText = ref('')
const loading = ref(false)
const privateSessions = ref([])
const groupSessions = ref([])
const currentSessionId = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const defaultGroupAvatar = 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png'
const groupDialogVisible = ref(false)
const newChatDialogVisible = ref(false)
const submitLoading = ref(false)
const searchingUsers = ref(false)
const userSearchResult = ref([])

// 群组表单
const groupFormRef = ref(null)
const groupForm = ref({
  name: '',
  description: ''
})
const groupRules = {
  name: [
    { required: true, message: '请输入群组名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 过滤后的会话列表
const filteredPrivateSessions = computed(() => {
  if (!searchText.value) return privateSessions.value
  const keyword = searchText.value.toLowerCase()
  return privateSessions.value.filter(session => 
    session.name.toLowerCase().includes(keyword) || 
    session.lastMessage.toLowerCase().includes(keyword)
  )
})

const filteredGroupSessions = computed(() => {
  if (!searchText.value) return groupSessions.value
  const keyword = searchText.value.toLowerCase()
  return groupSessions.value.filter(group => 
    group.name.toLowerCase().includes(keyword) || 
    group.lastMessage.toLowerCase().includes(keyword)
  )
})

// 轮询获取会话列表的定时器
let pollTimer = null

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  return formatTimeAgo(new Date(timestamp))
}

// 加载会话列表
const loadSessions = async () => {
  loading.value = true
  try {
    const res = await getChatSessionList()
    if (res && res.data) {
      // 分离私聊和群聊
      privateSessions.value = res.data.filter(session => session.type === 'private')
      
      // 按最后消息时间排序
      privateSessions.value.sort((a, b) => new Date(b.lastMessageTime) - new Date(a.lastMessageTime))
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
    ElMessage.error('获取会话列表失败')
  } finally {
    loading.value = false
  }
}

// 加载群组列表
const loadGroups = async () => {
  loading.value = true
  try {
    const res = await getChatGroupList()
    if (res && res.data) {
      groupSessions.value = res.data
      // 按最后消息时间排序
      groupSessions.value.sort((a, b) => new Date(b.lastMessageTime) - new Date(a.lastMessageTime))
    }
  } catch (error) {
    console.error('获取群组列表失败:', error)
    ElMessage.error('获取群组列表失败')
  } finally {
    loading.value = false
  }
}

// 过滤会话
const filterSessions = () => {
  // 已经通过计算属性实现
}

// 标签页切换
const handleTabClick = (tab) => {
  searchText.value = ''
  if (tab.props.name === 'group' && groupSessions.value.length === 0) {
    loadGroups()
  }
}

// 打开聊天会话
const openSession = (session) => {
  router.push(`/message/chat/${session.id}`)
}

// 打开群组会话
const openGroupSession = (group) => {
  router.push(`/message/group/${group.id}`)
}

// 创建群组
const createGroup = () => {
  groupForm.value = {
    name: '',
    description: ''
  }
  groupDialogVisible.value = true
}

// 提交创建群组表单
const submitGroupForm = async () => {
  if (!groupFormRef.value) return
  
  await groupFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const res = await createChatGroup(groupForm.value)
      ElMessage.success('群组创建成功')
      groupDialogVisible.value = false
      
      // 更新群组列表
      await loadGroups()
      
      // 打开新创建的群组
      if (res && res.data) {
        router.push(`/message/group/${res.data.id}`)
      }
    } catch (error) {
      console.error('创建群组失败:', error)
      ElMessage.error('创建群组失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 查找群组
const findGroups = () => {
  router.push('/message/groups')
}

// 发起新的聊天
const startNewChat = () => {
  newChatDialogVisible.value = true
  searchUserText.value = ''
  userSearchResult.value = []
}

// 搜索用户
const searchUsers = async () => {
  if (!searchUserText.value || searchUserText.value.length < 2) {
    userSearchResult.value = []
    return
  }
  
  searchingUsers.value = true
  try {
    // 模拟API调用，实际项目中替换为真实API
    // const res = await searchUserForChat(searchUserText.value)
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    userSearchResult.value = [
      { id: 1, name: '张三', type: 'student', avatar: '' },
      { id: 2, name: '李四', type: 'student', avatar: '' },
      { id: 3, name: '王五企业', type: 'enterprise', avatar: '' }
    ].filter(user => user.name.includes(searchUserText.value))
  } catch (error) {
    console.error('搜索用户失败:', error)
    ElMessage.error('搜索用户失败')
  } finally {
    searchingUsers.value = false
  }
}

// 与用户开始聊天
const startChatWithUser = async (user) => {
  try {
    // 创建新会话
    const res = await createChatSession({
      targetId: user.id,
      targetType: user.type
    })
    
    newChatDialogVisible.value = false
    
    // 加载会话列表后跳转
    await loadSessions()
    
    if (res && res.data) {
      router.push(`/message/chat/${res.data.id}`)
    }
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error('创建会话失败')
  }
}

// 组件挂载时加载会话列表
onMounted(() => {
  loadSessions()
  
  // 设置轮询，定期更新会话列表
  pollTimer = setInterval(() => {
    loadSessions()
    if (activeTab.value === 'group') {
      loadGroups()
    }
  }, 30000) // 30秒更新一次
})

// 组件卸载时清除轮询
onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
  }
})
</script>

<style lang="scss" scoped>
.message-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
    
    h2 {
      margin: 0 0 15px 0;
      font-weight: bold;
    }
  }
  
  .message-tabs {
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    
    :deep(.el-tabs__header) {
      margin-bottom: 0;
      padding: 0 20px;
    }
    
    :deep(.el-tabs__content) {
      padding: 20px;
    }
  }
  
  .search-bar {
    margin-bottom: 20px;
    display: flex;
    
    .el-input {
      flex: 1;
      margin-right: 10px;
    }
    
    .create-group-btn {
      white-space: nowrap;
    }
  }
  
  .loading-container {
    padding: 20px 0;
  }
  
  .empty-container {
    padding: 30px 0;
    text-align: center;
    
    .empty-actions {
      margin-top: 20px;
      display: flex;
      justify-content: center;
      gap: 10px;
    }
  }
  
  .session-list {
    .session-item {
      display: flex;
      padding: 15px;
      border-radius: 8px;
      margin-bottom: 10px;
      cursor: pointer;
      transition: background-color 0.3s;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      &.session-active {
        background-color: #ecf5ff;
      }
      
      .session-avatar {
        position: relative;
        margin-right: 15px;
        
        .unread-badge {
          position: absolute;
          top: -5px;
          right: -5px;
        }
      }
      
      .session-info {
        flex: 1;
        min-width: 0; // 确保子元素可以正确溢出
        
        .session-header {
          display: flex;
          justify-content: space-between;
          margin-bottom: 5px;
          
          .session-name {
            font-weight: 500;
            font-size: 16px;
            color: #303133;
          }
          
          .session-time {
            color: #909399;
            font-size: 12px;
            white-space: nowrap;
          }
        }
        
        .session-message {
          color: #606266;
          font-size: 14px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin-bottom: 5px;
          
          .sender-name {
            color: #409eff;
            font-weight: 500;
          }
        }
        
        .group-info {
          font-size: 12px;
          color: #909399;
          display: flex;
          align-items: center;
          
          .el-icon {
            margin-right: 5px;
            font-size: 14px;
          }
        }
      }
    }
  }
  
  .user-list {
    margin-top: 20px;
    max-height: 300px;
    overflow-y: auto;
    
    .user-item {
      display: flex;
      align-items: center;
      padding: 10px;
      border-radius: 4px;
      cursor: pointer;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      .el-avatar {
        margin-right: 15px;
      }
      
      .user-info {
        .user-name {
          font-size: 16px;
          margin-bottom: 5px;
        }
        
        .user-type {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
  
  .searching-users {
    margin-top: 20px;
  }
  
  .empty-search-result {
    margin-top: 20px;
    text-align: center;
    padding: 20px 0;
  }
}
</style> 