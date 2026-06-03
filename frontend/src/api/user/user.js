import request from '@/utils/request'

/**
 * 上传头像
 * @param {FormData} formData - 包含图片文件的 FormData
 */
export function uploadAvatar(formData) {
  return request({
    url: '/upload/avatar',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/**
 * 修改密码
 * @param {Object} data - { oldPassword, newPassword }
 */
export function changePassword(data) {
  return request({
    url: '/login/changepwd',
    method: 'post',
    data,
  })
}

/**
 * 请求AI优化简历
 * 后端从数据库获取当前用户简历 -> 发给AI -> 返回优化后的JSON
 */
export function aiOptimizeResume() {
  return request({
    url: '/user/resume/ai-optimize',
    method: 'post',
  })
}

/**
 * 采纳AI优化结果，更新简历
 * @param {Object} data - AI返回的优化后简历数据
 */
/** 获取个人中心统计：收藏、投递、面试数 */
export function getUserStats() {
  return request({ url: '/user/stats', method: 'get' })
}

/** 获取首页统计：在招岗位、入驻企业、求职者、成功匹配 */
export function getHomeStats() {
  return request({ url: '/user/home/stats', method: 'get' })
}

export function acceptAiResume(data) {
  return request({
    url: '/user/resume/update',
    method: 'put',
    data,
  })
}
