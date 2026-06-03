import request from '@/utils/request'

export function getLatestAnnouncements(limit = 5) {
  return request({ url: '/announcements', method: 'get', params: { limit } })
}
