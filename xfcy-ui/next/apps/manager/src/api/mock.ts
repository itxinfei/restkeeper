/**
 * 管家端首版 mock 数据源。
 * 以下均为演示「示意」数据，非真实经营数据（见 DESIGN.md §10）。
 * 字段口径：金额前端统一「¥ + 两位小数」展示，后端存分。
 */

export type Trend = 'up' | 'down' | 'flat'

export type KpiItem = {
  key: string
  label: string
  value: string
  trend: Trend
  trendText: string
}

/** 工作台 KPI（示意） */
export const dashboardKpis: KpiItem[] = [
  { key: 'revenue', label: '今日营业额（示意）', value: '¥8,642.00', trend: 'up', trendText: '▲ 12.4%' },
  { key: 'orders', label: '今日单量（示意）', value: '42', trend: 'up', trendText: '▲ 6 单' },
  { key: 'avg', label: '客单价（示意）', value: '¥205.80', trend: 'down', trendText: '▼ 3.2%' },
  { key: 'turnover', label: '翻台率（示意）', value: '1.8', trend: 'flat', trendText: '目标 2.0' },
]

export type OrderStatus = '已结' | '挂账' | '未结'

export type OrderRow = {
  id: number
  orderNo: string
  table: string
  guests: number
  amount: string
  pay: string
  status: OrderStatus
  time: string
}

/** 最新订单（示意） */
export const latestOrders: OrderRow[] = [
  { id: 1, orderNo: 'FK-018', table: 'A03 大厅', guests: 4, amount: '¥328.00', pay: '微信', status: '未结', time: '12:41' },
  { id: 2, orderNo: 'FK-017', table: 'B01 包间', guests: 8, amount: '¥1,246.00', pay: '挂账', status: '挂账', time: '12:18' },
  { id: 3, orderNo: 'FK-015', table: 'A01 大厅', guests: 2, amount: '¥96.00', pay: '现金', status: '已结', time: '11:52' },
  { id: 4, orderNo: 'FK-012', table: 'C02 卡座', guests: 6, amount: '¥512.00', pay: '支付宝', status: '已结', time: '11:20' },
  { id: 5, orderNo: 'FK-009', table: 'A05 大厅', guests: 3, amount: '¥178.00', pay: '微信', status: '已结', time: '10:47' },
]

export type TableStat = {
  key: string
  label: string
  value: number
  tone: 'normal' | 'warn'
}

/** 桌台盘（示意） */
export const tableStats: TableStat[] = [
  { key: 'free', label: '空闲', value: 12, tone: 'normal' },
  { key: 'opened', label: '开台', value: 7, tone: 'warn' },
  { key: 'locked', label: '锁定', value: 1, tone: 'normal' },
]

export type SoldoutRow = {
  id: number
  name: string
  remain: string
  danger: boolean
}

/** 沽清余量（示意） */
export const soldoutRows: SoldoutRow[] = [
  { id: 1, name: '招牌红烧肉', remain: '余 3', danger: false },
  { id: 2, name: '清蒸鲈鱼', remain: '余 0', danger: true },
  { id: 3, name: '蒜蓉时蔬', remain: '不限', danger: false },
]

export type DishStatus = '起售' | '停售'

export type DishRow = {
  id: number
  name: string
  desc: string
  category: string
  code: string
  price: string
  remain: string
  remainDanger: boolean
  status: DishStatus
  /** 缩略图占位（示意）：首版用 emoji + 渐变底代替真实菜品图 */
  thumb: string
  /** 口味（示意）：点餐规格，可简化为单个口味 */
  flavor?: string
}

