import request from '@/utils/request'

/**
 * 获取评价列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getRatingsList(params) {
  return request({
    url: '/api/ratings/list',
    method: 'get',
    params
  })
}

/**
 * 获取评价详情
 * @param {number} id 评价ID
 * @returns {Promise}
 */
export function getRatingDetail(id) {
  return request({
    url: `/api/ratings/${id}`,
    method: 'get'
  })
}

/**
 * 审核评价 - 批准
 * @param {number} id 评价ID
 * @returns {Promise}
 */
export function approveRating(id) {
  return request({
    url: `/api/ratings/${id}/approve`,
    method: 'post'
  })
}

/**
 * 审核评价 - 拒绝
 * @param {number} id 评价ID
 * @param {Object} data 拒绝原因
 * @returns {Promise}
 */
export function rejectRating(id, data) {
  return request({
    url: `/api/ratings/${id}/reject`,
    method: 'post',
    data
  })
}

/**
 * 处理评价申诉
 * @param {number} id 申诉ID
 * @param {Object} data 处理结果
 * @returns {Promise}
 */
export function handleRatingAppeal(id, data) {
  return request({
    url: `/api/ratings/appeals/${id}/handle`,
    method: 'post',
    data
  })
}

/**
 * 获取评价统计数据
 * @returns {Promise}
 */
export function getRatingStatistics() {
  return request({
    url: '/api/ratings/statistics',
    method: 'get'
  })
}

/**
 * 批量审核评价 - 批准
 * @param {Array} ids 评价ID列表
 * @returns {Promise}
 */
export function batchApproveRatings(ids) {
  return request({
    url: '/api/ratings/batch-approve',
    method: 'post',
    data: { ids }
  })
}

/**
 * 批量审核评价 - 拒绝
 * @param {Array} ids 评价ID列表
 * @param {Object} data 拒绝原因
 * @returns {Promise}
 */
export function batchRejectRatings(ids, data) {
  return request({
    url: '/api/ratings/batch-reject',
    method: 'post',
    data: {
      ids,
      ...data
    }
  })
}

/**
 * 用户提交企业评价
 * @param {Object} data 评价数据
 * @returns {Promise}
 */
export function submitRating(data) {
  return request({
    url: '/api/ratings/submit',
    method: 'post',
    data
  })
}

/**
 * 企业申诉评价
 * @param {number} ratingId 评价ID
 * @param {Object} data 申诉数据
 * @returns {Promise}
 */
export function appealRating(ratingId, data) {
  return request({
    url: `/api/ratings/${ratingId}/appeal`,
    method: 'post',
    data
  })
}

/**
 * 上传评价图片
 * @param {FormData} data 文件表单数据
 * @returns {Promise}
 */
export function uploadRatingImage(data) {
  return request({
    url: '/api/ratings/upload-image',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取评分申诉列表
 * @param {Object} params 查询参数
 * @returns {Promise} 返回请求Promise
 */
export function getAppealsList(params) {
  return request({
    url: '/ratings/appeals',
    method: 'get',
    params
  })
}

/**
 * 获取用户已提交的评分
 * @param {Object} params 查询参数
 * @returns {Promise} 返回请求Promise
 */
export function getUserRatings(params) {
  return request({
    url: '/ratings/user',
    method: 'get',
    params
  })
}

/**
 * 获取企业评分列表
 * @param {number} enterpriseId 企业ID
 * @param {Object} params 查询参数
 * @returns {Promise} 返回请求Promise
 */
export function getEnterpriseRatings(enterpriseId, params) {
  return request({
    url: `/ratings/enterprise/${enterpriseId}`,
    method: 'get',
    params
  })
}

/**
 * 更新企业评分
 * @param {string|number} id - 评分ID
 * @param {Object} data - 更新的评分数据
 * @returns {Promise}
 */
export function updateRating(id, data) {
  return request({
    url: `/api/ratings/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除企业评分
 * @param {string|number} id - 评分ID
 * @returns {Promise}
 */
export function deleteRating(id) {
  return request({
    url: `/api/ratings/${id}`,
    method: 'delete'
  })
}

/**
 * 企业回复评分
 * @param {string|number} id - 评分ID
 * @param {Object} data - 回复数据
 * @returns {Promise}
 */
export function replyRating(id, data) {
  return request({
    url: `/api/ratings/${id}/reply`,
    method: 'post',
    data
  })
}

/**
 * 获取评分申诉详情
 * @param {string|number} id - 申诉ID
 * @returns {Promise}
 */
export function getAppealDetail(id) {
  return request({
    url: `/api/ratings/appeals/${id}`,
    method: 'get'
  })
}

/**
 * 获取企业的评分统计
 * @param {string|number} enterpriseId - 企业ID
 * @returns {Promise}
 */
export function getEnterpriseRatingStats(enterpriseId) {
  return request({
    url: `/api/enterprises/${enterpriseId}/rating-stats`,
    method: 'get'
  })
}

/**
 * 用户对评价点赞/取消点赞
 * @param {string|number} id - 评分ID
 * @param {boolean} like - 点赞状态
 * @returns {Promise}
 */
export function toggleLikeRating(id, like) {
  return request({
    url: `/api/ratings/${id}/like`,
    method: 'put',
    data: { like }
  })
}

// 获取评分统计（管理员功能）
export function getRatingAdminStatistics() {
  return request({
    url: '/admin/rating/statistics',
    method: 'get'
  })
}

// 获取申诉统计（管理员功能）
export function getAppealAdminStatistics() {
  return request({
    url: '/admin/rating/appeal/statistics',
    method: 'get'
  })
}

// 获取最新评分列表（管理员功能）
export function getLatestRatings(params) {
  return request({
    url: '/admin/rating/latest',
    method: 'get',
    params
  })
}

// 获取需要审核的评分列表（管理员功能）
export function getPendingReviewRatings(params) {
  return request({
    url: '/admin/rating/pending',
    method: 'get',
    params
  })
}

// 获取待处理申诉列表（管理员功能）
export function getPendingAppeals(params) {
  return request({
    url: '/admin/rating/appeal/pending',
    method: 'get',
    params
  })
}

// 举报评分
export function reportRating(ratingId, data) {
  return request({
    url: `/enterprise/rating/${ratingId}/report`,
    method: 'post',
    data
  })
}

// 获取管理员待审核评价列表
export function getPendingRatings(params) {
  return request({
    url: '/api/admin/ratings/pending',
    method: 'get',
    params
  })
}

// 获取管理员已处理评价列表
export function getProcessedRatings(params) {
  return request({
    url: '/api/admin/ratings/processed',
    method: 'get',
    params
  })
}

// 管理员处理申诉
export function processAppeal(appealId, data) {
  return request({
    url: `/api/admin/appeals/${appealId}/process`,
    method: 'post',
    data
  })
} 
