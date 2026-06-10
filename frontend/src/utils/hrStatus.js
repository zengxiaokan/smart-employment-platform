import { ref } from "vue";
import { ElMessage } from "element-plus";

// 默认值改 false：未确认状态时按"未通过"对待,避免默认值坑导致守卫误放行
export const companyApproved = ref(false);
export const statusChecked = ref(false);
// 原始审核状态。-1=未查询 0=待审核 1=已通过 2=未通过
export const auditStatus = ref(-1);
export const companyRejected = ref(false);

let fetching = false;

/** 重置所有状态，登录/登出时调用，确保每次登录都重新检测 */
export function resetHrStatus() {
  fetching = false;
  companyApproved.value = false;
  statusChecked.value = false;
  auditStatus.value = -1;
  companyRejected.value = false;
}

export async function checkCompanyStatus() {
  if (fetching || statusChecked.value) return;
  fetching = true;
  try {
    const { getApplicationStatus } = await import("@/api/hr/company");
    const res = await getApplicationStatus();
    if (res.code === 1 && res.data) {
      const c = res.data;
      const status = typeof c.auditStatus === "number" ? c.auditStatus : -1;
      auditStatus.value = status;
      companyApproved.value = status === 1;
      companyRejected.value = status === 2;
      if (c.name) {
        const user = JSON.parse(localStorage.getItem("loginUser") || "{}");
        user.companyName = c.name;
        user.companyId = c.id;
        localStorage.setItem("loginUser", JSON.stringify(user));
      }
      localStorage.setItem("companyInfo", JSON.stringify(c));
    } else {
      // 接口返回异常:严格按"未通过"对待,不强制放行
      ElMessage.warning(res.msg || "企业状态查询异常，部分功能可能受限");
      companyApproved.value = false;
      auditStatus.value = -1;
      companyRejected.value = false;
    }
    statusChecked.value = true;
  } catch (e) {
    // 401 已在全局响应拦截器统一处理（清 token + 跳转 /login），此处不重复操作
    if (e?.response?.status === 401) {
      statusChecked.value = false;
      companyApproved.value = false;
      auditStatus.value = -1;
      companyRejected.value = false;
    } else {
      // 网络异常:按"未通过"对待,守卫会强制跳转到 SetupPending
      ElMessage.warning("网络异常，无法获取企业状态");
      companyApproved.value = false;
      statusChecked.value = true;
      auditStatus.value = -1;
      companyRejected.value = false;
    }
  } finally {
    fetching = false;
  }
}