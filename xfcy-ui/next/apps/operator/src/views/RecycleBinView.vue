<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { XfButton, XfModal, XfPanel, XfTable, useToast } from '@xfcy/ui'
import { isMockMode } from '@xfcy/api'
import { useEnterpriseStore } from '../stores/enterprise'
import type { RecycleRow } from '../stores/enterprise'

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

const store = useEnterpriseStore()
const rows = computed(() => store.recycleRows)

/* 恢复 / 彻底删除 */
const toast = useToast()
const confirmOpen = ref(false)
const pendingRow = ref<RecycleRow | null>(null)
const busy = ref(false)

const summary = computed(() => `共 ${rows.value.length} 家已删除企业，数据保留 30 天`)

async function restore(row: RecycleRow) {
  busy.value = true
  try {
    await store.restore(row.id)
    toast.success(`已恢复「${row.name}」；若账号为停用状态，需再次编辑启用`)
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '恢复失败')
  } finally {
    busy.value = false
  }
}

function askDelete(row: RecycleRow) {
  pendingRow.value = row
  confirmOpen.value = true
}

async function confirmDelete() {
  const row = pendingRow.value
  pendingRow.value = null
  confirmOpen.value = false
  if (!row) return
  busy.value = true
  try {
    await store.destroy(row.id)
    toast.success(`已彻底删除「${row.name}」`)
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '删除失败')
  } finally {
    busy.value = false
  }
}

onMounted(() => {
  if (!store.loaded) void store.load()
})
</script>

<template>
  <div class="xfo-toolbar">
    <span class="xfo-muted">{{ summary }} · 数据源：{{ isMockMode() ? '@xfcy/api mock' : '真实后端' }}</span>
    <div class="xfo-toolbar__grow"></div>
    <XfButton variant="secondary" :disabled="store.loading" @click="store.refresh()">
      {{ store.loading ? '刷新中…' : '刷新' }}
    </XfButton>
  </div>

  <XfPanel>
    <XfTable
      :columns="columns"
      :data="rows"
      row-key="id"
      empty-text="回收站为空"
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
          <XfButton size="sm" variant="secondary" :disabled="busy" @click="restore(row as RecycleRow)">
            恢复
          </XfButton>
          <XfButton size="sm" variant="danger" :disabled="busy" @click="askDelete(row as RecycleRow)">
            彻底删除
          </XfButton>
        </div>
      </template>
    </XfTable>
  </XfPanel>

  <XfModal v-model:open="confirmOpen" title="彻底删除">
    <p class="xfo-muted">
      彻底删除后不可恢复，确定删除「{{ pendingRow?.name ?? '' }}」？真实后端无物理删除接口，仅 mock 模式可用。
    </p>
    <template #footer>
      <XfButton variant="secondary" @click="confirmOpen = false">取消</XfButton>
      <XfButton variant="danger" @click="confirmDelete">彻底删除</XfButton>
    </template>
  </XfModal>
</template>
