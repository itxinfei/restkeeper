<script setup lang="ts">
/**
 * B 端后台应用框架（运营中心 / 管家端共用）。
 * 结构依据 DESIGN.md §5.1：侧栏 220px + 顶栏 56px + 内容区 padding 24px。
 * 断点依据 DESIGN.md §7：≥1280 全量；1024–1280 侧栏可收起；<1024 侧栏转顶部。
 *
 * 导航不依赖 vue-router：由使用方监听 `navigate` 事件自行跳转。
 */
import { computed } from 'vue'
import XfUserMenu from './XfUserMenu.vue'

type NavItem = { key: string; label: string; path: string; icon: string }
type NavGroup = { label: string; items: NavItem[] }

const props = withDefaults(
  defineProps<{
    brandTitle: string
    brandSub?: string
    navGroups: NavGroup[]
    activePath?: string
    title?: string
    breadcrumb?: string
    userName: string
    userRole?: string
    userMeta?: string
    avatarText?: string
    collapsed?: boolean
  }>(),
  {
    brandSub: '',
    activePath: '',
    title: '',
    breadcrumb: '',
    userRole: '',
    userMeta: '',
    avatarText: '',
    collapsed: false,
  },
)

const emit = defineEmits<{
  (e: 'update:collapsed', value: boolean): void
  (e: 'navigate', path: string): void
  (e: 'logout'): void
}>()

const collapsed = computed({
  get: () => props.collapsed,
  set: (value: boolean) => emit('update:collapsed', value),
})

function toggleCollapsed() {
  collapsed.value = !collapsed.value
}
</script>

<template>
  <div class="xf-shell" :class="{ 'is-collapsed': collapsed }">
    <aside class="xf-rail">
      <div class="xf-brand">
        <span class="xf-brand__logo" aria-hidden="true">心</span>
        <span class="xf-brand__text">
          <b>{{ brandTitle }}</b>
          <span v-if="brandSub" class="xf-brand__sub">{{ brandSub }}</span>
        </span>
      </div>

      <nav class="xf-nav" aria-label="主导航">
        <template v-for="group in navGroups" :key="group.label">
          <div class="xf-nav__grp">{{ group.label }}</div>
          <button
            v-for="item in group.items"
            :key="item.key"
            type="button"
            class="xf-nav__item"
            :class="{ 'is-active': item.path === activePath }"
            :aria-current="item.path === activePath ? 'page' : undefined"
            @click="emit('navigate', item.path)"
          >
            <svg
              class="xf-nav__icon"
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
            <span class="xf-nav__label">{{ item.label }}</span>
          </button>
        </template>
      </nav>

      <div class="xf-railfoot">
        <b>{{ userName }}</b>
        <span v-if="userRole || userMeta">
          {{ userRole }}{{ userMeta ? ` · ${userMeta}` : '' }}
        </span>
      </div>
    </aside>

    <div class="xf-main">
      <header class="xf-topbar">
        <button
          type="button"
          class="xf-topbar__collapse"
          :aria-label="collapsed ? '展开侧栏' : '收起侧栏'"
          :aria-expanded="!collapsed"
          @click="toggleCollapsed"
        >
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <path d="M4 5h16v14H4zM9 5v14" />
          </svg>
        </button>

        <h1 class="xf-topbar__title">{{ title }}</h1>
        <span v-if="breadcrumb" class="xf-topbar__crumb">{{ breadcrumb }}</span>

        <div class="xf-topbar__right">
          <slot name="topbar" />
          <XfUserMenu
            :user-name="userName"
            :role="userRole"
            :avatar-text="avatarText"
            @logout="emit('logout')"
          />
        </div>
      </header>

      <main class="xf-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.xf-shell {
  display: flex;
  min-height: 100vh;
  background: var(--c-bg);
}

.xf-rail {
  flex: none;
  width: 220px;
  height: 100vh;
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  padding: var(--space-3) 0;
  background: var(--c-surface);
  border-right: 1px solid var(--c-border);
}

.xf-main {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.xf-content {
  flex: 1 1 auto;
  padding: var(--space-5) var(--space-6);
  overflow-y: auto;
}

/* ---------- 侧栏 ---------- */
.xf-brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 6px var(--space-5) var(--space-3);
  font-size: 15px;
  font-weight: var(--weight-strong);
  color: var(--c-text);
}

.xf-brand__logo {
  flex: none;
  width: 26px;
  height: 26px;
  border-radius: 7px;
  background: var(--c-primary);
  color: var(--c-surface);
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: var(--weight-title);
}

