/**
 * 传输层抽象。
 *
 * 模块方法只依赖 HttpTransport，不关心底层是 axios（Vite）还是 Taro request。
 * - Vite：createAxiosTransport
 * - Taro：宿主提供 createTaroTransport（见 apps/microapp/src/api/transport.ts）
 */

import axios from 'axios'
import type { AxiosAdapter, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'

export interface TransportRequest {
  method: HttpMethod
  /** 相对 baseURL 的路径，如 /menu/category */
  url: string
  /** query 参数 */
  params?: Record<string, unknown>
  /** 请求体 */
  data?: unknown
  headers?: Record<string, string>
}

export interface TransportResponse {
  status: number
  data: unknown
}

export interface HttpTransport {
  request(req: TransportRequest): Promise<TransportResponse>
}

export interface AxiosTransportOptions {
  baseURL: string
  timeout: number
  /** 可选自定义 adapter（mock 模式挂载本地应答） */
  adapter?: AxiosAdapter
}

/** 基于 axios 的传输实现（B 端默认） */
export function createAxiosTransport(options: AxiosTransportOptions): HttpTransport {
  const instance = axios.create({
    baseURL: options.baseURL,
    timeout: options.timeout,
  })
  if (options.adapter) {
    instance.defaults.adapter = options.adapter
  }
  return {
    async request(req) {
      const response: AxiosResponse = await instance.request({
        method: req.method,
        url: req.url,
        params: req.params as InternalAxiosRequestConfig['params'],
        data: req.data,
        headers: req.headers,
      })
      return { status: response.status, data: response.data }
    },
  }
}
