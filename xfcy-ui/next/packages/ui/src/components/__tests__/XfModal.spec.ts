import { nextTick } from 'vue'
import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it } from 'vitest'
import XfModal from '../XfModal.vue'

function query<T extends Element = HTMLElement>(selector: string): T {
  const el = document.querySelector<T>(selector)
  if (!el) throw new Error(`未找到元素 ${selector}`)
  return el
}

describe('XfModal', () => {
  afterEach(() => {
    document.body.innerHTML = ''
    document.body.style.overflow = ''
  })

  it('renders nothing when closed', () => {
    mount(XfModal, { props: { open: false } })
    expect(document.querySelector('.xf-modal')).toBeNull()
  })

  it('renders dialog with accessibility attributes when open', async () => {
    mount(XfModal, { props: { open: true, title: '提示' } })
    await nextTick()
    const dialog = query('.xf-modal')
    expect(dialog.getAttribute('role')).toBe('dialog')
    expect(dialog.getAttribute('aria-modal')).toBe('true')
    expect(dialog.getAttribute('aria-labelledby')).toBe(query('.xf-modal__title').id)
    const close = query<HTMLButtonElement>('.xf-modal__close')
    expect(close.getAttribute('type')).toBe('button')
    expect(close.getAttribute('aria-label')).toBe('关闭')
  })

  it('locks body scroll while open and restores it on close', async () => {
    const wrapper = mount(XfModal, { props: { open: false } })
    await wrapper.setProps({ open: true })
    expect(document.body.style.overflow).toBe('hidden')
    await wrapper.setProps({ open: false })
    expect(document.body.style.overflow).toBe('')
  })

  it('emits update:open and close from the close button', async () => {
    const wrapper = mount(XfModal, { props: { open: true, title: 'x' } })
    await nextTick()
    query<HTMLButtonElement>('.xf-modal__close').click()
    await nextTick()
    expect(wrapper.emitted('update:open')![0]).toEqual([false])
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('closes when clicking the mask', async () => {
    const wrapper = mount(XfModal, { props: { open: true } })
    await nextTick()
    query('.xf-modal__overlay').dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
    await nextTick()
    expect(wrapper.emitted('update:open')![0]).toEqual([false])
  })

  it('closes on Escape', async () => {
    const wrapper = mount(XfModal, { props: { open: true } })
    await nextTick()
    query('.xf-modal__overlay').dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }),
    )
    await nextTick()
    expect(wrapper.emitted('update:open')![0]).toEqual([false])
  })

  it('does not close when closable is false', async () => {
    const wrapper = mount(XfModal, { props: { open: true, closable: false } })
    await nextTick()
    expect(document.querySelector('.xf-modal__close')).toBeNull()
    query('.xf-modal__overlay').dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }),
    )
    await nextTick()
    expect(wrapper.emitted('update:open')).toBeUndefined()
  })
})
