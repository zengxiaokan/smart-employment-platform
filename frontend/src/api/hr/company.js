import request from "@/utils/request";

/**
 * 上传图片到 OSS（营业执照 / 企业Logo 通用）
 * @param {FormData} formData
 */
export function uploadLicense(formData) {
  return request({
    url: "/upload/company",
    method: "post",
    data: formData,
    headers: { "Content-Type": "multipart/form-data" },
  });
}

/**
 * 提交企业注册申请（创建新企业）
 */
export function submitCompanyApplication(data) {
  return request({
    url: "/hr/company/apply",
    method: "post",
    data,
  });
}

/**
 * 查询当前 HR 的公司状态（含审核状态、公司详情）
 */
export function getApplicationStatus() {
  return request({
    url: "/hr/company/application/status",
    method: "get",
  });
}
