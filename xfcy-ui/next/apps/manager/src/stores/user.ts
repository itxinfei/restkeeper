import { createUserStore } from '@xfcy/auth'

export type { UserProfile } from '@xfcy/auth'

/** 门店经理：登录走 POST /store/manager/login */
export const useUserStore = createUserStore('manager', {
  name: '李国栋',
  role: '门店经理',
  shopCount: 3,
})
