<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { XfSelect } from '@xfcy/ui'
import { useUserStore } from '../stores/user'
import { useShopStore } from '../stores/shop'

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

/** 侧栏菜单：分组与顺序依据 docs/ui/manager.html 原型 */
const navGroups: NavGroup[] = [
  {
    label: '经营',
    items: [
      { key: 'dashboard', label: '工作台', path: '/dashboard', icon: 'M3 3h7v7H3zM14 3h7v7h-7zM14 14h7v7h-7zM3 14h7v7H3z' },
      { key: 'dishes', label: '菜品管理', path: '/dishes', icon: 'M4 6h16M4 10h10M4 14h10M4 18h6' },
      { key: 'combos', label: '套餐管理', path: '/combos', icon: 'M12 3 3 8l9 5 9-5-9-5zM3 13l9 5 9-5' },
      { key: 'tables', label: '桌台管理', path: '/tables', icon: 'M4 8h16M6 8v10M18 8v10M4 18h16' },
      { key: 'staff', label: '员工管理', path: '/staff', icon: 'M16 19v-1a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v1M9.5 10a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zM21 19v-1a4 4 0 0 0-3-3.87M15 3.13a4 4 0 0 1 0 7.75' },
    ],
  },
  {
    label: '账务',
    items: [
      { key: 'credit', label: '挂账管理', path: '/credit', icon: 'M3 7h18v10H3zM3 11h18' },
      { key: 'soldout', label: '沽清管理', path: '/soldout', icon: 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM5.6 5.6l12.8 12.8' },
    ],
  },
  {
    label: '数据',
    items: [
      { key: 'reports', label: '经营报表', path: '/reports', icon: 'M5 20v-6M11 20V6M17 20v-9' },
    ],
  },
]

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const shop = useShopStore()

const pageTitle = computed(() => String(route.meta.title ?? ''))
const pageGroup = computed(() => String(route.meta.group ?? ''))
const breadcrumb = computed(() =>
  pageGroup.value ? `${pageGroup.value} / ${pageTitle.value}` : pageTitle.value,
)

/* 顶栏时间：每 30s 刷新一次（示意） */
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

/* 用户菜单：点击外部关闭 */
const menuOpen = ref(false)
const menuRef = ref<HTMLElement | null>(null)

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function onDocClick(event: MouseEvent) {
  if (menuRef.value && !menuRef.value.contains(event.target as Node)) {
    menuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', onDocClick)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
})

function onShopChange(id: string | number | null) {
  if (id !== null) shop.switchShop(String(id))
}

function logout() {
  menuOpen.value = false
  user.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="xfm-shell">
    <aside class="xfm-rail">
      <div class="xfm-brand">
        <span class="xfm-brand__logo" aria-hidden="true">心</span>
        <span>
          心飞管家端
          <span class="xfm-brand__sub">{{ user.profile.name }} · {{ shop.currentShop.short }}</span>
        </span>
      </div>

      <nav class="xfm-nav" aria-label="主导航">
        <template v-for="group in navGroups" :key="group.label">
          <div class="xfm-nav__grp">{{ group.label }}</div>
          <router-link
            v-for="item in group.items"
            :key="item.key"
            class="xfm-nav__item"
            :class="{ 'is-active': route.path === item.path }"
            :to="item.path"
          >
            <svg
              class="xfm-nav__icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.8"
              stroke-linecap="round"
              stroke-linejoin="round"
              aria-hidden="true"
            >
              <path :d="item.icon" />
            </svg>
            {{ item.label }}
          </router-link>
        </template>
      </nav>

      <div class="xfm-railfoot">
        <b>{{ user.profile.name }}</b>
        {{ user.profile.role }} · {{ user.profile.shopCount }} 家门店
      </div>
    </aside>

    <div class="xfm-main">
      <header class="xfm-topbar">
        <h1 class="xfm-topbar__title">{{ pageTitle }}</h1>
        <span class="xfm-topbar__crumb">{{ breadcrumb }}</span>

        <div class="xfm-topbar__right">
          <XfSelect
            :model-value="shop.currentShopId"
            :options="shop.shopOptions"
            placeholder="切换门店"
            @update:model-value="onShopChange"
          />
          <span class="xfm-topbar__time">{{ timeText }}</span>
          <div ref="menuRef" class="xfm-user">
            <button
              type="button"
              class="xfm-avatar"
              :aria-label="`用户菜单：${user.profile.name}`"
              :aria-expanded="menuOpen"
              @click="toggleMenu"
            >
              {{ user.avatarText }}
            </button>
            <div v-if="menuOpen" class="xfm-menu" role="menu">
              <div class="xfm-menu__head">
                {{ user.profile.name }} · {{ user.profile.role }}
              </div>
              <button type="button" class="xfm-menu__item" role="menuitem" @click="menuOpen = false">
                账户信息（示意）
              </button>
              <button
                type="button"
                class="xfm-menu__item is-danger"
                role="menuitem"
                @click="logout"
              >
                退出登录
              </button>
            </div>
          </div>
        </div>
      </header>

      <main class="xfm-content">
        <div class="xf-page">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>
