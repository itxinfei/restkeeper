import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { auth, getToken, isMockMode, setToken } from '@xfcy/api'

export type UserProfile = {
  name: string
  role: string
  shop: string
}

/**
 * 用户状态：登录统一走 @xfcy/api auth。
 * - mock 模式（VITE_USE_MOCK=1，默认）：保持演示行为，不校验账号密码；
 * - real 模式：调用门店员工登录契约 POST /staff/login（shopId/loginName/password）。
 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken())
  const profile = ref<UserProfile>({
    name: '陈晓',
    role: '收银员',
    shop: '心飞餐饮 · 旗舰店',
  })

  const isLoggedIn = computed(() => token.value.length > 0)
  const avatarText = computed(() => profile.value.name.slice(0, 1))

  async function login(username: string, password: string, shopId?: string) {
    if (isMockMode()) {
      token.value = `xfcy-demo-${Date.now()}`
      setToken(token.value)
      return
    }
    const result = await auth.loginStaff(shopId ?? '', username, password)
    token.value = result.token
    profile.value = {
      name: result.user.staffName,
      role: '收银员',
      shop: shopId ? `门店 ${shopId}` : '心飞餐饮',
    }
  }

  function logout() {
    auth.logout()
    token.value = ''
  }

  return { token, profile, isLoggedIn, avatarText, login, logout }
})
