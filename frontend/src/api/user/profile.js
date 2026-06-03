import request from '@/utils/request'

export function getUserProfileApi() {
  return request({ url: '/user/profile', method: 'get' })
}

export function updateUserProfileApi(data) {
  return request({ url: '/user/profile', method: 'put', data })
}

export function getJobApplicationsApi() {
  return request({ url: '/user/applications', method: 'get' })
}

export function cancelApplyApi(applicationId) {
  return request({ url: `/user/application/cancel/${applicationId}`, method: 'put' })
}

export function reApplyApi(applicationId) {
  return request({ url: `/user/application/reapply/${applicationId}`, method: 'put' })
}

export function getApplicationDetailApi(applicationId) {
  return request({ url: `/user/application/detail/${applicationId}`, method: 'get' })
}
