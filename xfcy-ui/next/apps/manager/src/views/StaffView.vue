<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
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
import { staff } from '@xfcy/api'
import type { Staff } from '@xfcy/api'

const PAGE_SIZE = 10

type StaffRow = {
  id: string
  name: string
  phone: string
  sex: string
  status: number
  idNumber: string
  raw?: Staff
}

const rows = ref<StaffRow[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const loaded = ref(false)
const nameKeyword = ref('')
const phoneKeyword = ref('')

const columns = [
  { key: 'name', label: '姓名' },
  { key: 'phone', label: '手机号' },
  { key: 'sex', label: '性别' },
  { key: 'status', label: '状态' },
  { key: 'actions', label: '操作', align: 'right' as const },
]

function toRow(item: Staff): StaffRow {
  return {
    id: item.staffId ?? '',
    name: item.staffName,
    phone: item.phone ?? '-',
    sex: item.sex || '-',
    status: item.status ?? 1,
    idNumber: item.idNumber ?? '-',
    raw: item,
  }
}

async function load() {
  if (loading.value) return
  loading.value = true
  try {
    const result = await staff.page(
      page.value,
      PAGE_SIZE,
      nameKeyword.value.trim() || undefined,
      phoneKeyword.value.trim() || undefined,
    )
    rows.value = result.records.map(toRow)
    total.value = result.total
    loaded.value = true
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(page, () => void load())

function search() {
  page.value = 1
  void load()
}

/* ---------------- 新增 / 编辑 ---------------- */

const sexOptions = [
  { label: '男', value: '男' },
  { label: '女', value: '女' },
]

const modalOpen = ref(false)
const editing = ref<StaffRow | null>(null)
const form = ref({ name: '', phone: '', sex: '男', idNumber: '', password: '' })
const errors = ref({ name: '', phone: '', password: '' })

function openCreate() {
  editing.value = null
  form.value = { name: '', phone: '', sex: '男', idNumber: '', password: '' }
  errors.value = { name: '', phone: '', password: '' }
  modalOpen.value = true
}

function openEdit(row: StaffRow) {
  editing.value = row
  form.value = {
    name: row.name,
    phone: row.phone === '-' ? '' : row.phone,
    sex: row.sex === '-' ? '男' : row.sex,
    idNumber: row.idNumber === '-' ? '' : row.idNumber,
    password: '',
  }
  errors.value = { name: '', phone: '', password: '' }
  modalOpen.value = true
}

async function save() {
  const name = form.value.name.trim()
  const phone = form.value.phone.trim()
  errors.value = { name: '', phone: '', password: '' }
  if (!name) errors.value.name = '请填写员工姓名'
  if (!phone) errors.value.phone = '请填写手机号'
  if (!editing.value && !form.value.password) errors.value.password = '请设置初始密码'
  if (errors.value.name || errors.value.phone || errors.value.password) return

  const payload: Staff = {
    staffName: name,
    phone,
    sex: form.value.sex,
    idNumber: form.value.idNumber.trim() || undefined,
    password: form.value.password || undefined,
    status: editing.value?.status ?? 1,
  }
  if (editing.value) {
    await staff.update({ ...payload, staffId: editing.value.id })
  } else {
    await staff.add(payload)
  }
  modalOpen.value = false
  await load()
}

/* ---------------- 停用 / 启用 ---------------- */

const noticeOpen = ref(false)
const noticeText = ref('')

async function toggleStatus(row: StaffRow) {
  if (row.status === 1) {
    await staff.pause(row.id)
    noticeText.value = `已停用「${row.name}」`
  } else {
    const current: Staff = row.raw ?? { staffName: row.name, staffId: row.id }
    await staff.update({ ...current, staffId: row.id, status: 1 })
    noticeText.value = `已启用「${row.name}」`
  }
  noticeOpen.value = true
  await load()
}
</script>

<template>
  <div class="xfm-toolbar">
    <div class="xfm-toolbar__search">
      <XfInput v-model="nameKeyword" placeholder="姓名" />
    </div>
    <div class="xfm-toolbar__search">
      <XfInput v-model="phoneKeyword" placeholder="手机号" />
    </div>
    <XfButton variant="secondary" @click="search">查询</XfButton>
    <div class="xfm-toolbar__grow"></div>
    <XfButton @click="openCreate">＋ 新增员工</XfButton>
  </div>

  <XfPanel>
    <XfTable :columns="columns" :data="rows" :loading="loading && !loaded" row-key="id">
      <template #status="{ row }">
        <XfTag :tone="row.status === 1 ? 'success' : 'neutral'">
          {{ row.status === 1 ? '在职' : '停用' }}
        </XfTag>
      </template>
      <template #actions="{ row }">
        <div class="xfm-actions">
          <XfButton size="sm" variant="secondary" @click="openEdit(row as StaffRow)">编辑</XfButton>
          <XfButton size="sm" :variant="row.status === 1 ? 'danger' : 'secondary'" @click="toggleStatus(row as StaffRow)">
            {{ row.status === 1 ? '停用' : '启用' }}
          </XfButton>
        </div>
      </template>
    </XfTable>

    <div v-if="loaded" class="xfm-pager">
      <span class="xfm-pager__summary">共 {{ total }} 名员工</span>
      <XfPagination v-model:page="page" :total="total" :page-size="PAGE_SIZE" />
    </div>
  </XfPanel>

  <!-- 新增 / 编辑员工 -->
  <XfModal v-model:open="modalOpen" :title="editing ? '编辑员工' : '新增员工'">
    <div class="xfm-form">
      <XfField label="姓名" required :error="errors.name || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.name" placeholder="如 李国栋" />
        </template>
      </XfField>
      <div class="xfm-form--grid">
        <XfField label="手机号" required :error="errors.phone || ''">
          <template #default="{ id }">
            <XfInput :id="id" v-model="form.phone" placeholder="如 13801002301" />
          </template>
        </XfField>
        <XfField label="性别">
          <template #default="{ id }">
            <XfSelect :id="id" v-model="form.sex" :options="sexOptions" />
          </template>
        </XfField>
      </div>
      <XfField label="身份证号">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.idNumber" placeholder="选填" />
        </template>
      </XfField>
      <XfField
        :label="editing ? '重置密码' : '初始密码'"
        :required="!editing"
        :error="errors.password || ''"
        :hint="editing ? '留空表示不修改密码' : ''"
      >
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.password" type="password" placeholder="如 123456" />
        </template>
      </XfField>
    </div>
    <template #footer>
      <XfButton variant="secondary" @click="modalOpen = false">取消</XfButton>
      <XfButton @click="save">保存</XfButton>
    </template>
  </XfModal>

  <!-- 提示 -->
  <XfModal v-model:open="noticeOpen" title="提示">
    <p class="xfm-muted">{{ noticeText }}</p>
    <template #footer>
      <XfButton variant="secondary" @click="noticeOpen = false">知道了</XfButton>
    </template>
  </XfModal>
</template>
