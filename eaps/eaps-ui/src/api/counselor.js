import request from './request'

/**
 * 获取就业统计数据
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getEmploymentStatistics(params) {
  return request({
    url: '/counselor/employment/statistics',
    method: 'get',
    params
  })
}

/**
 * 获取就业统计详情
 * @param {String} id 统计ID
 * @returns {Promise}
 */
export function getEmploymentStatisticsDetail(id) {
  return request({
    url: `/counselor/employment/statistics/${id}`,
    method: 'get'
  })
}

/**
 * 获取学生列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getStudentList(params) {
  return request({
    url: '/counselor/students',
    method: 'get',
    params
  })
}

/**
 * 获取学生详情
 * @param {String} id 学生ID
 * @returns {Promise}
 */
export function getStudentDetail(id) {
  return request({
    url: `/counselor/students/${id}`,
    method: 'get'
  })
}

/**
 * 更新学生信息
 * @param {String} id 学生ID
 * @param {Object} data 学生信息
 * @returns {Promise}
 */
export function updateStudent(id, data) {
  return request({
    url: `/counselor/students/${id}`,
    method: 'put',
    data
  })
}

/**
 * 获取班级列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getClassList(params) {
  return request({
    url: '/counselor/classes',
    method: 'get',
    params
  })
}

/**
 * 获取就业进度统计
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getEmploymentProgress(params) {
  return request({
    url: '/counselor/employment/progress',
    method: 'get',
    params
  })
}

/**
 * 导出就业统计数据
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function exportEmploymentStatistics(params) {
  return request({
    url: '/counselor/employment/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 获取就业率趋势
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getEmploymentTrend(params) {
  return request({
    url: '/counselor/employment/trend',
    method: 'get',
    params
  })
}

/**
 * 获取行业分布统计
 * @returns {Promise}
 */
export function getIndustryDistribution() {
  return request({
    url: '/counselor/employment/industry-distribution',
    method: 'get'
  })
}

/**
 * 获取地区分布统计
 * @returns {Promise}
 */
export function getRegionDistribution() {
  return request({
    url: '/counselor/employment/region-distribution',
    method: 'get'
  })
} 