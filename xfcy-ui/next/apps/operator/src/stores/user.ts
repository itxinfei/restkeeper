import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { auth, getToken, isMockMode, setToken } from '@xfcy/api'

export type UserProfile = {
  name: string
  role: string
}

/**
 * 用户状态：登录统一走 @xfcy/api auth。
 * - mock 模式（VITE_USE_MOCK=1，默认）：保持演示行为，不校验账号密码；
 * - real 模式：调用运营人员登录契约 POST /operator/user/login。
 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken())
  const profile = ref<UserProfile>({
    name: '王运营',
    role: '平台运营',
  })

  const isLoggedIn = computed(() => token.value.length > 0)
  const avatarText = computed(() => profile.value.name.slice(0, 1))

  async function login(username: string, password: string) {
    if (isMockMode()) {
      token.value = `xfcy-demo-${Date.now()}`
      setToken(token.value)
      return
    }
    const result = await auth.loginOperator(username, password)
    token.value = result.token
    profile.value = {
      name: result.user.loginname,
      role: '运营人员',
    }
  }

  function logout() {
    auth.logout()
    token.value = ''
  }

  return { token, profile, isLoggedIn, avatarText, login, logout }
})
