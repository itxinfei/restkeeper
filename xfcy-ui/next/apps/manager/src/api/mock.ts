/**
 * @deprecated 首版 mock 数据已迁移至共享包 @xfcy/api（packages/api）。
 * 本文件仅保留兼容再导出，请勿在新代码中引用。
 */
export {
  demoDashboardKpis as dashboardKpis,
  demoLatestOrders as latestOrders,
  demoSoldoutRows as soldoutRows,
  demoTableStats as tableStats,
} from '@xfcy/api'
export type { DemoOrderStatus as OrderStatus } from '@xfcy/api'
