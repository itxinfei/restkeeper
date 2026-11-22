<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  XfButton,
  XfField,
  XfInput,
  XfModal,
  XfPagination,
  XfPanel,
  XfSelect,
  XfTable,
  XfTag,
} from '@xfcy/ui'
import { enterpriseList, planOptions } from '../api/mock'
import type { EnterpriseRow, EnterpriseStatus, Plan } from '../api/mock'

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
  { key: 'shopCount', label: '门店数', align: 'center', sortable: true },
  { key: 'status', label: '状态' },
  { key: 'expireDate', label: '有效期', align: 'center', sortable: true },
  { key: 'createdAt', label: '创建时间', align: 'center', sortable: true },
  { key: 'action', label: '操作', width: '150px' },
]

const enterprises = ref<EnterpriseRow[]>([...enterpriseList])
const keyword = ref('')
const statusFilter = ref('all')
const planFilter = ref('all')
const page = ref(1)
const pageSize = 10

const statusOptions = [
  { label: '状态：全部', value: 'all' },
  { label: '试用', value: '试用' },
  { label: '正式', value: '正式' },
  { label: '禁用', value: '禁用' },
]
const planFilterOptions = computed(() => [
  { label: '套餐：全部', value: 'all' },
  ...planOptions.map((item) => ({ label: item, value: item })),
])

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return enterprises.value.filter((row) => {
    if (statusFilter.value !== 'all' && row.status !== statusFilter.value) return false
    if (planFilter.value !== 'all' && row.plan !== planFilter.value) return false
    if (kw && !row.name.toLowerCase().includes(kw) && !row.code.toLowerCase().includes(kw)) {
      return false
    }
    return true
  })
})

const paged = computed(() => {
  const start = (page.value - 1) * pageSize
  return filtered.value.slice(start, start + pageSize)
})

const stat = computed(() => ({
  total: enterprises.value.length,
  trial: enterprises.value.filter((row) => row.status === '试用').length,
  official: enterprises.value.filter((row) => row.status === '正式').length,
  disabled: enterprises.value.filter((row) => row.status === '禁用').length,
}))

function statusTone(status: EnterpriseStatus) {
  if (status === '正式') return 'success' as const
  if (status === '试用') return 'warning' as const
  return 'neutral' as const
}

function resetFilters() {
  keyword.value = ''
  statusFilter.value = 'all'
  planFilter.value = 'all'
  page.value = 1
}

function toggleStatus(row: EnterpriseRow) {
  const index = enterprises.value.findIndex((item) => item.id === row.id)
  if (index < 0) return
  const next: EnterpriseStatus = row.status === '禁用' ? '正式' : '禁用'
  enterprises.value[index] = { ...enterprises.value[index], status: next }
}

/* -------- 开通 / 编辑弹窗 -------- */

type EnterpriseForm = {
  id: number | null
  name: string
  contact: string
  phone: string
  plan: Plan | null
  expireDate: string
}

