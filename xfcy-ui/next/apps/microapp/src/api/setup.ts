/**
 * @xfcy/api 运行时装配（C 端 / Taro）。
 *
 * - 默认 mock（TARO_APP_USE_MOCK=1），行为与原型一致；置 0 走真实后端；
 * - 真实模式 baseURL 默认 /microApp（网关 microapp-web 路由，见 deploy/nacos/gateway.yml）；
 * - token 走 Taro Storage，401 仅提示（C 端无独立登录页，重新进入小程序即可）。
 *
 * 环境变量（Taro 约定 .env，前缀 TARO_APP_）：
 * - TARO_APP_USE_MOCK=1|0
 * - TARO_APP_API_BASE_URL=/microApp
 * - TARO_APP_API_TIMEOUT=15000
 */
import Taro from '@tarojs/taro'
import { configureApi } from '@xfcy/api'
import { createTaroStorage, createTaroTransport } from './transport'

/**
 * 读取 Taro 注入的环境变量。
 * Taro 通过 DefinePlugin 把 .env 中 TARO_APP_* 替换为字面量；未定义时运行期
 * 可能不存在 process，直接回退默认值。
 */
function readTaroEnv(): Record<string, string | undefined> {
  const env: Record<string, string | undefined> = {}
  try {
    env.useMock = process.env.TARO_APP_USE_MOCK
    env.baseURL = process.env.TARO_APP_API_BASE_URL
    env.timeout = process.env.TARO_APP_API_TIMEOUT
  } catch {
    /* process 未注入：使用默认值 */
  }
  return env
}

let configured = false

/** 装配 API 层（幂等，App 启动时调用一次） */
export function setupApi(): void {
  if (configured) return
  configured = true

  const env = readTaroEnv()
  const baseURL = env.baseURL || '/microApp'
  const timeout = Number(env.timeout) > 0 ? Number(env.timeout) : 15000

  configureApi({
    baseURL,
    timeout,
    mock: (env.useMock ?? '1') === '1',
    tokenHeader: 'Authorization',
    tokenStorageKey: 'xfcy.microapp.token',
    userStorageKey: 'xfcy.microapp.user',
    storage: createTaroStorage(),
    transport: createTaroTransport(baseURL, timeout),
    onUnauthorized: () => {
      Taro.showToast({ title: '登录已过期，请重新进入', icon: 'none' })
    },
  })
}
