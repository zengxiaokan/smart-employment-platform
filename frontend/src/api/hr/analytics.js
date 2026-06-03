import request from '@/utils/request'

/**
 * 概览统计：发布职位数、在招岗位数、收到简历总数、本周简历数、面试转化率、入职成功率
 */
export function getAnalyticsOverview(startDate, endDate) {
  return request({ url: '/hr/analytics/overview', method: 'get', params: { startDate, endDate } })
}

/**
 * 本周趋势：最近7天每天的投递数和面试数
 */
export function getAnalyticsTrend(startDate, endDate) {
  return request({ url: '/hr/analytics/trend', method: 'get', params: { startDate, endDate } })
}

/**
 * 招聘漏斗：各职位的 投递数 → 面试数 → 入职数（全部累计）
 */
export function getAnalyticsFunnel() {
  return request({ url: '/hr/analytics/funnel', method: 'get' })
}
