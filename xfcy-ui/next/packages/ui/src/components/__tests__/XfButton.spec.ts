import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfButton from '../XfButton.vue'

describe('XfButton', () => {
  it('renders default variant, size, type and slot', () => {
    const wrapper = mount(XfButton, { slots: { default: '保存' } })
    const button = wrapper.get('button')
    expect(button.attributes('type')).toBe('button')
    expect(button.classes()).toContain('xf-btn--primary')
    expect(button.classes()).toContain('xf-btn--md')
    expect(button.text()).toBe('保存')
    expect(button.attributes('disabled')).toBeUndefined()
  })

  it('applies variant, size and block classes', () => {
    const wrapper = mount(XfButton, { props: { variant: 'danger', size: 'lg', block: true } })
    const classes = wrapper.get('button').classes()
    expect(classes).toContain('xf-btn--danger')
    expect(classes).toContain('xf-btn--lg')
    expect(classes).toContain('xf-btn--block')
  })

  it('emits click with the mouse event', async () => {
    const wrapper = mount(XfButton, { slots: { default: '按钮' } })
    await wrapper.get('button').trigger('click')
    expect(wrapper.emitted('click')).toHaveLength(1)
    expect(wrapper.emitted('click')![0][0]).toBeInstanceOf(MouseEvent)
  })

  it('blocks click and exposes disabled state', async () => {
    const wrapper = mount(XfButton, { props: { disabled: true } })
    const button = wrapper.get('button')
    expect(button.attributes('disabled')).toBeDefined()
    expect(button.attributes('aria-disabled')).toBe('true')
    await button.trigger('click')
    expect(wrapper.emitted('click')).toBeUndefined()
  })

  it('shows spinner, marks busy and blocks click while loading', async () => {
    const wrapper = mount(XfButton, { props: { loading: true } })
    const button = wrapper.get('button')
    expect(button.attributes('aria-busy')).toBe('true')
    expect(button.find('.xf-btn__spinner').exists()).toBe(true)
    await button.trigger('click')
    expect(wrapper.emitted('click')).toBeUndefined()
  })

  it('supports submit type', () => {
    const wrapper = mount(XfButton, { props: { type: 'submit' } })
    expect(wrapper.get('button').attributes('type')).toBe('submit')
  })
})
