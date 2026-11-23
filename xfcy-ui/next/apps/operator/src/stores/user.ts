import { createUserStore } from '@xfcy/auth'

export type { UserProfile } from '@xfcy/auth'

/** 运营人员：登录走 POST /operator/user/login */
export const useUserStore = createUserStore('operator', {
  name: '王运营',
  role: '平台运营',
})
