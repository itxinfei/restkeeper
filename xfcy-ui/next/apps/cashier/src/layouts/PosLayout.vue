<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { XfDemoBadge, XfTag, XfUserMenu } from '@xfcy/ui'
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

function logout() {
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
        <XfUserMenu
          :user-name="user.profile.name"
          :role="user.profile.role"
          :avatar-text="user.avatarText"
          :avatar-size="40"
          touch
          @logout="logout"
        />
      </div>
    </header>

    <main class="xfc-body">
      <router-view />
    </main>
  </div>
</template>
