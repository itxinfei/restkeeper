/**
 * axios 实例与横切处理。
 *
 * - baseURL：import.meta.env.VITE_API_BASE_URL，默认 /api（由 Vite 代理到网关）。
 * - token：按后端 Sa-Token 配置注入 Authorization 头（token-name=Authorization，
 *   is-read-header=true / is-read-cookie=false，见 gateway.yml 与各 provider bootstrap.yml）。
 * - 401：清空 token 并跳转登录页。
 * - 错误归一化为 ApiError；provider Result 与 BFF BaseResponse 信封统一解包。
 * - mock：VITE_USE_MOCK==='1'（默认）时挂载本地 adapter，不发真实请求。
 */

import axios from 'axios'
import type { AxiosInstance } from 'axios'
import { isMockMode, mockAdapter } from './mock'
import type { BffResponse, ProviderResult } from './types'

export const TOKEN_HEADER = 'Authorization'
export const TOKEN_STORAGE_KEY = 'xfcy.token'

export function getBaseURL(): string {
  return (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? '/api'
}

export const http: AxiosInstance = axios.create({
  baseURL: getBaseURL(),
  timeout: 15000,
})

if (isMockMode()) {
  http.defaults.adapter = mockAdapter
}

export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly payload?: unknown

  constructor(message: string, status = 0, code = '', payload?: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.payload = payload
  }
}

export function getToken(): string {
  return localStorage.getItem(TOKEN_STORAGE_KEY) ?? ''
}

export function setToken(token: string): void {
  if (token) localStorage.setItem(TOKEN_STORAGE_KEY, token)
  else clearToken()
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_STORAGE_KEY)
}

interface ErrorBody {
  desc?: string
  message?: string
  code?: number | string
  status?: number
}

function normalize(error: unknown): ApiError {
  if (error instanceof ApiError) return error
  if (axios.isAxiosError(error)) {
    const status = error.response?.status ?? 0
    const body = (error.response?.data ?? {}) as ErrorBody
    const message = body.desc ?? body.message ?? error.message
    return new ApiError(message, status, String(body.code ?? body.status ?? ''), error.response?.data)
  }
  return new ApiError(error instanceof Error ? error.message : String(error))
}

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.set(TOKEN_HEADER, token)
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    const apiError = normalize(error)
    if (apiError.status === 401) {
      clearToken()
      if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
        window.location.assign('/login')
      }
    }
    return Promise.reject(apiError)
  },
)

export function isProviderResult(value: unknown): value is ProviderResult {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as ProviderResult).status === 'number'
  )
}

export function isBffResponse(value: unknown): value is BffResponse {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as BffResponse).success === 'boolean'
  )
}

/**
 * 统一解包：
 * - BFF BaseResponse：success=false 抛错，否则返回 data；
 * - provider Result：status!==200 抛错，否则返回 data；
 * - 其他（PageResult/boolean/实体）原样返回。
 */
export function unwrap<T>(body: unknown): T {
  if (isBffResponse(body)) {
    if (!body.success) {
      throw new ApiError(body.message ?? '操作失败', 200, String(body.code ?? ''))
    }
    return body.data as T
  }
  if (isProviderResult(body)) {
    if (body.status !== 200) {
      throw new ApiError(body.desc ?? '请求失败', 200, String(body.status))
    }
    return body.data as T
  }
  return body as T
}

/** 解包登录返回：token 在 Result 顶层，user 在 data。 */
export function unwrapLogin<T>(body: unknown): { token: string; user: T } {
  let result: ProviderResult<T> | undefined
  if (isProviderResult(body)) {
    result = body as ProviderResult<T>
  } else if (isBffResponse(body)) {
    result = body.data as ProviderResult<T>
  } else {
    result = body as ProviderResult<T>
  }
  if (!result || result.status !== 200) {
    throw new ApiError(result?.desc ?? '登录失败', 200, String(result?.status ?? ''))
  }
  return { token: result.token ?? '', user: result.data as T }
}
