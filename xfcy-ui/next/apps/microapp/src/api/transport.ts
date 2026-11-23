/**
 * Taro 传输实现：把 @xfcy/api 的 HttpTransport 契约接到 Taro.request。
 *
 * - 与 B 端 axios 传输行为对齐：返回 { status, data }；
 * - GET 参数走 query（Taro 对 GET 的 data 会序列化为 query）；
 * - POST/PUT 参数走 JSON body；
 * - 网络异常归一化为 { status, data, message }，由 http 客户端统一转 ApiError。
 */
import Taro from '@tarojs/taro'
import type { HttpTransport, StorageLike, TransportRequest } from '@xfcy/api'

function joinUrl(baseURL: string, url: string): string {
  if (/^https?:\/\//i.test(url)) return url
  if (!baseURL) return url
  const base = baseURL.endsWith('/') ? baseURL.slice(0, -1) : baseURL
  return `${base}${url.startsWith('/') ? url : `/${url}`}`
}

/** 基于 Taro.request 的传输（C 端真实模式） */
export function createTaroTransport(baseURL: string, timeout: number): HttpTransport {
  return {
    async request(req: TransportRequest) {
      const isGet = req.method === 'GET'
      const params = req.params ?? {}
      const body =
        req.data && typeof req.data === 'object' && !isGet
          ? { ...params, ...(req.data as Record<string, unknown>) }
          : params
      try {
        const res = await Taro.request({
          url: joinUrl(baseURL, req.url),
          method: req.method,
          data: body,
          header: {
            'Content-Type': 'application/json',
            ...(req.headers ?? {}),
          },
          timeout,
        })
        return { status: res.statusCode, data: res.data }
      } catch (err) {
        const e = err as { statusCode?: number; errMsg?: string; data?: unknown }
        throw {
          status: e?.statusCode ?? 0,
          data: e?.data ?? {},
          message: e?.errMsg ?? '网络异常',
        }
      }
    },
  }
}

/** Taro Storage 适配为 @xfcy/api 的 StorageLike（H5 / 小程序一致） */
export function createTaroStorage(): StorageLike {
  return {
    getItem: (key) => {
      try {
        const value = Taro.getStorageSync(key)
        return typeof value === 'string' ? value : null
      } catch {
        return null
      }
    },
    setItem: (key, value) => {
      try {
        Taro.setStorageSync(key, value)
      } catch {
        /* 忽略存储异常（如隐私模式） */
      }
    },
    removeItem: (key) => {
      try {
        Taro.removeStorageSync(key)
      } catch {
        /* ignore */
      }
    },
  }
}
