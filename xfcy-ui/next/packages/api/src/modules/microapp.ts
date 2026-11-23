/**
 * C 端（xfcy-microapp-web）契约模块。
 *
 * 路径与入参以 xfcy-microapp-web 的 Controller 为准（context-path /microApp）：
 * - LoginController   POST /login            （code/shopId/storeId，dev 打桩）
 * - MenuController    GET  /menu/category?type=
 *                    GET  /menu/dish/{categoryId}
 *                    GET  /menu/setMeal/{id}
 *                    GET  /menu/area
 * - SearchController  GET  /search?keyword=&pageNum=&pageSize=
 * - OrderController   POST /order/submit      （返回订单 id）
 *                    POST /order/add         （加菜，body 带 orderId）
 *                    POST /order/returnDish  （body: DetailDTO）
 *                    GET  /order/{id}
 * - PayController     POST /pay/mock         （dev 打桩，body: OrderEntity）
 *
 * 所有方法都经由传输无关的 http 客户端，mock / 真实仅由配置切换。
 */

import { http, unwrap } from '../http'
import type {
  DishEsItem,
  MicroappSearchResponse,
  OrderEntity,
} from '../types'

export { loginCustomer as login } from './auth'

/** 分类行（MenuController 返回 Map 列表，仅含分类与名称） */
export interface MicroappCategoryRow {
  categoryId: string
  name: string
  type?: number
}

/** 菜品行（MenuController 精简列：id/name/status/price；mock 附带展示字段） */
export interface MicroappDishRow {
  id: string
  name?: string
  status?: number
  price?: number
  image?: string
  description?: string
  categoryId?: string
  /** mock 展示字段：描述 / 单位 / 占位图 / 渐变 / 招牌 */
  desc?: string
  unit?: string
  emoji?: string
  gradient?: string
  signature?: boolean
}

/** 桌台区域行（MenuController listTableArea 返回，mock 附带桌台/人数） */
export interface MicroappAreaRow {
  areaId: string
  areaName: string
  indexNumber?: number
  tableCount?: number
  tableName?: string
  people?: number
}

/** 套餐详情（MenuController 返回 SetMeal 实体） */
export type MicroappSetMeal = Record<string, unknown>

/* ---------------- 菜单 ---------------- */

/** 分类列表：GET /menu/category?type=（1 菜品 / 2 套餐） */
export async function menuCategory(type?: number): Promise<MicroappCategoryRow[]> {
  const { data } = await http.get('/menu/category', type === undefined ? {} : { params: { type } })
  return unwrap<MicroappCategoryRow[]>(data)
}

/** 可售菜品：GET /menu/dish/{categoryId} */
export async function menuDish(categoryId: string): Promise<MicroappDishRow[]> {
  const { data } = await http.get(`/menu/dish/${categoryId}`)
  return unwrap<MicroappDishRow[]>(data)
}

/** 套餐详情：GET /menu/setMeal/{id} */
export async function menuSetMeal(id: string): Promise<MicroappSetMeal> {
  const { data } = await http.get(`/menu/setMeal/${id}`)
  return unwrap<MicroappSetMeal>(data)
}

/** 桌台区域：GET /menu/area */
export async function menuArea(): Promise<MicroappAreaRow[]> {
  const { data } = await http.get('/menu/area')
  return unwrap<MicroappAreaRow[]>(data)
}

/* ---------------- 检索 ---------------- */

/** 菜品/套餐检索：GET /search?keyword=&pageNum=&pageSize= */
export async function search(
  keyword: string,
  pageNum = 1,
  pageSize = 10,
): Promise<MicroappSearchResponse> {
  const { data } = await http.get('/search', { params: { keyword, pageNum, pageSize } })
  return unwrap<MicroappSearchResponse>(data)
}

/* ---------------- 订单 ---------------- */

/** 提交订单（购物车结算）：POST /order/submit，返回订单 id */
export async function submitOrder(order: OrderEntity): Promise<string> {
  const { data } = await http.post('/order/submit', order)
  return unwrap<string>(data)
}

/** 加菜：POST /order/add（body 带 orderId） */
export async function addOrder(order: OrderEntity): Promise<boolean> {
  const { data } = await http.post('/order/add', order)
  return unwrap<boolean>(data)
}

/** 退菜：POST /order/returnDish（body: DetailDTO） */
export async function returnDish(detailId: string, remarks: string[]): Promise<boolean> {
  const { data } = await http.post('/order/returnDish', { detailId, remarks })
  return unwrap<boolean>(data)
}

/** 订单详情：GET /order/{id} */
export async function orderDetail(id: string): Promise<OrderEntity> {
  const { data } = await http.get(`/order/${id}`)
  return unwrap<OrderEntity>(data)
}

/* ---------------- 支付（dev 打桩） ---------------- */

/** 模拟支付：POST /pay/mock（body: OrderEntity，金额字段前端透传） */
export async function pay(order: OrderEntity): Promise<boolean> {
  const { data } = await http.post('/pay/mock', order)
  return unwrap<boolean>(data)
}

/** 供检索映射使用的文档类型再导出 */
export type { DishEsItem }
