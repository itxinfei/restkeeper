/**
 * 金额工具：后端与接口均为「分」（Integer），前端统一「元」两位小数展示（DESIGN.md §10）。
 */

/** 分 → 元字符串（两位小数），如 4800 → "48.00" */
export function formatYuan(fen: number): string {
  return (fen / 100).toFixed(2)
}

/** 展示用：¥ + 两位小数，如 ¥48.00 */
export function yuan(fen: number): string {
  return `¥${formatYuan(fen)}`
}

/** 当前时间 HH:mm */
export function nowHHmm(date = new Date()): string {
  const h = `${date.getHours()}`.padStart(2, '0')
  const m = `${date.getMinutes()}`.padStart(2, '0')
  return `${h}:${m}`
}

/** 生成示意订单号：FK + yyyyMMdd + 序号 */
export function makeOrderNumber(date = new Date()): string {
  const y = date.getFullYear()
  const m = `${date.getMonth() + 1}`.padStart(2, '0')
  const d = `${date.getDate()}`.padStart(2, '0')
  const seq = `${Math.floor(Math.random() * 900) + 100}`
  return `FK${y}${m}${d}-${seq}`
}
