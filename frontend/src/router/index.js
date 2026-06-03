import { createRouter, createWebHashHistory } from "vue-router";
import userRoutes from "./modules/user";
import hrRoutes from "./modules/hr";
import adminRoutes from "./modules/admin";
import { companyApproved, statusChecked } from "@/utils/hrStatus";
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

router.beforeEach((to) => {
  const publicPaths = ['/login'];
  if (!publicPaths.includes(to.path)) {
    const loginUser = JSON.parse(localStorage.getItem('loginUser') || 'null');
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
