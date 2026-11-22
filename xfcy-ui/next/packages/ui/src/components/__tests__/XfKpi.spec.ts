import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfKpi from '../XfKpi.vue'

describe('XfKpi', () => {
  it('renders label and value with group semantics', () => {
    const wrapper = mount(XfKpi, { props: { label: '营业额', value: 1288 } })
    expect(wrapper.attributes('role')).toBe('group')
    expect(wrapper.attributes('aria-label')).toBe('营业额')
    expect(wrapper.get('.xf-kpi__label').text()).toBe('营业额')
    expect(wrapper.get('.xf-kpi__value').text()).toBe('1288')
  })

  it('renders flat trend by default', () => {
    const wrapper = mount(XfKpi, { props: { label: 'x', value: 1 } })
    const trend = wrapper.get('.xf-kpi__trend')
    expect(trend.classes()).toContain('is-flat')
    expect(trend.attributes('aria-label')).toBe('同比持平')
  })

  it('renders up trend with custom text', () => {
    const wrapper = mount(XfKpi, { props: { label: 'x', value: 1, trend: 'up', trendText: '+12%' } })
    const trend = wrapper.get('.xf-kpi__trend')
    expect(trend.classes()).toContain('is-up')
    expect(trend.text()).toContain('+12%')
    expect(trend.attributes('aria-label')).toBe('同比上升')
  })

  it('renders down trend', () => {
    const wrapper = mount(XfKpi, { props: { label: 'x', value: 1, trend: 'down' } })
    expect(wrapper.get('.xf-kpi__trend').classes()).toContain('is-down')
    expect(wrapper.get('.xf-kpi__arrow').text()).toBe('↓')
  })
})
