const userRoutes = [
  {
    path: "/user",
    component: () => import("@/layout/UserLayout.vue"),
    children: [
      {
        path: "",
        name: "UserHome",
        component: () => import("@/views/user/index.vue"),
        meta: { title: "平台介绍" },
      },
      {
        path: "jobs",
        name: "UserJobs",
        component: () => import("@/views/user/Jobs.vue"),
        meta: { title: "职位广场" },
      },
      {
        path: "resume",
        name: "UserResume",
        component: () => import("@/views/user/ResumeList.vue"),
        meta: { title: "我的简历" },
      },
      {
        path: "resume/edit",
        name: "UserResumeEdit",
        component: () => import("@/views/user/ResumeEdit.vue"),
        meta: { title: "编辑简历" },
      },
      {
        path: "messages",
        name: "UserMessage",
        component: () => import("@/views/user/Company.vue"),
        meta: { title: "公司列表" },
      },
      {
        path: "company/:id",
        name: "UserCompanyDetail",
        component: () => import("@/views/user/CompanyDetail.vue"),
        meta: { title: "公司详情" },
      },
      {
        path: "profile",
        name: "UserProfile",
        component: () => import("@/views/user/profile.vue"),
        meta: { title: "个人中心" },
      },
    
    ],
  },
];

export default userRoutes;
