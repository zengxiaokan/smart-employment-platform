import request from '@/utils/request'

export function getCompanies(params) {
  return request({ url: '/admin/companies', method: 'get', params })
}

export function getCompanyDetail(id) {
  return request({ url: `/admin/companies/${id}`, method: 'get' })
}

export function createCompany(data) {
  return request({ url: '/admin/companies', method: 'post', data })
}

export function updateCompany(id, data) {
  return request({ url: `/admin/companies/${id}`, method: 'put', data })
}

export function auditCompany(id, auditStatus, remark) {
  return request({
    url: `/admin/companies/${id}/audit`,
    method: 'put',
    data: { auditStatus, remark: remark || null },
  })
}

export function deleteCompany(id) {
  return request({ url: `/admin/companies/${id}`, method: 'delete' })
}

export function getCompanyRegistrant(id) {
  return request({ url: `/admin/companies/${id}/registrant`, method: 'get' })
}