/** 菜品列表（示意） */
export const dishList: DishRow[] = [
  { id: 1, name: '招牌红烧肉', desc: '五花肉 · 冰糖慢炖', category: '热菜', code: 'D001', price: '¥48.00', remain: '余 3', remainDanger: false, status: '起售', thumb: '🍖' },
  { id: 2, name: '清蒸鲈鱼', desc: '鲜活 · 粤式蒸法', category: '热菜', code: 'D002', price: '¥88.00', remain: '余 0', remainDanger: true, status: '起售', thumb: '🐟' },
  { id: 3, name: '蒜蓉时蔬', desc: '时令 · 清炒', category: '热菜', code: 'D003', price: '¥22.00', remain: '不限', remainDanger: false, status: '起售', thumb: '🥬' },
  { id: 4, name: '腊味煲仔饭', desc: '丝苗米 · 锅巴香', category: '主食', code: 'D004', price: '¥36.00', remain: '不限', remainDanger: false, status: '停售', thumb: '🍚' },
  { id: 5, name: '酸梅汤', desc: '冰镇 · 解腻', category: '饮品', code: 'D005', price: '¥12.00', remain: '不限', remainDanger: false, status: '起售', thumb: '🍹' },
]

/** 分页汇总（示意） */
export const dishSummary = {
  total: 28,
  onSale: 25,
  offSale: 3,
}

export const dishCategories = ['热菜', '凉菜', '主食', '饮品']

/* ---------------- 套餐（示意） ---------------- */

export type ComboStatus = '起售' | '停售'

export type ComboRow = {
  id: number
  name: string
  code: string
  price: string
  dishCount: number
  status: ComboStatus
  /** 内含菜品（示意） */
  items: string[]
}

/** 套餐列表（示意） */
export const comboList: ComboRow[] = [
  {
    id: 1,
    name: '双人小聚套餐（示意）',
    code: 'C001',
    price: '¥128.00',
    dishCount: 3,
    status: '起售',
    items: ['招牌红烧肉', '蒜蓉时蔬', '酸梅汤 ×2'],
  },
  {
    id: 2,
    name: '家庭四人餐（示意）',
    code: 'C002',
    price: '¥268.00',
    dishCount: 5,
    status: '起售',
    items: ['清蒸鲈鱼', '招牌红烧肉', '蒜蓉时蔬', '腊味煲仔饭', '酸梅汤 ×4'],
  },
  {
    id: 3,
    name: '单人商务餐（示意）',
    code: 'C003',
    price: '¥58.00',
    dishCount: 2,
    status: '停售',
    items: ['腊味煲仔饭', '酸梅汤'],
  },
]

/* ---------------- 菜品分类（示意） ---------------- */

export type CategoryStatus = '启用' | '停用'

export type CategoryRow = {
  id: number
  name: string
  sort: number
  dishCount: number
  status: CategoryStatus
}

/** 分类列表（示意） */
export const categoryList: CategoryRow[] = [
  { id: 1, name: '热菜', sort: 1, dishCount: 3, status: '启用' },
  { id: 2, name: '凉菜', sort: 2, dishCount: 0, status: '启用' },
  { id: 3, name: '主食', sort: 3, dishCount: 1, status: '启用' },
  { id: 4, name: '饮品', sort: 4, dishCount: 1, status: '启用' },
  { id: 5, name: '甜点', sort: 5, dishCount: 0, status: '停用' },
]

/* ---------------- 口味库（示意） ---------------- */

export const flavorTypes = ['辣度', '甜度', '温度', '口感']

export type FlavorRow = {
  id: number
  name: string
  type: string
  dishCount: number
  status: CategoryStatus
}

/** 口味列表（示意） */
export const flavorList: FlavorRow[] = [
  { id: 1, name: '微辣', type: '辣度', dishCount: 2, status: '启用' },
  { id: 2, name: '中辣', type: '辣度', dishCount: 1, status: '启用' },
  { id: 3, name: '特辣', type: '辣度', dishCount: 0, status: '停用' },
  { id: 4, name: '少糖', type: '甜度', dishCount: 1, status: '启用' },
  { id: 5, name: '冰镇', type: '温度', dishCount: 2, status: '启用' },
  { id: 6, name: '去冰', type: '温度', dishCount: 1, status: '启用' },
  { id: 7, name: '加冰', type: '温度', dishCount: 0, status: '启用' },
  { id: 8, name: '酥脆', type: '口感', dishCount: 1, status: '启用' },
]
