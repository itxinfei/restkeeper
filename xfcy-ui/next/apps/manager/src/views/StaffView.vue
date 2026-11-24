<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import {
  XfButton,
  XfErrorState,
  XfField,
  XfInput,
  XfModal,
  XfPageHead,
  XfPagination,
  XfPanel,
  XfSelect,
  XfTable,
  XfTag,
  labelOf,
  toneOf,
  useToast,
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
const error = ref('')
const nameKeyword = ref('')
const phoneKeyword = ref('')

const columns = [
  { key: 'name', label: '姓名', width: '120px' },
  { key: 'phone', label: '手机号', width: '150px' },
  { key: 'sex', label: '性别', align: 'center' as const, width: '80px' },
  { key: 'status', label: '状态', width: '100px' },
  { key: 'actions', label: '操作', align: 'right' as const, width: '190px' },
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
  error.value = ''
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
  } catch (err) {
    error.value = err instanceof Error ? err.message : '员工列表加载失败'
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
const passwordOpen = ref(false)
const initialPassword = ref('')
const copied = ref(false)
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
  if (errors.value.name || errors.value.phone) return

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
    toast.success(`已保存「${name}」`)
  } else {
    const generated = await staff.add(payload)
    initialPassword.value = generated ?? form.value.password
    passwordOpen.value = true
  }
  modalOpen.value = false
  await load()
}

async function copyPassword() {
  try {
    await navigator.clipboard.writeText(initialPassword.value)
    copied.value = true
    window.setTimeout(() => {
      copied.value = false
    }, 1600)
  } catch {
    toast.error('复制失败，请手动选择密码')
  }
}

/* ---------------- 停用 / 启用 ---------------- */

const toast = useToast()

const statusConfirmOpen = ref(false)
const statusRow = ref<StaffRow | null>(null)

function askToggleStatus(row: StaffRow) {
  statusRow.value = row
  statusConfirmOpen.value = true
}

async function confirmToggleStatus() {
  const row = statusRow.value
  if (!row) return
  try {
    if (row.status === 1) {
      await staff.pause(row.id)
      toast.success(`已停用「${row.name}」`)
    } else {
      const current: Staff = row.raw ?? { staffName: row.name, staffId: row.id }
      await staff.update({ ...current, staffId: row.id, status: 1 })
      toast.success(`已启用「${row.name}」`)
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : '状态更新失败')
  } finally {
    statusConfirmOpen.value = false
    statusRow.value = null
    await load()
  }
}
</script>

<template>
  <XfPageHead title="员工管理" description="员工账号、状态与初始密码">
    <template #actions>
      <XfButton @click="openCreate">＋ 新增员工</XfButton>
    </template>
  </XfPageHead>

  <div class="xf-toolbar">
    <div class="xf-toolbar__search">
      <XfInput v-model="nameKeyword" placeholder="姓名" />
    </div>
    <div class="xf-toolbar__search">
      <XfInput v-model="phoneKeyword" placeholder="手机号" />
    </div>
    <XfButton variant="secondary" @click="search">查询</XfButton>
    <div class="xf-toolbar__grow"></div>
  </div>

  <XfPanel>
    <XfErrorState v-if="error && !rows.length" :message="error" @retry="load" />
    <XfTable v-else :columns="columns" :data="rows" :loading="loading && !loaded" row-key="id">
      <template #status="{ row }">
        <XfTag :tone="toneOf('staff', row.status as string | number | undefined)">
          {{ labelOf('staff', row.status as string | number | undefined) }}
        </XfTag>
      </template>
      <template #actions="{ row }">
        <div class="xfm-actions">
          <XfButton size="sm" variant="secondary" @click="openEdit(row as StaffRow)">编辑</XfButton>
          <XfButton size="sm" :variant="row.status === 1 ? 'danger' : 'secondary'" @click="askToggleStatus(row as StaffRow)">
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
        :error="errors.password || ''"
        :hint="editing ? '留空表示不修改密码' : '留空由系统生成 8 位数字'"
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

  <!-- 停用 / 启用二次确认 -->
  <XfModal v-model:open="statusConfirmOpen" title="状态确认" width="440px">
    <p v-if="statusRow" class="xfm-confirm">
      <template v-if="statusRow.status === 1">
        确认停用「{{ statusRow.name }}」？停用后该员工将无法登录收银端。
      </template>
      <template v-else> 确认启用「{{ statusRow.name }}」？启用后恢复该员工的登录资格。 </template>
    </p>
    <template #footer>
      <XfButton variant="secondary" @click="statusConfirmOpen = false">取消</XfButton>
      <XfButton :variant="statusRow?.status === 1 ? 'danger' : undefined" @click="confirmToggleStatus">
        确认{{ statusRow?.status === 1 ? '停用' : '启用' }}
      </XfButton>
    </template>
  </XfModal>

  <!-- 初始密码告知（新增成功后必须展示，否则员工无法登录） -->
  <XfModal v-model:open="passwordOpen" title="员工初始密码">
    <p class="xf-muted">请将该密码告知员工，员工登录使用姓名 + 此密码。</p>
    <div class="xfm-pwd">
      <code class="xfm-pwd__code xf-num">{{ initialPassword || '—' }}</code>
      <XfButton size="sm" variant="secondary" @click="copyPassword">
        {{ copied ? '已复制' : '复制' }}
      </XfButton>
    </div>
    <template #footer>
      <XfButton @click="passwordOpen = false">我已知晓</XfButton>
    </template>
  </XfModal>
</template>
