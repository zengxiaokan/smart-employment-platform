import request from '@/utils/request'

export function getMyFeedbacks(params) {
  return request({ url: '/feedbacks', method: 'get', params })
}

export function getFeedbackDetail(id) {
  return request({ url: `/feedbacks/${id}`, method: 'get' })
}

export function submitFeedback(data) {
  return request({ url: '/feedbacks', method: 'post', data })
}
