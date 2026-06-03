import request from "@/utils/request";

/**
 * 获取简历详情（后端自动标记已查看）
 * @param {number} resumeId - 简历ID
 * @param {number} applicationId - 申请ID
 */
export function getResumeDetail(resumeId, applicationId) {
  return request({ url: `/hr/resumes/detail/${resumeId}`, method: "get", params: { applicationId } });
}

/**
 * 通过申请表ID获取候选人简历（面试管理查看简历用）
 * @param {number} applicationId - 申请表ID
 */
export function getResumeByApplication(applicationId) {
  return request({ url: `/hr/interviews/resume/${applicationId}`, method: "get" });
}