import type { RouteRecordRaw } from 'vue-router'
import { isAdminRole } from '@/utils/auth'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: {
      title: '注册',
      requiresAuth: false
    }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: {
      title: '无权限',
      requiresAuth: false
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      requiresAuth: false
    }
  },
  {
    path: 'profile/change-password',
    component: () => import('@/views/user/profile/change-password.vue'),
    meta: { title: '修改密码', requiresAuth: true }
  },

  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: {
      requiresAuth: true,
      title: '用户端'
    },
    children: [
      {
        path: 'home',
        name: 'UserHome',
        component: () => import('@/views/user/home/index.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'item/list',
        name: 'ItemList',
        component: () => import('@/views/user/item/list.vue'),
        meta: { title: '物品广场', requiresAuth: true }
      },
      {
        path: 'item/detail/:id',
        name: 'ItemDetail',
        component: () => import('@/views/user/item/detail.vue'),
        meta: { title: '物品详情', requiresAuth: true }
      },
      {
        path: 'item/publish-lost',
        name: 'PublishLost',
        component: () => import('@/views/user/item/publish-lost.vue'),
        meta: { title: '发布丢失物品', requiresAuth: true }
      },
      {
        path: 'item/publish-found',
        name: 'PublishFound',
        component: () => import('@/views/user/item/publish-found.vue'),
        meta: { title: '发布拾取物品', requiresAuth: true }
      },
      {
        path: 'item/edit/:id',
        name: 'EditItem',
        component: () => import('@/views/user/item/edit.vue'),
        meta: { title: '编辑物品', requiresAuth: true }
      },
      {
        path: 'item/my',
        name: 'MyItems',
        component: () => import('@/views/user/item/my.vue'),
        meta: { title: '我的物品', requiresAuth: true }
      },
      {
        path: 'ai',
        name: 'AiPage',
        component: () => import('@/views/user/ai/index.vue'),
        meta: { title: 'AI 助手', requiresAuth: true }
      },
      {
        path: 'message',
        name: 'PrivateMessage',
        component: () => import('@/views/user/message/index.vue'),
        meta: { title: '私信', requiresAuth: true }
      },
      {
        path: 'notification',
        name: 'Notification',
        component: () => import('@/views/user/notification/index.vue'),
        meta: { title: '通知', requiresAuth: true }
      },
      {
        path: 'pin/my',
        name: 'MyPin',
        component: () => import('@/views/user/pin/my.vue'),
        meta: { title: '我的置顶申请', requiresAuth: true }
      },
      {
        path: 'claim/pending',
        name: 'PendingClaim',
        component: () => import('@/views/user/claim/pending.vue'),
        meta: { title: '认领申请', requiresAuth: true }
      },
      {
        path: 'report/my',
        name: 'MyReport',
        component: () => import('@/views/user/report/my.vue'),
        meta: { title: '我的举报', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/profile/index.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: {
      requiresAuth: true,
      title: '管理端',
      adminOnly: true
    },
    beforeEnter: () => {
      const role = localStorage.getItem('USER_ROLE') || ''
      return isAdminRole(role) ? true : '/403'
    },
    children: [
      {
        path: 'log',
        name: 'AdminLog',
        component: () => import('@/views/admin/log/index.vue'),
        meta: { title: '操作日志', requiresAuth: true, adminOnly: true }
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard/index.vue'),
        meta: { title: '统计面板', requiresAuth: true, adminOnly: true }
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/admin/user/index.vue'),
        meta: { title: '用户管理', requiresAuth: true, adminOnly: true }
      },
      {
        path: 'report',
        name: 'AdminReport',
        component: () => import('@/views/admin/report/index.vue'),
        meta: { title: '举报管理', requiresAuth: true, adminOnly: true }
      },
      {
        path: 'report/:id',
        name: 'AdminReportDetail',
        component: () => import('@/views/admin/report/detail.vue'),
        meta: { title: '举报详情', requiresAuth: true, adminOnly: true }
      },
      {
        path: 'pin',
        name: 'AdminPin',
        component: () => import('@/views/admin/pin/index.vue'),
        meta: { title: '置顶审核', requiresAuth: true, adminOnly: true }
      },
      {
        path: 'pin/:id',
        name: 'AdminPinDetail',
        component: () => import('@/views/admin/pin/detail.vue'),
        meta: { title: '置顶详情', requiresAuth: true, adminOnly: true }
      },
      {
        path: 'risk',
        name: 'AdminRisk',
        component: () => import('@/views/admin/risk/index.vue'),
        meta: { title: '风险事件', requiresAuth: true, adminOnly: true }
      },
      {
        path: 'risk/:id',
        name: 'AdminRiskDetail',
        component: () => import('@/views/admin/risk/detail.vue'),
        meta: { title: '风险详情', requiresAuth: true, adminOnly: true }
      },
      {
        path: 'item',
        name: 'AdminItem',
        component: () => import('@/views/admin/item/index.vue'),
        meta: { title: '物品管理', requiresAuth: true, adminOnly: true }
      }
    ]
  }
]
