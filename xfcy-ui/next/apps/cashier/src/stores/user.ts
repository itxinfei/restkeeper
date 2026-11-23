import { createUserStore } from '@xfcy/auth'

export type { UserProfile } from '@xfcy/auth'

/** 门店员工（收银）：登录走 POST /staff/login（shopId/loginName/password） */
export const useUserStore = createUserStore('cashier', {
  name: '陈晓',
  role: '收银员',
  shop: '心飞餐饮 · 旗舰店',
})
