import request from '@/api/request'

/**
 * 获取企业发布的职位列表
 * @param {Object} params - 查询参数
 * @returns {Promise} - 职位列表结果
 */
export function getPositionList(params) {
  return request({
    url: '/recruit/position/list',
    method: 'get',
    params
  })
}

/**
 * 获取职位详情
 * @param {number} id - 职位ID
 * @returns {Promise} - 职位详情结果
 */
export function getPositionDetail(id) {
  return request({
    url: `/recruit/position/${id}`,
    method: 'get'
  })
}

/**
 * 添加职位
 * @param {Object} data - 职位数据
 * @returns {Promise} - 添加结果
 */
export function addPosition(data) {
  return request({
    url: '/recruit/position',
    method: 'post',
    data
  })
}

/**
 * 更新职位信息
 * @param {number} id - 职位ID
 * @param {Object} data - 更新的职位数据
 * @returns {Promise} - 更新结果
 */
export function updatePosition(id, data) {
  return request({
    url: `/recruit/position/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除职位
 * @param {number} id - 职位ID
 * @returns {Promise} - 删除结果
 */
export function deletePosition(id) {
  return request({
    url: `/recruit/position/${id}`,
    method: 'delete'
  })
}

/**
 * 更改职位状态（开放/关闭）
 * @param {number} id - 职位ID
 * @param {string} status - 新状态
 * @returns {Promise} - 更新结果
 */
export function updatePositionStatus(id, status) {
  return request({
    url: `/recruit/position/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 获取申请列表
 * @param {Object} params - 查询参数
 * @returns {Promise} - 申请列表结果
 */
export function getApplicationList(params) {
  return request({
    url: '/recruit/application/list',
    method: 'get',
    params
  })
}

/**
 * 获取申请详情
 * @param {number} id - 申请ID
 * @returns {Promise} - 申请详情结果
 */
export function getApplicationDetail(id) {
  return request({
    url: `/recruit/application/${id}`,
    method: 'get'
  })
}

/**
 * 更新申请状态
 * @param {Object} data - 更新数据，包含id, status等
 * @returns {Promise} - 更新结果
 */
export function updateApplicationStatus(data) {
  return request({
    url: '/recruit/application/status',
    method: 'put',
    data
  })
}

/**
 * 批量更新申请状态
 * @param {Object} data - 更新数据，包含ids数组, status等
 * @returns {Promise} - 更新结果
 */
export function batchUpdateApplicationStatus(data) {
  return request({
    url: '/recruit/application/batch-status',
    method: 'put',
    data
  })
}

/**
 * 下载简历文件
 * @param {string} fileId - 简历文件ID
 * @returns {Promise} - 文件下载结果
 */
export function downloadResumeFile(fileId) {
  return request({
    url: `/recruit/application/resume/${fileId}`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 获取企业统计数据
 * @returns {Promise} - 统计数据结果
 */
export function getRecruitStatistics() {
  return request({
    url: '/recruit/statistics',
    method: 'get'
  })
}

/**
 * 邀请面试
 * @param {Object} data - 面试数据，包含申请ID、面试时间、地点等
 * @returns {Promise} - 邀请结果
 */
export function inviteInterview(data) {
  return request({
    url: '/recruit/application/interview',
    method: 'post',
    data
  })
}

/**
 * 获取企业所有职位选项（下拉框使用）
 * @returns {Promise} - 职位选项结果
 */
export function getPositionOptions() {
  return request({
    url: '/recruit/position/options',
    method: 'get'
  })
} 