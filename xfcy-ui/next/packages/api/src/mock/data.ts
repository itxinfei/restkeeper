/**
 * mock 演示数据源。
 *
 * 由 apps/manager 首版 mock 迁移而来，字段口径保持不变：
 * - 契约实体字段（Dish/SetMeal/Table 等）按后端实体形状，金额为「分」。
 * - 工作台/菜品页展示用数组（demo*）沿用原型示意数据，仅 mock 模式使用。
 *
 * 注意：以下均为演示「示意」数据，非真实经营数据。
 */

import type {
  Dish,
  DishCategory,
  OrderEntity,
  SetMeal,
  Table,
  TableArea,
} from '../types'

/* ---------------- 菜品分类（示意） ---------------- */

export const mockCategories: DishCategory[] = [
  { categoryId: 'cat-1', type: 1, name: '热菜', torder: 1 },
  { categoryId: 'cat-2', type: 1, name: '凉菜', torder: 2 },
  { categoryId: 'cat-3', type: 1, name: '主食', torder: 3 },
  { categoryId: 'cat-4', type: 1, name: '饮品', torder: 4 },
  { categoryId: 'cat-5', type: 1, name: '甜点', torder: 5 },
]

/* ---------------- 菜品（示意，价格单位：分） ---------------- */

export const mockDishes: Dish[] = [
  {
    id: '1',
    name: '招牌红烧肉',
    description: '五花肉 · 冰糖慢炖',
    categoryId: 'cat-1',
    code: 'D001',
    price: 4800,
    status: 1,
    image: '🍖',
    dishCategory: mockCategories[0],
  },
  {
    id: '2',
    name: '清蒸鲈鱼',
    description: '鲜活 · 粤式蒸法',
    categoryId: 'cat-1',
    code: 'D002',
    price: 8800,
    status: 1,
    image: '🐟',
    dishCategory: mockCategories[0],
  },
  {
    id: '3',
    name: '蒜蓉时蔬',
    description: '时令 · 清炒',
    categoryId: 'cat-1',
    code: 'D003',
    price: 2200,
    status: 1,
    image: '🥬',
    dishCategory: mockCategories[0],
  },
  {
    id: '4',
    name: '腊味煲仔饭',
    description: '丝苗米 · 锅巴香',
    categoryId: 'cat-3',
    code: 'D004',
    price: 3600,
    status: 0,
    image: '🍚',
    dishCategory: mockCategories[2],
  },
  {
    id: '5',
    name: '酸梅汤',
    description: '冰镇 · 解腻',
    categoryId: 'cat-4',
    code: 'D005',
    price: 1200,
    status: 1,
    image: '🍹',
    dishCategory: mockCategories[3],
  },
]

/* ---------------- 套餐（示意，价格单位：分） ---------------- */

export const mockSetMeals: SetMeal[] = [
  {
    id: 'combo-1',
    name: '双人小聚套餐（示意）',
    code: 'C001',
    price: 12800,
    status: 1,
    categoryId: 'cat-1',
    dishList: [
      { id: 'sd-1', setMealId: 'combo-1', dishId: '1', dishName: '招牌红烧肉', dishCopies: 1, index: 0 },
      { id: 'sd-2', setMealId: 'combo-1', dishId: '3', dishName: '蒜蓉时蔬', dishCopies: 1, index: 1 },
      { id: 'sd-3', setMealId: 'combo-1', dishId: '5', dishName: '酸梅汤', dishCopies: 2, index: 2 },
    ],
  },
  {
    id: 'combo-2',
    name: '家庭四人餐（示意）',
    code: 'C002',
    price: 26800,
    status: 1,
    categoryId: 'cat-1',
    dishList: [
      { id: 'sd-4', setMealId: 'combo-2', dishId: '2', dishName: '清蒸鲈鱼', dishCopies: 1, index: 0 },
      { id: 'sd-5', setMealId: 'combo-2', dishId: '1', dishName: '招牌红烧肉', dishCopies: 1, index: 1 },
      { id: 'sd-6', setMealId: 'combo-2', dishId: '3', dishName: '蒜蓉时蔬', dishCopies: 1, index: 2 },
      { id: 'sd-7', setMealId: 'combo-2', dishId: '4', dishName: '腊味煲仔饭', dishCopies: 1, index: 3 },
      { id: 'sd-8', setMealId: 'combo-2', dishId: '5', dishName: '酸梅汤', dishCopies: 4, index: 4 },
    ],
  },
  {
    id: 'combo-3',
    name: '单人商务餐（示意）',
    code: 'C003',
    price: 5800,
    status: 0,
    categoryId: 'cat-1',
    dishList: [
      { id: 'sd-9', setMealId: 'combo-3', dishId: '4', dishName: '腊味煲仔饭', dishCopies: 1, index: 0 },
      { id: 'sd-10', setMealId: 'combo-3', dishId: '5', dishName: '酸梅汤', dishCopies: 1, index: 1 },
    ],
  },
]

