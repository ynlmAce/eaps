import request from './request'

export function getInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 用户登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 更新用户基本信息
export function updateUserInfo(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

// 更新用户密码
export function updatePassword(data) {
  return request({
    url: '/user/update-password',
    method: 'put',
    data
  })
}

// 上传用户头像
export function uploadAvatar(data) {
  return request({
    url: '/user/upload-avatar',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

// 注销登录
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

export function getCurrentUserInfo() {
  return request({
    url: '/user/current',
    method: 'get'
  })
}

export function getUserList(query) {
  return request({
    url: '/user/list',
    method: 'get',
    params: query
  })
}

export function getUserDetail(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

export function addUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

export function updateUser(data) {
  return request({
    url: `/user/${data.id}`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
} 