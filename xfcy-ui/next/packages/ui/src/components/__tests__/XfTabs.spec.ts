import { nextTick } from 'vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfTabs from '../XfTabs.vue'

describe('XfTabs', () => {
  const tabs = [
    { key: 'a', label: '标签A' },
    { key: 'b', label: '标签B' },
  ]

  it('renders tabs with role and selection state', () => {
    const wrapper = mount(XfTabs, { props: { modelValue: 'a', tabs } })
    expect(wrapper.attributes('role')).toBe('tablist')
    const items = wrapper.findAll('[role="tab"]')
    expect(items).toHaveLength(2)
    expect(items[0].attributes('aria-selected')).toBe('true')
    expect(items[1].attributes('aria-selected')).toBe('false')
    expect(items[0].classes()).toContain('is-active')
    expect(items[0].attributes('tabindex')).toBe('0')
    expect(items[1].attributes('tabindex')).toBe('-1')
  })

  it('emits update:modelValue on click', async () => {
    const wrapper = mount(XfTabs, { props: { modelValue: 'a', tabs } })
    await wrapper.findAll('[role="tab"]')[1].trigger('click')
    expect(wrapper.emitted('update:modelValue')![0]).toEqual(['b'])
  })

  it('moves selection and focus with arrow keys', async () => {
    const wrapper = mount(XfTabs, { props: { modelValue: 'a', tabs }, attachTo: document.body })
    const items = wrapper.findAll('[role="tab"]')
    await items[0].trigger('keydown', { key: 'ArrowRight' })
    await nextTick()
    expect(wrapper.emitted('update:modelValue')![0]).toEqual(['b'])
    expect(document.activeElement).toBe(items[1].element)
    wrapper.unmount()
  })

  it('normalizes primitive tabs', () => {
    const wrapper = mount(XfTabs, { props: { modelValue: 'x', tabs: ['x', 'y'] } })
    expect(wrapper.findAll('[role="tab"]').map((tab) => tab.text())).toEqual(['x', 'y'])
  })
})
