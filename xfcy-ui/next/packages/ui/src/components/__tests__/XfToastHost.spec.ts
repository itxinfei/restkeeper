import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import XfToastHost from '../XfToastHost.vue'
import { resetToastState, useToast } from '../../toast'

describe('XfToastHost', () => {
  beforeEach(() => {
    resetToastState()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('默认无提示', () => {
    const wrapper = mount(XfToastHost)
    expect(wrapper.findAll('.xf-toast')).toHaveLength(0)
  })

  it('success 提示渲染文案与色调', () => {
    const toast = useToast()
    toast.success('保存成功')
    const wrapper = mount(XfToastHost)
    const item = wrapper.get('.xf-toast')
    expect(item.classes()).toContain('xf-toast--success')
    expect(item.get('.xf-toast__text').text()).toBe('保存成功')
  })

  it('error 提示用 danger 色调', () => {
    useToast().error('操作失败')
    const wrapper = mount(XfToastHost)
    expect(wrapper.get('.xf-toast').classes()).toContain('xf-toast--danger')
  })

  it('自动消失', async () => {
    useToast().info('稍后消失')
    const wrapper = mount(XfToastHost)
    expect(wrapper.findAll('.xf-toast')).toHaveLength(1)
    vi.advanceTimersByTime(2600)
    await nextTick()
    expect(wrapper.findAll('.xf-toast')).toHaveLength(0)
  })

  it('最多堆叠 4 条', () => {
    const toast = useToast()
    for (let i = 0; i < 6; i += 1) toast.info(`提示 ${i}`)
    const wrapper = mount(XfToastHost)
    expect(wrapper.findAll('.xf-toast')).toHaveLength(4)
  })
})
