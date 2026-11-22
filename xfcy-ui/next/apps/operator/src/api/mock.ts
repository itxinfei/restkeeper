/**
 * 运营中心「服务监控」演示数据源。
 *
 * 说明：企业账号 / 回收站 / 字典 / 运营人员已接入 @xfcy/api（mock/真实双模式），
 * 本文件仅保留服务监控页的示意数据（后端暂无 Nacos 探测契约）。
 * 以下均为演示「示意」数据，非真实探测结果（见 DESIGN.md §10）。
 */

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
