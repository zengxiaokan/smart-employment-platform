import request from '@/utils/request'

export function getResumes(params) {
  return request({ url: '/admin/resumes', method: 'get', params })
}

export function getResumeDetail(id) {
  return request({ url: `/admin/resumes/${id}`, method: 'get' })
}

export function deleteResume(id) {
  return request({ url: `/admin/resumes/${id}`, method: 'delete' })
}
