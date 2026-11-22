/**
 * 收银端展示模型与映射辅助。
 *
 * 数据源已统一到 @xfcy/api（mock/真实双模式，默认 mock）：
 * - 桌台区域/桌台：table.areaList + table.pageByArea
 * - 菜品分类/菜品：dish.categoryPage + dish.page
 * - 开台/加菜/结账/换台：table.openTable + order.addOrder/pay/changeTable
 *
 * 本文件只负责把契约实体映射为触屏 POS 的展示形状，不再保存本地 mock 数据。
 * 金额统一为「分」，展示用 @xfcy/api 的 formatYuan。
 */

export type TableStatus = '空闲' | '开台' | '锁定'

export type Area = {
  id: string
  name: string
}

export type PosTable = {
  id: string
  name: string
  areaId: string
  area: string
  seats: number
  status: TableStatus
}

export type DishStatus = '起售' | '停售'

export type PosDish = {
  id: string
  categoryId: string
  name: string
  desc: string
  /** 单价，单位「分」 */
  price: number
  /** 沽清余量：null 表示不限；0 表示沽清（不可点）。真实契约无此字段，来自 mock 扩展 */
  remain: number | null
  status: DishStatus
  /** 缩略图占位（示意）：emoji + 渐变底代替真实菜品图 */
  thumb: string
}

export type Category = {
  id: string
  name: string
}

/** 桌台状态：0 空闲 / 1 开台 / 2 锁定 */
export function tableStatusText(status?: number): TableStatus {
  if (status === 1) return '开台'
  if (status === 2) return '锁定'
  return '空闲'
}

export type PayMethod = '现金' | '微信' | '支付宝' | '挂账'

/** 支付方式 → 订单 payType（0 免单/1 现金/2 微信/3 支付宝/4 银行卡/5 挂账） */
export function payTypeFromMethod(method: PayMethod): number {
  if (method === '微信') return 2
  if (method === '支付宝') return 3
  if (method === '挂账') return 5
  return 1
}
