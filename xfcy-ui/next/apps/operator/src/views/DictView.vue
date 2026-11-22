<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  XfButton,
  XfField,
  XfInput,
  XfModal,
  XfPanel,
  XfSelect,
  XfTable,
  XfTag,
} from '@xfcy/ui'
import { dictList, dictTypes } from '../api/mock'
import type { DictRow, DictStatus } from '../api/mock'

type Column = {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  width?: string
}

const columns: Column[] = [
  { key: 'type', label: '字典类型' },
  { key: 'code', label: '编码' },
  { key: 'value', label: '值' },
  { key: 'sort', label: '排序', align: 'center', sortable: true },
  { key: 'status', label: '状态' },
  { key: 'action', label: '操作', width: '160px' },
]

const rows = ref<DictRow[]>([...dictList])
const keyword = ref('')

const typeOptions = dictTypes.map((item) => ({ label: item, value: item }))
const statusOptions = [
  { label: '启用', value: '启用' },
  { label: '停用', value: '停用' },
]

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return rows.value
  return rows.value.filter(
    (row) =>
      row.type.toLowerCase().includes(kw) ||
      row.code.toLowerCase().includes(kw) ||
      row.value.toLowerCase().includes(kw),
  )
})

function enableTone(status: string) {
  // 语义固定：启用 = 成功绿，停用 = 危险红（DESIGN.md §2）
  return status === '启用' ? ('success' as const) : ('danger' as const)
}

function toggle(row: DictRow) {
  const index = rows.value.findIndex((item) => item.id === row.id)
  if (index < 0) return
  const next: DictStatus = row.status === '启用' ? '停用' : '启用'
  rows.value[index] = { ...rows.value[index], status: next }
}

/* -------- 新增 / 编辑弹窗 -------- */

type DictForm = {
  id: number | null
  type: string
  code: string
  value: string
  sort: string
  status: DictStatus
}

const modalOpen = ref(false)
const form = ref<DictForm>({ id: null, type: '', code: '', value: '', sort: '', status: '启用' })
const typeError = ref('')
const codeError = ref('')
const valueError = ref('')

function openCreate() {
  form.value = { id: null, type: '', code: '', value: '', sort: '', status: '启用' }
  typeError.value = ''
  codeError.value = ''
  valueError.value = ''
  modalOpen.value = true
}

function openEdit(row: DictRow) {
  form.value = {
    id: row.id,
    type: row.type,
    code: row.code,
    value: row.value,
    sort: String(row.sort),
    status: row.status,
  }
  typeError.value = ''
  codeError.value = ''
  valueError.value = ''
  modalOpen.value = true
}

function save() {
  const type = form.value.type
  const code = form.value.code.trim()
  const value = form.value.value.trim()
  typeError.value = ''
  codeError.value = ''
  valueError.value = ''

  if (!type) {
    typeError.value = '请选择字典类型'
    return
  }
  if (!code) {
    codeError.value = '请填写编码'
    return
  }
  if (!value) {
    valueError.value = '请填写值'
    return
  }

  const sort = Number.parseInt(form.value.sort, 10)
  const sortValue = Number.isFinite(sort) ? sort : rows.value.length + 1

  if (form.value.id != null) {
    const index = rows.value.findIndex((item) => item.id === form.value.id)
    if (index >= 0) {
      rows.value[index] = {
        ...rows.value[index],
        type,
        code,
        value,
        sort: sortValue,
        status: form.value.status,
      }
    }
  } else {
    const id = rows.value.reduce((max, item) => Math.max(max, item.id), 0) + 1
    rows.value.push({
      id,
      type,
      code,
      value,
      sort: sortValue,
      status: form.value.status,
    })
  }

  modalOpen.value = false
}
</script>

<template>
  <div class="xfo-toolbar">
    <div class="xfo-toolbar__search">
      <XfInput v-model="keyword" placeholder="搜索类型 / 编码 / 值" />
    </div>
    <div class="xfo-toolbar__grow"></div>
    <span class="xfo-muted">共 {{ rows.length }} 项（示意）</span>
    <XfButton @click="openCreate">＋ 新增字典</XfButton>
  </div>

  <XfPanel>
    <XfTable
      :columns="columns"
      :data="filtered"
      row-key="id"
      empty-text="暂无字典（示意）"
      empty-description="点击右上角「新增字典」创建"
    >
      <template #type="{ row }">
        <span class="xfo-cell-name">{{ (row as DictRow).type }}</span>
      </template>
      <template #code="{ row }">
        <span class="xfo-num">{{ (row as DictRow).code }}</span>
      </template>
      <template #sort="{ row }">
        <span class="xfo-num">{{ (row as DictRow).sort }}</span>
      </template>
      <template #status="{ row }">
        <XfTag :tone="enableTone((row as DictRow).status)">{{ (row as DictRow).status }}</XfTag>
      </template>
      <template #action="{ row }">
        <div class="xfo-actions">
          <XfButton size="sm" variant="ghost" @click="openEdit(row as DictRow)">编辑</XfButton>
          <XfButton
            size="sm"
            :variant="(row as DictRow).status === '启用' ? 'danger' : 'secondary'"
            @click="toggle(row as DictRow)"
          >
            {{ (row as DictRow).status === '启用' ? '停用' : '启用' }}
          </XfButton>
        </div>
      </template>
    </XfTable>
  </XfPanel>

  <XfModal
    v-model:open="modalOpen"
    :title="form.id ? '编辑字典（示意）' : '新增字典（示意）'"
  >
    <div class="xfo-form">
      <XfField label="字典类型" required :error="typeError || ''">
        <template #default="{ id }">
          <XfSelect :id="id" v-model="form.type" :options="typeOptions" placeholder="请选择字典类型" />
        </template>
      </XfField>
      <XfField label="编码" required :error="codeError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.code" placeholder="如 dish_category" />
        </template>
      </XfField>
      <XfField label="值" required :error="valueError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.value" placeholder="如 热菜" />
        </template>
      </XfField>
      <XfField label="排序" hint="数字越小越靠前">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.sort" placeholder="如 1" />
        </template>
      </XfField>
      <XfField label="状态">
        <template #default="{ id }">
          <XfSelect :id="id" v-model="form.status" :options="statusOptions" />
        </template>
      </XfField>
    </div>
    <template #footer>
      <XfButton variant="secondary" @click="modalOpen = false">取消</XfButton>
      <XfButton @click="save">保存</XfButton>
    </template>
  </XfModal>
</template>
