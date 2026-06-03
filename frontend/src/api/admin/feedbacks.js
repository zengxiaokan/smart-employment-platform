import request from '@/utils/request'

export function getFeedbacks(params) {
  return request({ url: '/admin/feedbacks', method: 'get', params })
}

export function getFeedbackDetail(id) {
  return request({ url: `/admin/feedbacks/${id}`, method: 'get' })
}

export function replyFeedback(id, data) {
  return request({ url: `/admin/feedbacks/${id}/reply`, method: 'put', data })
}

export function updateFeedbackStatus(id, status) {
  return request({ url: `/admin/feedbacks/${id}/status`, method: 'put', data: { status } })
}
