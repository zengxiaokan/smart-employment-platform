import request from "@/utils/request";

/**
 * 获取面试列表（HR端）
 */
export function getInterviewList() {
  return request({ url: "/hr/interviews/list", method: "get" });
}

/**
 * 更新面试状态（复用 /hr/interview/status）
 * 后端根据 status 判断：待定→仅更新、面试成功→同步更新申请为已录用、拒绝→同步更新申请为不合适
 * @param {number} interviewId - 面试ID
 * @param {number} status - 目标状态码：2=待定 3=面试成功 4=拒绝 5=过期 6=取消
 * @param {string|null} remark - 状态变更备注
 */
export function updateInterviewStatus(interviewId, status, hrRemark) {
  return request({ url: "/hr/interviews/status", method: "put", data: { id: interviewId, status, hrRemark } });
}

/**
 * 获取面试详情
 * @param {number} interviewId - 面试ID
 */
export function getInterviewDetail(interviewId) {
  return request({ url: `/hr/interviews/${interviewId}`, method: "get" });
}

/**
 * 创建/更新面试邀约
 * 后端根据 data.id 是否存在决定：有id→更新，无id→插入
 * @param {Object} data - { id?, applicationId, userId, jobId, status, interviewTime, locationType, interviewLocation, contactPerson, contactPhone, hrRemark }
 */
export function createInterviewInvitation(data) {
  return request({ url: "/hr/resumes/interview", method: "post", data });
}

/**
 * 根据jobId+userId查询已有面试邀约（点击邀请面试时回显用）
 * @param {number} jobId
 * @param {number} userId
 */
export function getInterviewByJobAndUser(jobId, userId) {
  return request({ url: "/hr/interviews/query", method: "get", params: { jobId, userId } });
}

