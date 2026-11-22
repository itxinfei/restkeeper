import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfInput from '../XfInput.vue'

describe('XfInput', () => {
  it('renders defaults', () => {
    const wrapper = mount(XfInput)
    const input = wrapper.get('input')
    expect(input.attributes('type')).toBe('text')
    expect(input.attributes('placeholder')).toBe('')
    expect(input.attributes('autocomplete')).toBe('off')
    expect(input.attributes('disabled')).toBeUndefined()
    expect(input.attributes('id')).toMatch(/^xf-input-/)
  })

  it('binds value and emits update:modelValue on input', async () => {
    const wrapper = mount(XfInput, { props: { modelValue: 'a' } })
    const input = wrapper.get('input')
    expect((input.element as HTMLInputElement).value).toBe('a')
    await input.setValue('abc')
    expect(wrapper.emitted('update:modelValue')![0]).toEqual(['abc'])
  })

  it('disables the control', () => {
    const wrapper = mount(XfInput, { props: { disabled: true } })
    expect(wrapper.get('input').attributes('disabled')).toBeDefined()
    expect(wrapper.get('.xf-input').classes()).toContain('is-disabled')
  })

  it('renders prefix', () => {
    const wrapper = mount(XfInput, { props: { prefix: '¥' } })
    expect(wrapper.get('.xf-input__prefix').text()).toBe('¥')
  })

  it('exposes error message with alert role and describedby', () => {
    const wrapper = mount(XfInput, { props: { error: '必填' } })
    const input = wrapper.get('input')
    expect(input.attributes('aria-invalid')).toBe('true')
    const error = wrapper.get('.xf-input__error')
    expect(error.attributes('role')).toBe('alert')
    expect(error.text()).toBe('必填')
    expect(input.attributes('aria-describedby')).toBe(error.attributes('id'))
  })

  it('applies error state without message for boolean error', () => {
    const wrapper = mount(XfInput, { props: { error: true } })
    expect(wrapper.get('.xf-input').classes()).toContain('has-error')
    expect(wrapper.find('.xf-input__error').exists()).toBe(false)
  })

  it('uses the provided id', () => {
    const wrapper = mount(XfInput, { props: { id: 'amount' } })
    expect(wrapper.get('input').attributes('id')).toBe('amount')
  })
})
