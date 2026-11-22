<script setup lang="ts">
import { computed, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    total: number
    page: number
    pageSize?: number
    /** 最多展示的页码按钮数（含省略号占位） */
    pager?: number
  }>(),
  { pageSize: 10, pager: 7 },
)

const emit = defineEmits<{ (e: 'update:page', value: number): void }>()

const pageCount = computed(() =>
  Math.max(1, Math.ceil((props.total > 0 ? props.total : 0) / Math.max(1, props.pageSize))),
)

watch(pageCount, (count) => {
  if (props.page > count) emit('update:page', count)
})

type PageItem = number | '...'

const pages = computed<PageItem[]>(() => {
  const count = pageCount.value
  const current = props.page
  const limit = Math.max(1, props.pager)

  if (count <= limit) {
    return Array.from({ length: count }, (_, index) => index + 1)
  }

  const half = Math.floor(limit / 2)
  let start = Math.max(1, current - half)
  const end = Math.min(count, start + limit - 1)
  start = Math.max(1, end - limit + 1)

  const result: PageItem[] = []
  if (start > 1) {
    result.push(1)
    if (start > 2) result.push('...')
  }
  for (let page = start; page <= end; page += 1) result.push(page)
  if (end < count) {
    if (end < count - 1) result.push('...')
    result.push(count)
  }
  return result
})

function go(page: number) {
  if (page < 1 || page > pageCount.value || page === props.page) return
  emit('update:page', page)
}
</script>

<template>
  <nav class="xf-pagination" role="navigation" aria-label="分页">
    <button
      class="xf-pagination__btn"
      type="button"
      :disabled="page <= 1"
      aria-label="上一页"
      @click="go(page - 1)"
    >
      ‹
    </button>

    <template v-for="(item, index) in pages" :key="`${item}-${index}`">
      <span v-if="item === '...'" class="xf-pagination__ellipsis" aria-hidden="true">…</span>
      <button
        v-else
        class="xf-pagination__btn"
        :class="{ 'is-active': item === page }"
        type="button"
        :aria-current="item === page ? 'page' : undefined"
        :aria-label="`第 ${item} 页`"
        @click="go(item)"
      >
        {{ item }}
      </button>
    </template>

    <button
      class="xf-pagination__btn"
      type="button"
      :disabled="page >= pageCount"
      aria-label="下一页"
      @click="go(page + 1)"
    >
      ›
    </button>

    <span class="xf-pagination__total xf-num">共 {{ total }} 条</span>
  </nav>
</template>

<style scoped>
.xf-pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-1);
  font-size: var(--text-caption);
  color: var(--c-text-2);
}

.xf-pagination__btn {
  min-width: 32px;
  height: 32px;
  padding: 0 var(--space-2);
  border: 1px solid var(--c-border-strong);
  border-radius: var(--radius-xs);
  background: var(--c-surface);
  color: var(--c-text-2);
  cursor: pointer;
  transition: border-color 0.15s ease, color 0.15s ease, background-color 0.15s ease;
}

.xf-pagination__btn:hover:not(:disabled):not(.is-active) {
  border-color: var(--c-primary);
  color: var(--c-primary);
}

.xf-pagination__btn:focus-visible {
  outline: none;
  box-shadow: var(--focus-ring);
}

.xf-pagination__btn.is-active {
  border-color: var(--c-primary);
  background: var(--c-primary);
  color: #ffffff;
  font-weight: var(--weight-emphasis);
}

.xf-pagination__btn:disabled {
  background: var(--c-bg);
  color: var(--c-text-3);
  cursor: not-allowed;
}

.xf-pagination__ellipsis {
  padding: 0 var(--space-1);
  color: var(--c-text-3);
}

.xf-pagination__total {
  margin-left: var(--space-2);
  color: var(--c-text-3);
}
</style>
