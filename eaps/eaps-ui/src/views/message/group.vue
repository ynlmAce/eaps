<template>
  <div class="group-chat-container">
    <div class="chat-header">
      <div class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="chat-info">
        <div class="chat-name">{{ groupInfo.name || '群聊' }}</div>
        <div class="member-count">{{ groupInfo.memberCount || 0 }}人</div>
      </div>
      <div class="chat-actions">
        <el-dropdown trigger="click">
          <span class="el-dropdown-link">
            <el-icon><MoreFilled /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="viewGroupInfo">群组信息</el-dropdown-item>
              <el-dropdown-item @click="viewMembers">成员列表</el-dropdown-item>
              <el-dropdown-item @click="clearHistory">清空聊天记录</el-dropdown-item>
              <el-dropdown-item v-if="isGroupOwner" @click="manageGroup">管理群组</el-dropdown-item>
              <el-dropdown-item v-if="!isGroupOwner" @click="quitGroup" class="danger-item">退出群组</el-dropdown-item>
              <el-dropdown-item v-if="isGroupOwner" @click="dismissGroup" class="danger-item">解散群组</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <div class="chat-main" ref="chatMainRef">
      <div v-if="loading" class="loading-container">
        <el-skeleton animated :rows="3" />
        <el-skeleton animated :rows="2" style="margin-left: auto; margin-top: 20px;" />
        <el-skeleton animated :rows="4" style="margin-top: 20px;" />
      </div>
      
      <div v-else-if="messages.length === 0" class="empty-container">
        <el-empty description="暂无消息" />
      </div>
      
      <div v-else class="message-list">
        <div v-for="(message, index) in messages" :key="message.id" class="message-item">
          <!-- 日期分隔线 -->
          <div v-if="showDateDivider(message, index)" class="date-divider">
            {{ formatDate(message.sendTime) }}
          </div>
          
          <!-- 消息气泡 -->
          <div 
            class="message-bubble" 
            :class="{ 
              'message-self': message.senderId === currentUserId,
              'message-other': message.senderId !== currentUserId 
            }"
          >
            <!-- 头像 (只在对方的消息显示) -->
            <div v-if="message.senderId !== currentUserId" class="message-avatar">
              <el-avatar :size="40" :src="message.senderAvatar">
                {{ message.senderName ? message.senderName.substr(0, 1) : 'U' }}
              </el-avatar>
            </div>
            
            <div class="message-content">
              <!-- 发送者名称 (只在对方的消息显示) -->
              <div v-if="message.senderId !== currentUserId" class="sender-name">
                {{ message.senderName }}
              </div>
              
              <div class="message-text" :class="'message-type-' + message.contentType">
                <!-- 根据消息类型展示不同内容 -->
                <template v-if="message.contentType === 'text'">
                  {{ message.content }}
                </template>
                <template v-else-if="message.contentType === 'image'">
                  <el-image 
                    :src="message.content" 
                    :preview-src-list="[message.content]"
                    fit="cover"
                    class="message-image"
                    loading="lazy"
                  />
                </template>
                <template v-else-if="message.contentType === 'file'">
                  <div class="file-message" @click="downloadFile(message.content, message.fileName)">
                    <el-icon><Document /></el-icon>
                    <div class="file-info">
                      <div class="file-name">{{ message.fileName }}</div>
                      <div class="file-size">{{ formatFileSize(message.fileSize) }}</div>
                    </div>
                    <el-icon><Download /></el-icon>
                  </div>
                </template>
              </div>
              <div class="message-time">
                {{ formatMessageTime(message.sendTime) }}
                <el-icon v-if="message.senderId === currentUserId" class="message-status">
                  <component :is="getMessageStatusIcon(message.status)" />
                </el-icon>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 加载更多按钮 -->
        <div v-if="hasMoreMessages" class="load-more">
          <el-button link type="primary" @click="loadMoreMessages" :loading="loadingMore">
            加载更多
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="chat-footer">
      <div class="message-tools">
        <el-tooltip content="发送图片" placement="top">
          <el-icon class="tool-icon" @click="openImageUpload"><Picture /></el-icon>
        </el-tooltip>
        <el-tooltip content="发送文件" placement="top">
          <el-icon class="tool-icon" @click="openFileUpload"><FolderAdd /></el-icon>
        </el-tooltip>
      </div>
      
      <div class="message-input">
        <el-input
          v-model="messageText"
          type="textarea"
          :rows="2"
          placeholder="输入消息..."
          resize="none"
          @keydown.enter.prevent="sendMessage"
        />
      </div>
      
      <div class="message-send">
        <el-button type="primary" @click="sendMessage" :loading="sending">发送</el-button>
      </div>
    </div>
    
    <!-- 文件上传隐藏的input -->
    <input
      ref="fileInputRef"
      type="file"
      class="hidden-input"
      @change="handleFileSelected"
      accept="application/pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.rar"
    />
    <!-- 图片上传隐藏的input -->
    <input
      ref="imageInputRef"
      type="file"
      class="hidden-input"
      @change="handleImageSelected"
      accept="image/jpeg,image/png,image/gif,image/webp"
    />
    
    <!-- 群组成员对话框 -->
    <el-dialog
      v-model="membersDialogVisible"
      title="群组成员"
      width="500px"
    >
      <el-input
        v-model="memberSearchText"
        placeholder="搜索成员"
        prefix-icon="Search"
        clearable
        style="margin-bottom: 15px;"
      />
      
      <div v-if="loadingMembers" class="loading-members">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="filteredMembers.length === 0" class="empty-members">
        <el-empty description="暂无成员" />
      </div>
      
      <div v-else class="member-list">
        <div
          v-for="member in filteredMembers"
          :key="member.id"
          class="member-item"
        >
          <el-avatar :size="36" :src="member.avatar">
            {{ member.name ? member.name.substr(0, 1) : 'U' }}
          </el-avatar>
          <div class="member-info">
            <div class="member-name">{{ member.name }}</div>
            <div class="member-role">
              <el-tag v-if="member.isOwner" size="small" type="danger">群主</el-tag>
              <el-tag v-else-if="member.isAdmin" size="small" type="warning">管理员</el-tag>
              <el-tag v-else size="small" type="info">成员</el-tag>
            </div>
          </div>
          <div v-if="isGroupOwner && !member.isOwner" class="member-actions">
            <el-dropdown trigger="click">
              <el-icon class="more-icon"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="setAsAdmin(member)" v-if="!member.isAdmin">设为管理员</el-dropdown-item>
                  <el-dropdown-item @click="removeAdmin(member)" v-if="member.isAdmin">取消管理员</el-dropdown-item>
                  <el-dropdown-item @click="removeMember(member)" class="danger-item">移出群组</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 群组信息对话框 -->
    <el-dialog
      v-model="groupInfoDialogVisible"
      title="群组信息"
      width="500px"
    >
      <div class="group-info-container">
        <div class="group-avatar">
          <el-avatar :size="80" :src="groupInfo.avatar">
            {{ groupInfo.name ? groupInfo.name.substr(0, 1) : 'G' }}
          </el-avatar>
        </div>
        <div class="group-details">
          <div class="group-name">{{ groupInfo.name }}</div>
          <div class="group-id">群号：{{ groupInfo.id }}</div>
          <div class="group-member-count">成员数：{{ groupInfo.memberCount }}人</div>
          <div class="group-create-time">创建时间：{{ formatDateTime(groupInfo.createTime) }}</div>
        </div>
      </div>
      
      <el-divider />
      
      <div class="group-description">
        <div class="description-title">群组简介</div>
        <div class="description-content">{{ groupInfo.description || '暂无简介' }}</div>
      </div>
      
      <el-divider />
      
      <div class="group-announcement" v-if="groupInfo.announcement">
        <div class="announcement-title">群公告</div>
        <div class="announcement-content">{{ groupInfo.announcement }}</div>
        <div class="announcement-time">{{ formatDateTime(groupInfo.announcementTime) }}</div>
      </div>
      
      <div v-if="isGroupOwner || isGroupAdmin" class="group-admin-actions">
        <el-button type="primary" @click="editGroupInfo">编辑群组信息</el-button>
        <el-button v-if="isGroupOwner" type="danger" @click="dismissGroup">解散群组</el-button>
        <el-button v-if="!isGroupOwner" type="danger" @click="quitGroup">退出群组</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, MoreFilled, Picture, FolderAdd, Download, Document, 
  CheckCircle, CircleCheck, WarningFilled, User, Search 
} from '@element-plus/icons-vue'
import { 
  getChatGroupDetail,
  getChatGroupMessages,
  sendGroupMessage,
  uploadGroupAttachment,
  markGroupMessagesAsRead,
  getGroupMembers,
  quitChatGroup,
  updateGroupMemberRole,
  removeGroupMember,
  dismissGroup as apiDismissGroup,
  updateGroupAnnouncement
} from '@/api/message'
import { formatTimeAgo, formatDateTime, formatFileSize } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const groupId = computed(() => route.params.id)
const chatMainRef = ref(null)
const fileInputRef = ref(null)
const imageInputRef = ref(null)
const messages = ref([])
const messageText = ref('')
const loading = ref(false)
const loadingMore = ref(false)
const sending = ref(false)
const hasMoreMessages = ref(false)
const membersDialogVisible = ref(false)
const groupInfoDialogVisible = ref(false)
const loadingMembers = ref(false)
const memberSearchText = ref('')
const members = ref([])
const pageSize = 20
const currentPage = ref(1)
const groupInfo = ref({
  id: '',
  name: '',
  avatar: '',
  description: '',
  memberCount: 0,
  announcement: '',
  announcementTime: '',
  createTime: '',
  ownerId: ''
})

