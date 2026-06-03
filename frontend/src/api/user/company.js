import request from "@/utils/request";

/**
 * 获取公司列表
 * @param {Object} params - { page: number, size: number, keyword?: string, industry?: string, size?: string }
 */
export function getCompanyList(params) {
  return request({
    url: "/user/company/list",
    method: "get",
    params,
  });
}

/**
 * 获取筛选选项（行业和规模列表）
 */
export function getCompanyFilters() {
  return request({
    url: "/user/company/filters",
    method: "get",
  });
}

/**
 * 获取公司详情（包含在招岗位列表）
 * @param {number|string} id - 公司ID
 */
export function getCompanyDetail(id) {
  return request({
    url: `/user/company/detail/${id}`,
    method: "get",
  });
}

/**
 * 获取公司的在招岗位列表（分页）
 * @param {number|string} companyId - 公司ID
 * @param {Object} params - 查询参数 { page, pageSize }
 */
export function getCompanyJobs(companyId, params) {
  return request({
    url: `/user/company/${companyId}/jobs`,
    method: "get",
    params,
  });
}
