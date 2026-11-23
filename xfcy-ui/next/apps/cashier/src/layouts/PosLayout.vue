<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { XfDemoBadge, XfTag } from '@xfcy/ui'
import { isMockMode } from '@xfcy/api'
import { useUserStore } from '../stores/user'
import { useOrderStore } from '../stores/order'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const order = useOrderStore()

/* mock 模式数据均为演示，全局显式标识（DESIGN.md §2 数据可信） */
const demoMode = isMockMode()

/* 顶栏时钟：每秒刷新（触屏 POS 常驻时间，DESIGN.md §5.2） */
const now = ref(new Date())
let timer: number | undefined
onMounted(() => {
  timer = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
})
onBeforeUnmount(() => {
  if (timer !== undefined) window.clearInterval(timer)
})

const pad = (value: number) => String(value).padStart(2, '0')
const timeText = computed(() => {
  const d = now.value
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
})
const dateText = computed(() => {
  const d = now.value
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
})

const pageTitle = computed(() => String(route.meta.title ?? ''))
const currentTable = computed(() => order.currentTable)

/* 用户菜单：点击外部关闭 */
const menuOpen = ref(false)
const menuRef = ref<HTMLElement | null>(null)
function toggleMenu() {
  menuOpen.value = !menuOpen.value
}
function onDocClick(event: MouseEvent) {
  if (menuRef.value && !menuRef.value.contains(event.target as Node)) menuOpen.value = false
}
onMounted(() => document.addEventListener('click', onDocClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocClick))

function logout() {
  menuOpen.value = false
  user.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="xfc-shell">
    <header class="xfc-topbar">
      <div class="xfc-brand">
        <span class="xfc-brand__logo" aria-hidden="true">心</span>
        <span class="xfc-brand__text">
          <b>心飞收银端</b>
          <small>{{ user.profile.shop }}</small>
        </span>
      </div>

      <h1 class="xfc-topbar__title">{{ pageTitle }}</h1>

      <XfTag v-if="currentTable" tone="primary" class="xfc-topbar__table">
        {{ currentTable.name }} · {{ currentTable.area }} · {{ order.guests }} 人
      </XfTag>

      <div class="xfc-topbar__right">
        <XfDemoBadge :show="demoMode" />
        <span class="xfc-topbar__time">
          <b class="xfc-num">{{ timeText }}</b>
          <small class="xfc-num">{{ dateText }}</small>
        </span>
        <div ref="menuRef" class="xfc-user">
          <button
            type="button"
            class="xfc-avatar"
            :aria-label="`用户菜单：${user.profile.name}`"
            :aria-expanded="menuOpen"
            @click="toggleMenu"
          >
            {{ user.avatarText }}
          </button>
          <div v-if="menuOpen" class="xfc-menu" role="menu">
            <div class="xfc-menu__head">{{ user.profile.name }} · {{ user.profile.role }}</div>
            <button
              type="button"
              class="xfc-menu__item is-danger"
              role="menuitem"
              @click="logout"
            >
              退出登录
            </button>
          </div>
        </div>
      </div>
    </header>

    <main class="xfc-body">
      <router-view />
    </main>
  </div>
</template>
