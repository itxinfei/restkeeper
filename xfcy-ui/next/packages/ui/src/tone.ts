/**
 * 状态语义 → 标签色调的唯一映射（DESIGN.md §2 语义固定）。
 * 各端不得自行硬编码颜色判断，统一走 toneOf / labelOf。
 */
export type Tone = 'success' | 'warning' | 'danger' | 'info' | 'neutral' | 'primary'

export type ToneDomain = 'pay' | 'table' | 'sale' | 'account' | 'staff' | 'credit'

const TONES: Record<ToneDomain, Record<string, Tone>> = {
  // 支付：未结=危险红、已结=成功绿、挂账=警告橙
  pay: {
    '0': 'danger',
    '1': 'success',
    '5': 'warning',
    未结: 'danger',
    已结: 'success',
    挂账: 'warning',
  },
  // 桌台：空闲=中性、开台=主色、锁定=信息
  table: {
    '0': 'neutral',
    '1': 'primary',
    '2': 'info',
    空闲: 'neutral',
    开台: 'primary',
    锁定: 'info',
  },
  // 菜品 / 套餐：起售=成功绿、停售=危险红
  sale: {
    '0': 'danger',
    '1': 'success',
    起售: 'success',
    停售: 'danger',
  },
  // 企业账号：试用=警告、正式=成功、停用=中性
  account: {
    '0': 'warning',
    '1': 'success',
    '-1': 'neutral',
    试用: 'warning',
    正式: 'success',
    停用: 'neutral',
  },
  // 员工：在职=成功、停用=中性
  staff: {
    '0': 'neutral',
    '1': 'success',
    在职: 'success',
    停用: 'neutral',
  },
  // 挂账单位：启用=成功、停用=中性
  credit: {
    '0': 'neutral',
    '1': 'success',
    启用: 'success',
    停用: 'neutral',
  },
}

/** 支付状态标签文案 */
export const PAY_LABELS: Record<string, string> = {
  '0': '未结',
  '1': '已结',
  '5': '挂账',
}

/** 支付方式文案（OrderEntity.payType） */
export const PAY_TYPE_LABELS: Record<string, string> = {
  '0': '免单',
  '1': '现金',
  '2': '微信',
  '3': '支付宝',
  '4': '银行卡',
  '5': '挂账',
}

/** 桌台状态文案 */
export const TABLE_LABELS: Record<string, string> = {
  '0': '空闲',
  '1': '开台',
  '2': '锁定',
}

/** 菜品 / 套餐状态文案 */
export const SALE_LABELS: Record<string, string> = {
  '0': '停售',
  '1': '起售',
}

/** 企业账号状态文案 */
export const ACCOUNT_LABELS: Record<string, string> = {
  '0': '试用',
  '1': '正式',
  '-1': '停用',
}

/** 员工状态文案 */
export const STAFF_LABELS: Record<string, string> = {
  '0': '停用',
  '1': '在职',
}

/** 挂账单位状态文案 */
export const CREDIT_LABELS: Record<string, string> = {
  '0': '停用',
  '1': '启用',
}

function keyOf(value: string | number | null | undefined): string {
  if (value === null || value === undefined) return ''
  return String(value)
}

/** 取某领域取值的标签色调；未知取值回退 neutral */
export function toneOf(domain: ToneDomain, value: string | number | null | undefined): Tone {
  const table = TONES[domain]
  const hit = table[keyOf(value)]
  return hit ?? 'neutral'
}

const LABELS: Record<ToneDomain, Record<string, string>> = {
  pay: PAY_LABELS,
  table: TABLE_LABELS,
  sale: SALE_LABELS,
  account: ACCOUNT_LABELS,
  staff: STAFF_LABELS,
  credit: CREDIT_LABELS,
}

/** 取某领域取值的展示文案；未知取值回退原值 */
export function labelOf(
  domain: ToneDomain,
  value: string | number | null | undefined,
): string {
  const key = keyOf(value)
  if (!key) return ''
  return LABELS[domain][key] ?? key
}

/** 支付方式文案 */
export function payTypeLabel(value: string | number | null | undefined): string {
  return PAY_TYPE_LABELS[keyOf(value)] ?? keyOf(value)
}
