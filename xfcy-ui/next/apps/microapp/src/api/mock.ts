/**
 * 心飞餐饮 C 端点餐 · 示意数据与模拟接口
 * ⚠️ 全部为「示意」静态数据，不接真实后端（见 DESIGN.md §10 与 requirements.md §4.4）。
 * 真实后端（xfcy-microapp-web，8087 /microApp）就绪后，用同结构的请求替换本文件即可。
 */
import { Category, Dish, OrderResult, PayMethod, StoreInfo } from '../types'
import { makeOrderNumber, nowHHmm } from '../utils/format'

/* ---------------- 门店（示意） ---------------- */
export const mockStore: StoreInfo = {
  id: 'store-001',
  name: '心飞餐饮 · 国贸店',
  tableName: 'A03',
  areaName: '大厅',
  people: 4,
}

/* ---------------- 分类（示意） ---------------- */
export const mockCategories: Category[] = [
  { id: 'c1', name: '招牌推荐' },
  { id: 'c2', name: '热菜' },
  { id: 'c3', name: '凉菜' },
  { id: 'c4', name: '主食' },
  { id: 'c5', name: '饮品' },
  { id: 'c6', name: '套餐' },
]

/* ---------------- 菜品（示意） ---------------- */
export const mockDishes: Dish[] = [
  // 招牌推荐
  { id: 'd1', categoryId: 'c1', name: '招牌红烧肉', desc: '五花肉 · 冰糖慢炖', price: 4800, unit: '例', emoji: '🍖', gradient: 'g1', signature: true },
  { id: 'd2', categoryId: 'c1', name: '清蒸鲈鱼', desc: '鲜活 · 粤式蒸法', price: 8800, unit: '条', emoji: '🐟', gradient: 'g2', signature: true },
  { id: 'd3', categoryId: 'c1', name: '蒜蓉时蔬', desc: '时令 · 清炒', price: 2200, unit: '例', emoji: '🥬', gradient: 'g3', signature: true },
  { id: 'd4', categoryId: 'c1', name: '腊味煲仔饭', desc: '丝苗米 · 锅巴香', price: 3600, unit: '份', emoji: '🍚', gradient: 'g4', signature: true },
  // 热菜
  { id: 'd5', categoryId: 'c2', name: '宫保鸡丁', desc: '鸡腿肉 · 糊辣荔枝口', price: 3200, unit: '例', emoji: '🍗', gradient: 'g1' },
  { id: 'd6', categoryId: 'c2', name: '麻婆豆腐', desc: '嫩豆腐 · 麻辣鲜香', price: 2400, unit: '例', emoji: '🌶️', gradient: 'g3' },
  { id: 'd7', categoryId: 'c2', name: '回锅肉', desc: '二刀肉 · 灯盏窝', price: 3800, unit: '例', emoji: '🥓', gradient: 'g4' },
  { id: 'd8', categoryId: 'c2', name: '干煸四季豆', desc: '四季豆 · 干香下饭', price: 2600, unit: '例', emoji: '🫛', gradient: 'g2' },
  // 凉菜
  { id: 'd9', categoryId: 'c3', name: '拍黄瓜', desc: '蒜香 · 爽脆', price: 1400, unit: '例', emoji: '🥒', gradient: 'g2' },
  { id: 'd10', categoryId: 'c3', name: '口水鸡', desc: '三黄鸡 · 红油', price: 3400, unit: '例', emoji: '🍥', gradient: 'g1' },
  { id: 'd11', categoryId: 'c3', name: '凉拌木耳', desc: '秋耳 · 酸辣', price: 1600, unit: '例', emoji: '🍄', gradient: 'g4' },
  // 主食
  { id: 'd12', categoryId: 'c4', name: '丝苗米饭', desc: '东北大米', price: 300, unit: '碗', emoji: '🍚', gradient: 'g4' },
  { id: 'd13', categoryId: 'c4', name: '葱油拌面', desc: '手擀面 · 葱香', price: 1800, unit: '份', emoji: '🍜', gradient: 'g3' },
  { id: 'd14', categoryId: 'c4', name: '手工水饺', desc: '猪肉白菜 · 12 只', price: 2800, unit: '份', emoji: '🥟', gradient: 'g1' },
  // 饮品
  { id: 'd15', categoryId: 'c5', name: '酸梅汤', desc: '乌梅 · 桂花', price: 1200, unit: '杯', emoji: '🥤', gradient: 'g3' },
  { id: 'd16', categoryId: 'c5', name: '鲜榨橙汁', desc: '现榨 · 无添加', price: 2200, unit: '杯', emoji: '🍊', gradient: 'g1' },
  { id: 'd17', categoryId: 'c5', name: '柠檬蜂蜜水', desc: '黄柠檬 · 蜂蜜', price: 1500, unit: '杯', emoji: '🍋', gradient: 'g2' },
  // 套餐
  { id: 'd18', categoryId: 'c6', name: '双人经典套餐', desc: '红烧肉 + 时蔬 + 2 米饭 + 2 酸梅汤', price: 12800, unit: '套', emoji: '🍱', gradient: 'g4', signature: true },
  { id: 'd19', categoryId: 'c6', name: '四人欢聚餐', desc: '鲈鱼 + 宫保鸡丁 + 时蔬 + 4 米饭 + 4 饮品', price: 23800, unit: '套', emoji: '🥘', gradient: 'g1', signature: true },
]

/* ---------------- 支付方式（示意） ---------------- */
export const mockPayMethods: PayMethod[] = [
  { id: 'wechat', name: '微信支付', desc: '推荐使用' },
  { id: 'alipay', name: '支付宝', desc: '模拟渠道' },
]

/* ---------------- 模拟接口（示意） ---------------- */

function delay<T>(value: T, ms = 260): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(value), ms))
}

/** 模拟「购物车提交订单」（对应 /order/add，示意） */
export function submitOrder(totalAmount: number): Promise<OrderResult> {
  const now = new Date()
  const result: OrderResult = {
    orderId: `order-${now.getTime()}`,
    orderNumber: makeOrderNumber(now),
    totalAmount,
    createTime: nowHHmm(now),
    payTime: nowHHmm(now),
  }
  return delay(result, 320)
}

/** 模拟「支付打桩」（对应 dev 打桩：微信 code2session / JSAPI，示意） */
export function mockPay(): Promise<{ payTime: string }> {
  return delay({ payTime: nowHHmm() }, 320)
}
