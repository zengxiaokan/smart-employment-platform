import request from '@/utils/request'

export function getAnnouncements(params) {
  return request({ url: '/admin/announcements', method: 'get', params })
}

export function createAnnouncement(data) {
  return request({ url: '/admin/announcements', method: 'post', data })
}

export function updateAnnouncement(id, data) {
  return request({ url: `/admin/announcements/${id}`, method: 'put', data })
}

export function deleteAnnouncement(id) {
  return request({ url: `/admin/announcements/${id}`, method: 'delete' })
}

export function togglePinAnnouncement(id, pinned) {
  return request({ url: `/admin/announcements/${id}/pin`, method: 'put', data: { pinned } })
}
