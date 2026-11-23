/**
 * C 端（xfcy-microapp-web）mock 数据与路由。
 *
 * 数据与 apps/microapp 首版示意数据保持一致，保证默认 mock 模式下 4 页交互与视觉不变。
 * 真实契约路径以 xfcy-microapp-web 的 LoginController / MenuController /
 * OrderController / PayController / SearchController 为准（context-path /microApp）。
 */

import type { OrderEntity } from '../types'
import type { MockRoute } from './types'

export interface MockMicroappStore {
  id: string
  name: string
  tableName: string
  areaName: string
  people: number
}

export interface MockMicroappCategory {
  id: string
  name: string
  type?: number
}

export interface MockMicroappDish {
  id: string
  categoryId: string
  name: string
  desc: string
  /** 单价，单位：分 */
  price: number
  unit: string
  emoji: string
  gradient: string
  soldOut?: boolean
  signature?: boolean
}

export interface MockPayMethod {
  id: string
  name: string
  desc: string
}

/* ---------------- 门店（示意） ---------------- */
export const mockMicroappStore: MockMicroappStore = {
  id: 'store-001',
  name: '心飞餐饮 · 国贸店',
  tableName: 'A03',
  areaName: '大厅',
  people: 4,
}

/* ---------------- 分类（示意） ---------------- */
export const mockMicroappCategories: MockMicroappCategory[] = [
  { id: 'c1', name: '招牌推荐', type: 1 },
  { id: 'c2', name: '热菜', type: 1 },
  { id: 'c3', name: '凉菜', type: 1 },
  { id: 'c4', name: '主食', type: 1 },
  { id: 'c5', name: '饮品', type: 1 },
  { id: 'c6', name: '套餐', type: 2 },
]

/* ---------------- 菜品（示意，价格单位：分） ---------------- */
export const mockMicroappDishes: MockMicroappDish[] = [
  { id: 'd1', categoryId: 'c1', name: '招牌红烧肉', desc: '五花肉 · 冰糖慢炖', price: 4800, unit: '例', emoji: '🍖', gradient: 'g1', signature: true },
  { id: 'd2', categoryId: 'c1', name: '清蒸鲈鱼', desc: '鲜活 · 粤式蒸法', price: 8800, unit: '条', emoji: '🐟', gradient: 'g2', signature: true },
  { id: 'd3', categoryId: 'c1', name: '蒜蓉时蔬', desc: '时令 · 清炒', price: 2200, unit: '例', emoji: '🥬', gradient: 'g3', signature: true },
  { id: 'd4', categoryId: 'c1', name: '腊味煲仔饭', desc: '丝苗米 · 锅巴香', price: 3600, unit: '份', emoji: '🍚', gradient: 'g4', signature: true },
  { id: 'd5', categoryId: 'c2', name: '宫保鸡丁', desc: '鸡腿肉 · 糊辣荔枝口', price: 3200, unit: '例', emoji: '🍗', gradient: 'g1' },
  { id: 'd6', categoryId: 'c2', name: '麻婆豆腐', desc: '嫩豆腐 · 麻辣鲜香', price: 2400, unit: '例', emoji: '🌶️', gradient: 'g3' },
  { id: 'd7', categoryId: 'c2', name: '回锅肉', desc: '二刀肉 · 灯盏窝', price: 3800, unit: '例', emoji: '🥓', gradient: 'g4' },
  { id: 'd8', categoryId: 'c2', name: '干煸四季豆', desc: '四季豆 · 干香下饭', price: 2600, unit: '例', emoji: '🫛', gradient: 'g2' },
  { id: 'd9', categoryId: 'c3', name: '拍黄瓜', desc: '蒜香 · 爽脆', price: 1400, unit: '例', emoji: '🥒', gradient: 'g2' },
  { id: 'd10', categoryId: 'c3', name: '口水鸡', desc: '三黄鸡 · 红油', price: 3400, unit: '例', emoji: '🍥', gradient: 'g1' },
  { id: 'd11', categoryId: 'c3', name: '凉拌木耳', desc: '秋耳 · 酸辣', price: 1600, unit: '例', emoji: '🍄', gradient: 'g4' },
  { id: 'd12', categoryId: 'c4', name: '丝苗米饭', desc: '东北大米', price: 300, unit: '碗', emoji: '🍚', gradient: 'g4' },
  { id: 'd13', categoryId: 'c4', name: '葱油拌面', desc: '手擀面 · 葱香', price: 1800, unit: '份', emoji: '🍜', gradient: 'g3' },
  { id: 'd14', categoryId: 'c4', name: '手工水饺', desc: '猪肉白菜 · 12 只', price: 2800, unit: '份', emoji: '🥟', gradient: 'g1' },
  { id: 'd15', categoryId: 'c5', name: '酸梅汤', desc: '乌梅 · 桂花', price: 1200, unit: '杯', emoji: '🥤', gradient: 'g3' },
  { id: 'd16', categoryId: 'c5', name: '鲜榨橙汁', desc: '现榨 · 无添加', price: 2200, unit: '杯', emoji: '🍊', gradient: 'g1' },
  { id: 'd17', categoryId: 'c5', name: '柠檬蜂蜜水', desc: '黄柠檬 · 蜂蜜', price: 1500, unit: '杯', emoji: '🍋', gradient: 'g2' },
  { id: 'd18', categoryId: 'c6', name: '双人经典套餐', desc: '红烧肉 + 时蔬 + 2 米饭 + 2 酸梅汤', price: 12800, unit: '套', emoji: '🍱', gradient: 'g4', signature: true },
  { id: 'd19', categoryId: 'c6', name: '四人欢聚餐', desc: '鲈鱼 + 宫保鸡丁 + 时蔬 + 4 米饭 + 4 饮品', price: 23800, unit: '套', emoji: '🥘', gradient: 'g1', signature: true },
]

