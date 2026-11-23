import { createRouter, createWebHistory } from 'vue-router'
import { createAuthGuard } from '@xfcy/auth'
import PosLayout from './layouts/PosLayout.vue'
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
    { path: '/', redirect: '/tables' },
    {
      path: '/',
      component: PosLayout,
      children: [
        {
          path: 'tables',
          name: 'tables',
          component: () => import('./views/TablesView.vue'),
          meta: { title: '桌台盘点' },
        },
        {
          path: 'order/:tableId',
          name: 'order',
          component: () => import('./views/OrderView.vue'),
          meta: { title: '点餐收银' },
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/tables' },
  ],
})

router.beforeEach(
  createAuthGuard({
    loginName: 'login',
    homeName: 'tables',
    isLoggedIn: () => useUserStore().isLoggedIn,
  }),
)
