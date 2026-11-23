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
    { path: '/', redirect: '/dashboard' },
    {
      path: '/',
      component: AppLayout,
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('./views/DashboardView.vue'),
          meta: { title: '工作台', group: '经营' },
        },
        {
          path: 'dishes',
          name: 'dishes',
          component: () => import('./views/DishesView.vue'),
          meta: { title: '菜品管理', group: '经营' },
        },
        {
          path: 'combos',
          name: 'combos',
          component: () => import('./views/PlaceholderView.vue'),
          meta: { title: '套餐管理', group: '经营' },
        },
        {
          path: 'tables',
          name: 'tables',
          component: () => import('./views/TablesView.vue'),
          meta: { title: '桌台管理', group: '经营' },
        },
        {
          path: 'staff',
          name: 'staff',
          component: () => import('./views/StaffView.vue'),
          meta: { title: '员工管理', group: '经营' },
        },
        {
          path: 'credit',
          name: 'credit',
          component: () => import('./views/CreditView.vue'),
          meta: { title: '挂账管理', group: '账务' },
        },
        {
          path: 'soldout',
          name: 'soldout',
          component: () => import('./views/SoldoutView.vue'),
          meta: { title: '沽清管理', group: '账务' },
        },
        {
          path: 'reports',
          name: 'reports',
          component: () => import('./views/ReportsView.vue'),
          meta: { title: '经营报表', group: '数据' },
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.beforeEach((to) => {
  const user = useUserStore()
  if (!to.meta.public && !user.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && user.isLoggedIn) {
    return { name: 'dashboard' }
  }
  return true
})
