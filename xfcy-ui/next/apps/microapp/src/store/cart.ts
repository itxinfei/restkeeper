/**
 * 购物车状态（轻量外部 store）
 * - 使用 Taro storage 持久化，跨页面 / 刷新保持（H5 localStorage、小程序 Storage）。
 * - 通过订阅通知 React 组件重渲染。
 */
import { useEffect, useReducer } from 'react'
import Taro from '@tarojs/taro'
import { CartItem, Dish, OrderResult } from '../types'

const CART_KEY = 'microapp.cart.v1'
const ORDER_KEY = 'microapp.order.v1'

let items: CartItem[] = []
let remark = ''
let lastOrder: OrderResult | null = null
const listeners = new Set<() => void>()

function readJSON<T>(key: string): T | null {
  try {
    const raw = Taro.getStorageSync(key)
    return raw ? (JSON.parse(raw) as T) : null
  } catch {
    return null
  }
}

function writeJSON(key: string, value: unknown): void {
  try {
    Taro.setStorageSync(key, JSON.stringify(value))
  } catch {
    /* 忽略存储异常（如隐私模式） */
  }
}

function emit(): void {
  listeners.forEach((l) => l())
}

// 首次进入恢复
const cached = readJSON<CartItem[]>(CART_KEY)
if (Array.isArray(cached)) items = cached
const cachedRemark = Taro.getStorageSync('microapp.remark.v1')
if (typeof cachedRemark === 'string') remark = cachedRemark
const cachedOrder = readJSON<OrderResult>(ORDER_KEY)
if (cachedOrder) lastOrder = cachedOrder

export function getCartItems(): CartItem[] {
  return items
}

export function getQty(dishId: string): number {
  const found = items.find((i) => i.dish.id === dishId)
  return found ? found.qty : 0
}

export function getCartCount(): number {
  return items.reduce((sum, i) => sum + i.qty, 0)
}

/** 菜品合计（分） */
export function getCartTotal(): number {
  return items.reduce((sum, i) => sum + i.qty * i.dish.price, 0)
}

export function setQty(dish: Dish, qty: number): void {
  const idx = items.findIndex((i) => i.dish.id === dish.id)
  if (qty <= 0) {
    if (idx >= 0) {
      const next = items.slice()
      next.splice(idx, 1)
      items = next
    }
  } else if (idx >= 0) {
    const next = items.slice()
    next[idx] = { ...next[idx], qty }
    items = next
  } else {
    items = [...items, { dish, qty }]
  }
  writeJSON(CART_KEY, items)
  emit()
}

export function clearCart(): void {
  items = []
  remark = ''
  try {
    Taro.removeStorageSync(CART_KEY)
    Taro.removeStorageSync('microapp.remark.v1')
  } catch {
    /* ignore */
  }
  emit()
}

export function getRemark(): string {
  return remark
}

export function setRemark(value: string): void {
  remark = value
  writeJSON('microapp.remark.v1', value)
}

export function getLastOrder(): OrderResult | null {
  return lastOrder
}

export function setLastOrder(order: OrderResult): void {
  lastOrder = order
  writeJSON(ORDER_KEY, order)
}

export interface CartSnapshot {
  items: CartItem[]
  count: number
  total: number
}

/** 订阅购物车变化，返回快照 */
export function useCart(): CartSnapshot {
  const [, force] = useReducer((x: number) => x + 1, 0)
  useEffect(() => {
    const listener = () => force()
    listeners.add(listener)
    return () => {
      listeners.delete(listener)
    }
  }, [])
  return { items, count: getCartCount(), total: getCartTotal() }
}