function formatDate(date: Date): string {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function defaultExpireDate(): string {
  return formatDate(new Date(Date.now() + 365 * 24 * 60 * 60 * 1000))
}

function emptyForm(): EnterpriseForm {
  return { id: null, name: '', contact: '', phone: '', plan: '基础版', expireDate: defaultExpireDate() }
}

const modalOpen = ref(false)
const form = ref<EnterpriseForm>(emptyForm())
const nameError = ref('')
const contactError = ref('')
const phoneError = ref('')

function openCreate() {
  form.value = emptyForm()
  nameError.value = ''
  contactError.value = ''
  phoneError.value = ''
  modalOpen.value = true
}

function openEdit(row: EnterpriseRow) {
  form.value = {
    id: row.id,
    name: row.name,
    contact: row.contact,
    phone: row.phone,
    plan: row.plan,
    expireDate: row.expireDate,
  }
  nameError.value = ''
  contactError.value = ''
  phoneError.value = ''
  modalOpen.value = true
}

function save() {
  const name = form.value.name.trim()
  const contact = form.value.contact.trim()
  const phone = form.value.phone.trim()
  nameError.value = ''
  contactError.value = ''
  phoneError.value = ''

  if (!name) {
    nameError.value = '请填写企业名称'
    return
  }
  if (!contact) {
    contactError.value = '请填写联系人'
    return
  }
  if (!/^[0-9-]{6,20}$/.test(phone)) {
    phoneError.value = '请填写有效的联系电话'
    return
  }

  if (form.value.id != null) {
    const index = enterprises.value.findIndex((item) => item.id === form.value.id)
    if (index >= 0) {
      enterprises.value[index] = {
        ...enterprises.value[index],
        name,
        contact,
        phone,
        plan: form.value.plan ?? enterprises.value[index].plan,
        expireDate: form.value.expireDate,
      }
    }
  } else {
    const id = enterprises.value.reduce((max, item) => Math.max(max, item.id), 0) + 1
    const maxCode = enterprises.value.reduce(
      (max, item) => Math.max(max, Number(item.code) || 0),
      88010000,
    )
    enterprises.value.unshift({
      id,
      code: String(maxCode + 1).padStart(8, '0'),
      name,
      contact,
      phone,
      shopCount: 0,
      plan: form.value.plan ?? '基础版',
      status: '试用',
      expireDate: form.value.expireDate,
      createdAt: formatDate(new Date()),
    })
  }

  modalOpen.value = false
  page.value = 1
}
</script>

<template>
  <div class="xfo-toolbar">
    <div class="xfo-toolbar__search">
      <XfInput v-model="keyword" placeholder="搜索企业名称 / 编码" />
    </div>
    <div class="xfo-toolbar__select">
      <XfSelect v-model="statusFilter" :options="statusOptions" />
    </div>
    <div class="xfo-toolbar__select">
      <XfSelect v-model="planFilter" :options="planFilterOptions" />
    </div>
    <XfButton variant="secondary" @click="resetFilters">重置</XfButton>
    <div class="xfo-toolbar__grow"></div>
    <XfButton @click="openCreate">＋ 开通企业</XfButton>
  </div>

  <XfPanel>
    <XfTable
      :columns="columns"
      :data="paged"
      row-key="id"
      empty-text="暂无企业（示意）"
      empty-description="调整筛选条件，或点击右上角「开通企业」"
    >
      <template #code="{ row }">
        <span class="xfo-num">{{ (row as EnterpriseRow).code }}</span>
      </template>
      <template #name="{ row }">
        <div class="xfo-cell-name">{{ (row as EnterpriseRow).name }}</div>
        <div class="xfo-cell-sub">{{ (row as EnterpriseRow).plan }}</div>
      </template>
      <template #shopCount="{ row }">
        <span class="xfo-num">{{ (row as EnterpriseRow).shopCount }}</span>
      </template>
      <template #status="{ row }">
        <XfTag :tone="statusTone((row as EnterpriseRow).status)">
          {{ (row as EnterpriseRow).status }}
        </XfTag>
      </template>
      <template #expireDate="{ row }">
        <span class="xfo-num">{{ (row as EnterpriseRow).expireDate }}</span>
      </template>
      <template #createdAt="{ row }">
        <span class="xfo-num">{{ (row as EnterpriseRow).createdAt }}</span>
      </template>
      <template #action="{ row }">
        <div class="xfo-actions">
          <XfButton size="sm" variant="ghost" @click="openEdit(row as EnterpriseRow)">编辑</XfButton>
          <XfButton
            size="sm"
            :variant="(row as EnterpriseRow).status === '禁用' ? 'secondary' : 'danger'"
            @click="toggleStatus(row as EnterpriseRow)"
          >
            {{ (row as EnterpriseRow).status === '禁用' ? '启用' : '停用' }}
          </XfButton>
        </div>
      </template>
    </XfTable>

    <div class="xfo-pager">
      <span class="xfo-pager__summary">
        共 {{ stat.total }} 家 · 试用 {{ stat.trial }} · 正式 {{ stat.official }} · 禁用 {{ stat.disabled }}（示意）
      </span>
      <XfPagination v-model:page="page" :total="filtered.length" :page-size="pageSize" />
    </div>
  </XfPanel>

  <XfModal
    v-model:open="modalOpen"
    :title="form.id ? '编辑企业（示意）' : '开通企业（示意）'"
  >
    <div class="xfo-form xfo-form--grid">
      <XfField class="xfo-form__full" label="企业名称" required :error="nameError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.name" placeholder="如 心飞餐饮（国贸店）" />
        </template>
      </XfField>
      <XfField label="联系人" required :error="contactError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.contact" placeholder="如 李国栋" />
        </template>
      </XfField>
      <XfField label="联系电话" required :error="phoneError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.phone" placeholder="如 13801002301" />
        </template>
      </XfField>
      <XfField label="套餐">
        <template #default="{ id }">
          <XfSelect
            :id="id"
            v-model="form.plan"
            :options="planOptions.map((item) => ({ label: item, value: item }))"
          />
        </template>
      </XfField>
      <XfField label="有效期" hint="默认自今日起一年">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.expireDate" type="date" />
        </template>
      </XfField>
    </div>
    <template #footer>
      <XfButton variant="secondary" @click="modalOpen = false">取消</XfButton>
      <XfButton @click="save">保存</XfButton>
    </template>
  </XfModal>
</template>
