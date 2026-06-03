import request from '@/utils/request';

/**
 * 获取职位列表 (APIFox 模拟接口)
 * @param {Object} params - 查询参数 { page, size, keyword, city }
 */
export const queryAllJobs = (params) => request.get('/user/jobs/list', { params });

/**
 * 投递简历
 * @param {Object} data - { jobId, resumeId }
 */
export const applyJob = (data) => request.post('/user/job/apply', data);

/**
 * 收藏职位
 * @param {number|string} jobId
 */
export const favoriteJob = (jobId) => request.post('/user/jobs/favorite', { jobId });

/**
 * 取消收藏职位
 * @param {number|string} jobId
 */
export const unfavoriteJob = (jobId) => request.delete(`/user/jobs/favorite/${jobId}`);

/**
 * 获取收藏职位列表
 */
export const getFavoriteJobs = () => request.get('/user/jobs/favorite');

/**
 * 记录职位浏览
 * @param {number|string} jobId
 */

export const recordBrowse = (jobId) =>
  request.post('/user/jobs/browse', jobId, {
    headers: { 'Content-Type': 'application/json' }
  });

export const getRecommendedJobs = () => request.get('/user/jobs/recommended');

/** 热门岗位：按投递数+浏览量排序 */
export const getHotJobs = (limit = 6) => request.get('/user/jobs/hot', { params: { limit } });

