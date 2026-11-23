import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfDemoBadge from '../XfDemoBadge.vue'
import XfErrorState from '../XfErrorState.vue'

describe('XfDemoBadge', () => {
  it('show=false 时不渲染', () => {
    const wrapper = mount(XfDemoBadge, { props: { show: false } })
    expect(wrapper.find('.xf-tag').exists()).toBe(false)
  })

  it('show=true 时显示警告标签与文案', () => {
    const wrapper = mount(XfDemoBadge, { props: { show: true } })
    const tag = wrapper.get('.xf-tag')
    expect(tag.classes()).toContain('xf-tag--warning')
    expect(tag.text()).toBe('演示数据')
  })
})

describe('XfErrorState', () => {
  it('展示标题与错误信息', () => {
    const wrapper = mount(XfErrorState, { props: { message: '连接超时' } })
    expect(wrapper.get('.xf-error__title').text()).toBe('加载失败')
    expect(wrapper.get('.xf-error__msg').text()).toBe('连接超时')
  })

  it('点击重试抛出 retry 事件', async () => {
    const wrapper = mount(XfErrorState, { props: { message: '连接超时' } })
    await wrapper.get('button').trigger('click')
    expect(wrapper.emitted('retry')).toHaveLength(1)
  })

  it('无错误信息时不渲染副文案', () => {
    const wrapper = mount(XfErrorState)
    expect(wrapper.find('.xf-error__msg').exists()).toBe(false)
  })
})
