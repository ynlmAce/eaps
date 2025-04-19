import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

// 路由懒加载
const Login = () => import('@/views/login/index.vue')
const Register = () => import('@/views/register/index.vue')
const ForgotPassword = () => import('@/views/forgot-password/index.vue')
const ResetPassword = () => import('@/views/reset-password/index.vue')
const Dashboard = () => import('@/views/dashboard/index.vue')
const NotFound = () => import('@/views/error/404.vue')

// 公共路由
export const constantRoutes = [
  {
    path: '/login',
    component: Login,
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/register',
    component: Register,
    meta: { title: '注册', hidden: true }
  },
  {
    path: '/forgot-password',
    component: ForgotPassword,
    meta: { title: '找回密码', hidden: true }
  },
  {
    path: '/reset-password',
    component: ResetPassword,
    meta: { title: '重置密码', hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: Dashboard,
        name: 'Dashboard',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/profile',
    component: Layout,
    hidden: true,
    children: [
      {
        path: 'index',
        component: () => import('@/views/profile/index.vue'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    component: NotFound,
    hidden: true
  }
]

// 动态路由，根据用户角色动态加载
export const asyncRoutes = [
  // 学生相关路由
  {
    path: '/job',
    component: Layout,
    meta: { title: '就业招聘', icon: 'building', roles: ['student'] },
    children: [
      {
        path: 'position',
        component: () => import('@/views/job/position/index.vue'),
        name: 'JobPosition',
        meta: { title: '职位浏览', icon: 'list' }
      },
      {
        path: 'position/:id',
        component: () => import('@/views/job/position/detail.vue'),
        name: 'PositionDetail',
        meta: { title: '职位详情', activeMenu: '/job/position', hidden: true }
      },
      {
        path: 'application',
        component: () => import('@/views/job/my-applications.vue'),
        name: 'MyApplication',
        meta: { title: '我的申请', icon: 'document' }
      },
      {
        path: 'resume',
        component: () => import('@/views/resume/my-resume.vue'),
        name: 'MyResume',
        meta: { title: '我的简历', icon: 'user' }
      }
    ]
  },
  {
    path: '/enterprise',
    component: Layout,
    meta: { title: '企业信息', icon: 'office-building', roles: ['student'] },
    children: [
      {
        path: 'list',
        component: () => import('@/views/enterprise/list.vue'),
        name: 'EnterpriseList',
        meta: { title: '企业列表', icon: 'list' }
      },
      {
        path: 'detail/:id',
        component: () => import('@/views/enterprise/detail.vue'),
        name: 'EnterpriseDetail',
        meta: { title: '企业详情', activeMenu: '/enterprise/list', hidden: true }
      },
      {
        path: 'rating',
        component: () => import('@/views/enterprise/rating.vue'),
        name: 'EnterpriseRating',
        meta: { title: '企业评分', icon: 'star' }
      },
      {
        path: 'promotion',
        component: () => import('@/views/enterprise/promotion.vue'),
        name: 'EnterprisePromotion',
        meta: { title: '企业宣传', icon: 'megaphone' }
      }
    ]
  },
  
  // 企业相关路由
  {
    path: '/recruit',
    component: Layout,
    meta: { title: '招聘管理', icon: 'user-search', roles: ['enterprise'] },
    children: [
      {
        path: 'position',
        component: () => import('@/views/recruit/position/index.vue'),
        name: 'PositionManage',
        meta: { title: '职位管理', icon: 'list-check' }
      },
      {
        path: 'position/add',
        component: () => import('@/views/recruit/position/edit.vue'),
        name: 'PositionAdd',
        meta: { title: '发布职位', activeMenu: '/recruit/position', hidden: true }
      },
      {
        path: 'position/edit/:id',
        component: () => import('@/views/recruit/position/edit.vue'),
        name: 'PositionEdit',
        meta: { title: '编辑职位', activeMenu: '/recruit/position', hidden: true }
      },
      {
        path: 'application',
        component: () => import('@/views/recruit/application/index.vue'),
        name: 'ApplicationManage',
        meta: { title: '申请管理', icon: 'document-text' }
      }
    ]
  },
  {
    path: '/promotion',
    component: Layout,
    meta: { title: '宣传管理', icon: 'speakerphone', roles: ['enterprise'] },
    children: [
      {
        path: 'index',
        component: () => import('@/views/promotion/index.vue'),
        name: 'PromotionManage',
        meta: { title: '宣传内容管理', icon: 'document' }
      },
      {
        path: 'add',
        component: () => import('@/views/promotion/edit.vue'),
        name: 'PromotionAdd',
        meta: { title: '发布宣传', activeMenu: '/promotion/index', hidden: true }
      },
      {
        path: 'edit/:id',
        component: () => import('@/views/promotion/edit.vue'),
        name: 'PromotionEdit',
        meta: { title: '编辑宣传', activeMenu: '/promotion/index', hidden: true }
      }
    ]
  },
  {
    path: '/rating',
    component: Layout,
    meta: { title: '评分管理', icon: 'star', roles: ['enterprise'] },
    children: [
      {
        path: 'list',
        component: () => import('@/views/rating/list.vue'),
        name: 'RatingList',
        meta: { title: '评分列表', icon: 'list' }
      },
      {
        path: 'appeal',
        component: () => import('@/views/rating/appeal.vue'),
        name: 'RatingAppeal',
        meta: { title: '评分申诉', icon: 'exclamation-circle' }
      }
    ]
  },
  
  // 管理员相关路由
  {
    path: '/admin',
    component: Layout,
    meta: { title: '评价管理', icon: 'star', roles: ['admin', 'superAdmin'] },
    children: [
      {
        path: 'ratings',
        component: () => import('@/views/admin/ratings/index.vue'),
        name: 'AdminRatings',
        meta: { title: '企业评价管理', icon: 'star' }
      },
      {
        path: 'promotion',
        component: () => import('@/views/admin/promotion/index.vue'),
        name: 'AdminPromotion',
        meta: { title: '宣传内容管理', icon: 'speakerphone' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    meta: { title: '系统管理', icon: 'cog', roles: ['admin', 'superAdmin'] },
    children: [
      {
        path: 'user',
        component: () => import('@/views/system/user/index.vue'),
        name: 'UserManage',
        meta: { title: '用户管理', icon: 'users' }
      },
      {
        path: 'role',
        component: () => import('@/views/system/role/index.vue'),
        name: 'RoleManage',
        meta: { title: '角色管理', icon: 'shield-check' }
      },
      {
        path: 'permission',
        component: () => import('@/views/system/permission/index.vue'),
        name: 'PermissionManage',
        meta: { title: '权限管理', icon: 'key' }
      },
      {
        path: 'config',
        component: () => import('@/views/system/config/index.vue'),
        name: 'ConfigManage',
        meta: { title: '参数设置', icon: 'adjustments' }
      },
      {
        path: 'log',
        component: () => import('@/views/system/log/index.vue'),
        name: 'LogManage',
        meta: { title: '操作日志', icon: 'document-text' }
      }
    ]
  },
  {
    path: '/audit',
    component: Layout,
    meta: { title: '审核管理', icon: 'clipboard-check', roles: ['admin', 'superAdmin'] },
    children: [
      {
        path: 'enterprise',
        component: () => import('@/views/audit/enterprise/index.vue'),
        name: 'EnterpriseAudit',
        meta: { title: '企业审核', icon: 'office-building' }
      },
      {
        path: 'position',
        component: () => import('@/views/audit/position/index.vue'),
        name: 'PositionAudit',
        meta: { title: '职位审核', icon: 'briefcase' }
      },
      {
        path: 'promotion',
        component: () => import('@/views/audit/promotion/index.vue'),
        name: 'PromotionAudit',
        meta: { title: '宣传审核', icon: 'speakerphone' }
      },
      {
        path: 'rating',
        component: () => import('@/views/audit/rating/index.vue'),
        name: 'RatingAudit',
        meta: { title: '评分审核', icon: 'star' }
      },
      {
        path: 'appeal',
        component: () => import('@/views/audit/appeal/index.vue'),
        name: 'AppealAudit',
        meta: { title: '申诉处理', icon: 'exclamation-circle' }
      }
    ]
  },
  
  // 辅导员相关路由
  {
    path: '/counselor',
    component: Layout,
    meta: { title: '学生管理', icon: 'academic-cap', roles: ['counselor'] },
    children: [
      {
        path: 'student',
        component: () => import('@/views/counselor/student/index.vue'),
        name: 'StudentManage',
        meta: { title: '学生列表', icon: 'users' }
      },
      {
        path: 'employment',
        component: () => import('@/views/counselor/employment/index.vue'),
        name: 'EmploymentManage',
        meta: { title: '就业情况', icon: 'chart-bar' }
      },
      {
        path: 'statistics',
        component: () => import('@/views/counselor/employment-statistics.vue'),
        name: 'EmploymentStatistics',
        meta: { title: '就业统计', icon: 'chart-pie' }
      }
    ]
  },
  
  // 消息中心路由
  {
    path: '/message',
    component: Layout,
    meta: { title: '消息中心', icon: 'chat-dot-round', roles: ['student', 'enterprise', 'counselor', 'admin', 'superAdmin'] },
    children: [
      {
        path: 'index',
        component: () => import('@/views/message/index.vue'),
        name: 'MessageCenter',
        meta: { title: '消息列表', icon: 'chat-line-round' }
      },
      {
        path: 'chat/:id',
        component: () => import('@/views/message/chat.vue'),
        name: 'ChatSession',
        meta: { title: '聊天会话', activeMenu: '/message/index', hidden: true }
      },
      {
        path: 'group/:id',
        component: () => import('@/views/message/group.vue'),
        name: 'GroupChat',
        meta: { title: '群组聊天', activeMenu: '/message/index', hidden: true }
      },
      {
        path: 'groups',
        component: () => import('@/views/message/groups.vue'),
        name: 'GroupList',
        meta: { title: '查找群组', activeMenu: '/message/index', hidden: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})
// 新增：重置路由方法
export function resetRouter() {
  const newRouter = createRouter({
    history: createWebHistory(),
    routes: constantRoutes // 这里我们只保留静态路由
  })
  router.matcher = newRouter.matcher // reset router
}

export default router