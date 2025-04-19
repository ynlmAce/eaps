import request from './request'

// 获取聊天会话列表
export function getChatSessionList() {
  return request({
    url: '/chat/sessions',
    method: 'get'
  })
}

// 获取聊天会话详情
export function getChatSession(sessionId) {
  return request({
    url: `/chat/session/${sessionId}`,
    method: 'get'
  })
}

// 创建聊天会话
export function createChatSession(data) {
  return request({
    url: '/chat/session',
    method: 'post',
    data
  })
}

// 获取会话消息列表
export function getChatMessages(sessionId, params) {
  return request({
    url: `/chat/session/${sessionId}/messages`,
    method: 'get',
    params
  })
}

// 发送消息
export function sendChatMessage(sessionId, data) {
  return request({
    url: `/chat/session/${sessionId}/message`,
    method: 'post',
    data
  })
}

// 上传消息附件
export function uploadChatAttachment(sessionId, data) {
  return request({
    url: `/chat/session/${sessionId}/attachment`,
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

// 获取聊天群组列表
export function getChatGroupList() {
  return request({
    url: '/chat/groups',
    method: 'get'
  })
}

// 获取群组详情
export function getChatGroupDetail(groupId) {
  return request({
    url: `/chat/group/${groupId}`,
    method: 'get'
  })
}

// 创建群组
export function createChatGroup(data) {
  return request({
    url: '/chat/group',
    method: 'post',
    data
  })
}

// 加入群组
export function joinChatGroup(groupId) {
  return request({
    url: `/chat/group/${groupId}/join`,
    method: 'post'
  })
}

// 申请加入群组
export function applyJoinChatGroup(groupId, data) {
  return request({
    url: `/chat/group/${groupId}/apply`,
    method: 'post',
    data
  })
}

// 处理加入群组申请
export function handleJoinApplication(groupId, userId, data) {
  return request({
    url: `/chat/group/${groupId}/application/${userId}`,
    method: 'put',
    data
  })
}

// 退出群组
export function quitChatGroup(groupId) {
  return request({
    url: `/chat/group/${groupId}/quit`,
    method: 'post'
  })
}

// 获取未读消息数量
export function getUnreadMessageCount() {
  return request({
    url: '/chat/unread-count',
    method: 'get'
  })
}

// 标记消息为已读
export function markMessagesAsRead(sessionId, messageIds) {
  return request({
    url: `/chat/session/${sessionId}/read`,
    method: 'put',
    data: { messageIds }
  })
} 