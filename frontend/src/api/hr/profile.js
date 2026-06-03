import request from '@/utils/request'

export function getHrProfile() {
  return request({ url: '/hr/profile', method: 'get' })
}

export function updateHrProfile(data) {
  return request({ url: '/hr/profile', method: 'put', data })
}
