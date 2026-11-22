import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from './layouts/AppLayout.vue'
import { useUserStore } from './stores/user'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('./views/LoginView.vue'),
      meta: { title: '登录', public: true },
    },
    { path: '/', redirect: '/enterprises' },
    {
      path: '/',
      component: AppLayout,
      children: [
        {
          path: 'enterprises',
          name: 'enterprises',
          component: () => import('./views/EnterpriseAccountsView.vue'),
          meta: { title: '企业账号', group: '账号' },
        },
        {
          path: 'recycle',
          name: 'recycle',
          component: () => import('./views/RecycleBinView.vue'),
          meta: { title: '回收站', group: '账号' },
        },
        {
          path: 'dict',
          name: 'dict',
          component: () => import('./views/DictView.vue'),
          meta: { title: '字典管理', group: '系统' },
        },
        {
          path: 'operators',
          name: 'operators',
          component: () => import('./views/OperatorsView.vue'),
          meta: { title: '运营人员', group: '系统' },
        },
        {
          path: 'monitor',
          name: 'monitor',
          component: () => import('./views/MonitorView.vue'),
          meta: { title: '服务监控', group: '监控' },
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/enterprises' },
  ],
})

router.beforeEach((to) => {
  const user = useUserStore()
  if (!to.meta.public && !user.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && user.isLoggedIn) {
    return { name: 'enterprises' }
  }
  return true
})