// 当前用户ID，实际项目中会从store或其他地方获取
const currentUserId = 1 // 模拟当前用户ID

// 判断是否是群主
const isGroupOwner = computed(() => {
  return groupInfo.value.ownerId === currentUserId
})

// 判断是否是管理员
const isGroupAdmin = computed(() => {
  const currentMember = members.value.find(m => m.id === currentUserId)
  return currentMember && currentMember.isAdmin
})

// 过滤后的成员列表
const filteredMembers = computed(() => {
  if (!memberSearchText.value) return members.value
  const keyword = memberSearchText.value.toLowerCase()
  return members.value.filter(member => 
    member.name.toLowerCase().includes(keyword)
  )
})

// 格式化消息时间
const formatMessageTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  
  if (date.toDateString() === today.toDateString()) {
    return '今天'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  } else {
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
  }
}

// 判断是否显示日期分隔线
const showDateDivider = (message, index) => {
  if (index === 0) return true
  
  const currentDate = new Date(message.sendTime).toDateString()
  const prevDate = new Date(messages.value[index - 1].sendTime).toDateString()
  
  return currentDate !== prevDate
}

// 获取消息状态图标
const getMessageStatusIcon = (status) => {
  switch (status) {
    case 'sent': return CheckCircle
    case 'delivered': return CheckCircle
    case 'read': return CircleCheck
    case 'failed': return WarningFilled
    default: return CheckCircle
  }
}

