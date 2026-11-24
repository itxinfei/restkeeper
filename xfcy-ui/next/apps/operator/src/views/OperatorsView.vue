<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  XfButton,
  XfField,
  XfInput,
  XfModal,
  XfPageHead,
  XfPanel,
  XfSelect,
  XfTable,
  XfTag,
  XfErrorState,
  labelOf,
  toneOf,
} from '@xfcy/ui'
import { isMockMode, system } from '@xfcy/api'
import type { OperatorUser } from '@xfcy/api'

type Column = {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  width?: string
}

type DictStatus = '启用' | '停用'

type OperatorRow = {
  id: string
  account: string
  name: string
  role: string
  status: DictStatus
  lastLogin: string
}

const columns: Column[] = [
  { key: 'account', label: '账号', sortable: true },
  { key: 'name', label: '姓名' },
  { key: 'role', label: '角色' },
  { key: 'status', label: '状态' },
  { key: 'lastLogin', label: '最后登录', align: 'center', sortable: true },
  { key: 'action', label: '操作', width: '160px' },
]

const rows = ref<OperatorRow[]>([])
const users = ref<OperatorUser[]>([])
const loading = ref(false)
const loaded = ref(false)
const error = ref('')
/** 后端暂无运营人员写接口，写操作仅 mock 模式可用 */
const writable = isMockMode()

const roleOptions = ['平台运营', '运营', '客服'].map((item) => ({ label: item, value: item }))
const statusOptions = [
  { label: '启用', value: '启用' },
  { label: '停用', value: '停用' },
]

type Draft = OperatorUser & { name?: string; role?: string; status?: string; lastLogin?: string }

function toRow(user: OperatorUser): OperatorRow {
  const draft = user as Draft
  return {
    id: user.uid,
    account: user.loginname,
    name: draft.name ?? user.loginname,
    role: draft.role ?? '—',
    status: draft.status === '停用' ? '停用' : '启用',
    lastLogin: draft.lastLogin ?? '—',
  }
}

async function load() {
  if (loading.value) return
  loading.value = true
  error.value = ''
  try {
    const result = await system.operatorPage(1, 1000)
    users.value = result.records
    rows.value = users.value.map(toRow)
    loaded.value = true
  } catch (err) {
    error.value = err instanceof Error ? err.message : '运营人员加载失败'
  } finally {
    loading.value = false
  }
}

async function toggle(row: OperatorRow) {
  if (!writable) return
  const user = users.value.find((item) => item.uid === row.id)
  if (!user) return
  const draft = user as Draft
  const prev = draft.status
  try {
    await system.operatorUpdate({
      ...user,
      status: row.status === '启用' ? '停用' : '启用',
      name: draft.name,
      role: draft.role,
      lastLogin: draft.lastLogin,
    })
    await load()
  } catch {
    draft.status = prev
  }
}

/* -------- 新增 / 编辑弹窗 -------- */

type OperatorForm = {
  id: string | null
  account: string
  name: string
  role: string
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
const saveError = ref('')

function openCreate() {
  form.value = { id: null, account: '', name: '', role: '', status: '启用', password: '' }
  accountError.value = ''
  nameError.value = ''
  roleError.value = ''
  passwordError.value = ''
  saveError.value = ''
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
  saveError.value = ''
  modalOpen.value = true
}

async function save() {
  const account = form.value.account.trim()
  const name = form.value.name.trim()
  accountError.value = ''
  nameError.value = ''
  roleError.value = ''
  passwordError.value = ''
  saveError.value = ''

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

  try {
    if (form.value.id != null) {
      const user = users.value.find((item) => item.uid === form.value.id)
      if (!user) throw new Error('未找到该运营人员')
      await system.operatorUpdate({
        uid: user.uid,
        loginname: account,
        name,
        role: form.value.role,
        status: form.value.status,
        lastLogin: (user as Draft).lastLogin,
      })
      if (form.value.password) {
        await system.operatorResetPwd(user.uid, form.value.password)
      }
    } else {
      await system.operatorSave({
        loginname: account,
        loginpass: form.value.password,
        name,
        role: form.value.role,
        status: form.value.status,
        lastLogin: '—',
      })
    }
    modalOpen.value = false
    await load()
  } catch (error) {
    saveError.value = error instanceof Error ? error.message : '保存失败'
  }
}

onMounted(load)
</script>

<template>
  <XfPageHead title="运营人员" description="账号、角色与启停状态">
    <template #actions>
      <XfButton :disabled="!writable" @click="openCreate">＋ 新增运营人员</XfButton>
    </template>
  </XfPageHead>

  <div class="xf-toolbar">
    <span class="xf-muted">共 {{ rows.length }} 名运营人员 · 数据源：{{ writable ? '@xfcy/api mock' : '真实后端' }}</span>
    <div class="xf-toolbar__grow"></div>
  </div>

  <XfPanel>
    <p v-if="!writable" class="xfo-note">后端未提供运营人员写接口，新增/编辑/停用仅在 mock 模式可用。</p>
    <XfErrorState v-if="error && !rows.length" :message="error" @retry="load" />
    <XfTable
      v-else
      :columns="columns"
      :data="rows"
      :loading="loading && !loaded"
      row-key="id"
      empty-text="暂无运营人员"
      empty-description="点击右上角「新增运营人员」创建"
    >
      <template #account="{ row }">
        <span class="xf-num">{{ (row as OperatorRow).account }}</span>
      </template>
      <template #name="{ row }">
        <span class="xfo-cell-name">{{ (row as OperatorRow).name }}</span>
      </template>
      <template #role="{ row }">
        <span class="xf-muted">{{ (row as OperatorRow).role }}</span>
      </template>
      <template #status="{ row }">
        <XfTag :tone="toneOf('enable', (row as OperatorRow).status)">{{ labelOf('enable', (row as OperatorRow).status) }}</XfTag>
      </template>
      <template #lastLogin="{ row }">
        <span class="xf-num">{{ (row as OperatorRow).lastLogin }}</span>
      </template>
      <template #action="{ row }">
        <div class="xfo-actions">
          <XfButton size="sm" variant="ghost" :disabled="!writable" @click="openEdit(row as OperatorRow)">
            编辑
          </XfButton>
          <XfButton
            size="sm"
            :disabled="!writable"
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
    :title="form.id ? '编辑运营人员' : '新增运营人员'"
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
        :hint="form.id ? '留空表示不修改' : '至少 6 位'"
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
    <p v-if="saveError" class="xfo-note">{{ saveError }}</p>
    <template #footer>
      <XfButton variant="secondary" @click="modalOpen = false">取消</XfButton>
      <XfButton @click="save">保存</XfButton>
    </template>
  </XfModal>
</template>
