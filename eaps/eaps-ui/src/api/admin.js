import request from '@/utils/request'

// 企业审核相关API
export function getPendingEnterpriseList(params) {
  return request({
    url: '/admin/enterprise/pending',
    method: 'get',
    params
  })
}

export function verifyEnterprise(id, data) {
  return request({
    url: `/admin/enterprise/${id}/verify`,
    method: 'put',
    data
  })
}

export function resetEnterprisePassword(id) {
  return request({
    url: `/admin/enterprise/${id}/reset-password`,
    method: 'put'
  })
}

export function toggleEnterpriseStatus(id, data) {
  return request({
    url: `/admin/enterprise/${id}/status`,
    method: 'put',
    data
  })
}

// 职位审核相关API
export function getPendingPositionList(params) {
  return request({
    url: '/admin/position/pending',
    method: 'get',
    params
  })
}

export function verifyPosition(id, data) {
  return request({
    url: `/admin/position/${id}/verify`,
    method: 'put',
    data
  })
}

export function batchVerifyPositions(data) {
  return request({
    url: '/admin/position/batch-verify',
    method: 'post',
    data
  })
}

// 企业宣传审核相关API
export function getPendingPromotionList(params) {
  return request({
    url: '/admin/promotion/pending',
    method: 'get',
    params
  })
}

export function reviewPromotion(id, data) {
  return request({
    url: `/admin/promotion/${id}/review`,
    method: 'put',
    data
  })
}

export function setPromotionTop(id, isTop) {
  return request({
    url: `/admin/promotion/${id}/top`,
    method: 'put',
    data: { isTop }
  })
}

// 企业评价审核相关API
export function getEnterpriseRatingList(params) {
  return request({
    url: '/admin/rating/list',
    method: 'get',
    params
  })
}

export function approveEnterpriseRating(id) {
  return request({
    url: `/admin/rating/${id}/approve`,
    method: 'post'
  })
}

export function rejectEnterpriseRating(id, data) {
  return request({
    url: `/admin/rating/${id}/reject`,
    method: 'post',
    data
  })
}

export function batchApproveEnterpriseRatings(ids) {
  return request({
    url: '/admin/rating/batch-approve',
    method: 'post',
    data: { ids }
  })
}

export function batchRejectEnterpriseRatings(data) {
  return request({
    url: '/admin/rating/batch-reject',
    method: 'post',
    data
  })
}

// 企业评价申诉处理相关API
export function getAppealsList(params) {
  return request({
    url: '/admin/appeal/list',
    method: 'get',
    params
  })
}

export function approveAppeal(id) {
  return request({
    url: `/admin/appeal/${id}/approve`,
    method: 'post'
  })
}

export function rejectAppeal(id, data) {
  return request({
    url: `/admin/appeal/${id}/reject`,
    method: 'post',
    data
  })
}

export function batchApproveAppeals(ids) {
  return request({
    url: '/admin/appeal/batch-approve',
    method: 'post',
    data: { ids }
  })
}

export function batchRejectAppeals(data) {
  return request({
    url: '/admin/appeal/batch-reject',
    method: 'post',
    data
  })
}

export function getAppealDetail(id) {
  return request({
    url: `/admin/appeal/${id}`,
    method: 'get'
  })
}

export function getRatingDetail(id) {
  return request({
    url: `/admin/rating/${id}`,
    method: 'get'
  })
} 