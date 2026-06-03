const adminRoutes = [
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'users',
        name: 'AdminUserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'hr',
        name: 'AdminHrManage',
        component: () => import('@/views/admin/HrManage.vue'),
        meta: { title: 'HR管理' }
      },
      {
        path: 'companies',
        name: 'AdminCompanyManage',
        component: () => import('@/views/admin/CompanyManage.vue'),
        meta: { title: '企业/公司管理' }
      },
      {
        path: 'jobs',
        name: 'AdminJobManage',
        component: () => import('@/views/admin/JobManage.vue'),
        meta: { title: '职位管理' }
      },
      {
        path: 'resumes',
        name: 'AdminResumeManage',
        component: () => import('@/views/admin/ResumeManage.vue'),
        meta: { title: '简历管理' }
      },
      {
        path: 'records',
        name: 'AdminRecordManage',
        component: () => import('@/views/admin/RecordManage.vue'),
        meta: { title: '投递记录管理' }
      },
      {
        path: 'logs',
        name: 'AdminLogManage',
        component: () => import('@/views/admin/LogManage.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: 'feedbacks',
        name: 'AdminFeedback',
        component: () => import('@/views/admin/FeedbackManage.vue'),
        meta: { title: '反馈与公告' }
      },
      {
        path: 'account',
        name: 'AdminAccount',
        component: () => import('@/views/admin/AdminAccount.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'docs',
        name: 'AdminProjectDocs',
        component: () => import('@/views/admin/ProjectDocs.vue'),
        meta: { title: '项目文档' }
      }
    ]
  }
];

export default adminRoutes;