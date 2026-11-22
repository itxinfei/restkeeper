import { createRouter, createWebHistory } from 'vue-router'
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

router.beforeEach((to) => {
  const user = useUserStore()
  if (!to.meta.public && !user.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && user.isLoggedIn) {
    return { name: 'tables' }
  }
  return true
})
