import Vue from 'vue'
import Router from 'vue-router'
import Layout from '@/layout/index.vue'

Vue.use(Router)

export default new Router({
  // mode: 'history',  // Enable this if you need.
  scrollBehavior: (to, from, savedPosition) => {
    if (savedPosition) {
      return savedPosition
    } else {
      return { x: 0, y: 0 }
    }
  },
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/login',
      component: () => import(/* webpackChunkName: "login" */ '@/views/login/index.vue'),
      meta: { title: '心飞餐饮', hidden: true }
    },
    {
      path: '/404',
      component: () => import(/* webpackChunkName: "404" */ '@/views/404.vue'),
      meta: { title: '心飞餐饮', hidden: true }
    },
    {
      path: '/',
      component: Layout,
      redirect: '/member',
      meta: {
        title: '账号管理',
        icon: 'main',
        roles: ['admin']
      },
      children: [
        {
          path: '/member',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/member/index.vue'),
          meta: {
            title: '账号管理'
          }
        },
        {
          path: '/member/add',
          component: () => import(/* webpackChunkName: "dashboard" */ '@/views/member/addMember.vue'),
          meta: {
            title: '新增账号'
          }
        }
       ]
    },
    {
      path: '/shop',
      component: Layout,
      redirect: '/shop/about',
      meta: {
        title: '账号管理',
        icon: 'shop',
        roles: ['shop'],
        hidden:true
      },
      children: [
        {
          path: '/member',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/member/index.vue'),
          meta: {
            title: '账号管理'
          }
        },
        {
          path: '/member/add',
          component: () => import(/* webpackChunkName: "dashboard" */ '@/views/member/addMember.vue'),
          meta: {
            title: '新增账号'
          }
        },
      ]
    },
    {
      path: '/shopAdmin',
      component: Layout,
      redirect: '/shop/about',
      meta: {
        title: '订单中心',
        icon: 'shop',
        roles: ['shop'],
        hidden:true
      },
      children: [
        {
          path: 'about',
          component: () => import(/* webpackChunkName: "tree" */ '@/views/shopAbout/index.vue'),
          meta: {
            title: '订单中心',
            hidden: true,
          }
        },
      ]
    },
    {
      path: '/shopSet',
      component: Layout,
      redirect: '/shop/about',
      meta: {
        title: '平台配置',
        icon: 'shop',
        roles: ['shop'],
        hidden:true
      },
      children: [{
          path: 'member',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/member/index.vue'),
          meta: {
            title: '支付配置'
          }
        },
        {
          path: 'class',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/class/index.vue'),
          meta: {
            title: '新增支付'
          }
        },
        {
          path: 'fond',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/foodType/index.vue'),
          meta: {
            title: '备注配置'
          }
        }
       ]
    },
    {
      path: '*',
      redirect: '/404',
      meta: { hidden: true }
    }
  ]
})
