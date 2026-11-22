import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfTag from '../XfTag.vue'

describe('XfTag', () => {
  it('defaults to neutral tone', () => {
    const wrapper = mount(XfTag, { slots: { default: '文本' } })
    expect(wrapper.classes()).toContain('xf-tag--neutral')
    expect(wrapper.text()).toBe('文本')
  })

  it.each(['success', 'warning', 'danger', 'info', 'primary'] as const)(
    'applies the %s tone class',
    (tone) => {
      const wrapper = mount(XfTag, { props: { tone }, slots: { default: 'x' } })
      expect(wrapper.classes()).toContain(`xf-tag--${tone}`)
    },
  )
})
