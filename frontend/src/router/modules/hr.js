import { companyApproved, checkCompanyStatus } from "@/utils/hrStatus";

const hrRoutes = [
  {
    path: "/hr/setup",
    name: "HrSetup",
    component: () => import("@/views/hr/CompanySetup.vue"),
    meta: { title: "企业绑定" },
  },
  {
    path: "/hr/setup/pending",
    name: "HrSetupPending",
    component: () => import("@/views/hr/SetupPending.vue"),
    meta: { title: "审核中" },
  },
  {
    path: "/hr",
    component: () => import("@/layout/HrLayout.vue"),
    children: [
      {
        path: "",
        name: "HrDashboard",
        component: () => import("@/views/hr/Dashboard.vue"),
        meta: { title: "首页概览" },
      },
      {
        path: "ai-match",
        name: "HrAiMatch",
        component: () => import("@/views/hr/AiMatch.vue"),
        meta: { title: "智能匹配", needApproval: true },
      },
      {
        path: "search",
        name: "HrSearch",
        component: () => import("@/views/hr/TalentSearch.vue"),
        meta: { title: "人才搜索", needApproval: true },
      },
      {
        path: "talent-pool",
        name: "HrTalentPool",
        component: () => import("@/views/hr/TalentPool.vue"),
        meta: { title: "收藏夹", needApproval: true },
      },
      {
        path: "jobs",
        name: "HrJobs",
        component: () => import("@/views/hr/JobManage.vue"),
        meta: { title: "职位管理", needApproval: true },
      },
      {
        path: "jobs/create",
        name: "HrJobCreate",
        component: () => import("@/views/hr/JobForm.vue"),
        meta: { title: "发布职位", needApproval: true },
      },
      {
        path: "jobs/edit",
        name: "HrJobEdit",
        component: () => import("@/views/hr/JobForm.vue"),
        meta: { title: "编辑职位", needApproval: true },
      },
      {
        path: "applications",
        name: "HrApplications",
        component: () => import("@/views/hr/ApplicationManage.vue"),
        meta: { title: "简历管理", needApproval: true },
      },
      {
        path: "interview",
        name: "HrInterview",
        component: () => import("@/views/hr/InterviewManage.vue"),
        meta: { title: "面试管理", needApproval: true },
      },
      {
        path: "analytics",
        name: "HrAnalytics",
        component: () => import("@/views/hr/DataAnalytics.vue"),
        meta: { title: "数据分析", needApproval: true },
      },
      {
        path: "account",
        name: "HrAccount",
        component: () => import("@/views/hr/AccountPage.vue"),
        meta: { title: "账户设置" },
      },
      {
        path: "company",
        redirect: "/hr/account",
      },
    ],
  },
];

export default hrRoutes;
