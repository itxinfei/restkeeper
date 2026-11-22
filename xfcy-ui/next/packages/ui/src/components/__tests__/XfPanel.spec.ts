import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import XfPanel from '../XfPanel.vue'

describe('XfPanel', () => {
  it('renders body slot without header when no title or actions', () => {
    const wrapper = mount(XfPanel, { slots: { default: '内容' } })
    expect(wrapper.find('.xf-panel__header').exists()).toBe(false)
    expect(wrapper.get('.xf-panel__body').text()).toBe('内容')
  })

  it('renders title and actions slot', () => {
    const wrapper = mount(XfPanel, {
      props: { title: '标题' },
      slots: { actions: '<button>更多</button>' },
    })
    expect(wrapper.get('.xf-panel__title').text()).toBe('标题')
    expect(wrapper.get('.xf-panel__actions').text()).toBe('更多')
  })

  it('renders header for actions even without title', () => {
    const wrapper = mount(XfPanel, { slots: { actions: '<button>更多</button>' } })
    expect(wrapper.find('.xf-panel__header').exists()).toBe(true)
    expect(wrapper.get('.xf-panel__actions').text()).toBe('更多')
  })
})
