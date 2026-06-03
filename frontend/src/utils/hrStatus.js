import { ref } from "vue";
import { ElMessage } from "element-plus";
import router from "@/router";

export const companyApproved = ref(true);
export const statusChecked = ref(false);

let fetching = false;

export async function checkCompanyStatus() {
  if (fetching || statusChecked.value) return;
  fetching = true;
  try {
    const { getApplicationStatus } = await import("@/api/hr/company");
    const res = await getApplicationStatus();
    if (res.code === 1 && res.data) {
      const c = res.data;
      companyApproved.value = c.auditStatus === 1;
      if (c.name) {
        const user = JSON.parse(localStorage.getItem("loginUser") || "{}");
        user.companyName = c.name;
        user.companyId = c.id;
        localStorage.setItem("loginUser", JSON.stringify(user));
      }
      localStorage.setItem("companyInfo", JSON.stringify(c));
    } else {
      ElMessage.warning(res.msg || "企业状态查询异常，部分功能可能受限");
      companyApproved.value = true;
    }
    statusChecked.value = true;
  } catch (e) {
    if (e?.response?.status === 401) {
      ElMessage.error("登录已过期，请重新登录");
      localStorage.removeItem("loginUser");
      statusChecked.value = false;
      companyApproved.value = true;
      router.replace("/login");
    } else {
      ElMessage.warning("网络异常，无法获取企业状态");
      companyApproved.value = true;
      statusChecked.value = true;
    }
  } finally {
    fetching = false;
  }
}