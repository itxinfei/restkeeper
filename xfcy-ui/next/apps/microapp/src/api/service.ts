/**
 * C 端业务服务层：把 @xfcy/api 的 microapp 契约模块适配成页面可用的数据。
 *
 * - mock 模式：数据来自 @xfcy/api 内置 mock 路由，与原型 4 页交互 / 视觉一致；
 * - 真实模式：走 Taro 传输请求 xfcy-microapp-web（/microApp/**）。
 * - 门店信息优先取 /menu/area，缺省回退内置示意门店。
 */
import Taro from '@tarojs/taro'
import {
  getToken,
  isMockMode,
  microapp,
  mockMicroappPayMethods,
  mockMicroappStore,
  type DishEsItem,
  type MicroappAreaRow,
  type MicroappCategoryRow,
  type MicroappDishRow,
  type OrderEntity,
} from '@xfcy/api'
import type { CartItem, Category, Dish, OrderResult, PayMethod, StoreInfo } from '../types'
import { nowHHmm } from '../utils/format'

const SET_MEAL_CATEGORY_ID = 'c6'

/* ---------------- 映射 ---------------- */

function toCategory(row: MicroappCategoryRow): Category {
  return { id: row.categoryId, name: row.name }
}

function toDish(row: MicroappDishRow, categoryId: string): Dish {
  return {
    id: row.id,
    categoryId: row.categoryId ?? categoryId,
    name: row.name ?? '',
    desc: row.desc ?? row.description ?? '',
    price: row.price ?? 0,
    unit: row.unit ?? '例',
    emoji: row.emoji ?? row.image ?? '🍽️',
    gradient: row.gradient ?? 'g1',
    soldOut: row.status === 0,
    signature: row.signature ?? false,
    dishType: row.categoryId === SET_MEAL_CATEGORY_ID ? 2 : 1,
  }
}

function toSearchDish(item: DishEsItem): Dish {
  const isSetMeal = item.type === 2
  return {
    id: item.id,
    categoryId: isSetMeal ? SET_MEAL_CATEGORY_ID : '',
    name: item.dishName ?? '',
    desc: item.description ?? '',
    price: item.price ?? 0,
    unit: '例',
    emoji: item.image ?? '🍽️',
    gradient: 'g1',
    soldOut: false,
    signature: false,
    dishType: item.type,
  }
}

function toStoreInfo(area?: MicroappAreaRow): StoreInfo {
  return {
    id: area?.areaId ?? mockMicroappStore.id,
    // 真实契约 area 不返回门店名，保留示意门店名
    name: mockMicroappStore.name,
    tableName: area?.tableName ?? mockMicroappStore.tableName,
    areaName: area?.areaName ?? mockMicroappStore.areaName,
    people: area?.people ?? mockMicroappStore.people,
  }
}

/* ---------------- 门店信息 ---------------- */

let storeInfo: StoreInfo = { ...mockMicroappStore }

export function getStoreInfo(): StoreInfo {
  return storeInfo
}

/* ---------------- 登录 ---------------- */

function getShopContext(): { shopId: string; storeId: string } {
  try {
    const query = Taro.getLaunchOptionsSync?.().query ?? {}
    return {
      shopId: String(query.shopId ?? 's001'),
      storeId: String(query.storeId ?? 'st001'),
    }
  } catch {
    return { shopId: 's001', storeId: 'st001' }
  }
}

/** 确保已登录：C 端 BFF 用独立 StpLogic，下单 / 支付前必须有 token */
export async function ensureLogin(): Promise<void> {
  if (getToken()) return
  let code = 'dev-code'
  try {
    const res = await Taro.login()
    if (res?.code) code = res.code
  } catch {
    /* 非微信环境（如 H5）：沿用 dev 打桩 code */
  }
  const { shopId, storeId } = getShopContext()
  await microapp.login(code, shopId, storeId)
}

/* ---------------- 菜单 / 检索 ---------------- */

export interface MenuData {
  store: StoreInfo
  categories: Category[]
  dishes: Dish[]
}

/** 拉取菜单：分类（菜品 + 套餐）、桌台区域、各分类可售菜品 */
export async function fetchMenu(): Promise<MenuData> {
  const ctx = getShopContext()
  const [dishCats, setMealCats, areas] = await Promise.all([
    microapp.menuCategory(1, ctx),
    microapp.menuCategory(2, ctx),
    microapp.menuArea(ctx),
  ])
  const categories = [...dishCats, ...setMealCats].map(toCategory)
  const groups = await Promise.all(
    categories.map(async (cat) => {
      const rows = await microapp.menuDish(cat.id, ctx)
      return rows.map((row) => toDish(row, cat.id))
    }),
  )
  const store = toStoreInfo(areas[0])
  storeInfo = store
  return { store, categories, dishes: groups.flat() }
}

/** 关键词检索（ES 索引：菜品 + 套餐） */
export async function searchMenu(keyword: string): Promise<Dish[]> {
  const kw = keyword.trim()
  if (!kw) return []
  const res = await microapp.search(kw, 1, 50, getShopContext())
  return [...(res.dish?.records ?? []), ...(res.setMeal?.records ?? [])].map(toSearchDish)
}

/* ---------------- 下单 / 支付 ---------------- */

/** 支付方式为前端固定渠道（后端 dev 打桩按 payType 流转） */
export function getPayMethods(): PayMethod[] {
  return mockMicroappPayMethods.map((m) => ({ ...m }))
}

function toPayType(payMethod: string): number {
  if (payMethod === 'wechat') return 2
  if (payMethod === 'alipay') return 3
  return 1
}

/**
 * 提交订单并支付：/order/submit → /pay/mock → /order/{id}。
 * 返回页面展示用 OrderResult（金额单位：分，时间 HH:mm）。
 */
export async function submitOrder(
  items: CartItem[],
  remark: string,
  payMethod: string,
): Promise<OrderResult> {
  await ensureLogin()

  const total = items.reduce((sum, item) => sum + item.qty * item.dish.price, 0)
  const store = getStoreInfo()
  const payType = toPayType(payMethod)

  const order: OrderEntity = {
    orderId: '',
    tableId: store.id,
    personNumbers: store.people,
    totalAmount: total,
    payAmount: total,
    payType,
    orderSource: 1,
    orderRemark: remark,
    orderDetails: items.map((item) => ({
      detailId: '',
      dishId: item.dish.id,
      dishType: item.dish.dishType ?? (item.dish.categoryId === SET_MEAL_CATEGORY_ID ? 2 : 1),
      dishName: item.dish.name,
      dishPrice: item.dish.price,
      dishNumber: item.qty,
      dishAmount: item.qty * item.dish.price,
      detailStatus: 1,
    })),
  }

  const orderId = await microapp.submitOrder(order)
  await microapp.pay({ ...order, orderId })
  const detail = await microapp.orderDetail(orderId)

  const parsed = new Date(detail.createTime ?? Date.now())
  const time = nowHHmm(Number.isNaN(parsed.getTime()) ? new Date() : parsed)

  return {
    orderId,
    orderNumber: detail.orderNumber ?? '',
    totalAmount: detail.totalAmount ?? total,
    createTime: time,
    payTime: time,
  }
}

/** 是否处于 mock 模式（页面初始占位数据用） */
export function useMockData(): boolean {
  return isMockMode()
}
