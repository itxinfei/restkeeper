import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfSelect from '../XfSelect.vue'

describe('XfSelect', () => {
  const options = [
    { label: '堂食', value: 1 },
    { label: '外卖', value: 2 },
  ]

  it('renders placeholder and options', () => {
    const wrapper = mount(XfSelect, { props: { options, placeholder: '请选择' } })
    const select = wrapper.get('select')
    expect(select.attributes('disabled')).toBeUndefined()
    const opts = wrapper.findAll('option')
    expect(opts).toHaveLength(3)
    expect(opts[0].attributes('disabled')).toBeDefined()
    expect(opts[0].text()).toBe('请选择')
    expect(opts[1].text()).toBe('堂食')
    expect(wrapper.classes()).toContain('is-placeholder')
  })

  it('normalizes primitive options', () => {
    const wrapper = mount(XfSelect, { props: { options: ['甲', '乙'] } })
    const opts = wrapper.findAll('option')
    expect(opts[1].text()).toBe('甲')
    expect(opts[1].attributes('value')).toBe('甲')
  })

  it('emits the option value on change', async () => {
    const wrapper = mount(XfSelect, { props: { options } })
    const select = wrapper.get('select')
    ;(select.element as HTMLSelectElement).value = '2'
    await select.trigger('change')
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([2])
  })

  it('reflects the selected modelValue', () => {
    const wrapper = mount(XfSelect, { props: { options, modelValue: 1 } })
    expect((wrapper.get('select').element as HTMLSelectElement).value).toBe('1')
    expect(wrapper.classes()).not.toContain('is-placeholder')
  })

  it('disables the control', () => {
    const wrapper = mount(XfSelect, { props: { disabled: true } })
    expect(wrapper.get('select').attributes('disabled')).toBeDefined()
    expect(wrapper.classes()).toContain('is-disabled')
  })
})
