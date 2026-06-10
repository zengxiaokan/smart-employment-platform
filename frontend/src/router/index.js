import { createRouter, createWebHashHistory } from "vue-router";
import userRoutes from "./modules/user";
import hrRoutes from "./modules/hr";
import adminRoutes from "./modules/admin";
import { companyApproved, statusChecked, auditStatus, companyRejected, checkCompanyStatus } from "@/utils/hrStatus";
import { ElMessage } from "element-plus";

const routes = [
  {
    path: "/",
    redirect: "/login",
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login/index.vue"),
    meta: { title: "登录" },
  },
  ...userRoutes,
  ...hrRoutes,
  ...adminRoutes,
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/NotFound.vue"),
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

function isTokenExpired(token) {
  try {
    // JWT 使用 Base64URL 编码（- 替代 +, _ 替代 /），atob() 只认标准 Base64
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(atob(base64))
    return payload.exp * 1000 < Date.now()
  } catch { return true }
}

// HR 路由白名单:审核中/被拒 HR 也必须能进的页面
// (申请表、状态页、账户设置)
const HR_ALWAYS_ALLOW = new Set([
  "/hr",                    // 首页概览(未审核也能看)
  "/hr/account",            // 账户设置(改资料、重新申请入口)
  "/hr/setup",              // 申请表
  "/hr/setup/pending",      // 状态页(独立页面)
]);

router.beforeEach(async (to) => {
  const publicPaths = ['/login'];
  // loginUser 提到守卫函数顶层,后续 HR 路由判断也要用
  const loginUser = JSON.parse(localStorage.getItem('loginUser') || 'null');
  if (!publicPaths.includes(to.path)) {
    if (!loginUser || !loginUser.token) {
      ElMessage.warning('请先登录');
      return '/login';
    }
    if (isTokenExpired(loginUser.token)) {
      localStorage.removeItem('loginUser');
      ElMessage.warning('登录已过期，请重新登录');
      return '/login';
    }
  }

  // HR 业务子路由拦截:未审核/被拒时跳到 /hr/account
  // (账户中心有审核状态 banner + 重新申请按钮,不让 HR 困在 SetupPending 独立页)
  // 注意:vue-router 解析后 /hr 本身的 path 是 "/hr" 不是 "/hr/",所以必须同时匹配
  const isHrRoute = to.path === '/hr' || to.path.startsWith('/hr/');
  if (isHrRoute && !HR_ALWAYS_ALLOW.has(to.path)) {
    if (loginUser && Number(loginUser.role) === 1) {
      if (!statusChecked.value) {
        try { await checkCompanyStatus(); } catch { /* 失败时由异常分支降级为未通过 */ }
      }
      if (auditStatus.value !== 1) {
        ElMessage.warning(
          companyRejected.value
            ? "企业审核未通过，请前往账户设置查看拒绝原因"
            : "企业正在审核中，请前往账户设置查看进度"
        );
        return '/hr/account';
      }
    }
  }

  // 旧逻辑兜底：meta.needApproval 标记的子路由
  if (to.meta.needApproval && statusChecked.value && !companyApproved.value) {
    ElMessage.warning("企业审核通过后可访问该功能");
    return "/hr/account";
  }
});

// 路由全局后置钩子：动态修改页面标题
router.afterEach((to) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - 智能就业服务平台`;
  }
});

export default router;
