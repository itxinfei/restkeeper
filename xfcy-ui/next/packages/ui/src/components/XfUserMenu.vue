<script setup lang="ts">
/**
 * 顶栏用户菜单（头像 + 下拉：账户信息 + 退出登录）。
 * B 端后台与收银端共用；touch 态放大命中区（≥48px）与字号，供触屏 POS 使用。
 */
import { onBeforeUnmount, onMounted, ref } from 'vue'

withDefaults(
  defineProps<{
    userName: string
    role?: string
    avatarText?: string
    /** 头像边长，默认 30（后台），收银端传 40 */
    avatarSize?: number
    /** 触屏态：菜单项命中区 ≥48px */
    touch?: boolean
  }>(),
  {
    role: '',
    avatarText: '',
    avatarSize: 30,
    touch: false,
  },
)

const emit = defineEmits<{ (e: 'logout'): void }>()

const open = ref(false)
const root = ref<HTMLElement | null>(null)

function toggle() {
  open.value = !open.value
}

function onDocClick(event: MouseEvent) {
  if (root.value && !root.value.contains(event.target as Node)) open.value = false
}

onMounted(() => document.addEventListener('click', onDocClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocClick))

function logout() {
  open.value = false
  emit('logout')
}
</script>

<template>
  <div ref="root" class="xf-usermenu" :class="{ 'is-touch': touch }">
    <button
      type="button"
      class="xf-usermenu__avatar"
      :style="{ width: `${avatarSize}px`, height: `${avatarSize}px` }"
      :aria-label="`用户菜单：${userName}`"
      :aria-expanded="open"
      @click="toggle"
    >
      {{ avatarText }}
    </button>
    <div v-if="open" class="xf-usermenu__menu" role="menu">
      <div class="xf-usermenu__head">{{ userName }}<template v-if="role"> · {{ role }}</template></div>
      <slot />
      <button type="button" class="xf-usermenu__item is-danger" role="menuitem" @click="logout">
        退出登录
      </button>
    </div>
  </div>
</template>

<style scoped>
.xf-usermenu {
  position: relative;
}

.xf-usermenu__avatar {
  flex: none;
  border-radius: 50%;
  background: var(--c-primary);
  color: var(--c-surface);
  display: grid;
  place-items: center;
  font-size: 12px;
  border: none;
  cursor: pointer;
  padding: 0;
}

.xf-usermenu__avatar:focus-visible {
  outline: none;
  box-shadow: var(--focus-ring);
}

.xf-usermenu__menu {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  z-index: 50;
  min-width: 168px;
  padding: 4px;
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
}

.xf-usermenu__head {
  padding: 8px 12px;
  border-bottom: 1px solid var(--c-border);
  margin-bottom: 4px;
  font-size: 12px;
  color: var(--c-text-3);
}

.xf-usermenu__item {
  display: block;
  width: 100%;
  padding: 8px 12px;
  border: none;
  background: transparent;
  border-radius: var(--radius-xs);
  text-align: left;
  font-family: var(--font-sans);
  font-size: 13px;
  color: var(--c-text);
  cursor: pointer;
}

.xf-usermenu__item:hover {
  background: var(--c-surface-2);
}

.xf-usermenu__item.is-danger {
  color: var(--c-danger);
}

/* 触屏：菜单项命中区 ≥48px（DESIGN.md §5.2） */
.xf-usermenu.is-touch .xf-usermenu__avatar {
  font-size: 14px;
}

.xf-usermenu.is-touch .xf-usermenu__item {
  min-height: var(--tap-target);
  padding: 10px 12px;
  font-size: 14px;
}
</style>
