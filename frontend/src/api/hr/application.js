import request from "@/utils/request";

/**
 * 获取投递列表（分页+筛选）
 * @param {Object} params - { page, size, applicantName, jobTitle, status }
 */
export function getApplicationList(params) {
  return request({ url: "/hr/resumes/list", method: "get", params });
}

/**
 * 修改投递状态
 * @param {number} id - 申请ID
 * @param {number} status - 状态码：3=不合适 5=已录用
 */
export function updateApplicationStatus(id, status) {
  return request({ url: "/hr/resumes/status", method: "put", data: { id, status } });
}

/**
 * 修改投递备注
 * @param {number} id - 申请ID
 * @param {string|null} hrRemark - 备注内容
 */
export function updateApplicationRemark(id, hrRemark) {
  return request({ url: "/hr/resumes/remark", method: "put", data: { id, hrRemark } });
}

/**
 * 创建面试邀约
 * @param {Object} data - { applicationId, userId, jobId, interviewTime, locationType, interviewLocation, contactPerson, contactPhone, hrRemark }
 */
export function createInterviewInvitation(data) {
  return request({ url: "/hr/resumes/interview", method: "post", data });
}
