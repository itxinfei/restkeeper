/**
 * 兼容出口：C 端示意数据已上移到 @xfcy/api 的 mock/microapp。
 * 页面请优先使用 ./service（内部已按 mock / 真实模式取数）。
 */
export {
  mockMicroappStore as mockStore,
  mockMicroappCategories as mockCategories,
  mockMicroappDishes as mockDishes,
  mockMicroappPayMethods as mockPayMethods,
} from '@xfcy/api'