.xf-brand__text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.xf-brand__sub {
  margin-top: 2px;
  font-size: 12px;
  font-weight: var(--weight-body);
  color: var(--c-text-3);
}

.xf-nav {
  display: flex;
  flex-direction: column;
}

.xf-nav__grp {
  padding: var(--space-2) var(--space-5) 4px;
  font-size: 11px;
  letter-spacing: 1px;
  color: var(--c-text-3);
}

.xf-nav__item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 9px;
  height: 36px;
  padding: 0 var(--space-5);
  font-family: var(--font-sans);
  font-size: 13.5px;
  color: var(--c-text-2);
  background: transparent;
  border: none;
  border-left: 2px solid transparent;
  cursor: pointer;
  text-align: left;
}

.xf-nav__item:hover {
  color: var(--c-text);
  background: var(--c-surface-2);
}

.xf-nav__item.is-active {
  background: var(--c-primary-soft);
  color: var(--c-primary);
  font-weight: var(--weight-emphasis);
  border-left-color: var(--c-primary);
}

.xf-nav__icon {
  flex: none;
  width: 16px;
  height: 16px;
  opacity: 0.9;
}

.xf-railfoot {
  margin-top: auto;
  padding: var(--space-3) var(--space-5);
  border-top: 1px solid var(--c-border);
  font-size: 12px;
  color: var(--c-text-2);
}

.xf-railfoot b {
  display: block;
  margin-bottom: 2px;
  font-size: 13px;
  color: var(--c-text);
}

/* ---------- 顶栏 ---------- */
.xf-topbar {
  flex: none;
  min-height: 56px;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-6);
  background: var(--c-surface);
  border-bottom: 1px solid var(--c-border);
}

.xf-topbar__collapse {
  display: none;
  flex: none;
  width: var(--tap-target);
  height: var(--tap-target);
  padding: 0;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--c-text-2);
  cursor: pointer;
}

.xf-topbar__collapse:hover {
  background: var(--c-surface-2);
  color: var(--c-text);
}

.xf-topbar__collapse:focus-visible {
  outline: none;
  box-shadow: var(--focus-ring);
}

.xf-topbar__collapse svg {
  width: 18px;
  height: 18px;
}

.xf-topbar__title {
  font-size: 19px;
  font-weight: var(--weight-title);
  line-height: var(--leading-title);
  color: var(--c-text);
}

.xf-topbar__crumb {
  font-size: 12px;
  color: var(--c-text-3);
}

.xf-topbar__right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-body);
  color: var(--c-text-2);
}

/* ---------- 断点（DESIGN.md §7） ----------
 * ≥1280 全量；1024–1280 侧栏可收起（图标态）；<1024 侧栏转顶部横向。
 * 注意：CSS 自定义属性不能用于 @media，断点值需与各端样式保持一致。
 */
@media (max-width: 1279px) {
  .xf-topbar__collapse {
    display: grid;
    place-items: center;
  }

  .xf-shell.is-collapsed .xf-rail {
    width: 68px;
  }

  .xf-shell.is-collapsed .xf-brand {
    justify-content: center;
    padding-left: 0;
    padding-right: 0;
  }

  .xf-shell.is-collapsed .xf-brand__text,
  .xf-shell.is-collapsed .xf-nav__grp,
  .xf-shell.is-collapsed .xf-nav__label,
  .xf-shell.is-collapsed .xf-railfoot {
    display: none;
  }

  .xf-shell.is-collapsed .xf-nav__item {
    justify-content: center;
    padding: 0;
  }
}

@media (max-width: 1023px) {
  .xf-shell {
    flex-direction: column;
  }

  .xf-rail {
    width: 100%;
    height: auto;
    position: static;
    border-right: none;
    border-bottom: 1px solid var(--c-border);
  }

  .xf-nav {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .xf-nav__grp {
    width: 100%;
  }

  /* 顶部态不适用收起 */
  .xf-shell.is-collapsed .xf-rail {
    width: 100%;
  }

  .xf-shell.is-collapsed .xf-brand {
    justify-content: flex-start;
    padding-left: var(--space-5);
    padding-right: var(--space-5);
  }

  .xf-shell.is-collapsed .xf-brand__text,
  .xf-shell.is-collapsed .xf-railfoot {
    display: block;
  }

  .xf-shell.is-collapsed .xf-nav__grp {
    display: block;
  }

  .xf-shell.is-collapsed .xf-nav__label {
    display: inline;
  }

  .xf-shell.is-collapsed .xf-nav__item {
    justify-content: flex-start;
    padding: 0 var(--space-5);
  }
}
</style>
