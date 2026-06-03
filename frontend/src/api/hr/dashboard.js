import request from '@/utils/request'

export function getDashboard() {
  return request({ url: '/hr/dashboard', method: 'get' })
}

export function getAllTodos() {
  return request({ url: '/hr/dashboard/todos', method: 'get' })
}
