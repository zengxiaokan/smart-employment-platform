import request from "@/utils/request";

/**
 * 获取简历列表
 */
export function getResumeList() {
  return request({
    url: "/user/resume/list",
    method: "get",
  });
}

/**
 * 获取简历详情
 * @param {number|string} id
 */
export function getResumeDetail(id) {
  return request({
    url: `/user/resume/detail/${id}`,
    method: "get",
  });
}

/**
 * 保存简历 (新增或更新)
 * @param {Object} data
 */
export function saveResume(data) {
  return request({
    url: "/user/resume/save",
    method: "post",
    data,
  });
}

/**
 * 删除简历
 * @param {number|string} id
 */
export function deleteResume(id) {
  return request({
    url: `/user/resume/delete/${id}`,
    method: "delete",
  });
}

/**
 * 设置默认简历
 * @param {number|string} id
 */
export function setDefaultResume(id) {
  return request({
    url: `/user/resume/setDefault/${id}`,
    method: "put",
  });
}

/**
 * 上传简历头像
 * @param {FormData} formData
 */
export function uploadResumeAvatar(formData) {
  return request({
    url: "/upload/resume",
    method: "post",
    data: formData,
    headers: { "Content-Type": "multipart/form-data" },
  });
}

/**
 * AI 优化简历
 * @param {Object} data - 完整简历 JSON
 */
export function optimizeResume(data) {
  return request({
    url: "user/resume/optimize",
    method: "post",
    data,
  });
}

/**
 * 单独保存一条教育经历
 * @param {Object} data - { resumeId, id?, school, major, education, startTime, endTime, description }
 */
export function saveEducation(data) {
  return request({ url: "/user/resume/education", method: "post", data });
}

export function deleteEducation(id) {
  return request({ url: `/user/resume/education/${id}`, method: "delete" });
}

/**
 * 单独保存一条工作经历
 * @param {Object} data - { resumeId, id?, company, position, startTime, endTime, description }
 */
export function saveExperience(data) {
  return request({ url: "/user/resume/experience", method: "post", data });
}

export function deleteExperience(id) {
  return request({ url: `/user/resume/experience/${id}`, method: "delete" });
}

/**
 * 单独保存一条项目经历
 * @param {Object} data - { resumeId, id?, name, role, startTime, endTime, description }
 */
export function saveProject(data) {
  return request({ url: "/user/resume/project", method: "post", data });
}

export function deleteProject(id) {
  return request({ url: `/user/resume/project/${id}`, method: "delete" });
}

export function exportResumePdf(id) {
  return request({
    url: `/user/resume/export/${id}`,
    method: "get",
    responseType: "blob",
  });
}

/**
 * 保存求职意向（简历基本表）
 * @param {Object} data - { name, jobIntention, city, salaryMin, salaryMax, jobType, industry, availableFrom, selfDescription }
 */
export function saveJobIntention(data) {
  return request({ url: "/user/resume/intention", method: "post", data });
}

/**
 * 获取默认简历（求职意向表回显用）
 */
export function getDefaultResume() {
  return request({ url: "/user/resume/default", method: "get" });
}

/**
 * 单独保存简历基本信息
 * @param {Object} data - 简历基本表字段
 */
export function saveResumeBasic(data) {
  return request({ url: "/user/resume/basic", method: "post", data });
}
