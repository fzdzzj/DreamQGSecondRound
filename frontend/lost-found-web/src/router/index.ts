import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import UserLayout from '@/layouts/UserLayout.vue'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/register',
    component: () => import('@/views/register/index.vue')
  },

  // 用户端
  {
    path: '/',
    component: UserLayout,
    children: [
      { path: 'home', component: () => import('@/views/user/home/index.vue') },
      { path: 'item/list', component: () => import('@/views/user/item/list.vue') },
      { path: 'item/detail/:id', component: () => import('@/views/user/item/detail.vue') },
      { path: 'item/my', component: () => import('@/views/user/item/my.vue') },
      { path: 'item/publish-lost', component: () => import('@/views/user/item/publish-lost.vue') },
      { path: 'item/publish-found', component: () => import('@/views/user/item/publish-found.vue') },
      { path: 'item/edit/:id', component: () => import('@/views/user/item/edit.vue') },
      { path: 'ai', component: () => import('@/views/user/ai/index.vue') },
      { path: 'message', component: () => import('@/views/user/message/index.vue') },
      { path: 'notification', component: () => import('@/views/user/notification/index.vue') },
      { path: 'profile', component: () => import('@/views/user/profile/index.vue') },
      { path: 'profile/change-password', component: () => import('@/views/user/profile/change-password.vue') }
      , { path: 'claim/pending', component: () => import('@/views/user/claim/pending.vue') }
    ]
  },

  // 管理端
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/admin/dashboard/index.vue')
      },
      {
        path: 'item/list',
        component: () => import('@/views/user/item/list.vue')
      },
      {
        path: 'user',
        component: () => import('@/views/admin/user/index.vue')
      },
      {
        path: 'report',
        component: () => import('@/views/admin/report/index.vue')
      },
      {
        path: 'report/:id',
        component: () => import('@/views/admin/report/detail.vue')
      },
      {
        path: 'pin',
        component: () => import('@/views/admin/pin/index.vue')
      },
      {
        path: 'pin/:id',
        component: () => import('@/views/admin/pin/detail.vue')
      },
      {
        path: 'risk',
        component: () => import('@/views/admin/risk/index.vue')
      },
      {
        path: 'risk/:id',
        component: () => import('@/views/admin/risk/detail.vue')
      },
      {
        path: 'item',
        component: () => import('@/views/admin/item/index.vue')
      },
      {
        path: 'log',
        component: () => import('@/views/admin/log/index.vue')
      }
    ]
  }
]

export default createRouter({
  history: createWebHistory(),
  routes
})