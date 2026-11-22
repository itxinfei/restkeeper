import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfPagination from '../XfPagination.vue'

describe('XfPagination', () => {
  it('renders navigation semantics and total count', () => {
    const wrapper = mount(XfPagination, { props: { total: 95, page: 1 } })
    expect(wrapper.attributes('role')).toBe('navigation')
    expect(wrapper.attributes('aria-label')).toBe('分页')
    expect(wrapper.get('.xf-pagination__total').text()).toBe('共 95 条')
  })

  it('disables prev on the first page and next on the last page', async () => {
    const wrapper = mount(XfPagination, { props: { total: 95, page: 1 } })
    const prev = wrapper.findAll('.xf-pagination__btn')[0]
    expect(prev.attributes('aria-label')).toBe('上一页')
    expect(prev.attributes('disabled')).toBeDefined()

    await wrapper.setProps({ page: 10 })
    const buttons = wrapper.findAll('.xf-pagination__btn')
    const next = buttons[buttons.length - 1]
    expect(next.attributes('aria-label')).toBe('下一页')
    expect(next.attributes('disabled')).toBeDefined()
  })

  it('emits update:page when a page button is clicked', async () => {
    const wrapper = mount(XfPagination, { props: { total: 95, page: 1 } })
    const page2 = wrapper.findAll('.xf-pagination__btn').find((btn) => btn.text() === '2')!
    await page2.trigger('click')
    expect(wrapper.emitted('update:page')![0]).toEqual([2])
  })

  it('marks the active page', () => {
    const wrapper = mount(XfPagination, { props: { total: 95, page: 2 } })
    const active = wrapper.get('.xf-pagination__btn.is-active')
    expect(active.text()).toBe('2')
    expect(active.attributes('aria-current')).toBe('page')
    expect(active.attributes('aria-label')).toBe('第 2 页')
  })

  it('clamps the page when total shrinks below the current page', async () => {
    const wrapper = mount(XfPagination, { props: { total: 200, page: 20 } })
    await wrapper.setProps({ total: 10 })
    expect(wrapper.emitted('update:page')).toBeTruthy()
    expect(wrapper.emitted('update:page')![0]).toEqual([1])
  })
})
