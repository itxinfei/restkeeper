<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'

type TabKey = string | number
type Tab = TabKey | { key: TabKey; label: string }

const props = withDefaults(
  defineProps<{
    modelValue: TabKey
    tabs?: Tab[]
  }>(),
  { tabs: () => [] },
)

const emit = defineEmits<{ (e: 'update:modelValue', value: TabKey): void }>()

interface NormalizedTab {
  key: TabKey
  label: string
}

const items = computed<NormalizedTab[]>(() =>
  props.tabs.map((tab) => {
    if (tab !== null && typeof tab === 'object') {
      return { key: tab.key, label: tab.label }
    }
    return { key: tab as TabKey, label: String(tab) }
  }),
)

const tabRefs = ref<Array<HTMLElement | null>>([])

function setTabRef(el: unknown, index: number) {
  tabRefs.value[index] = (el as HTMLElement | null) ?? null
}

function select(key: TabKey) {
  if (key !== props.modelValue) emit('update:modelValue', key)
}

async function onKeydown(event: KeyboardEvent, index: number) {
  let next = -1
  if (event.key === 'ArrowRight') next = (index + 1) % items.value.length
  else if (event.key === 'ArrowLeft') next = (index - 1 + items.value.length) % items.value.length
  else if (event.key === 'Home') next = 0
  else if (event.key === 'End') next = items.value.length - 1

  if (next >= 0 && items.value[next]) {
    event.preventDefault()
    select(items.value[next].key)
    await nextTick()
    tabRefs.value[next]?.focus()
  }
}
</script>

<template>
  <div class="xf-tabs" role="tablist" aria-orientation="horizontal">
    <button
      v-for="(item, index) in items"
      :key="String(item.key)"
      :ref="(el) => setTabRef(el, index)"
      class="xf-tabs__item"
      :class="{ 'is-active': item.key === modelValue }"
      type="button"
      role="tab"
      :aria-selected="item.key === modelValue"
      :aria-controls="`xf-tabpanel-${String(item.key)}`"
      :id="`xf-tab-${String(item.key)}`"
      :tabindex="item.key === modelValue ? 0 : -1"
      @click="select(item.key)"
      @keydown="onKeydown($event, index)"
    >
      {{ item.label }}
    </button>
  </div>
</template>

<style scoped>
.xf-tabs {
  display: flex;
  align-items: center;
  gap: var(--space-6);
  border-bottom: 1px solid var(--c-border);
}

.xf-tabs__item {
  position: relative;
  padding: var(--space-3) 0;
  border: none;
  background: transparent;
  color: var(--c-text-2);
  font-size: var(--text-body);
  cursor: pointer;
  transition: color 0.15s ease;
}

.xf-tabs__item:hover {
  color: var(--c-text);
}

.xf-tabs__item:focus-visible {
  outline: none;
  box-shadow: var(--focus-ring);
  border-radius: var(--radius-xs);
}

.xf-tabs__item.is-active {
  color: var(--c-primary);
  font-weight: var(--weight-emphasis);
}

/* 下划线式：选中 2px --c-primary（DESIGN.md §6 导航） */
.xf-tabs__item.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 2px;
  background: var(--c-primary);
  border-radius: 2px;
}
</style>