// 获取群组详情
const fetchGroupInfo = async () => {
  try {
    const res = await getChatGroupDetail(groupId.value)
    if (res && res.data) {
      groupInfo.value = res.data
    }
  } catch (error) {
    console.error('获取群组信息失败:', error)
    ElMessage.error('获取群组信息失败')
  }
}

// 获取群组消息
const fetchMessages = async (page = 1, refresh = false) => {
  if (page === 1) {
    loading.value = true
  } else {
    loadingMore.value = true
  }
  
  try {
    const res = await getChatGroupMessages(groupId.value, {
      pageNum: page,
      pageSize
    })
    
    if (res && res.data) {
      if (refresh) {
        messages.value = res.data.records || []
      } else {
        // 追加到前面，因为消息是按时间倒序返回的
        messages.value = [...res.data.records, ...messages.value]
      }
      
      // 更新分页信息
      currentPage.value = page
      hasMoreMessages.value = res.data.total > page * pageSize
      
      // 如果是第一页且有新消息，标记为已读
      if (page === 1) {
        const unreadMessages = (res.data.records || [])
          .filter(msg => msg.senderId !== currentUserId && msg.status !== 'read')
          .map(msg => msg.id)
        
        if (unreadMessages.length > 0) {
          markGroupMessagesAsRead(groupId.value, unreadMessages).catch(err => {
            console.error('标记已读失败:', err)
          })
        }
      }
    }
  } catch (error) {
    console.error('获取群组消息失败:', error)
    ElMessage.error('获取群组消息失败')
  } finally {
    if (page === 1) {
      loading.value = false
    } else {
      loadingMore.value = false
    }
    
    // 滚动到底部
    if (page === 1) {
      scrollToBottom()
    }
  }
}

