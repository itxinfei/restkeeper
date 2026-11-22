import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { auth, getToken, isMockMode, setToken } from '@xfcy/api'

export type UserProfile = {
  name: string
  role: string
  shopCount: number
}

/**
 * 用户状态：登录统一走 @xfcy/api auth。
 * - mock 模式（VITE_USE_MOCK=1，默认）：保持首版演示行为，不校验账号密码；
 * - real 模式：调用门店经理登录契约 POST /store/manager/login。
 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken())
  const profile = ref<UserProfile>({
    name: '李国栋',
    role: '门店经理',
    shopCount: 3,
  })

  const isLoggedIn = computed(() => token.value.length > 0)
  const avatarText = computed(() => profile.value.name.slice(0, 1))

  async function login(username: string, password: string, shopId?: string) {
    if (isMockMode()) {
      token.value = `xfcy-demo-${Date.now()}`
      setToken(token.value)
      return
    }
    const result = await auth.loginManager({
      shopId: shopId ?? '',
      phone: username,
      loginPass: password,
    })
    token.value = result.token
    profile.value = {
      name: result.user.storeManagerName,
      role: '门店经理',
      shopCount: result.user.stores?.length ?? 0,
    }
  }

  function logout() {
    auth.logout()
    token.value = ''
  }

  return { token, profile, isLoggedIn, avatarText, login, logout }
})
