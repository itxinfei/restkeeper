/**
 * 运行时配置：默认值 + 可注入覆盖。
 *
 * 设计目标：让 @xfcy/api 同时可被 Vite（B 端）与 Taro（C 端）复用。
 * - 默认值与 Vite 环境变量兼容（VITE_API_BASE_URL / VITE_USE_MOCK），保持 B 端现状；
 * - Taro 环境没有 import.meta.env，由宿主在启动时调用 configureApi 注入
 *   baseURL / transport / storage 等，避免共享层直接依赖具体运行环境。
 */

import type { HttpTransport } from './transport'

/** 存储抽象：浏览器 localStorage 与 Taro Storage 可互换 */
export interface StorageLike {
  getItem(key: string): string | null
  setItem(key: string, value: string): void
  removeItem(key: string): void
}

export interface ApiRuntimeConfig {
  /** 请求前缀，B 端默认 /api（Vite 代理），C 端默认 /microApp */
  baseURL: string
  timeout: number
  /** true = 本地 mock；false = 真实后端 */
  mock: boolean
  /** Sa-Token 请求头（B/C 端均为 Authorization） */
  tokenHeader: string
  tokenStorageKey: string
  userStorageKey: string
  storage: StorageLike
  /** 真实模式传输实现；为空时默认使用 axios */
  transport: HttpTransport | null
  /** 401 回调（B 端跳登录页，C 端提示） */
  onUnauthorized: () => void
}

/**
 * 安全读取 Vite 注入的环境变量。
 * Taro webpack 不注入 import.meta.env，此时返回空对象，由 configureApi 注入。
 */
function readViteEnv(): Record<string, string | undefined> {
  try {
    const meta = import.meta as unknown as { env?: Record<string, string | undefined> }
    return meta.env ?? {}
  } catch {
    return {}
  }
}

function createMemoryStorage(): StorageLike {
  const map = new Map<string, string>()
  return {
    getItem: (key) => map.get(key) ?? null,
    setItem: (key, value) => {
      map.set(key, value)
    },
    removeItem: (key) => {
      map.delete(key)
    },
  }
}

/** 浏览器 localStorage；不可用（SSR / 小程序）时退化为内存，宿主可注入持久化实现 */
export function createBrowserStorage(): StorageLike {
  try {
    if (typeof localStorage !== 'undefined' && localStorage) {
      return {
        getItem: (key) => localStorage.getItem(key),
        setItem: (key, value) => {
          localStorage.setItem(key, value)
        },
        removeItem: (key) => {
          localStorage.removeItem(key)
        },
      }
    }
  } catch {
    /* 隐私模式等场景下退化为内存 */
  }
  return createMemoryStorage()
}

function defaultUnauthorized(): void {
  if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
    window.location.assign('/login')
  }
}

const env = readViteEnv()

let state: ApiRuntimeConfig = {
  baseURL: env.VITE_API_BASE_URL ?? '/api',
  timeout: 15000,
  mock: (env.VITE_USE_MOCK ?? '1') === '1',
  tokenHeader: 'Authorization',
  tokenStorageKey: 'xfcy.token',
  userStorageKey: 'xfcy.user',
  storage: createBrowserStorage(),
  transport: null,
  onUnauthorized: defaultUnauthorized,
}

/** 注入/覆盖运行时配置（宿主 App 启动时调用一次） */
export function configureApi(patch: Partial<ApiRuntimeConfig>): void {
  state = { ...state, ...patch }
}

export function getApiConfig(): ApiRuntimeConfig {
  return state
}
