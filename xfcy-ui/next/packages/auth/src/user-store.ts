import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { auth, getToken, isMockMode } from '@xfcy/api'

/** 登录身份类型：决定调用哪套登录契约 */
export type UserKind = 'manager' | 'operator' | 'cashier'

export type UserProfile = {
  name: string
  role: string
  /** 门店经理：可管理门店数 */
  shopCount?: number
  /** 收银员：所属门店展示名 */
  shop?: string
}

/**
 * 三端统一用户 store 工厂：登录统一走 @xfcy/api auth。
 * - mock 模式（VITE_USE_MOCK=1，默认）：保持演示行为，不校验账号密码；
 * - real 模式：按 kind 调用门店经理 / 运营人员 / 门店员工登录契约。
 *
 * 各端只需传入身份类型与演示默认资料：
 *   export const useUserStore = createUserStore('manager', { name: '李国栋', role: '门店经理', shopCount: 3 })
 */
export function createUserStore(kind: UserKind, defaults: UserProfile) {
  return defineStore('user', () => {
    const token = ref<string>(getToken())
    const profile = ref<UserProfile>({ ...defaults })

    const isLoggedIn = computed(() => token.value.length > 0)
    const avatarText = computed(() => profile.value.name.slice(0, 1))

    async function login(username: string, password: string, shopId?: string) {
      // mock / real 均由 @xfcy/api 内部按模式分流；token 统一由 api 层签发并落存储，
      // 前端不再自行拼接演示 token（避免多处明文凭据生成逻辑）。
      if (kind === 'manager') {
        const result = await auth.loginManager({
          shopId: shopId ?? '',
          phone: username,
          loginPass: password,
        })
        token.value = result.token
        profile.value = {
          name: result.user.storeManagerName || defaults.name,
          role: '门店经理',
          shopCount: result.user.stores?.length ?? defaults.shopCount ?? 0,
        }
        return
      }
      if (kind === 'operator') {
        const result = await auth.loginOperator(username, password)
        token.value = result.token
        profile.value = { name: isMockMode() ? defaults.name : result.user.loginname, role: '运营人员' }
        return
      }
      const result = await auth.loginStaff(shopId ?? '', username, password)
      token.value = result.token
      profile.value = {
        name: isMockMode() ? defaults.name : result.user.staffName,
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
}
