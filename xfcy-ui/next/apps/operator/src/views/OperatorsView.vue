<script setup lang="ts">
import { ref } from 'vue'
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
import { operatorList, operatorRoles } from '../api/mock'
import type { DictStatus, OperatorRole, OperatorRow } from '../api/mock'

type Column = {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  width?: string
}

const columns: Column[] = [
  { key: 'account', label: '账号', sortable: true },
  { key: 'name', label: '姓名' },
  { key: 'role', label: '角色' },
  { key: 'status', label: '状态' },
  { key: 'lastLogin', label: '最后登录', align: 'center', sortable: true },
  { key: 'action', label: '操作', width: '160px' },
]

const rows = ref<OperatorRow[]>([...operatorList])

const roleOptions = operatorRoles.map((item) => ({ label: item, value: item }))
const statusOptions = [
  { label: '启用', value: '启用' },
  { label: '停用', value: '停用' },
]

function enableTone(status: string) {
  return status === '启用' ? ('success' as const) : ('danger' as const)
}

function toggle(row: OperatorRow) {
  const index = rows.value.findIndex((item) => item.id === row.id)
  if (index < 0) return
  const next: DictStatus = row.status === '启用' ? '停用' : '启用'
  rows.value[index] = { ...rows.value[index], status: next }
}

/* -------- 新增 / 编辑弹窗 -------- */

type OperatorForm = {
  id: number | null
  account: string
  name: string
  role: OperatorRole | ''
  status: DictStatus
  password: string
}

const modalOpen = ref(false)
const form = ref<OperatorForm>({
  id: null,
  account: '',
  name: '',
  role: '',
  status: '启用',
  password: '',
})
const accountError = ref('')
const nameError = ref('')
const roleError = ref('')
const passwordError = ref('')

function openCreate() {
  form.value = { id: null, account: '', name: '', role: '', status: '启用', password: '' }
  accountError.value = ''
  nameError.value = ''
  roleError.value = ''
  passwordError.value = ''
  modalOpen.value = true
}

function openEdit(row: OperatorRow) {
  form.value = {
    id: row.id,
    account: row.account,
    name: row.name,
    role: row.role,
    status: row.status,
    password: '',
  }
  accountError.value = ''
  nameError.value = ''
  roleError.value = ''
  passwordError.value = ''
  modalOpen.value = true
}

function save() {
  const account = form.value.account.trim()
  const name = form.value.name.trim()
  accountError.value = ''
  nameError.value = ''
  roleError.value = ''
  passwordError.value = ''

  if (!account) {
    accountError.value = '请填写账号'
    return
  }
  if (!name) {
    nameError.value = '请填写姓名'
    return
  }
  if (!form.value.role) {
    roleError.value = '请选择角色'
    return
  }
  if (form.value.id == null && form.value.password.length < 6) {
    passwordError.value = '初始密码至少 6 位'
    return
  }

  if (form.value.id != null) {
    const index = rows.value.findIndex((item) => item.id === form.value.id)
    if (index >= 0) {
      rows.value[index] = {
        ...rows.value[index],
        account,
        name,
        role: form.value.role,
        status: form.value.status,
      }
    }
  } else {
    const id = rows.value.reduce((max, item) => Math.max(max, item.id), 0) + 1
    rows.value.push({
      id,
      account,
      name,
      role: form.value.role,
      status: form.value.status,
      lastLogin: '—',
    })
  }

  modalOpen.value = false
}
</script>

<template>
  <div class="xfo-toolbar">
    <span class="xfo-muted">共 {{ rows.length }} 名运营人员（示意）</span>
    <div class="xfo-toolbar__grow"></div>
    <XfButton @click="openCreate">＋ 新增运营人员</XfButton>
  </div>

  <XfPanel>
    <XfTable
      :columns="columns"
      :data="rows"
      row-key="id"
      empty-text="暂无运营人员（示意）"
      empty-description="点击右上角「新增运营人员」创建"
    >
      <template #account="{ row }">
        <span class="xfo-num">{{ (row as OperatorRow).account }}</span>
      </template>
      <template #name="{ row }">
        <span class="xfo-cell-name">{{ (row as OperatorRow).name }}</span>
      </template>
      <template #role="{ row }">
        <span class="xfo-muted">{{ (row as OperatorRow).role }}</span>
      </template>
      <template #status="{ row }">
        <XfTag :tone="enableTone((row as OperatorRow).status)">{{ (row as OperatorRow).status }}</XfTag>
      </template>
      <template #lastLogin="{ row }">
        <span class="xfo-num">{{ (row as OperatorRow).lastLogin }}</span>
      </template>
      <template #action="{ row }">
        <div class="xfo-actions">
          <XfButton size="sm" variant="ghost" @click="openEdit(row as OperatorRow)">编辑</XfButton>
          <XfButton
            size="sm"
            :variant="(row as OperatorRow).status === '启用' ? 'danger' : 'secondary'"
            @click="toggle(row as OperatorRow)"
          >
            {{ (row as OperatorRow).status === '启用' ? '停用' : '启用' }}
          </XfButton>
        </div>
      </template>
    </XfTable>
  </XfPanel>

  <XfModal
    v-model:open="modalOpen"
    :title="form.id ? '编辑运营人员（示意）' : '新增运营人员（示意）'"
  >
    <div class="xfo-form">
      <XfField label="账号" required :error="accountError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.account" placeholder="如 ops03" />
        </template>
      </XfField>
      <XfField label="姓名" required :error="nameError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.name" placeholder="如 王运营" />
        </template>
      </XfField>
      <XfField label="角色" required :error="roleError || ''">
        <template #default="{ id }">
          <XfSelect :id="id" v-model="form.role" :options="roleOptions" placeholder="请选择角色" />
        </template>
      </XfField>
      <XfField label="状态">
        <template #default="{ id }">
          <XfSelect :id="id" v-model="form.status" :options="statusOptions" />
        </template>
      </XfField>
      <XfField
        :label="form.id ? '重置密码' : '初始密码'"
        :required="form.id == null"
        :error="passwordError || ''"
        :hint="form.id ? '留空表示不修改（示意）' : '至少 6 位'"
      >
        <template #default="{ id }">
          <XfInput
            :id="id"
            v-model="form.password"
            type="password"
            placeholder="至少 6 位"
            autocomplete="new-password"
          />
        </template>
      </XfField>
    </div>
    <template #footer>
      <XfButton variant="secondary" @click="modalOpen = false">取消</XfButton>
      <XfButton @click="save">保存</XfButton>
    </template>
  </XfModal>
</template>
