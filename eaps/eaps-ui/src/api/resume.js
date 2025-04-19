import request from '@/utils/request'

/**
 * 获取我的简历列表
 * @returns {Promise} 简历列表数据
 */
export function getResumeList() {
  return request({
    url: '/api/resume/list',
    method: 'get'
  })
}

/**
 * 获取简历详情
 * @param {string} id 简历ID
 * @returns {Promise} 简历详情
 */
export function getResumeDetail(id) {
  return request({
    url: `/api/resume/${id}`,
    method: 'get'
  })
}

/**
 * 删除简历
 * @param {string} id 简历ID
 * @returns {Promise}
 */
export function deleteResume(id) {
  return request({
    url: `/api/resume/${id}`,
    method: 'delete'
  })
}

/**
 * 设置默认简历
 * @param {string} id 简历ID
 * @returns {Promise}
 */
export function setDefaultResume(id) {
  return request({
    url: `/api/resume/${id}/default`,
    method: 'put'
  })
}

/**
 * 重命名简历
 * @param {string} id 简历ID
 * @param {Object} data 包含name字段的对象
 * @returns {Promise}
 */
export function renameResume(id, data) {
  return request({
    url: `/api/resume/${id}/rename`,
    method: 'put',
    data
  })
}

/**
 * 上传简历文件
 * @param {FormData} data 包含文件和相关信息的FormData
 * @returns {Promise}
 */
export function uploadResumeFile(data) {
  return request({
    url: '/api/resume/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 创建在线简历
 * @param {Object} data 简历数据
 * @returns {Promise}
 */
export function createOnlineResume(data) {
  return request({
    url: '/api/resume/create',
    method: 'post',
    data
  })
}

/**
 * 更新在线简历
 * @param {string} id 简历ID
 * @param {Object} data 简历数据
 * @returns {Promise}
 */
export function updateOnlineResume(id, data) {
  return request({
    url: `/api/resume/${id}`,
    method: 'put',
    data
  })
}

/**
 * 下载简历文件
 * @param {string} id 简历ID
 * @returns {Promise} 简历文件的blob对象
 */
export function downloadResume(id) {
  return request({
    url: `/api/resume/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 获取默认简历
 * @returns {Promise} 默认简历信息
 */
export function getDefaultResume() {
  return request({
    url: '/api/resume/default',
    method: 'get'
  })
} 