// 获取群组成员
const fetchMembers = async () => {
  loadingMembers.value = true
  try {
    const res = await getGroupMembers(groupId.value)
    if (res && res.data) {
      members.value = res.data
    }
  } catch (error) {
    console.error('获取群组成员失败:', error)
    ElMessage.error('获取群组成员失败')
  } finally {
    loadingMembers.value = false
  }
}

// 加载更多消息
const loadMoreMessages = () => {
  fetchMessages(currentPage.value + 1)
}

// 发送消息
const sendMessage = async () => {
  if (!messageText.value.trim()) return
  
  sending.value = true
  try {
    await sendGroupMessage(groupId.value, {
      content: messageText.value.trim(),
      contentType: 'text'
    })
    
    // 本地添加消息
    messages.value.push({
      id: Date.now().toString(), // 临时ID
      senderId: currentUserId,
      senderName: '我', // 实际项目中应该从用户信息中获取
      senderAvatar: '',
      content: messageText.value.trim(),
      contentType: 'text',
      sendTime: new Date().toISOString(),
      status: 'sent'
    })
    
    // 清空输入框
    messageText.value = ''
    
    // 滚动到底部
    scrollToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (chatMainRef.value) {
    chatMainRef.value.scrollTop = chatMainRef.value.scrollHeight
  }
}

// 返回上一页
const goBack = () => {
  router.push('/message')
}

// 查看群组信息
const viewGroupInfo = () => {
  groupInfoDialogVisible.value = true
}

// 查看成员列表
const viewMembers = async () => {
  await fetchMembers()
  membersDialogVisible.value = true
}

// 管理群组
const manageGroup = () => {
  // 实际项目中可以跳转到专门的管理页面
  viewGroupInfo()
}

// 清空聊天记录
const clearHistory = () => {
  ElMessageBox.confirm('确定要清空所有聊天记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 实际项目中应该调用API删除聊天记录
    ElMessage({
      type: 'success',
      message: '聊天记录已清空'
    })
    messages.value = []
  }).catch(() => {})
}

// 退出群组
const quitGroup = () => {
  ElMessageBox.confirm('确定要退出该群组吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await quitChatGroup(groupId.value)
      ElMessage.success('已退出群组')
      router.push('/message')
    } catch (error) {
      console.error('退出群组失败:', error)
      ElMessage.error('退出群组失败')
    }
  }).catch(() => {})
}

// 解散群组
const dismissGroup = () => {
  ElMessageBox.confirm('确定要解散该群组吗？解散后不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await apiDismissGroup(groupId.value)
      ElMessage.success('群组已解散')
      router.push('/message')
    } catch (error) {
      console.error('解散群组失败:', error)
      ElMessage.error('解散群组失败')
    }
  }).catch(() => {})
}

// 编辑群组信息
const editGroupInfo = () => {
  // 实际项目中可以打开新的对话框
  ElMessage.info('功能开发中')
}

// 设置为管理员
const setAsAdmin = async (member) => {
  try {
    await updateGroupMemberRole(groupId.value, member.id, 'admin')
    ElMessage.success(`已将 ${member.name} 设为管理员`)
    await fetchMembers() // 刷新成员列表
  } catch (error) {
    console.error('设置管理员失败:', error)
    ElMessage.error('设置管理员失败')
  }
}

// 取消管理员
const removeAdmin = async (member) => {
  try {
    await updateGroupMemberRole(groupId.value, member.id, 'member')
    ElMessage.success(`已取消 ${member.name} 的管理员权限`)
    await fetchMembers() // 刷新成员列表
  } catch (error) {
    console.error('取消管理员失败:', error)
    ElMessage.error('取消管理员失败')
  }
}

