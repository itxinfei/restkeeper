import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

const TOKEN_KEY = 'xfcy.cashier.token'

export type UserProfile = {
  name: string
  role: string
  shop: string
}

/**
 * 用户状态（示意）：首版用 localStorage token 模拟登录态，
 * 后续接入真实认证接口时仅需替换 login / logout 实现。
 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '')
  const profile = ref<UserProfile>({
    name: '陈晓',
    role: '收银员',
    shop: '心飞餐饮 · 旗舰店',
  })

  const isLoggedIn = computed(() => token.value.length > 0)
  const avatarText = computed(() => profile.value.name.slice(0, 1))

  function login(_username: string, _password: string) {
    // 示意：不校验真实账号密码，仅写入本地 token
    token.value = `xfcy-demo-${Date.now()}`
    localStorage.setItem(TOKEN_KEY, token.value)
  }

  function logout() {
    token.value = ''
    localStorage.removeItem(TOKEN_KEY)
  }

  return { token, profile, isLoggedIn, avatarText, login, logout }
})
