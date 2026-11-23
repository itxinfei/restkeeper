<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { XfAppShell, XfDemoBadge } from '@xfcy/ui'
import { isMockMode } from '@xfcy/api'
import { useUserStore } from '../stores/user'

type NavItem = {
  key: string
  label: string
  path: string
  icon: string
}

type NavGroup = {
  label: string
  items: NavItem[]
}

/** 侧栏菜单：分组与顺序依据 docs/ui/operator.html 原型 */
const navGroups: NavGroup[] = [
  {
    label: '账号',
    items: [
      { key: 'enterprises', label: '企业账号', path: '/enterprises', icon: 'M3 3h7v7H3zM14 3h7v7h-7zM14 14h7v7h-7zM3 14h7v7H3z' },
      { key: 'recycle', label: '回收站', path: '/recycle', icon: 'M4 7h16M9 7V5h6v2M6 7l1 13h10l1-13M10 11v6M14 11v6' },
    ],
  },
  {
    label: '系统',
    items: [
      { key: 'dict', label: '字典管理', path: '/dict', icon: 'M4 6h16M4 10h16M4 14h10M4 18h10' },
      { key: 'operators', label: '运营人员', path: '/operators', icon: 'M16 19v-1a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v1M9.5 10a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zM21 19v-1a4 4 0 0 0-3-3.87M15 3.13a4 4 0 0 1 0 7.75' },
    ],
  },
  {
    label: '监控',
    items: [
      { key: 'monitor', label: '服务监控', path: '/monitor', icon: 'M3 12h4l2-6 4 12 2-6h6' },
    ],
  },
]

const route = useRoute()
const router = useRouter()
const user = useUserStore()

/* mock 模式数据均为演示，全局显式标识（DESIGN.md §2 数据可信） */
const demoMode = isMockMode()

/* 1024–1280 侧栏收起（XfAppShell 内部断点，见 DESIGN.md §7） */
const collapsed = ref(false)

const pageTitle = computed(() => String(route.meta.title ?? ''))
const pageGroup = computed(() => String(route.meta.group ?? ''))
const breadcrumb = computed(() =>
  pageGroup.value ? `${pageGroup.value} / ${pageTitle.value}` : pageTitle.value,
)

/* 顶栏时间：每 30s 刷新一次 */
const now = ref(new Date())
let timer: number | undefined
onMounted(() => {
  timer = window.setInterval(() => {
    now.value = new Date()
  }, 30_000)
})
onBeforeUnmount(() => {
  if (timer !== undefined) window.clearInterval(timer)
})

const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const timeText = computed(() => {
  const d = now.value
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} 星期${weekdays[d.getDay()]} ${pad(d.getHours())}:${pad(d.getMinutes())}`
})

function onNavigate(path: string) {
  router.push(path)
}

function logout() {
  user.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <XfAppShell
    brand-title="心飞运营中心"
    :brand-sub="`${user.profile.name} · ${user.profile.role}`"
    :nav-groups="navGroups"
    :active-path="route.path"
    :title="pageTitle"
    :breadcrumb="breadcrumb"
    :user-name="user.profile.name"
    :user-role="user.profile.role"
    :avatar-text="user.avatarText"
    v-model:collapsed="collapsed"
    @navigate="onNavigate"
    @logout="logout"
  >
    <template #topbar>
      <XfDemoBadge :show="demoMode" />
      <span class="xfo-topbar__time">{{ timeText }}</span>
    </template>
  </XfAppShell>
</template>

<style scoped>
.xfo-topbar__time {
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
</style>
