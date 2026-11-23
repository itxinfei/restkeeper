<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  XfButton,
  XfErrorState,
  XfField,
  XfInput,
  XfModal,
  XfPagination,
  XfPanel,
  XfSelect,
  XfTable,
  XfTag,
  labelOf,
  toneOf,
  useToast,
} from '@xfcy/ui'
import { isMockMode } from '@xfcy/api'
import {
  planOptions,
  useEnterpriseStore,
} from '../stores/enterprise'
import type { EnterpriseDraft, EnterpriseRow, Plan } from '../stores/enterprise'

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

const store = useEnterpriseStore()
const enterprises = computed(() => store.enterpriseRows)
const rows = computed(() => store.enterpriseRows)
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

const stat = computed(() => ({
  total: store.total,
  trial: enterprises.value.filter((row) => row.status === '试用').length,
  official: enterprises.value.filter((row) => row.status === '正式').length,
  disabled: enterprises.value.filter((row) => row.status === '禁用').length,
}))

const toast = useToast()

async function toggleStatus(row: EnterpriseRow) {
  try {
    await store.toggleStatus(row)
    toast.success(row.status === '禁用' ? `已启用「${row.name}」` : `已停用「${row.name}」`)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : '状态更新失败')
  }
}

/* -------- 开通 / 编辑弹窗 -------- */

type EnterpriseForm = {
  id: string | null
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
const saveError = ref('')
const saving = ref(false)

function openCreate() {
  form.value = emptyForm()
  nameError.value = ''
  contactError.value = ''
  phoneError.value = ''
  saveError.value = ''
  modalOpen.value = true
}

function openEdit(row: EnterpriseRow) {
  form.value = {
    id: row.id,
    name: row.name,
    contact: row.contact,
    phone: row.phone,
    plan: (row.plan as Plan) ?? '基础版',
    expireDate: row.expireDate,
  }
  nameError.value = ''
  contactError.value = ''
  phoneError.value = ''
  saveError.value = ''
  modalOpen.value = true
}

async function save() {
  const name = form.value.name.trim()
  const contact = form.value.contact.trim()
  const phone = form.value.phone.trim()
  nameError.value = ''
  contactError.value = ''
  phoneError.value = ''
  saveError.value = ''

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

  saving.value = true
  try {
    if (form.value.id != null) {
      const row = enterprises.value.find((item) => item.id === form.value.id)
      if (!row) throw new Error('未找到该企业')
      const draft: EnterpriseDraft = {
        enterpriseId: row.id,
        enterpriseName: name,
        shopId: row.code,
        applicant: contact,
        phone,
        expireTime: form.value.expireDate,
        status: row.statusValue,
      }
      if (isMockMode()) draft.plan = form.value.plan ?? '基础版'
      await store.update(draft)
    } else {
      const maxCode = enterprises.value.reduce(
        (max, item) => Math.max(max, Number(item.code) || 0),
        88010000,
      )
      const draft: EnterpriseDraft = {
        enterpriseId: '',
        enterpriseName: name,
        // 真实后端会生成商户号；mock 模式下本地生成演示编码
        shopId: isMockMode() ? String(maxCode + 1).padStart(8, '0') : '',
        applicant: contact,
        phone,
        expireTime: form.value.expireDate,
        status: 0,
      }
      if (isMockMode()) {
        draft.plan = form.value.plan ?? '基础版'
        draft.shopCount = 0
      }
      await store.create(draft)
    }
    modalOpen.value = false
    if (form.value.id == null) {
      // 后端生成 6 位随机初始密码并通过短信下发，接口不返回明文
      toast.success(`已开通「${name}」；初始密码已通过短信下发至 ${phone}`)
    }
    await store.search()
  } catch (error) {
    saveError.value = error instanceof Error ? error.message : '保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (!store.loaded) void store.load()
})
</script>

<template>
  <div class="xfo-toolbar">
    <div class="xfo-toolbar__search">
      <XfInput v-model="store.keyword" placeholder="搜索企业名称（服务端模糊）" />
    </div>
    <div class="xfo-toolbar__select">
      <XfSelect v-model="store.statusFilter" :options="statusOptions" />
    </div>
    <div class="xfo-toolbar__select">
      <XfSelect v-model="store.planFilter" :options="planFilterOptions" />
    </div>
    <XfButton variant="secondary" @click="store.search()">查询</XfButton>
    <XfButton variant="secondary" @click="store.resetFilters()">重置</XfButton>
    <XfButton variant="secondary" :disabled="store.loading" @click="store.refresh()">
      {{ store.loading ? '刷新中…' : '刷新' }}
    </XfButton>
    <div class="xfo-toolbar__grow"></div>
    <span class="xfo-muted">{{ isMockMode() ? '数据源：@xfcy/api mock' : '数据源：真实后端' }}</span>
    <XfButton @click="openCreate">＋ 开通企业</XfButton>
  </div>

  <XfPanel>
    <XfErrorState
      v-if="store.error && !store.accounts.length"
      :message="store.error"
      @retry="store.load"
    />
    <XfTable
      v-else
      :columns="columns"
      :data="rows"
      :loading="store.loading && !store.loaded"
      row-key="id"
      empty-text="暂无企业"
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
        <XfTag :tone="toneOf('account', (row as EnterpriseRow).status)">
          {{ labelOf('account', (row as EnterpriseRow).status) }}
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
        共 {{ stat.total }} 家<template v-if="!store.clientSide">
          · 当前页 试用 {{ stat.trial }} / 正式 {{ stat.official }} / 禁用 {{ stat.disabled }}
        </template>
      </span>
      <XfPagination v-model:page="store.page" :total="store.total" :page-size="pageSize" />
    </div>
  </XfPanel>

  <XfModal
    v-model:open="modalOpen"
    :title="form.id ? '编辑企业' : '开通企业'"
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
    <p v-if="saveError" class="xfo-note">{{ saveError }}</p>
    <template #footer>
      <XfButton variant="secondary" @click="modalOpen = false">取消</XfButton>
      <XfButton :disabled="saving" @click="save">{{ saving ? '保存中…' : '保存' }}</XfButton>
    </template>
  </XfModal>
</template>
