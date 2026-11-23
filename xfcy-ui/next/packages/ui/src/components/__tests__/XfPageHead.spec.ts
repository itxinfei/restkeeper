import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfPageHead from '../XfPageHead.vue'

describe('XfPageHead', () => {
  it('renders the page title', () => {
    const wrapper = mount(XfPageHead, { props: { title: '菜品管理' } })
    expect(wrapper.get('h1.xf-page__title').text()).toBe('菜品管理')
  })

  it('renders the description when provided', () => {
    const wrapper = mount(XfPageHead, {
      props: { title: '菜品管理', description: '维护菜品、套餐与分类' },
    })
    expect(wrapper.get('.xf-page__desc').text()).toBe('维护菜品、套餐与分类')
  })

  it('omits the description element when empty', () => {
    const wrapper = mount(XfPageHead, { props: { title: '菜品管理' } })
    expect(wrapper.find('.xf-page__desc').exists()).toBe(false)
  })

  it('renders the actions slot aligned to the right', () => {
    const wrapper = mount(XfPageHead, {
      props: { title: '菜品管理' },
      slots: { actions: '<button>新增菜品</button>' },
    })
    const actions = wrapper.get('.xf-page__actions')
    expect(actions.text()).toBe('新增菜品')
  })
})
