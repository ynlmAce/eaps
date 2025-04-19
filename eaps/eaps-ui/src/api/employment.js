import request from '@/utils/request'

/**
 * 获取就业统计概览数据
 * @returns {Promise}
 */
export function getEmploymentStatistics() {
  return request({
    url: '/api/employment/statistics',
    method: 'get'
  })
}

/**
 * 获取学生就业列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getEmploymentStudentList(params) {
  return request({
    url: '/api/employment/students',
    method: 'get',
    params
  })
}

/**
 * 获取各专业就业率数据
 * @returns {Promise}
 */
export function getMajorEmploymentRate() {
  return request({
    url: '/api/employment/statistics/major-rate',
    method: 'get'
  })
}

/**
 * 获取就业行业分布数据
 * @returns {Promise}
 */
export function getIndustryDistribution() {
  return request({
    url: '/api/employment/statistics/industry-distribution',
    method: 'get'
  })
}

/**
 * 获取薪资分布数据
 * @returns {Promise}
 */
export function getSalaryDistribution() {
  return request({
    url: '/api/employment/statistics/salary-distribution',
    method: 'get'
  })
}

/**
 * 获取月度就业趋势数据
 * @param {Object} params - 查询参数，如年份
 * @returns {Promise}
 */
export function getEmploymentTrend(params) {
  return request({
    url: '/api/employment/statistics/trend',
    method: 'get',
    params
  })
}

/**
 * 导出就业数据
 * @param {Object} params - 导出参数，如筛选条件
 * @returns {Promise}
 */
export function exportEmploymentData(params) {
  return request({
    url: '/api/employment/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 获取学生就业详情
 * @param {string} studentId - 学生ID
 * @returns {Promise}
 */
export function getStudentEmploymentDetail(studentId) {
  return request({
    url: `/api/employment/student/${studentId}`,
    method: 'get'
  })
} 