/* ---------------- 桌台区域 / 桌台（示意） ---------------- */

export const mockTableAreas: TableArea[] = [
  { areaId: 'area-1', areaName: '大厅', indexNumber: 1 },
  { areaId: 'area-2', areaName: '包间', indexNumber: 2 },
  { areaId: 'area-3', areaName: '卡座', indexNumber: 3 },
]

export const mockTables: Table[] = [
  { tableId: 't-1', areaId: 'area-1', tableName: 'A01 大厅', tableSeatNumber: 2, status: 0 },
  { tableId: 't-2', areaId: 'area-1', tableName: 'A03 大厅', tableSeatNumber: 4, status: 1 },
  { tableId: 't-3', areaId: 'area-1', tableName: 'A05 大厅', tableSeatNumber: 3, status: 0 },
  { tableId: 't-4', areaId: 'area-2', tableName: 'B01 包间', tableSeatNumber: 8, status: 1 },
  { tableId: 't-5', areaId: 'area-3', tableName: 'C02 卡座', tableSeatNumber: 6, status: 0 },
]

/* ---------------- 订单（示意，金额单位：分） ---------------- */

export const mockOrders: OrderEntity[] = [
  {
    orderId: '1',
    orderNumber: 'FK-018',
    tableId: 't-2',
    payType: 2,
    payStatus: 0,
    payAmount: 32800,
    totalAmount: 32800,
    personNumbers: 4,
    createTime: '2026-11-22T12:41:00',
    operatorName: '李国栋',
  },
  {
    orderId: '2',
    orderNumber: 'FK-017',
    tableId: 't-4',
    payType: 5,
    payStatus: 0,
    payAmount: 124600,
    totalAmount: 124600,
    personNumbers: 8,
    createTime: '2026-11-22T12:18:00',
    operatorName: '李国栋',
  },
  {
    orderId: '3',
    orderNumber: 'FK-015',
    tableId: 't-1',
    payType: 1,
    payStatus: 1,
    payAmount: 9600,
    totalAmount: 9600,
    personNumbers: 2,
    createTime: '2026-11-22T11:52:00',
    operatorName: '李国栋',
  },
]

/* ---------------- 工作台展示数据（示意，仅 mock 使用） ---------------- */

export type Trend = 'up' | 'down' | 'flat'

export type DemoKpi = {
  key: string
  label: string
  value: string
  trend: Trend
  trendText: string
}

export const demoDashboardKpis: DemoKpi[] = [
  { key: 'revenue', label: '今日营业额（示意）', value: '¥8,642.00', trend: 'up', trendText: '▲ 12.4%' },
  { key: 'orders', label: '今日单量（示意）', value: '42', trend: 'up', trendText: '▲ 6 单' },
  { key: 'avg', label: '客单价（示意）', value: '¥205.80', trend: 'down', trendText: '▼ 3.2%' },
  { key: 'turnover', label: '翻台率（示意）', value: '1.8', trend: 'flat', trendText: '目标 2.0' },
]

export type DemoOrderStatus = '已结' | '挂账' | '未结'

export type DemoOrderRow = {
  id: number
  orderNo: string
  table: string
  guests: number
  amount: string
  pay: string
  status: DemoOrderStatus
  time: string
}

export const demoLatestOrders: DemoOrderRow[] = [
  { id: 1, orderNo: 'FK-018', table: 'A03 大厅', guests: 4, amount: '¥328.00', pay: '微信', status: '未结', time: '12:41' },
  { id: 2, orderNo: 'FK-017', table: 'B01 包间', guests: 8, amount: '¥1,246.00', pay: '挂账', status: '挂账', time: '12:18' },
  { id: 3, orderNo: 'FK-015', table: 'A01 大厅', guests: 2, amount: '¥96.00', pay: '现金', status: '已结', time: '11:52' },
  { id: 4, orderNo: 'FK-012', table: 'C02 卡座', guests: 6, amount: '¥512.00', pay: '支付宝', status: '已结', time: '11:20' },
  { id: 5, orderNo: 'FK-009', table: 'A05 大厅', guests: 3, amount: '¥178.00', pay: '微信', status: '已结', time: '10:47' },
]

export type DemoTableStat = {
  key: string
  label: string
  value: number
  tone: 'normal' | 'warn'
}

export const demoTableStats: DemoTableStat[] = [
  { key: 'free', label: '空闲', value: 12, tone: 'normal' },
  { key: 'opened', label: '开台', value: 7, tone: 'warn' },
  { key: 'locked', label: '锁定', value: 1, tone: 'normal' },
]

export type DemoSoldoutRow = {
  id: number
  name: string
  remain: string
  danger: boolean
}

export const demoSoldoutRows: DemoSoldoutRow[] = [
  { id: 1, name: '招牌红烧肉', remain: '余 3', danger: false },
  { id: 2, name: '清蒸鲈鱼', remain: '余 0', danger: true },
  { id: 3, name: '蒜蓉时蔬', remain: '不限', danger: false },
]