/* ---------------- 支付方式（示意） ---------------- */
export const mockMicroappPayMethods: MockPayMethod[] = [
  { id: 'wechat', name: '微信支付', desc: '推荐使用' },
  { id: 'alipay', name: '支付宝', desc: '模拟渠道' },
]

/** 已提交订单（mock 状态，供订单详情/支付回查） */
export const mockMicroappOrders = new Map<string, OrderEntity>()

const MOCK_TOKEN = 'xfcy-microapp-mock-token'
const SET_MEAL_CATEGORY_ID = 'c6'

function makeOrderNumber(date = new Date()): string {
  const y = date.getFullYear()
  const m = `${date.getMonth() + 1}`.padStart(2, '0')
  const d = `${date.getDate()}`.padStart(2, '0')
  const seq = `${Math.floor(Math.random() * 900) + 100}`
  return `FK${y}${m}${d}-${seq}`
}

function toSearchRecords(rows: MockMicroappDish[], type: number) {
  return rows.map((item) => ({
    id: item.id,
    dishName: item.name,
    price: item.price,
    type,
    image: item.emoji,
    description: item.desc,
  }))
}

export const microappRoutes: MockRoute[] = [
  /* ---------------- 登录（dev 打桩） ---------------- */
  {
    method: 'POST',
    pattern: /^\/login$/,
    handler: ({ body }) => {
      const shopId = String(body?.shopId ?? '')
      const storeId = String(body?.storeId ?? '')
      return {
        status: 200,
        desc: '登录成功',
        token: MOCK_TOKEN,
        data: { openid: 'mock-openid', shopId, storeId, userType: 'microapp' },
      }
    },
  },

  /* ---------------- 菜单 ---------------- */
  {
    method: 'GET',
    pattern: /^\/menu\/category$/,
    handler: ({ params }) => {
      const type = params.type !== undefined && params.type !== '' ? Number(params.type) : undefined
      return type === undefined
        ? mockMicroappCategories
        : mockMicroappCategories.filter((item) => item.type === type)
    },
  },
  {
    method: 'GET',
    pattern: /^\/menu\/dish\/[^/]+$/,
    handler: ({ url }) => {
      const categoryId = url.split('/').pop() as string
      return mockMicroappDishes.filter((item) => item.categoryId === categoryId)
    },
  },
  {
    method: 'GET',
    pattern: /^\/menu\/setMeal\/[^/]+$/,
    handler: ({ url }) => {
      const id = url.split('/').pop() as string
      const item = mockMicroappDishes.find((dish) => dish.id === id)
      if (!item) return null
      return {
        id: item.id,
        name: item.name,
        price: item.price,
        status: item.soldOut ? 0 : 1,
        description: item.desc,
        image: item.emoji,
        dishList: [],
      }
    },
  },
  {
    method: 'GET',
    pattern: /^\/menu\/area$/,
    handler: () => [
      {
        areaId: 'area-1',
        areaName: mockMicroappStore.areaName,
        indexNumber: 1,
        tableCount: 1,
        tableName: mockMicroappStore.tableName,
        people: mockMicroappStore.people,
      },
    ],
  },

  /* ---------------- 检索 ---------------- */
  {
    method: 'GET',
    pattern: /^\/search$/,
    handler: ({ params }) => {
      const keyword = String(params.keyword ?? '').trim().toLowerCase()
      const pageNum = Number(params.pageNum ?? 1)
      const pageSize = Number(params.pageSize ?? 10)
      const hit = (item: MockMicroappDish) =>
        !keyword ||
        item.name.toLowerCase().includes(keyword) ||
        item.desc.toLowerCase().includes(keyword)
      const dishRows = mockMicroappDishes.filter(
        (item) => item.categoryId !== SET_MEAL_CATEGORY_ID && hit(item),
      )
      const setMealRows = mockMicroappDishes.filter(
        (item) => item.categoryId === SET_MEAL_CATEGORY_ID && hit(item),
      )
      const paginate = <T,>(rows: T[]) => {
        const start = (pageNum - 1) * pageSize
        return { records: rows.slice(start, start + pageSize), total: rows.length }
      }
      return {
        dish: paginate(toSearchRecords(dishRows, 1)),
        setMeal: paginate(toSearchRecords(setMealRows, 2)),
      }
    },
  },

  /* ---------------- 下单 / 加菜 / 退菜 ---------------- */
  {
    method: 'POST',
    pattern: /^\/order\/submit$/,
    handler: ({ body }) => {
      const now = new Date()
      const orderId = `micro-order-${now.getTime()}`
      const order = {
        ...(body ?? {}),
        orderId,
        orderNumber: makeOrderNumber(now),
        createTime: now.toISOString(),
        payStatus: 0,
        orderSource: 1,
      } as OrderEntity
      mockMicroappOrders.set(orderId, order)
      return { status: 200, desc: 'ok', data: orderId }
    },
  },
  {
    method: 'POST',
    pattern: /^\/order\/add$/,
    handler: ({ body }) => {
      const order = (body ?? {}) as OrderEntity
      if (order.orderId) mockMicroappOrders.set(order.orderId, order)
      return { status: 200, desc: 'ok', data: null }
    },
  },
  {
    method: 'POST',
    pattern: /^\/order\/returnDish$/,
    handler: () => ({ status: 200, desc: 'ok', data: null }),
  },
  {
    // 需先于 B 端通用 /order/{id} 命中
    method: 'GET',
    pattern: /^\/order\/micro-order-[^/]+$/,
    handler: ({ url }) => {
      const id = url.split('/').pop() as string
      return mockMicroappOrders.get(id) ?? null
    },
  },

  /* ---------------- 支付（dev 打桩） ---------------- */
  {
    method: 'POST',
    pattern: /^\/pay\/mock$/,
    handler: ({ body }) => {
      const order = (body ?? {}) as OrderEntity
      if (order.orderId && mockMicroappOrders.has(order.orderId)) {
        const stored = mockMicroappOrders.get(order.orderId) as OrderEntity
        mockMicroappOrders.set(order.orderId, {
          ...stored,
          payStatus: 1,
          payType: order.payType ?? 2,
          payAmount: order.payAmount ?? stored.totalAmount ?? 0,
          totalAmount: order.totalAmount ?? stored.totalAmount ?? 0,
        })
      }
      return { status: 200, desc: '支付成功（dev 打桩）', data: true }
    },
  },
]
