/**
 * @xfcy/ui · 心飞餐饮五端统一组件库入口
 * 组件前缀 Xf；仅依赖 vue ^3.5，不含任何第三方 UI 库。
 */
import './styles/tokens.css'

export { default as XfButton } from './components/XfButton.vue'
export { default as XfInput } from './components/XfInput.vue'
export { default as XfSelect } from './components/XfSelect.vue'
export { default as XfTag } from './components/XfTag.vue'
export { default as XfPanel } from './components/XfPanel.vue'
export { default as XfKpi } from './components/XfKpi.vue'
export { default as XfTabs } from './components/XfTabs.vue'
export { default as XfTable } from './components/XfTable.vue'
export { default as XfEmpty } from './components/XfEmpty.vue'
export { default as XfPagination } from './components/XfPagination.vue'
export { default as XfModal } from './components/XfModal.vue'
export { default as XfField } from './components/XfField.vue'
export { default as XfToastHost } from './components/XfToastHost.vue'
export { default as XfDemoBadge } from './components/XfDemoBadge.vue'
export { default as XfErrorState } from './components/XfErrorState.vue'
export { default as XfAppShell } from './components/XfAppShell.vue'
export { default as XfUserMenu } from './components/XfUserMenu.vue'
export { useToast, resetToastState } from './toast'
export type { ToastItem } from './toast'
export { toneOf, labelOf, payTypeLabel } from './tone'
export { PAY_LABELS, PAY_TYPE_LABELS, TABLE_LABELS, SALE_LABELS, ACCOUNT_LABELS, STAFF_LABELS, CREDIT_LABELS } from './tone'
export type { Tone, ToneDomain } from './tone'
