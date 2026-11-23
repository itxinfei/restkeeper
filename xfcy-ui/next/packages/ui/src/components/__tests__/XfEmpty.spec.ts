import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfEmpty from '../XfEmpty.vue'

describe('XfEmpty', () => {
  it('renders the decorative icon only by default', () => {
    const wrapper = mount(XfEmpty)
    expect(wrapper.get('svg').attributes('aria-hidden')).toBe('true')
    expect(wrapper.find('.xf-empty__title').exists()).toBe(false)
    expect(wrapper.find('.xf-empty__description').exists()).toBe(false)
  })

  it('renders title and description', () => {
    const wrapper = mount(XfEmpty, { props: { title: '暂无数据', description: '请重试' } })
    expect(wrapper.get('.xf-empty__title').text()).toBe('暂无数据')
    expect(wrapper.get('.xf-empty__description').text()).toBe('请重试')
  })

  it('renders the action slot', () => {
    const wrapper = mount(XfEmpty, { slots: { action: '<button>刷新</button>' } })
    expect(wrapper.get('.xf-empty__action').text()).toBe('刷新')
  })

  it('renders actionText as a button and emits action', async () => {
    const wrapper = mount(XfEmpty, { props: { actionText: '重试' } })
    const action = wrapper.get('.xf-empty__action button')
    expect(action.text()).toBe('重试')
    await action.trigger('click')
    expect(wrapper.emitted('action')).toHaveLength(1)
  })
})
