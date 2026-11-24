<script setup lang="ts">
import { computed, ref } from 'vue'
import XfEmpty from './XfEmpty.vue'

interface Column {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  width?: string
  /** 允许单元格内换行（长文本主列使用，默认不换行） */
  wrap?: boolean
}

const props = withDefaults(
  defineProps<{
    columns: Column[]
    data: Array<Record<string, unknown>>
    emptyText?: string
    emptyDescription?: string
    loading?: boolean
    rowKey?: string
  }>(),
  {
    emptyText: '暂无数据',
    emptyDescription: '请调整筛选条件后重试',
    loading: false,
    rowKey: 'id',
  },
)

const emit = defineEmits<{
  (e: 'sort-change', payload: { key: string | null; order: 'asc' | 'desc' | null }): void
}>()

const sortKey = ref<string | null>(null)
const sortOrder = ref<'asc' | 'desc'>('asc')

const sortingColumn = computed(() => props.columns.find((col) => col.key === sortKey.value))

const sortedData = computed(() => {
  if (!sortingColumn.value) return props.data
  const key = sortingColumn.value.key
  const sign = sortOrder.value === 'asc' ? 1 : -1
  return [...props.data].sort((a, b) => {
    const av = a[key]
    const bv = b[key]
    if (typeof av === 'number' && typeof bv === 'number') return (av - bv) * sign
    return String(av ?? '').localeCompare(String(bv ?? ''), 'zh-Hans-CN') * sign
  })
})

function ariaSort(column: Column): 'ascending' | 'descending' | 'none' {
  if (!column.sortable || sortKey.value !== column.key) return 'none'
  return sortOrder.value === 'asc' ? 'ascending' : 'descending'
}

function onSort(column: Column) {
  if (!column.sortable) return
  if (sortKey.value !== column.key) {
    sortKey.value = column.key
    sortOrder.value = 'asc'
  } else if (sortOrder.value === 'asc') {
    sortOrder.value = 'desc'
  } else {
    sortKey.value = null
    sortOrder.value = 'asc'
  }
  emit('sort-change', {
    key: sortKey.value,
    order: sortKey.value ? sortOrder.value : null,
  })
}

function sortIcon(column: Column): string {
  if (sortKey.value !== column.key) return '↕'
  return sortOrder.value === 'asc' ? '↑' : '↓'
}

// 声明的列宽视为最小宽度：容器不足时表格横向滚动，避免列被挤压成逐字换行
function px(value: string | undefined): number {
  const n = Number.parseFloat(value ?? '')
  return Number.isFinite(n) ? n : 0
}
const tableMinWidth = computed(
  () => `${props.columns.reduce((sum, column) => sum + px(column.width), 0)}px`,
)
</script>

<template>
  <div class="xf-table">
    <table :style="{ minWidth: tableMinWidth }">
      <thead>
        <tr>
          <th
            v-for="column in columns"
            :key="column.key"
            :style="column.width ? { width: column.width } : undefined"
            :aria-sort="ariaSort(column)"
          >
            <button
              v-if="column.sortable"
              type="button"
              class="xf-table__sort"
              :class="{ 'is-active': sortKey === column.key }"
              :aria-label="`按${column.label}排序`"
              @click="onSort(column)"
            >
              {{ column.label }}
              <span class="xf-table__sort-icon" aria-hidden="true">{{ sortIcon(column) }}</span>
            </button>
            <template v-else>{{ column.label }}</template>
          </th>
        </tr>
      </thead>
      <tbody>
        <template v-if="loading">
          <tr v-for="row in 5" :key="`skeleton-${row}`" aria-hidden="true">
            <td v-for="column in columns" :key="column.key">
              <span class="xf-table__skeleton"></span>
            </td>
          </tr>
        </template>
        <template v-else>
          <tr v-for="(row, index) in sortedData" :key="String(row[rowKey] ?? index)">
            <td
              v-for="column in columns"
              :key="column.key"
              :class="[`is-${column.align ?? 'left'}`, { 'is-wrap': column.wrap }]"
            >
              <slot :name="column.key" :row="row" :index="index">{{ row[column.key] }}</slot>
            </td>
          </tr>
          <tr v-if="sortedData.length === 0">
            <td class="xf-table__empty" :colspan="columns.length">
              <XfEmpty :title="emptyText" :description="emptyDescription" />
            </td>
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.xf-table {
  width: 100%;
  overflow-x: auto;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-md);
}

.xf-table table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--text-body);
}

.xf-table th {
  height: 40px;
  padding: 0 var(--space-3);
  background: var(--c-surface-2);
  color: var(--c-text-2);
  font-size: var(--text-caption);
  font-weight: var(--weight-body);
  text-align: left;
  white-space: nowrap;
}

.xf-table td {
  white-space: nowrap;
  height: 44px;
  padding: 0 var(--space-3);
  border-top: 1px solid var(--c-border);
  color: var(--text);
  line-height: var(--leading-compact);
}

/* 长文本主列允许换行（如企业名称），其余列保持单行避免相互挤压 */
.xf-table td.is-wrap {
  white-space: normal;
}

.xf-table tbody tr:hover td {
  background: var(--c-surface-2);
}

.xf-table td.is-center,
.xf-table th.is-center {
  text-align: center;
}

.xf-table td.is-right {
  text-align: right;
}

.xf-table td.is-right .xf-num {
  font-variant-numeric: tabular-nums;
}

.xf-table__sort {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: 0;
  border: none;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
}

.xf-table__sort:hover {
  color: var(--c-text);
}

.xf-table__sort.is-active {
  color: var(--c-primary);
}

.xf-table__sort-icon {
  font-size: var(--text-mini);
}

.xf-table__empty {
  padding: var(--space-10) var(--space-4);
  text-align: center;
}

.xf-table__skeleton {
  display: block;
  height: 14px;
  border-radius: var(--radius-xs);
  background: var(--c-surface-2);
  animation: xf-table-pulse 1.4s ease-in-out infinite;
}

@keyframes xf-table-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.45;
  }
}
</style>
