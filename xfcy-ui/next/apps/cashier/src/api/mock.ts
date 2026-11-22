/**
 * 收银端首版 mock 数据源。
 * 以下均为演示「示意」数据，非真实经营数据（见 DESIGN.md §10）。
 * 金额字段单位为「元」（number），展示统一用 formatYuan 转为「¥ + 两位小数」。
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

/** 区域（示意） */
export const areas: Area[] = [
  { id: 'all', name: '全部' },
  { id: 'hall', name: '大厅' },
  { id: 'booth', name: '卡座' },
  { id: 'room', name: '包间' },
]

/** 桌台（示意）：状态语义见 DESIGN.md §2 */
export const tables: PosTable[] = [
  { id: 'A01', name: 'A01', areaId: 'hall', area: '大厅', seats: 2, status: '空闲' },
  { id: 'A02', name: 'A02', areaId: 'hall', area: '大厅', seats: 4, status: '开台' },
  { id: 'A03', name: 'A03', areaId: 'hall', area: '大厅', seats: 4, status: '开台' },
  { id: 'A04', name: 'A04', areaId: 'hall', area: '大厅', seats: 6, status: '空闲' },
  { id: 'A05', name: 'A05', areaId: 'hall', area: '大厅', seats: 2, status: '锁定' },
  { id: 'B01', name: 'B01', areaId: 'booth', area: '卡座', seats: 4, status: '空闲' },
  { id: 'B02', name: 'B02', areaId: 'booth', area: '卡座', seats: 6, status: '开台' },
  { id: 'B03', name: 'B03', areaId: 'booth', area: '卡座', seats: 4, status: '空闲' },
  { id: 'C01', name: 'C01', areaId: 'room', area: '包间', seats: 8, status: '开台' },
  { id: 'C02', name: 'C02', areaId: 'room', area: '包间', seats: 10, status: '空闲' },
]

export type DishStatus = '起售' | '停售'

export type Dish = {
  id: string
  categoryId: string
  name: string
  desc: string
  price: number
  /** 沽清余量：null 表示不限；0 表示沽清（不可点） */
  remain: number | null
  status: DishStatus
  /** 缩略图占位（示意）：首版用 emoji + 渐变底代替真实菜品图 */
  thumb: string
}

export type Category = {
  id: string
  name: string
}

/** 菜品分类（示意，顺序对齐 docs/ui/cashier.html 原型） */
export const categories: Category[] = [
  { id: 'rec', name: '招牌推荐' },
  { id: 'hot', name: '热菜' },
  { id: 'cold', name: '凉菜' },
  { id: 'staple', name: '主食' },
  { id: 'drink', name: '饮品' },
  { id: 'combo', name: '套餐' },
  { id: 'liquor', name: '酒水' },
]

/** 菜品（示意） */
export const dishes: Dish[] = [
  { id: 'D001', categoryId: 'rec', name: '招牌红烧肉', desc: '五花肉 · 冰糖慢炖', price: 48, remain: 3, status: '起售', thumb: '🍖' },
  { id: 'D002', categoryId: 'rec', name: '清蒸鲈鱼', desc: '鲜活 · 粤式蒸法', price: 88, remain: 0, status: '起售', thumb: '🐟' },
  { id: 'D003', categoryId: 'rec', name: '毛血旺', desc: '麻辣 · 现煮', price: 68, remain: 5, status: '起售', thumb: '🍲' },
  { id: 'D004', categoryId: 'hot', name: '蒜蓉时蔬', desc: '时令 · 清炒', price: 22, remain: null, status: '起售', thumb: '🥬' },
  { id: 'D005', categoryId: 'hot', name: '宫保鸡丁', desc: '经典 · 微辣', price: 38, remain: null, status: '起售', thumb: '🍗' },
  { id: 'D006', categoryId: 'cold', name: '凉拌黄瓜', desc: '蒜香 · 爽脆', price: 18, remain: null, status: '起售', thumb: '🥒' },
  { id: 'D007', categoryId: 'cold', name: '夫妻肺片', desc: '红油 · 卤香', price: 32, remain: 4, status: '起售', thumb: '🌶️' },
  { id: 'D008', categoryId: 'staple', name: '腊味煲仔饭', desc: '丝苗米 · 锅巴香', price: 36, remain: null, status: '停售', thumb: '🍚' },
  { id: 'D009', categoryId: 'staple', name: '手工水饺', desc: '猪肉白菜 · 12 只', price: 28, remain: null, status: '起售', thumb: '🥟' },
  { id: 'D010', categoryId: 'staple', name: '米饭', desc: '东北大米', price: 3, remain: null, status: '起售', thumb: '🍙' },
  { id: 'D011', categoryId: 'drink', name: '酸梅汤', desc: '冰镇 · 解腻', price: 12, remain: null, status: '起售', thumb: '🍹' },
  { id: 'D012', categoryId: 'drink', name: '红糖冰粉', desc: '手工 · 冰爽', price: 16, remain: null, status: '起售', thumb: '🍨' },
  { id: 'D013', categoryId: 'combo', name: '双人小聚套餐', desc: '红烧肉 + 时蔬 + 酸梅汤 ×2', price: 128, remain: null, status: '起售', thumb: '🍱' },
  { id: 'D014', categoryId: 'combo', name: '家庭四人餐', desc: '鲈鱼 + 红烧肉 + 时蔬 + 主食 + 饮品', price: 268, remain: 2, status: '起售', thumb: '🎉' },
  { id: 'D015', categoryId: 'liquor', name: '青岛啤酒', desc: '500ml · 冰镇', price: 10, remain: 20, status: '起售', thumb: '🍺' },
]

/** 金额展示：¥ + 两位小数（DESIGN.md §10） */
export function formatYuan(value: number): string {
  return `¥${value.toFixed(2)}`
}
