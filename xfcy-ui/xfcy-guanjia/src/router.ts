import Vue from 'vue';
import Router from 'vue-router';
import Layout from '@/layout/index.vue';

Vue.use(Router);

export default new Router({
    // mode: 'history',  // Enable this if you need.
    'scrollBehavior': (to, from, savedPosition) => {
        if (savedPosition) {
            return savedPosition;
        }
        return { 'x': 0, 'y': 0 };

    },
    'base': process.env.BASE_URL,
    'routes': [
        {
            'path': '/login',
            'component': () => import(/* webpackChunkName: "login" */ '@/views/login/index.vue'),
            'meta': { 'title': '心飞餐饮', 'hidden': true }
        },
        {
            'path': '/404',
            'component': () => import(/* webpackChunkName: "404" */ '@/views/404.vue'),
            'meta': { 'title': '心飞餐饮', 'hidden': true }
        },
        {
            'path': '/',
            'component': Layout,
            'redirect': '/brand',
            'meta': {
                'title': '集团管理',
                'icon': 'main',
                'roles': ['admin']
            },
            'children': [
                {
                    'path': 'brand',
                    'component': () => import(/* webpackChunkName: "dashboard" */ '@/views/brand/index.vue'),
                    'meta': {
                        'title': '品牌管理'
                    }
                },
                {
                    'path': '/brand/add',
                    'component': () => import(/* webpackChunkName: "dashboard" */ '@/views/brand/addBrand.vue'),
                    'meta': {
                        'title': '添加品牌',
                        'hidden': true
                    }
                },
                {
                    'path': '/shop',
                    'component': () => import(/* webpackChunkName: "dashboard" */ '@/views/shop/index.vue'),
                    'meta': {
                        'title': '门店管理'
                    }
                },
                {
                    'path': '/shop/add',
                    'component': () => import(/* webpackChunkName: "dashboard" */ '@/views/shop/addShop.vue'),
                    'meta': {
                        'title': '添加门店',
                        'hidden': true
                    }
                },
                {
                    'path': '/shopowner',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/shopowner/index.vue'),
                    'meta': {
                        'title': '店长管理'
                    }
                },
                {
                    'path': '/shopowner/addShopowner',
                    'component': () => import(/* webpackChunkName: "dashboard" */ '@/views/shopowner/addShopowner.vue'),
                    'meta': {
                        'title': '添加店长',
                        'hidden': true
                    }
                }
            ]
        },
        {
            'path': '/shopAdmin',
            'component': Layout,
            'redirect': '/shop/about',
            'meta': {
                'title': '门店管理',
                'icon': 'shop',
                'roles': ['shop']
            },
            'children': [
                {
                    'path': 'about',
                    'component': () => import(/* webpackChunkName: "tree" */ '@/views/shopAbout/index.vue'),
                    'meta': {
                        'title': '门店概况'
                    }
                },
                {
                    'path': 'member',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/member/index.vue'),
                    'meta': {
                        'title': '员工管理'
                    }
                },
                {
                    'path': 'class',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/class/index.vue'),
                    'meta': {
                        'title': '分类管理'
                    }
                },
                {
                    'path': '/member/add',
                    'component': () => import(/* webpackChunkName: "dashboard" */ '@/views/member/addMember.vue'),
                    'meta': {
                        'title': '添加员工',
                        'hidden': true
                    }
                },
                {
                    'path': 'fond',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/foodType/index.vue'),
                    'meta': {
                        'title': '菜品管理'
                    }
                },
                {
                    'path': '/fond/add',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/foodType/addFoodtype.vue'),
                    'meta': {
                        'title': '添加菜品',
                        'hidden': true
                    }
                },
                {
                    'path': 'setmeal',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/package/index.vue'),
                    'meta': {
                        'title': '套餐管理'
                    }
                },
                {
                    'path': '/setmeal/add',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/package/addSetmeal.vue'),
                    'meta': {
                        'title': '添加套餐',
                        'hidden': true
                    }
                },
                {
                    'path': 'shopTable',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/shopTable/index.vue'),
                    'meta': {
                        'title': '桌台管理'
                    }
                },
                {
                    'path': '/shopTable/add',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/shopTable/addTable.vue'),
                    'meta': {
                        'title': '新增桌台',
                        'hidden': true
                    }
                },
                {
                    'path': 'area',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/area/index.vue'),
                    'meta': {
                        'title': '区域管理'
                    }
                },
                {
                    'path': 'accounts',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/account/index.vue'),
                    'meta': {
                        'title': '挂账管理'
                    }
                },
                {
                    'path': '/accounts/add',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/account/addAccount.vue'),
                    'meta': {
                        'title': '添加挂账',
                        'hidden': true
                    }
                },
                {
                    'path': '/accounts/order',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/account/order.vue'),
                    'meta': {
                        'title': '订单详情',
                        'hidden': true
                    }
                },
                {
                    'path': '/accounts/repayment',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/account/repayment.vue'),
                    'meta': {
                        'title': '还款',
                        'hidden': true
                    }
                },
                {
                    'path': 'pay',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/pay/index.vue'),
                    'meta': {
                        'title': '支付方式',
                        'hidden': true
                    }
                },
                {
                    'path': 'shop/set',
                    'component': () => import(/* webpackChunkName: "shopTable" */ '@/views/shopSet/index.vue'),
                    'meta': {
                        'title': '门店设置'
                    }
                }
            ]
        },
        {
            'path': '/chart',
            'component': Layout,
            'redirect': '/chart/about',
            'meta': {
                'title': '收银报表',
                'icon': 'shop',
                'roles': ['shop']
            },
            'children': [
                {
                    'path': 'about',
                    'component': () => import(/* webpackChunkName: "tree" */ '@/views/chart/index.vue'),
                    'meta': {
                        'title': '营收概况'
                    }
                }
            ]
        },
        {
            'path': '*',
            'redirect': '/404',
            'meta': { 'hidden': true }
        }
    ]
});
