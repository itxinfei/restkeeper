import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import XfLoginPage from '../XfLoginPage.vue'

function mountPage(props: Record<string, unknown> = {}, slots: Record<string, string> = {}) {
  return mount(XfLoginPage, {
    props: {
      brandTitle: '心飞管家端',
      brandSubtitle: '心飞餐饮 · 旗舰店',
      login: vi.fn().mockResolvedValue(undefined),
      ...props,
    },
    slots,
  })
}

async function fillAndSubmit(wrapper: ReturnType<typeof mountPage>, username = '13800000000', password = '123456') {
  const inputs = wrapper.findAll('input')
  await inputs[0].setValue(username)
  await inputs[1].setValue(password)
  await wrapper.get('form').trigger('submit')
}

describe('XfLoginPage', () => {
  it('渲染品牌标题与副标题', () => {
    const wrapper = mountPage()
    expect(wrapper.get('.xf-login__title').text()).toBe('心飞管家端')
    expect(wrapper.get('.xf-login__sub').text()).toBe('心飞餐饮 · 旗舰店')
  })

  it('默认不展示商户号，showShopId 时展示', async () => {
    expect(mountPage().findAll('input')).toHaveLength(2)
    const wrapper = mountPage({ showShopId: true, shopIdLabel: '门店号' })
    const labels = wrapper.findAll('.xf-field__label').map((node) => node.text())
    expect(labels[0]).toContain('门店号')
    expect(wrapper.findAll('input')).toHaveLength(3)
  })

  it('触屏端追加 is-touch 类', () => {
    expect(mountPage({ touch: true }).get('.xf-login').classes()).toContain('is-touch')
  })

  it('缺账号/密码时本地校验并提示', async () => {
    const wrapper = mountPage()
    await wrapper.get('form').trigger('submit')
    expect(wrapper.get('.xf-login__error').text()).toBe('请输入账号')
    await wrapper.findAll('input')[0].setValue('13800000000')
    await wrapper.get('form').trigger('submit')
    expect(wrapper.get('.xf-login__error').text()).toBe('请输入密码')
  })

  it('showShopId 且缺商户号时提示', async () => {
    const wrapper = mountPage({ showShopId: true })
    const inputs = wrapper.findAll('input')
    await inputs[1].setValue('13800000000')
    await inputs[2].setValue('123456')
    await wrapper.get('form').trigger('submit')
    expect(wrapper.get('.xf-login__error').text()).toBe('请输入商户号')
  })

  it('登录成功抛出 success 并回传三元参数', async () => {
    const login = vi.fn().mockResolvedValue(undefined)
    const wrapper = mountPage({ login, showShopId: true })
    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('12345678')
    await inputs[1].setValue('13800000000')
    await inputs[2].setValue('123456')
    await wrapper.get('form').trigger('submit')
    expect(login).toHaveBeenCalledWith('13800000000', '123456', '12345678')
    expect(wrapper.emitted('success')).toHaveLength(1)
  })

  it('登录失败展示错误信息', async () => {
    const login = vi.fn().mockRejectedValue(new Error('账号或密码错误'))
    const wrapper = mountPage({ login })
    await fillAndSubmit(wrapper)
    expect(wrapper.get('.xf-login__error').text()).toBe('账号或密码错误')
    expect(wrapper.emitted('success')).toBeUndefined()
  })

  it('渲染 extra 插槽', () => {
    const wrapper = mountPage({}, { extra: '<p class="slot-extra">初始密码说明</p>' })
    expect(wrapper.get('.slot-extra').text()).toBe('初始密码说明')
  })
})
