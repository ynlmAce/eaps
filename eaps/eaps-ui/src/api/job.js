import request from './request'

// 获取职位列表（分页）
export function getJobPositionList(params) {
  return request({
    url: '/job/position/list',
    method: 'get',
    params
  })
}

// 获取职位详情
export function getJobPositionDetail(id) {
  return request({
    url: `/job/position/${id}`,
    method: 'get'
  })
}

// 企业：添加职位
export function addJobPosition(data) {
  return request({
    url: '/job/position',
    method: 'post',
    data
  })
}

// 企业：更新职位
export function updateJobPosition(id, data) {
  return request({
    url: `/job/position/${id}`,
    method: 'put',
    data
  })
}

// 企业：删除职位
export function deleteJobPosition(id) {
  return request({
    url: `/job/position/${id}`,
    method: 'delete'
  })
}

// 企业：修改职位状态（开放/关闭）
export function updateJobPositionStatus(id, status) {
  return request({
    url: `/job/position/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 申请职位
export function applyJobPosition(data) {
  return request({
    url: '/job/application',
    method: 'post',
    data
  })
}

// 获取我的申请列表
export function getMyApplications(params) {
  return request({
    url: '/job/application/my',
    method: 'get',
    params
  })
}

// 取消申请
export function cancelApplication(id) {
  return request({
    url: `/job/application/${id}/cancel`,
    method: 'put'
  })
}

// 企业：获取职位申请列表
export function getJobApplications(params) {
  return request({
    url: '/job/application/list',
    method: 'get',
    params
  })
}

// 企业：处理申请
export function processApplication(id, data) {
  return request({
    url: `/job/application/${id}/process`,
    method: 'put',
    data
  })
}

// 查看申请详情
export function getApplicationDetail(id) {
  return request({
    url: `/job/application/${id}`,
    method: 'get'
  })
} 