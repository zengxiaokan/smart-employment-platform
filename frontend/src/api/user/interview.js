import request from "@/utils/request";

/**
 * 获取面试通知列表（用户端）
 */
export function getUserInterviews() {
  return request({ url: "/user/interviews/list", method: "get" });
}

/**
 * 用户确认/拒绝面试（含备注）
 * @param {number} interviewId - 面试ID
 * @param {number} status - 1=接受 4=拒绝
 * @param {string} [remark=''] - 备注内容
 */
export function respondInterview(interviewId, status, remark = '') {
  return request({ url: "/user/interviews/status", method: "put", data: { id: interviewId, status, candidateRemark: remark } });
}