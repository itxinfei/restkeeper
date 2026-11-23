import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfAppShell from '../XfAppShell.vue'

const navGroups = [
  {
    label: '经营',
    items: [
      { key: 'dashboard', label: '工作台', path: '/dashboard', icon: 'M3 3h7v7H3z' },
      { key: 'staff', label: '员工管理', path: '/staff', icon: 'M4 6h16' },
    ],
  },
]

function mountShell(props: Record<string, unknown> = {}, slots: Record<string, string> = {}) {
  return mount(XfAppShell, {
    props: {
      brandTitle: '心飞管家端',
      userName: '李国栋',
      userRole: '门店经理',
      avatarText: '李',
      navGroups,
      title: '工作台',
      ...props,
    },
    slots,
  })
}

describe('XfAppShell', () => {
  it('渲染品牌、导航分组与导航项', () => {
    const wrapper = mountShell()
    expect(wrapper.get('.xf-brand__text b').text()).toBe('心飞管家端')
    expect(wrapper.findAll('.xf-nav__grp').map((node) => node.text())).toEqual(['经营'])
    expect(wrapper.findAll('.xf-nav__item').map((node) => node.text())).toEqual(['工作台', '员工管理'])
  })

  it('按 activePath 标记当前页', () => {
    const wrapper = mountShell({ activePath: '/staff' })
    const items = wrapper.findAll('.xf-nav__item')
    expect(items[0].classes()).not.toContain('is-active')
    expect(items[1].classes()).toContain('is-active')
    expect(items[1].attributes('aria-current')).toBe('page')
  })

  it('点击导航项抛出 navigate 事件', async () => {
    const wrapper = mountShell()
    await wrapper.findAll('.xf-nav__item')[1].trigger('click')
    expect(wrapper.emitted('navigate')?.[0]).toEqual(['/staff'])
  })

  it('收起按钮切换 update:collapsed', async () => {
    const wrapper = mountShell({ collapsed: false })
    await wrapper.get('.xf-topbar__collapse').trigger('click')
    expect(wrapper.emitted('update:collapsed')?.[0]).toEqual([true])
  })

  it('顶栏右侧插槽与用户菜单渲染', async () => {
    const wrapper = mountShell({}, { topbar: '<span class="slot-mark">门店切换</span>' })
    expect(wrapper.get('.slot-mark').text()).toBe('门店切换')
    await wrapper.get('.xf-usermenu__avatar').trigger('click')
    expect(wrapper.get('.xf-usermenu__menu').text()).toContain('退出登录')
  })

  it('退出登录抛出 logout', async () => {
    const wrapper = mountShell()
    await wrapper.get('.xf-usermenu__avatar').trigger('click')
    await wrapper.get('.xf-usermenu__item.is-danger').trigger('click')
    expect(wrapper.emitted('logout')).toHaveLength(1)
  })
})
