import request from '@/utils/request'

// 用户管理相关接口
export function getUserList(params) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params
  })
}

export function getUserDetail(userId) {
  return request({
    url: `/system/user/${userId}`,
    method: 'get'
  })
}

export function addUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

export function updateUser(data) {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

export function deleteUser(userId) {
  return request({
    url: `/system/user/${userId}`,
    method: 'delete'
  })
}

export function resetUserPwd(userId, password) {
  return request({
    url: `/system/user/${userId}/password`,
    method: 'put',
    data: { password }
  })
}

export function changeUserStatus(userId, status) {
  return request({
    url: `/system/user/${userId}/status`,
    method: 'put',
    data: { status }
  })
}

// 角色管理相关接口
export function getRoleList(params) {
  return request({
    url: '/system/role/list',
    method: 'get',
    params
  })
}

export function getRoleDetail(roleId) {
  return request({
    url: `/system/role/${roleId}`,
    method: 'get'
  })
}

export function addRole(data) {
  return request({
    url: '/system/role',
    method: 'post',
    data
  })
}

export function updateRole(data) {
  return request({
    url: '/system/role',
    method: 'put',
    data
  })
}

export function deleteRole(roleId) {
  return request({
    url: `/system/role/${roleId}`,
    method: 'delete'
  })
}

export function getRoleUsers(roleId, params) {
  return request({
    url: `/system/role/${roleId}/users`,
    method: 'get',
    params
  })
}

export function assignPermission(roleId, permissionIds) {
  return request({
    url: `/system/role/${roleId}/permission`,
    method: 'put',
    data: { permissionIds }
  })
}

// 权限管理相关接口
export function getPermissionList() {
  return request({
    url: '/system/permission/list',
    method: 'get'
  })
}

export function getPermissionDetail(permissionId) {
  return request({
    url: `/system/permission/${permissionId}`,
    method: 'get'
  })
}

export function addPermission(data) {
  return request({
    url: '/system/permission',
    method: 'post',
    data
  })
}

export function updatePermission(data) {
  return request({
    url: '/system/permission',
    method: 'put',
    data
  })
}

export function deletePermission(permissionId) {
  return request({
    url: `/system/permission/${permissionId}`,
    method: 'delete'
  })
}

// 系统配置相关接口
export function getConfigList(params) {
  return request({
    url: '/system/config/list',
    method: 'get',
    params
  })
}

export function getConfigDetail(configId) {
  return request({
    url: `/system/config/${configId}`,
    method: 'get'
  })
}

export function addConfig(data) {
  return request({
    url: '/system/config',
    method: 'post',
    data
  })
}

export function updateConfig(data) {
  return request({
    url: '/system/config',
    method: 'put',
    data
  })
}

export function deleteConfig(configId) {
  return request({
    url: `/system/config/${configId}`,
    method: 'delete'
  })
}

export function getConfigByKey(key) {
  return request({
    url: `/system/config/key/${key}`,
    method: 'get'
  })
}

export function exportConfig(params) {
  return request({
    url: '/system/config/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importConfig(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/system/config/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 操作日志相关接口
export function getLogList(params) {
  return request({
    url: '/system/log/list',
    method: 'get',
    params
  })
}

export function getLogDetail(logId) {
  return request({
    url: `/system/log/${logId}`,
    method: 'get'
  })
}

export function clearLogs() {
  return request({
    url: '/system/log/clear',
    method: 'delete'
  })
}

export function exportLogs(params) {
  return request({
    url: '/system/log/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
} 