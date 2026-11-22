import { h } from 'vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfField from '../XfField.vue'

describe('XfField', () => {
  it('renders no label when label is empty', () => {
    const wrapper = mount(XfField)
    expect(wrapper.find('label').exists()).toBe(false)
  })

  it('links label to the control id and marks required', () => {
    const wrapper = mount(XfField, {
      props: { label: '金额', required: true },
      slots: { default: ({ id }: { id: string }) => h('input', { id }) },
    })
    const label = wrapper.get('label')
    const input = wrapper.get('input')
    expect(label.attributes('for')).toBe(input.attributes('id'))
    expect(label.attributes('for')).toMatch(/^xf-field-/)
    expect(wrapper.get('.xf-field__required').attributes('aria-hidden')).toBe('true')
  })

  it('renders error message with alert role instead of hint', () => {
    const wrapper = mount(XfField, { props: { label: 'x', error: '必填', hint: '提示' } })
    const error = wrapper.get('.xf-field__error')
    expect(error.attributes('role')).toBe('alert')
    expect(error.text()).toBe('必填')
    expect(wrapper.find('.xf-field__hint').exists()).toBe(false)
    expect(wrapper.get('.xf-field').classes()).toContain('has-error')
  })

  it('renders hint when there is no error', () => {
    const wrapper = mount(XfField, { props: { hint: '提示' } })
    expect(wrapper.get('.xf-field__hint').text()).toBe('提示')
    expect(wrapper.find('.xf-field__error').exists()).toBe(false)
  })
})
