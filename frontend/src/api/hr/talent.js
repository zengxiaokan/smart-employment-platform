import request from '@/utils/request'

/**
 * 人才搜索：按条件分页查询求职意向表
 */
export function searchTalents(params) {
  return request({ url: '/hr/talent/search', method: 'get', params })
}

/**
 * 查看人才简历详情（无需申请ID，仅通过简历ID查询）
 * @param {number} resumeId - 简历ID
 */
export function getTalentResumeDetail(resumeId) {
  return request({ url: `/hr/talent/resume/${resumeId}`, method: 'get' })
}

/**
 * 收藏人才简历
 * @param {Object} data - { userId, resumeId, reason }
 */
export function addFavorite(data) {
  return request({ url: '/hr/talent/favorite', method: 'post', data })
}

/**
 * 取消收藏（通过简历ID）
 * @param {number} resumeId - 简历ID
 */
export function cancelFavorite(resumeId) {
  return request({ url: `/hr/talent/favorite/${resumeId}`, method: 'delete' })
}

/**
 * 获取收藏夹列表
 * @param {Object} params - { keyword }
 */
export function getFavorites(params) {
  return request({ url: '/hr/talent/favorites', method: 'get', params })
}

/**
 * 修改收藏备注
 * @param {Object} params - { resumeId, reason }
 */
export function updateFavoriteReason(params) {
  return request({ url: '/hr/talent/favorite/reason', method: 'put', params })
}
