import type { NavigationGuard } from 'vue-router'

export type AuthGuardOptions = {
  /** 登录页路由 name */
  loginName: string
  /** 已登录访问登录页时的落地页 name */
  homeName: string
  isLoggedIn: () => boolean
}

/**
 * 三端统一路由守卫：未登录拦截到登录页（带 redirect），已登录访问登录页回落地页。
 * 用法：router.beforeEach(createAuthGuard({ loginName: 'login', homeName: 'dashboard', isLoggedIn: () => useUserStore().isLoggedIn }))
 */
export function createAuthGuard(options: AuthGuardOptions): NavigationGuard {
  return (to) => {
    if (!to.meta.public && !options.isLoggedIn()) {
      return { name: options.loginName, query: { redirect: to.fullPath } }
    }
    if (to.name === options.loginName && options.isLoggedIn()) {
      return { name: options.homeName }
    }
    return true
  }
}