// 移出群组
const removeMember = async (member) => {
  ElMessageBox.confirm(`确定要将 ${member.name} 移出群组吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await removeGroupMember(groupId.value, member.id)
      ElMessage.success(`已将 ${member.name} 移出群组`)
      await fetchMembers() // 刷新成员列表
    } catch (error) {
      console.error('移出群组失败:', error)
      ElMessage.error('移出群组失败')
    }
  }).catch(() => {})
}

// 打开文件上传
const openFileUpload = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
}

// 打开图片上传
const openImageUpload = () => {
  if (imageInputRef.value) {
    imageInputRef.value.click()
  }
}

// 处理文件选择
const handleFileSelected = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 检查文件大小
  if (file.size > 10 * 1024 * 1024) { // 10MB限制
    ElMessage.error('文件大小不能超过10MB')
    return
  }
  
  sending.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await uploadGroupAttachment(groupId.value, formData)
    
    // 本地添加消息
    messages.value.push({
      id: Date.now().toString(), // 临时ID
      senderId: currentUserId,
      senderName: '我', // 实际项目中应该从用户信息中获取
      senderAvatar: '',
      content: res.data.url,
      contentType: 'file',
      fileName: file.name,
      fileSize: file.size,
      sendTime: new Date().toISOString(),
      status: 'sent'
    })
    
    // 滚动到底部
    scrollToBottom()
  } catch (error) {
    console.error('上传文件失败:', error)
    ElMessage.error('上传文件失败')
  } finally {
    sending.value = false
    // 清空文件输入框
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

// 处理图片选择
const handleImageSelected = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 检查文件大小
  if (file.size > 5 * 1024 * 1024) { // 5MB限制
    ElMessage.error('图片大小不能超过5MB')
    return
  }
  
  sending.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await uploadGroupAttachment(groupId.value, formData)
    
    // 本地添加消息
    messages.value.push({
      id: Date.now().toString(), // 临时ID
      senderId: currentUserId,
      senderName: '我', // 实际项目中应该从用户信息中获取
      senderAvatar: '',
      content: res.data.url,
      contentType: 'image',
      sendTime: new Date().toISOString(),
      status: 'sent'
    })
    
    // 滚动到底部
    scrollToBottom()
  } catch (error) {
    console.error('上传图片失败:', error)
    ElMessage.error('上传图片失败')
  } finally {
    sending.value = false
    // 清空图片输入框
    if (imageInputRef.value) {
      imageInputRef.value.value = ''
    }
  }
}

// 下载文件
const downloadFile = (url, fileName) => {
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 监听群组ID变化
watch(() => groupId.value, () => {
  if (groupId.value) {
    fetchGroupInfo()
    fetchMessages(1, true)
  }
}, { immediate: true })

// 组件挂载时初始化
onMounted(() => {
  // 可以初始化WebSocket连接
})

// 组件卸载时清理
onUnmounted(() => {
  // 可以关闭WebSocket连接
})
</script>

<style lang="scss" scoped>
.group-chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  
  .back-button {
    margin-right: 15px;
    cursor: pointer;
    
    .el-icon {
      font-size: 20px;
      color: #606266;
    }
  }
  
  .chat-info {
    flex: 1;
    
    .chat-name {
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
    
    .member-count {
      font-size: 12px;
      color: #909399;
    }
  }
  
  .chat-actions {
    .el-dropdown-link {
      cursor: pointer;
      
      .el-icon {
        font-size: 20px;
        color: #606266;
      }
    }
  }
}

.chat-main {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  
  .loading-container {
    padding: 20px;
  }
  
  .empty-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
  }
  
  .message-list {
    .date-divider {
      text-align: center;
      margin: 15px 0;
      
      &::before {
        content: '';
        display: inline-block;
        width: 40px;
        height: 1px;
        background-color: #dcdfe6;
        margin-right: 10px;
        vertical-align: middle;
      }
      
      &::after {
        content: '';
        display: inline-block;
        width: 40px;
        height: 1px;
        background-color: #dcdfe6;
        margin-left: 10px;
        vertical-align: middle;
      }
      
      font-size: 12px;
      color: #909399;
    }
    
    .message-bubble {
      display: flex;
      margin-bottom: 15px;
      
      &.message-self {
        flex-direction: row-reverse;
        
        .message-content {
          margin-left: 0;
          margin-right: 15px;
          
          .message-text {
            background-color: #ecf5ff;
            border-radius: 10px 2px 10px 10px;
          }
          
          .message-time {
            text-align: right;
          }
        }
      }
      
      &.message-other {
        .message-text {
          background-color: #fff;
          border-radius: 2px 10px 10px 10px;
        }
        
        .sender-name {
          font-size: 12px;
          color: #409eff;
          margin-bottom: 3px;
          font-weight: 500;
        }
      }
      
      .message-avatar {
        flex-shrink: 0;
      }
      
      .message-content {
        max-width: 70%;
        margin-left: 15px;
        
        .message-text {
          padding: 10px 15px;
          border-radius: 4px;
          word-break: break-word;
          box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
          
          &.message-type-image {
            padding: 3px;
            
            .message-image {
              max-width: 100%;
              max-height: 200px;
              border-radius: 4px;
            }
          }
          
          &.message-type-file {
            padding: 10px;
            
            .file-message {
              display: flex;
              align-items: center;
              cursor: pointer;
              
              .el-icon {
                font-size: 24px;
                margin-right: 10px;
                
                &:last-child {
                  margin-right: 0;
                  margin-left: 10px;
                }
              }
              
              .file-info {
                flex: 1;
                
                .file-name {
                  font-weight: 500;
                  margin-bottom: 3px;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  max-width: 200px;
                }
                
                .file-size {
                  font-size: 12px;
                  color: #909399;
                }
              }
            }
          }
        }
        
        .message-time {
          font-size: 12px;
          color: #909399;
          margin-top: 5px;
          
          .message-status {
            font-size: 14px;
            margin-left: 5px;
            vertical-align: middle;
          }
        }
      }
    }
    
    .load-more {
      text-align: center;
      margin-bottom: 20px;
    }
  }
}

.chat-footer {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background-color: #fff;
  box-shadow: 0 -1px 4px rgba(0, 0, 0, 0.1);
  
  .message-tools {
    display: flex;
    margin-right: 15px;
    
    .tool-icon {
      font-size: 20px;
      color: #606266;
      margin-right: 15px;
      cursor: pointer;
      
      &:hover {
        color: #409eff;
      }
    }
  }
  
  .message-input {
    flex: 1;
    
    :deep(.el-textarea__inner) {
      resize: none;
      border-radius: 20px;
      padding: 8px 15px;
    }
  }
  
  .message-send {
    margin-left: 15px;
    
    .el-button {
      border-radius: 20px;
    }
  }
}

.hidden-input {
  display: none;
}

.member-list {
  max-height: 400px;
  overflow-y: auto;
  
  .member-item {
    display: flex;
    align-items: center;
    padding: 10px;
    border-radius: 4px;
    
    &:hover {
      background-color: #f5f7fa;
    }
    
    .el-avatar {
      margin-right: 15px;
    }
    
    .member-info {
      flex: 1;
      
      .member-name {
        font-size: 14px;
        margin-bottom: 5px;
      }
      
      .member-role {
        display: flex;
      }
    }
    
    .member-actions {
      .more-icon {
        font-size: 18px;
        color: #909399;
        cursor: pointer;
        
        &:hover {
          color: #409eff;
        }
      }
    }
  }
}

.group-info-container {
  display: flex;
  margin-bottom: 20px;
  
  .group-avatar {
    margin-right: 20px;
  }
  
  .group-details {
    .group-name {
      font-size: 18px;
      font-weight: bold;
      margin-bottom: 8px;
    }
    
    .group-id, .group-member-count, .group-create-time {
      font-size: 14px;
      color: #606266;
      margin-bottom: 5px;
    }
  }
}

.group-description, .group-announcement {
  margin-bottom: 20px;
  
  .description-title, .announcement-title {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 10px;
  }
  
  .description-content, .announcement-content {
    font-size: 14px;
    line-height: 1.6;
    color: #303133;
    background-color: #f5f7fa;
    padding: 15px;
    border-radius: 4px;
    white-space: pre-wrap;
  }
  
  .announcement-time {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
    text-align: right;
  }
}

.group-admin-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.danger-item {
  color: #f56c6c;
}

.loading-members, .empty-members {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .group-chat-container {
    height: calc(100vh - 60px);
  }
  
  .chat-main {
    .message-list {
      .message-bubble {
        .message-content {
          max-width: 85%;
        }
      }
    }
  }
  
  .chat-footer {
    padding: 10px;
    
    .message-tools {
      .tool-icon {
        font-size: 18px;
        margin-right: 10px;
      }
    }
    
    .message-send {
      .el-button {
        padding: 8px 15px;
      }
    }
  }
  
  .group-info-container {
    flex-direction: column;
    align-items: center;
    
    .group-avatar {
      margin-right: 0;
      margin-bottom: 15px;
    }
    
    .group-details {
      text-align: center;
    }
  }
}
</style> 