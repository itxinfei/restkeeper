/**
 * 运营中心首版 mock 数据源。
 * 以下均为演示「示意」数据，非真实经营数据（见 DESIGN.md §10）。
 * 首版仅在内存中维护，刷新页面即还原；后续接入真实接口时替换本文件即可。
 */

/* ---------------- 企业账号（示意） ---------------- */

export type EnterpriseStatus = '试用' | '正式' | '禁用'
export type Plan = '基础版' | '标准版' | '旗舰版'

export type EnterpriseRow = {
  id: number
  code: string
  name: string
  contact: string
  phone: string
  shopCount: number
  plan: Plan
  status: EnterpriseStatus
  expireDate: string
  createdAt: string
}

/** 企业账号列表（示意） */
export const enterpriseList: EnterpriseRow[] = [
  { id: 1, code: '88010023', name: '心飞餐饮（国贸店）', contact: '李国栋', phone: '13801002301', shopCount: 3, plan: '旗舰版', status: '正式', expireDate: '2027-06-30', createdAt: '2025-06-30' },
  { id: 2, code: '88010024', name: '心飞餐饮（望京店）', contact: '赵敏', phone: '13801002302', shopCount: 1, plan: '基础版', status: '试用', expireDate: '2026-11-29', createdAt: '2026-11-15' },
  { id: 3, code: '88010025', name: '老城根火锅', contact: '孙浩', phone: '13801002303', shopCount: 2, plan: '标准版', status: '正式', expireDate: '2027-01-15', createdAt: '2025-09-10' },
  { id: 4, code: '88010026', name: '川香居', contact: '周婷', phone: '13801002304', shopCount: 1, plan: '基础版', status: '禁用', expireDate: '2026-05-20', createdAt: '2024-05-20' },
  { id: 5, code: '88010027', name: '蓝湾咖啡', contact: '吴斌', phone: '13801002305', shopCount: 4, plan: '标准版', status: '正式', expireDate: '2027-03-01', createdAt: '2025-11-22' },
]

export const planOptions: Plan[] = ['基础版', '标准版', '旗舰版']

/* ---------------- 回收站（示意） ---------------- */

export type RecycleRow = {
  id: number
  code: string
  name: string
  contact: string
  shopCount: number
  deletedAt: string
  deletedBy: string
}

/** 已删除企业（示意） */
export const recycleList: RecycleRow[] = [
  { id: 101, code: '88010018', name: '悦来小馆', contact: '郑爽', shopCount: 1, deletedAt: '2026-11-18 16:20', deletedBy: '王运营' },
  { id: 102, code: '88010019', name: '巷口面馆', contact: '冯远', shopCount: 2, deletedAt: '2026-11-20 09:05', deletedBy: '王运营' },
]

/* ---------------- 字典管理（示意） ---------------- */

export type DictStatus = '启用' | '停用'

export type DictRow = {
  id: number
  type: string
  code: string
  value: string
  sort: number
  status: DictStatus
}

/** 字典项（示意） */
export const dictList: DictRow[] = [
  { id: 1, type: '菜品分类', code: 'dish_category', value: '热菜', sort: 1, status: '启用' },
  { id: 2, type: '菜品分类', code: 'dish_category', value: '主食', sort: 2, status: '启用' },
  { id: 3, type: '订单状态', code: 'order_status', value: '已结', sort: 1, status: '启用' },
  { id: 4, type: '订单状态', code: 'order_status', value: '挂账', sort: 2, status: '启用' },
  { id: 5, type: '支付方式', code: 'pay_type', value: '微信', sort: 1, status: '启用' },
  { id: 6, type: '门店标签', code: 'shop_tag', value: '商场店', sort: 3, status: '停用' },
]

export const dictTypes = ['菜品分类', '订单状态', '支付方式', '门店标签']

/* ---------------- 运营人员（示意） ---------------- */

export type OperatorRole = '平台运营' | '运营' | '客服'

export type OperatorRow = {
  id: number
  account: string
  name: string
  role: OperatorRole
  status: DictStatus
  lastLogin: string
}

/** 运营人员（示意） */
export const operatorList: OperatorRow[] = [
  { id: 1, account: 'admin', name: '王运营', role: '平台运营', status: '启用', lastLogin: '2026-11-22 09:12' },
  { id: 2, account: 'ops01', name: '陈晓', role: '运营', status: '启用', lastLogin: '2026-11-21 18:40' },
  { id: 3, account: 'ops02', name: '刘洋', role: '客服', status: '停用', lastLogin: '2026-10-30 11:05' },
]

export const operatorRoles: OperatorRole[] = ['平台运营', '运营', '客服']

/* ---------------- 服务监控（示意） ---------------- */

export type ServiceStatus = '在线' | '离线' | '告警'

export type ServiceRow = {
  key: string
  name: string
  group: string
  status: ServiceStatus
  instances: number
  version: string
  heartbeat: string
}

/** Nacos 注册服务状态（示意，非真实探测结果） */
export const serviceList: ServiceRow[] = [
  { key: 'gateway', name: 'xfcy-gateway', group: '网关', status: '在线', instances: 2, version: '1.0.0', heartbeat: '10s 前' },
  { key: 'operator', name: 'xfcy-operator', group: '运营', status: '在线', instances: 1, version: '1.0.0', heartbeat: '12s 前' },
  { key: 'store', name: 'xfcy-store', group: '门店', status: '在线', instances: 2, version: '1.0.0', heartbeat: '9s 前' },
  { key: 'order', name: 'xfcy-order', group: '订单', status: '在线', instances: 1, version: '1.0.0', heartbeat: '11s 前' },
  { key: 'shop', name: 'xfcy-shop', group: '商品', status: '告警', instances: 1, version: '1.0.0', heartbeat: '45s 前' },
  { key: 'es', name: 'xfcy-es', group: '检索', status: '在线', instances: 1, version: '1.0.0', heartbeat: '8s 前' },
]

/** 监控 KPI（示意） */
export const monitorKpis = [
  { key: 'total', label: '注册服务（示意）', value: '6', trendText: 'Nacos 概览' },
  { key: 'online', label: '在线（示意）', value: '5', trendText: '最近心跳正常' },
  { key: 'offline', label: '离线（示意）', value: '0', trendText: '—' },
  { key: 'alert', label: '告警（示意）', value: '1', trendText: '需关注' },
]
