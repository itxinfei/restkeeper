import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfTable from '../XfTable.vue'

describe('XfTable', () => {
  const columns = [
    { key: 'name', label: '名称' },
    { key: 'amount', label: '金额', sortable: true, align: 'right' as const },
  ]
  const data = [
    { id: 1, name: '乙', amount: 20 },
    { id: 2, name: '甲', amount: 10 },
  ]

  it('renders headers and rows', () => {
    const wrapper = mount(XfTable, { props: { columns, data } })
    const headers = wrapper.findAll('th')
    expect(headers[0].text()).toBe('名称')
    expect(headers[1].text()).toContain('金额')
    const rows = wrapper.findAll('tbody tr')
    expect(rows).toHaveLength(2)
    expect(rows[0].text()).toContain('乙')
  })

  it('renders empty state', () => {
    const wrapper = mount(XfTable, { props: { columns, data: [], emptyText: '无数据' } })
    expect(wrapper.text()).toContain('无数据')
  })

  it('renders skeleton rows while loading', () => {
    const wrapper = mount(XfTable, { props: { columns, data, loading: true } })
    expect(wrapper.findAll('tbody tr[aria-hidden="true"]')).toHaveLength(5)
  })

  it('sorts ascending, descending then clears', async () => {
    const wrapper = mount(XfTable, { props: { columns, data } })
    const sortBtn = wrapper.get('.xf-table__sort')
    expect(sortBtn.attributes('aria-label')).toBe('按金额排序')

    await sortBtn.trigger('click')
    expect(wrapper.emitted('sort-change')![0]).toEqual([{ key: 'amount', order: 'asc' }])
    expect(wrapper.findAll('tbody tr')[0].text()).toContain('10')

    await sortBtn.trigger('click')
    expect(wrapper.emitted('sort-change')![1]).toEqual([{ key: 'amount', order: 'desc' }])
    expect(wrapper.findAll('tbody tr')[0].text()).toContain('20')

    await sortBtn.trigger('click')
    expect(wrapper.emitted('sort-change')![2]).toEqual([{ key: null, order: null }])
  })

  it('sets aria-sort on the sortable header', async () => {
    const wrapper = mount(XfTable, { props: { columns, data } })
    expect(wrapper.findAll('th')[1].attributes('aria-sort')).toBe('none')
    await wrapper.get('.xf-table__sort').trigger('click')
    expect(wrapper.findAll('th')[1].attributes('aria-sort')).toBe('ascending')
  })
})
