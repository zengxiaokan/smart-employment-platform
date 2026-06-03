import request from '@/utils/request'

//概览统计角色分布饼图
export function getOverview() {
  return request({ url: '/admin/dashboard/overview', method: 'get' })
}
//趋势数据返回近 N 天每日新增用户 + 新增投递
export function getTrends(days = 7) {
  return request({ url: '/admin/dashboard/trends', method: 'get', params: { days } })
}
//对应页面底部表格，字段全部来自 users 表
export function getRecentUsers(limit = 10) {
  return request({ url: '/admin/dashboard/recent-users', method: 'get', params: { limit } })
}