import { describe, expect, it } from 'vitest'
import {
  ACCOUNT_LABELS,
  PAY_LABELS,
  SALE_LABELS,
  TABLE_LABELS,
  labelOf,
  payTypeLabel,
  toneOf,
} from '../../tone'

describe('tone 状态语义映射', () => {
  it('支付状态：未结危险 / 已结成功 / 挂账警告', () => {
    expect(toneOf('pay', 0)).toBe('danger')
    expect(toneOf('pay', '未结')).toBe('danger')
    expect(toneOf('pay', 1)).toBe('success')
    expect(toneOf('pay', 5)).toBe('warning')
  })

  it('桌台状态：空闲中性 / 开台主色 / 锁定信息', () => {
    expect(toneOf('table', 0)).toBe('neutral')
    expect(toneOf('table', 1)).toBe('primary')
    expect(toneOf('table', 2)).toBe('info')
  })

  it('菜品与账号：起售成功 / 停售危险；试用警告 / 正式成功 / 停用中性', () => {
    expect(toneOf('sale', 1)).toBe('success')
    expect(toneOf('sale', 0)).toBe('danger')
    expect(toneOf('account', 0)).toBe('warning')
    expect(toneOf('account', 1)).toBe('success')
    expect(toneOf('account', -1)).toBe('neutral')
  })

  it('未知取值回退 neutral，不抛错', () => {
    expect(toneOf('pay', 99)).toBe('neutral')
    expect(toneOf('pay', undefined)).toBe('neutral')
  })

  it('文案映射与色调表一致', () => {
    expect(labelOf('pay', 0)).toBe(PAY_LABELS['0'])
    expect(labelOf('pay', 0)).toBe('未结')
    expect(labelOf('table', 2)).toBe(TABLE_LABELS['2'])
    expect(labelOf('sale', 1)).toBe(SALE_LABELS['1'])
    expect(labelOf('account', -1)).toBe(ACCOUNT_LABELS['-1'])
    expect(labelOf('pay', 42)).toBe('42')
  })

  it('支付方式文案覆盖 6 种取值', () => {
    expect(payTypeLabel(1)).toBe('现金')
    expect(payTypeLabel(2)).toBe('微信')
    expect(payTypeLabel(3)).toBe('支付宝')
    expect(payTypeLabel(4)).toBe('银行卡')
    expect(payTypeLabel(5)).toBe('挂账')
    expect(payTypeLabel(0)).toBe('免单')
  })
})
