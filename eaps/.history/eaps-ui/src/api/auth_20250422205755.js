import request from './request'

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 * @param {Object} data - 注册信息
 * @returns {Promise}
 */
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

/**
 * 修改密码
 * @param {Object} data - 修改密码信息
 * @returns {Promise}
 */
export function changePassword(data) {
  return request({
    url: '/auth/password',
    method: 'put',
    data
  })
}

/**
 * 重置密码
 * @param {Object} data - 重置密码信息
 * @returns {Promise}
 */
export function resetPassword(data) {
  return request({
    url: '/auth/password/reset',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 获取密保问题
 * @param {string} username - 用户名
 * @returns {Promise}
 */
export function getSecurityQuestion(username) {
  return request({
    url: `/auth/security-question/${username}`,
    method: 'get'
  })
}

/**
 * 验证密保答案
 * @param {Object} data - 验证信息
 * @returns {Promise}
 */
export function verifySecurityAnswer(data) {
  return request({
    url: '/auth/verify-security-answer',
    method: 'post',
    data
  })
}

/**
 * 找回密码
 * @param {Object} data - 找回密码信息
 * @returns {Promise}
 */
export function forgotPassword(data) {
  return request({
    url: '/auth/forgot-password',
    method: 'post',
    data
  })
} 