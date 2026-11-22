<script setup lang="ts">
import { computed, ref } from 'vue'
import { XfButton, XfModal, XfPanel, XfTable } from '@xfcy/ui'
import { recycleList } from '../api/mock'
import type { RecycleRow } from '../api/mock'

type Column = {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  width?: string
}

const columns: Column[] = [
  { key: 'code', label: '企业编码', sortable: true },
  { key: 'name', label: '企业名称' },
  { key: 'contact', label: '联系人' },
  { key: 'shopCount', label: '门店数', align: 'center' },
  { key: 'deletedAt', label: '删除时间', align: 'center', sortable: true },
  { key: 'action', label: '操作', width: '180px' },
]

const rows = ref<RecycleRow[]>([...recycleList])

/* 恢复 / 彻底删除（示意） */
const noticeOpen = ref(false)
const noticeText = ref('')
const confirmOpen = ref(false)
const pendingRow = ref<RecycleRow | null>(null)

const summary = computed(() => `共 ${rows.value.length}} 家已删除企业（示意）`)

function restore(row: RecycleRow) {
  rows.value = rows.value.filter((item) => item.id !== row.id)
  noticeText.value = `已恢复「${row.name}」（示意，仅本地生效）`
  noticeOpen.value = true
}

function askDelete(row: RecycleRow) {
  pendingRow.value = row
  confirmOpen.value = true
}

function confirmDelete() {
  const row = pendingRow.value
  if (row) {
    rows.value = rows.value.filter((item) => item.id !== row.id)
  }
  pendingRow.value = null
  confirmOpen.value = false
}
</script>

<template>
  <div class="xfo-toolbar">
    <span class="xfo-muted">{{ summary }}，数据保留 30 天（示意）</span>
  </div>

  <XfPanel>
    <XfTable
      :columns="columns"
      :data="rows"
      row-key="id"
      empty-text="回收站为空（示意）"
      empty-description="企业被删除后将进入回收站，可恢复或彻底删除"
    >
      <template #code="{ row }">
        <span class="xfo-num">{{ (row as RecycleRow).code }}</span>
      </template>
      <template #name="{ row }">
        <div class="xfo-cell-name">{{ (row as RecycleRow).name }}</div>
        <div class="xfo-cell-sub">删除人：{{ (row as RecycleRow).deletedBy }}</div>
      </template>
      <template #shopCount="{ row }">
        <span class="xfo-num">{{ (row as RecycleRow).shopCount }}</span>
      </template>
      <template #deletedAt="{ row }">
        <span class="xfo-num">{{ (row as RecycleRow).deletedAt }}</span>
      </template>
      <template #action="{ row }">
        <div class="xfo-actions">
          <XfButton size="sm" variant="secondary" @click="restore(row as RecycleRow)">恢复</XfButton>
          <XfButton size="sm" variant="danger" @click="askDelete(row as RecycleRow)">彻底删除</XfButton>
        </div>
      </template>
    </XfTable>
  </XfPanel>

  <XfModal v-model:open="noticeOpen" title="提示">
    <p class="xfo-muted">{{ noticeText }}</p>
    <template #footer>
      <XfButton variant="secondary" @click="noticeOpen = false">知道了</XfButton>
    </template>
  </XfModal>

  <XfModal v-model:open="confirmOpen" title="彻底删除（示意）">
    <p class="xfo-muted">
      彻底删除后不可恢复，确定删除「{{ pendingRow?.name ?? '' }}」？
    </p>
    <template #footer>
      <XfButton variant="secondary" @click="confirmOpen = false">取消</XfButton>
      <XfButton variant="danger" @click="confirmDelete">彻底删除</XfButton>
    </template>
  </XfModal>
</template>
