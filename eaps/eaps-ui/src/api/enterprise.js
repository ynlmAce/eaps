import request from './request'

// 获取企业列表
export function getEnterpriseList(params) {
  return request({
    url: '/enterprise/list',
    method: 'get',
    params
  })
}

// 获取企业详情
export function getEnterpriseDetail(id) {
  return request({
    url: `/enterprise/${id}`,
    method: 'get'
  })
}

// 获取企业招聘职位
export function getEnterprisePositions(enterpriseId, params) {
  return request({
    url: `/enterprise/${enterpriseId}/positions`,
    method: 'get',
    params
  })
}

// 获取企业宣传内容
export function getEnterprisePromotions(enterpriseId, params) {
  return request({
    url: `/enterprise/${enterpriseId}/promotions`,
    method: 'get',
    params
  })
}

// 获取企业评分和评价
export function getEnterpriseRatings(enterpriseId, params) {
  return request({
    url: `/enterprise/${enterpriseId}/ratings`,
    method: 'get',
    params
  })
}

// 提交企业评分
export function submitEnterpriseRating(enterpriseId, data) {
  return request({
    url: `/enterprise/${enterpriseId}/rating`,
    method: 'post',
    data
  })
}

// 修改企业评分
export function updateEnterpriseRating(ratingId, data) {
  return request({
    url: `/enterprise/rating/${ratingId}`,
    method: 'put',
    data
  })
}

// 删除企业评分
export function deleteEnterpriseRating(ratingId) {
  return request({
    url: `/enterprise/rating/${ratingId}`,
    method: 'delete'
  })
}

// 获取我的评分列表
export function getMyRatings(params) {
  return request({
    url: '/enterprise/rating/my',
    method: 'get',
    params
  })
}

// 企业回复评分
export function replyRating(ratingId, data) {
  return request({
    url: `/enterprise/rating/${ratingId}/reply`,
    method: 'post',
    data
  })
}

// 企业申诉评分
export function appealRating(ratingId, data) {
  return request({
    url: `/enterprise/rating/${ratingId}/appeal`,
    method: 'post',
    data
  })
} 