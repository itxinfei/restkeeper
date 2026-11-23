/**
 * 心飞餐饮 C 端点餐 · 类型定义
 * 说明：金额统一以「分」（Integer）存储与传输，展示层用 formatYuan 转「元」两位小数（DESIGN.md §10）。
 */

/** 门店信息（示意数据） */
export interface StoreInfo {
  id: string
  name: string
  tableName: string
  areaName: string
  people: number
}

/** 菜品分类（示意数据） */
export interface Category {
  id: string
  name: string
}

/** 菜品 / 套餐（示意数据） */
export interface Dish {
  id: string
  categoryId: string
  name: string
  desc: string
  /** 单价，单位：分 */
  price: number
  /** 计价单位，如 例 / 条 / 份 */
  unit: string
  /** 占位图表情（原型以 CSS 渐变 + emoji 占位，见 DESIGN.md §10） */
  emoji: string
  /** 占位渐变标识：g1..g4 */
  gradient: string
  /** 是否沽清 / 停售 */
  soldOut?: boolean
  /** 是否招牌 */
  signature?: boolean
  /** 1 菜品 / 2 套餐（下单明细 dishType，缺省按分类推断） */
  dishType?: number
}

/** 购物车行 */
export interface CartItem {
  dish: Dish
  qty: number
}

/** 下单结果（示意） */
export interface OrderResult {
  orderId: string
  orderNumber: string
  totalAmount: number
  createTime: string
  payTime: string
}

/** 支付方式 */
export interface PayMethod {
  id: string
  name: string
  desc: string
}
