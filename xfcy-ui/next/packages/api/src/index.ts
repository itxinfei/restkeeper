/**
 * @xfcy/api —— 心飞餐饮后端 HTTP 契约调用层。
 *
 * 用法：
 * ```ts
 * import { auth, dish, isMockMode, setToken } from '@xfcy/api'
 * if (isMockMode()) { ... } // 默认 VITE_USE_MOCK=1
 * const page = await dish.page(1, 10)
 * ```
 */

export * from './types'
export {
  ApiError,
  TOKEN_HEADER,
  TOKEN_STORAGE_KEY,
  clearToken,
  getBaseURL,
  getToken,
  http,
  setToken,
  unwrap,
  unwrapLogin,
} from './http'
export { isMockMode, mockAdapter } from './mock'
export {
  demoDashboardKpis,
  demoLatestOrders,
  demoSoldoutRows,
  demoTableStats,
  mockCategories,
  mockDishes,
  mockOrders,
  mockSetMeals,
  mockTableAreas,
  mockTables,
} from './mock/data'
export type {
  DemoKpi,
  DemoOrderRow,
  DemoOrderStatus,
  DemoSoldoutRow,
  DemoTableStat,
  Trend,
} from './mock/data'

import * as auth from './modules/auth'
import * as dish from './modules/dish'
import * as enterprise from './modules/enterprise'
import * as order from './modules/order'
import * as table from './modules/table'

export { auth, dish, enterprise, order, table }

/** 金额格式化：分 → 「¥x,xxx.xx」（展示层统一入口） */
export function formatYuan(cents?: number | null): string {
  if (cents === undefined || cents === null || Number.isNaN(cents)) return '¥0.00'
  return `¥${(cents / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}
