import request from '@/utils/request'

/**
 * 获取企业发布的职位列表
 * @param {Object} params - 查询参数
 * @returns {Promise} - 职位列表数据
 */
export function getEnterprisePositions(params) {
  return request({
    url: '/api/recruit/position/list',
    method: 'get',
    params
  })
}

/**
 * 获取职位详情
 * @param {string|number} positionId - 职位ID
 * @returns {Promise} - 职位详情数据
 */
export function getPositionDetail(positionId) {
  return request({
    url: `/api/recruit/position/${positionId}`,
    method: 'get'
  })
}

/**
 * 创建新职位
 * @param {Object} data - 职位数据
 * @returns {Promise} - 创建结果
 */
export function createPosition(data) {
  return request({
    url: '/api/recruit/position',
    method: 'post',
    data
  })
}

/**
 * 更新职位信息
 * @param {string|number} positionId - 职位ID
 * @param {Object} data - 更新的职位数据
 * @returns {Promise} - 更新结果
 */
export function updatePosition(positionId, data) {
  return request({
    url: `/api/recruit/position/${positionId}`,
    method: 'put',
    data
  })
}

/**
 * 保存职位草稿
 * @param {Object} data - 职位草稿数据
 * @returns {Promise} - 保存结果
 */
export function savePositionDraft(data) {
  return request({
    url: '/api/recruit/position/draft',
    method: 'post',
    data
  })
}

/**
 * 删除职位
 * @param {string|number} positionId - 职位ID
 * @returns {Promise} - 删除结果
 */
export function deletePosition(positionId) {
  return request({
    url: `/api/recruit/position/${positionId}`,
    method: 'delete'
  })
}

/**
 * 激活/上线职位
 * @param {string|number} positionId - 职位ID
 * @returns {Promise} - 操作结果
 */
export function activatePosition(positionId) {
  return request({
    url: `/api/recruit/position/${positionId}/activate`,
    method: 'put'
  })
}

/**
 * 关闭/下线职位
 * @param {string|number} positionId - 职位ID
 * @returns {Promise} - 操作结果
 */
export function deactivatePosition(positionId) {
  return request({
    url: `/api/recruit/position/${positionId}/deactivate`,
    method: 'put'
  })
}

/**
 * 获取职位申请列表
 * @param {string|number} positionId - 职位ID
 * @param {Object} params - 查询参数
 * @returns {Promise} - 申请列表数据
 */
export function getPositionApplications(positionId, params) {
  return request({
    url: `/api/recruit/position/${positionId}/applications`,
    method: 'get',
    params
  })
}

/**
 * 处理职位申请
 * @param {string|number} applicationId - 申请ID
 * @param {Object} data - 处理数据
 * @returns {Promise} - 处理结果
 */
export function handleApplication(applicationId, data) {
  return request({
    url: `/api/recruit/application/${applicationId}/handle`,
    method: 'put',
    data
  })
}

/**
 * 获取职位统计数据
 * @returns {Promise} - 统计数据
 */
export function getPositionStatistics() {
  return request({
    url: '/api/recruit/position/statistics',
    method: 'get'
  })
}

/**
 * 搜索职位
 * @param {Object} params - 搜索参数
 * @returns {Promise} - 搜索结果
 */
export function searchPositions(params) {
  return request({
    url: '/api/recruit/position/search',
    method: 'get',
    params
  })
}

/**
 * 申请职位
 * @param {string|number} positionId - 职位ID
 * @param {Object} data - 申请数据
 * @returns {Promise} - 申请结果
 */
export function applyPosition(positionId, data) {
  return request({
    url: `/api/recruit/position/${positionId}/apply`,
    method: 'post',
    data
  })
}

/**
 * 检查是否已经申请过该职位
 * @param {string|number} positionId - 职位ID
 * @returns {Promise} - 检查结果
 */
export function checkApplied(positionId) {
  return request({
    url: `/api/recruit/position/${positionId}/check-applied`,
    method: 'get'
  })
} 