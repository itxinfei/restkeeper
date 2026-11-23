<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
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
  useToast,
} from '@xfcy/ui'
import { credit, formatYuan } from '@xfcy/api'
import type { Credit, CreditPayload } from '@xfcy/api'

const PAGE_SIZE = 10

type CreditRow = {
  id: string
  type: number
  name: string
  contact: string
  phone: string
  creditAmount: number
  totalRepayment: number
  status: number
  raw?: Credit
}

type CreditForm = {
  creditType: number
  companyName: string
  userName: string
  phone: string
  /** 挂账额度（元，输入用） */
  amountYuan: string
  status: number
  users: Array<{ userName: string; phone: string }>
}

const rows = ref<CreditRow[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const loaded = ref(false)
const error = ref('')
const keyword = ref('')
const typeFilter = ref<'all' | '1' | '2'>('all')

const typeOptions = [
  { label: '类型：全部', value: 'all' },
  { label: '公司', value: '1' },
  { label: '个人', value: '2' },
]

const repayTypeOptions = [
  { label: '现金', value: '现金' },
  { label: '微信', value: '微信' },
  { label: '支付宝', value: '支付宝' },
  { label: '银行卡', value: '银行卡' },
]

const columns = [
  { key: 'type', label: '类型' },
  { key: 'name', label: '名称' },
  { key: 'contact', label: '联系人' },
  { key: 'creditAmount', label: '挂账额度', align: 'right' as const },
  { key: 'totalRepayment', label: '已还', align: 'right' as const },
  { key: 'status', label: '状态' },
  { key: 'actions', label: '操作', align: 'right' as const },
]

function toRow(item: Credit): CreditRow {
  return {
    id: item.creditId ?? '',
    type: item.creditType ?? 2,
    name: item.creditType === 1 ? item.companyName ?? '-' : item.userName ?? '-',
    contact: `${item.userName ?? '-'} · ${item.phone ?? '-'}`,
    phone: item.phone ?? '',
    creditAmount: item.creditAmount ?? 0,
    totalRepayment: item.totalRepaymentAmount ?? 0,
    status: item.status ?? 1,
    raw: item,
  }
}

async function load() {
  if (loading.value) return
  loading.value = true
  error.value = ''
  try {
    const result = await credit.page(page.value, PAGE_SIZE, keyword.value.trim() || undefined)
    const all = result.records.map(toRow)
    rows.value =
      typeFilter.value === 'all' ? all : all.filter((row) => String(row.type) === typeFilter.value)
    total.value = result.total
    loaded.value = true
  } catch (err) {
    error.value = err instanceof Error ? err.message : '挂账列表加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(page, () => void load())
watch(typeFilter, () => {
  page.value = 1
  void load()
})

function search() {
  page.value = 1
  void load()
}

/* ---------------- 新增 / 编辑 ---------------- */

const emptyForm = (): CreditForm => ({
  creditType: 1,
  companyName: '',
  userName: '',
  phone: '',
  amountYuan: '',
  status: 1,
  users: [],
})

const modalOpen = ref(false)
const editingId = ref<string | null>(null)
const form = ref<CreditForm>(emptyForm())
const errors = ref({ userName: '', amount: '' })

const isCompany = computed(() => form.value.creditType === 1)

function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  errors.value = { userName: '', amount: '' }
  modalOpen.value = true
}

async function openEdit(row: CreditRow) {
  editingId.value = row.id
  form.value = emptyForm()
  errors.value = { userName: '', amount: '' }
  modalOpen.value = true
  const detail = await credit.getById(row.id)
  form.value = {
    creditType: detail.creditType,
    companyName: detail.companyName ?? '',
    userName: detail.userName ?? '',
    phone: detail.phone ?? '',
    amountYuan: detail.creditAmount ? (detail.creditAmount / 100).toString() : '',
    status: detail.status ?? 1,
    users: (detail.users ?? []).map((user) => ({
      userName: user.userName ?? '',
      phone: user.phone ?? '',
    })),
  }
}

async function save() {
  const userName = form.value.userName.trim()
  const amount = Number(form.value.amountYuan)
  errors.value = { userName: '', amount: '' }
  if (!userName) errors.value.userName = '请填写挂账人姓名'
  if (!form.value.amountYuan || !Number.isFinite(amount) || amount < 0) {
    errors.value.amount = '请填写有效的挂账额度'
  }
  if (errors.value.userName || errors.value.amount) return

  const payload: CreditPayload = {
    creditType: form.value.creditType,
    companyName: isCompany.value ? form.value.companyName.trim() : undefined,
    userName,
    phone: form.value.phone.trim() || undefined,
    creditAmount: Math.round(amount * 100),
    status: form.value.status,
    users: isCompany.value
      ? form.value.users
          .filter((user) => user.userName.trim())
          .map((user) => ({ userName: user.userName.trim(), phone: user.phone.trim() || undefined }))
      : [],
  }
  if (editingId.value) payload.id = editingId.value

  if (editingId.value) await credit.update(payload)
  else await credit.add(payload)
  modalOpen.value = false
  await load()
}

/* ---------------- 还款 ---------------- */

const repayOpen = ref(false)
const repayRow = ref<CreditRow | null>(null)
const repayForm = ref({ userName: '', repaymentType: '现金', amountYuan: '' })
const repayError = ref('')

function openRepay(row: CreditRow) {
  repayRow.value = row
  repayForm.value = { userName: row.raw?.userName ?? '', repaymentType: '现金', amountYuan: '' }
  repayError.value = ''
  repayOpen.value = true
}

async function saveRepay() {
  const row = repayRow.value
  if (!row) return
  const amount = Number(repayForm.value.amountYuan)
  if (!Number.isFinite(amount) || amount <= 0) {
    repayError.value = '请填写有效的还款金额'
    return
  }
  await credit.repayment({
    creditType: String(row.type),
    creditId: row.id,
    companyName: row.type === 1 ? row.name : undefined,
    userName: repayForm.value.userName.trim() || undefined,
    repaymentType: repayForm.value.repaymentType,
    repaymentAmount: Math.round(amount * 100),
  })
  repayOpen.value = false
  await load()
}

/* ---------------- 状态切换 ---------------- */

const toast = useToast()

async function toggleStatus(row: CreditRow) {
  const next = row.status === 1 ? 0 : 1
  await credit.update({
    id: row.id,
    creditType: row.type,
    companyName: row.type === 1 ? row.raw?.companyName : undefined,
    userName: row.raw?.userName,
    phone: row.raw?.phone,
    creditAmount: row.creditAmount,
    status: next,
    users: (row.raw?.users ?? []).map((user) => ({ userName: user.userName, phone: user.phone })),
  })
  toast.success(next === 1 ? `已启用「${row.name}」` : `已停用「${row.name}」`)
  await load()
}
</script>

<template>
  <div class="xfm-toolbar">
    <div class="xfm-toolbar__search">
      <XfInput v-model="keyword" placeholder="公司 / 姓名" />
    </div>
    <div class="xfm-toolbar__select">
      <XfSelect v-model="typeFilter" :options="typeOptions" />
    </div>
    <XfButton variant="secondary" @click="search">查询</XfButton>
    <div class="xfm-toolbar__grow"></div>
    <XfButton @click="openCreate">＋ 新增挂账</XfButton>
  </div>

  <XfPanel>
    <XfErrorState v-if="error && !rows.length" :message="error" @retry="load" />
    <XfTable v-else :columns="columns" :data="rows" :loading="loading && !loaded" row-key="id">
      <template #type="{ row }">
        <XfTag :tone="row.type === 1 ? 'primary' : 'info'">{{ row.type === 1 ? '公司' : '个人' }}</XfTag>
      </template>
      <template #contact="{ row }">
        <span class="xfm-muted">{{ row.contact }}</span>
      </template>
      <template #creditAmount="{ row }">
        <span class="xfm-num">{{ formatYuan(Number(row.creditAmount)) }}</span>
      </template>
      <template #totalRepayment="{ row }">
        <span class="xfm-num">{{ formatYuan(Number(row.totalRepayment)) }}</span>
      </template>
      <template #status="{ row }">
        <XfTag :tone="row.status === 1 ? 'success' : 'neutral'">
          {{ row.status === 1 ? '启用' : '停用' }}
        </XfTag>
      </template>
      <template #actions="{ row }">
        <div class="xfm-actions">
          <XfButton size="sm" variant="secondary" @click="openEdit(row as CreditRow)">编辑</XfButton>
          <XfButton size="sm" variant="secondary" @click="openRepay(row as CreditRow)">还款</XfButton>
          <XfButton size="sm" :variant="row.status === 1 ? 'danger' : 'secondary'" @click="toggleStatus(row as CreditRow)">
            {{ row.status === 1 ? '停用' : '启用' }}
          </XfButton>
        </div>
      </template>
    </XfTable>

    <div v-if="loaded" class="xfm-pager">
      <span class="xfm-pager__summary">共 {{ total }} 个挂账主体</span>
      <XfPagination v-model:page="page" :total="total" :page-size="PAGE_SIZE" />
    </div>
  </XfPanel>

  <!-- 新增 / 编辑挂账 -->
  <XfModal v-model:open="modalOpen" :title="editingId ? '编辑挂账' : '新增挂账'" width="640px">
    <div class="xfm-form">
      <div class="xfm-form--grid">
        <XfField label="挂账类型" required>
          <template #default="{ id }">
            <XfSelect
              :id="id"
              :model-value="String(form.creditType)"
              :options="[
                { label: '公司', value: '1' },
                { label: '个人', value: '2' },
              ]"
              @update:model-value="form.creditType = Number($event)"
            />
          </template>
        </XfField>
        <XfField label="状态">
          <template #default="{ id }">
            <XfSelect
              :id="id"
              :model-value="String(form.status)"
              :options="[
                { label: '启用', value: '1' },
                { label: '停用', value: '0' },
              ]"
              @update:model-value="form.status = Number($event)"
            />
          </template>
        </XfField>
      </div>

      <XfField v-if="isCompany" label="公司名称" required>
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.companyName" placeholder="如 心飞科技有限公司" />
        </template>
      </XfField>

      <div class="xfm-form--grid">
        <XfField label="挂账人姓名" required :error="errors.userName || ''">
          <template #default="{ id }">
            <XfInput :id="id" v-model="form.userName" placeholder="如 陈国栋" />
          </template>
        </XfField>
        <XfField label="联系电话">
          <template #default="{ id }">
            <XfInput :id="id" v-model="form.phone" placeholder="如 13901001101" />
          </template>
        </XfField>
      </div>

      <XfField label="挂账额度（元）" required :error="errors.amount || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.amountYuan" placeholder="如 5000" />
        </template>
      </XfField>

      <XfField v-if="isCompany" label="关联用餐人" hint="挂账结账时可选择这些用餐人">
        <template #default>
          <div class="xfm-users">
            <div v-for="(user, index) in form.users" :key="index" class="xfm-users__row">
              <XfInput v-model="user.userName" placeholder="姓名" />
              <XfInput v-model="user.phone" placeholder="手机号" />
              <XfButton size="sm" variant="danger" @click="form.users.splice(index, 1)">删除</XfButton>
            </div>
            <XfButton size="sm" variant="secondary" @click="form.users.push({ userName: '', phone: '' })">
              ＋ 添加用餐人
            </XfButton>
          </div>
        </template>
      </XfField>
    </div>
    <template #footer>
      <XfButton variant="secondary" @click="modalOpen = false">取消</XfButton>
      <XfButton @click="save">保存</XfButton>
    </template>
  </XfModal>

  <!-- 还款 -->
  <XfModal v-model:open="repayOpen" title="挂账还款">
    <div v-if="repayRow" class="xfm-form">
      <div class="xfm-detail__line">
        <span>挂账主体</span>
        <b>{{ repayRow.name }}</b>
      </div>
      <div class="xfm-detail__line">
        <span>挂账额度</span>
        <b class="xfm-num">{{ formatYuan(repayRow.creditAmount) }}</b>
      </div>
      <div class="xfm-detail__line">
        <span>已还</span>
        <b class="xfm-num">{{ formatYuan(repayRow.totalRepayment) }}</b>
      </div>
      <XfField label="还款人">
        <template #default="{ id }">
          <XfInput :id="id" v-model="repayForm.userName" placeholder="如 陈国栋" />
        </template>
      </XfField>
      <div class="xfm-form--grid">
        <XfField label="还款方式" required>
          <template #default="{ id }">
            <XfSelect :id="id" v-model="repayForm.repaymentType" :options="repayTypeOptions" />
          </template>
        </XfField>
        <XfField label="还款金额（元）" required :error="repayError || ''">
          <template #default="{ id }">
            <XfInput :id="id" v-model="repayForm.amountYuan" placeholder="如 1000" />
          </template>
        </XfField>
      </div>
    </div>
    <template #footer>
      <XfButton variant="secondary" @click="repayOpen = false">取消</XfButton>
      <XfButton @click="saveRepay">确认还款</XfButton>
    </template>
  </XfModal>

</template>
