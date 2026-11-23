/**
 * 传输无关的 HTTP 客户端与横切处理。
 *
 * - baseURL / mock / storage / transport 均来自可注入配置（config.ts），
 *   默认值保持 Vite + axios 的 B 端现状，Taro 端由宿主注入。
 * - token：按后端 Sa-Token 配置注入 Authorization 头（timeout 259200，见各 bootstrap.yml）。
 * - 401：清空 token 并触发 onUnauthorized（B 端默认跳登录页）。
 * - 错误归一化为 ApiError；provider Result 与 BFF BaseResponse 信封统一解包。
 * - mock：配置 mock=true（默认）时使用本地 mock 传输，不发真实请求。
 */

import axios from 'axios'
import { configureApi, createBrowserStorage, getApiConfig } from './config'
import type { ApiRuntimeConfig, StorageLike } from './config'
import { createMockTransport } from './mock'
import { createAxiosTransport } from './transport'
import type { HttpMethod, HttpTransport } from './transport'

export { configureApi, createBrowserStorage, getApiConfig }
export type { ApiRuntimeConfig, StorageLike }
export type { HttpMethod, HttpTransport, TransportRequest, TransportResponse } from './transport'

export const TOKEN_HEADER = 'Authorization'
export const TOKEN_STORAGE_KEY = 'xfcy.token'

export function getBaseURL(): string {
  return getApiConfig().baseURL
}

let axiosTransport: HttpTransport | null = null
let axiosTransportKey = ''

/** 默认真实传输：axios（baseURL / timeout 变化时重建） */
function getDefaultTransport(): HttpTransport {
  const cfg = getApiConfig()
  const key = `${cfg.baseURL}|${cfg.timeout}`
  if (!axiosTransport || axiosTransportKey !== key) {
    axiosTransport = createAxiosTransport({ baseURL: cfg.baseURL, timeout: cfg.timeout })
    axiosTransportKey = key
  }
  return axiosTransport
}

let mockTransport: HttpTransport | null = null

function getMockTransport(): HttpTransport {
  if (!mockTransport) {
    mockTransport = createMockTransport()
  }
  return mockTransport
}

function activeTransport(): HttpTransport {
  const cfg = getApiConfig()
  if (cfg.mock) return getMockTransport()
  return cfg.transport ?? getDefaultTransport()
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
  const cfg = getApiConfig()
  return cfg.storage.getItem(cfg.tokenStorageKey) ?? ''
}

export function setToken(token: string): void {
  const cfg = getApiConfig()
  if (token) cfg.storage.setItem(cfg.tokenStorageKey, token)
  else clearToken()
}

export function clearToken(): void {
  const cfg = getApiConfig()
  cfg.storage.removeItem(cfg.tokenStorageKey)
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
  // Taro transport 抛出 { status, data, message }，或 mock 未命中抛出 Error
  const candidate = error as { status?: number; data?: unknown; message?: string }
  if (candidate && typeof candidate.status === 'number') {
    const body = (candidate.data ?? {}) as ErrorBody
    const message = body.desc ?? body.message ?? candidate.message ?? '请求失败'
    return new ApiError(message, candidate.status, String(body.code ?? candidate.status ?? ''), candidate.data)
  }
  return new ApiError(error instanceof Error ? error.message : String(error))
}

export interface HttpClientResponse {
  data: unknown
  status: number
}

export interface HttpClientConfig {
  params?: Record<string, unknown>
  data?: unknown
  headers?: Record<string, string>
}

export interface HttpClient {
  request(config: { method: HttpMethod; url: string } & HttpClientConfig): Promise<HttpClientResponse>
  get(url: string, config?: HttpClientConfig): Promise<HttpClientResponse>
  post(url: string, data?: unknown, config?: HttpClientConfig): Promise<HttpClientResponse>
  put(url: string, data?: unknown, config?: HttpClientConfig): Promise<HttpClientResponse>
  delete(url: string, config?: HttpClientConfig): Promise<HttpClientResponse>
}

/**
 * 模块方法使用的客户端。返回结构与 axios 的 `{ data }` 兼容，
 * 但底层可切换为 Taro request。
 */
export const http: HttpClient = {
  async request({ method, url, params, data, headers }) {
    const cfg = getApiConfig()
    const mergedHeaders: Record<string, string> = { ...(headers ?? {}) }
    const token = getToken()
    if (token) mergedHeaders[cfg.tokenHeader] = token

    try {
      const response = await activeTransport().request({
        method,
        url,
        params,
        data,
        headers: mergedHeaders,
      })
      return { status: response.status, data: response.data }
    } catch (error) {
      const apiError = normalize(error)
      if (apiError.status === 401) {
        clearToken()
        cfg.onUnauthorized()
      }
      throw apiError
    }
  },
  get: (url, config = {}) => http.request({ method: 'GET', url, ...config }),
  post: (url, data, config = {}) => http.request({ method: 'POST', url, data, ...config }),
  put: (url, data, config = {}) => http.request({ method: 'PUT', url, data, ...config }),
  delete: (url, config = {}) => http.request({ method: 'DELETE', url, ...config }),
}

export function isProviderResult(value: unknown): value is { status: number } {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as { status?: unknown }).status === 'number'
  )
}

export function isBffResponse(value: unknown): value is { success: boolean } {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as { success?: unknown }).success === 'boolean'
  )
}

/**
 * 统一解包：
 * - BFF BaseResponse：success=false 抛错，否则返回 data；
 * - provider Result：status!==200 抛错，否则返回 data；
 * - 其他（PageResult/boolean/实体/数组）原样返回。
 */
export function unwrap<T>(body: unknown): T {
  if (isBffResponse(body)) {
    if (!body.success) {
      throw new ApiError(
        (body as { message?: string }).message ?? '操作失败',
        200,
        String((body as { code?: number | string }).code ?? ''),
      )
    }
    return (body as { data?: T }).data as T
  }
  if (isProviderResult(body)) {
    if (body.status !== 200) {
      throw new ApiError(
        (body as { desc?: string }).desc ?? '请求失败',
        200,
        String(body.status),
      )
    }
    return (body as { data?: T }).data as T
  }
  return body as T
}

/** 解包登录返回：token 在 Result 顶层，user 在 data。 */
export function unwrapLogin<T>(body: unknown): { token: string; user: T } {
  let result: {
    status: number
    desc?: string
    token?: string
    data?: T
  }
  if (isProviderResult(body)) {
    result = body as { status: number; desc?: string; token?: string; data?: T }
  } else if (isBffResponse(body)) {
    result = (body as { data?: { status: number; desc?: string; token?: string; data?: T } }).data as typeof result
  } else {
    result = body as typeof result
  }
  if (!result || result.status !== 200) {
    throw new ApiError(result?.desc ?? '登录失败', 200, String(result?.status ?? ''))
  }
  return { token: result.token ?? '', user: result.data as T }
}
