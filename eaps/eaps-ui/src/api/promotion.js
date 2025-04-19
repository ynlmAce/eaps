import request from './request'

/**
 * 获取企业宣传列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getPromotionList(params) {
  return request({
    url: '/promotion/list',
    method: 'get',
    params
  })
}

/**
 * 获取宣传详情
 * @param {number} id 宣传ID
 * @returns {Promise}
 */
export function getPromotionDetail(id) {
  return request({
    url: `/promotion/${id}`,
    method: 'get'
  })
}

/**
 * 创建宣传
 * @param {Object} data 宣传数据
 * @returns {Promise}
 */
export function createPromotion(data) {
  return request({
    url: '/promotion',
    method: 'post',
    data
  })
}

/**
 * 更新宣传
 * @param {number} id 宣传ID
 * @param {Object} data 宣传数据
 * @returns {Promise}
 */
export function updatePromotion(id, data) {
  return request({
    url: `/promotion/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除宣传
 * @param {number} id 宣传ID
 * @returns {Promise}
 */
export function deletePromotion(id) {
  return request({
    url: `/promotion/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除宣传
 * @param {Array} ids 宣传ID数组
 * @returns {Promise}
 */
export function batchDeletePromotions(ids) {
  return request({
    url: '/promotion/batch',
    method: 'delete',
    data: { ids }
  })
}

/**
 * 更新宣传状态
 * @param {number} id 宣传ID
 * @param {Object} data 包含状态的数据对象
 * @returns {Promise}
 */
export function updatePromotionStatus(id, data) {
  return request({
    url: `/promotion/${id}/status`,
    method: 'patch',
    data
  })
}

/**
 * 上传宣传图片
 * @param {FormData} data 包含图片的FormData
 * @returns {Promise}
 */
export function uploadPromotionImage(data) {
  return request({
    url: '/promotion/upload/image',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取宣传统计数据
 * @returns {Promise}
 */
export function getPromotionStatistics() {
  return request({
    url: '/promotion/statistics',
    method: 'get'
  })
}

/**
 * 获取企业的宣传列表
 * @param {number} enterpriseId 企业ID
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getEnterprisePromotions(enterpriseId, params) {
  return request({
    url: `/enterprise/${enterpriseId}/promotions`,
    method: 'get',
    params
  })
}

/**
 * 审核宣传
 * @param {number} id 宣传ID
 * @param {Object} data 审核数据（包含审核结果和原因）
 * @returns {Promise}
 */
export function reviewPromotion(id, data) {
  return request({
    url: `/promotion/${id}/review`,
    method: 'post',
    data
  })
}

/**
 * 批量审核宣传
 * @param {Array} ids 宣传ID数组
 * @param {Object} data 审核数据（包含审核结果和原因）
 * @returns {Promise}
 */
export function batchReviewPromotions(ids, data) {
  return request({
    url: '/promotion/batch/review',
    method: 'post',
    data: { ids, ...data }
  })
}

/**
 * 推荐宣传（设置为推荐）
 * @param {number} id 宣传ID
 * @returns {Promise}
 */
export function recommendPromotion(id) {
  return request({
    url: `/promotion/${id}/recommend`,
    method: 'post'
  })
}

/**
 * 取消推荐宣传
 * @param {number} id 宣传ID
 * @returns {Promise}
 */
export function cancelRecommendPromotion(id) {
  return request({
    url: `/promotion/${id}/recommend`,
    method: 'delete'
  })
}

/**
 * 获取推荐宣传列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getRecommendedPromotions(params) {
  return request({
    url: '/promotion/recommended',
    method: 'get',
    params
  })
} 