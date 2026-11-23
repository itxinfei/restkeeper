<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
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
  XfTabs,
  XfTag,
  labelOf,
  toneOf,
  useToast,
} from '@xfcy/ui'
import { credit, formatYuan } from '@xfcy/api'
import type { Credit, CreditLog, CreditPayload } from '@xfcy/api'

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

type CreditTab = 'subject' | 'log'

const tabs: Array<{ key: CreditTab; label: string }> = [
  { key: 'subject', label: '挂账主体' },
  { key: 'log', label: '挂账流水' },
]
const activeTab = ref<CreditTab>('subject')

/* ---------------- 挂账流水 ---------------- */

type CreditLogRow = {
  logId: string
  orderId: string
  userName: string
  type: number
  orderAmount: number
  receivedAmount: number
  creditAmount: number
  lastUpdateTime: string
}

const LOG_PAGE_SIZE = 10
const logSubjects = ref<Array<{ label: string; value: string }>>([])
const logCreditId = ref('')
const logStart = ref('')
const logEnd = ref('')
const logRows = ref<CreditLogRow[]>([])
const logTotal = ref(0)
const logPage = ref(1)
const logLoading = ref(false)
const logLoaded = ref(false)
const logError = ref('')
const exporting = ref(false)

const logColumns = [
  { key: 'orderId', label: '订单号' },
  { key: 'userName', label: '挂账人' },
  { key: 'type', label: '类型' },
  { key: 'orderAmount', label: '订单金额', align: 'right' as const },
  { key: 'receivedAmount', label: '应收', align: 'right' as const },
  { key: 'creditAmount', label: '挂账金额', align: 'right' as const },
  { key: 'lastUpdateTime', label: '时间' },
]

function toLogRow(item: CreditLog): CreditLogRow {
  return {
    logId: item.logId ?? '',
    orderId: item.orderId ?? '-',
    userName: item.userName ?? '-',
    type: item.type ?? 2,
    orderAmount: item.orderAmount ?? 0,
    receivedAmount: item.receivedAmount ?? 0,
    creditAmount: item.creditAmount ?? 0,
    lastUpdateTime: item.lastUpdateTime ?? '-',
  }
}

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

async function openLogTab() {
  if (logSubjects.value.length) return
  try {
    const result = await credit.page(1, 200)
    logSubjects.value = result.records
      .filter((item) => item.creditId)
      .map((item) => ({
        label:
          item.creditType === 1
            ? `${item.companyName ?? '-'}（公司）`
            : `${item.userName ?? '-'}（个人）`,
        value: item.creditId as string,
      }))
  } catch {
    logSubjects.value = []
  }
}

async function loadLogs() {
  if (!logCreditId.value) {
    logRows.value = []
    logTotal.value = 0
    logLoaded.value = true
    return
  }
  if (logLoading.value) return
  logLoading.value = true
  logError.value = ''
  try {
    const result = await credit.logPage(logPage.value, LOG_PAGE_SIZE, logCreditId.value)
    logRows.value = result.records.map(toLogRow)
    logTotal.value = result.total
    logLoaded.value = true
  } catch (err) {
    logError.value = err instanceof Error ? err.message : '挂账流水加载失败'
  } finally {
    logLoading.value = false
  }
}

function searchLogs() {
  logPage.value = 1
  void loadLogs()
}

async function exportLog() {
  if (!logCreditId.value) {
    toast.warning('请先选择挂账主体')
    return
  }
  exporting.value = true
  try {
    await credit.exportLogs(logCreditId.value, logStart.value, logEnd.value)
    toast.success('挂账流水已导出')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : '挂账流水导出失败')
  } finally {
    exporting.value = false
  }
}

watch(activeTab, (tab) => {
  if (tab === 'log') void openLogTab()
})
watch(logCreditId, () => {
  logPage.value = 1
  void loadLogs()
})
watch(logPage, () => void loadLogs())

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
  const amountFen = Math.round(amount * 100)
  // creditAmount 为剩余欠款额度：还款不得超额（与后端一致）
  if (amountFen > row.creditAmount) {
    repayError.value = `还款金额超过剩余欠款额度（剩余 ${formatYuan(row.creditAmount)} 元）`
    return
  }
  await credit.repayment({
    creditType: String(row.type),
    creditId: row.id,
    companyName: row.type === 1 ? row.name : undefined,
    userName: repayForm.value.userName.trim() || undefined,
    repaymentType: repayForm.value.repaymentType,
    repaymentAmount: amountFen,
  })
  repayOpen.value = false
  await load()
}

/* ---------------- 状态切换 ---------------- */

const toast = useToast()

const statusConfirmOpen = ref(false)
const statusRow = ref<CreditRow | null>(null)

function askToggleStatus(row: CreditRow) {
  statusRow.value = row
  statusConfirmOpen.value = true
}

async function confirmToggleStatus() {
  const row = statusRow.value
  if (!row) return
  const next = row.status === 1 ? 0 : 1
  try {
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
  <XfPageHead title="挂账管理" description="挂账主体、还款与流水">
    <template #actions>
      <XfButton v-if="activeTab === 'subject'" @click="openCreate">＋ 新增挂账</XfButton>
      <XfButton v-else :disabled="exporting" @click="exportLog">
        {{ exporting ? '导出中…' : '导出流水' }}
      </XfButton>
    </template>
  </XfPageHead>

  <div class="xfm-tabs">
    <XfTabs v-model="activeTab" :tabs="tabs" />
  </div>

  <template v-if="activeTab === 'subject'">
  <div class="xfm-toolbar">
    <div class="xfm-toolbar__search">
      <XfInput v-model="keyword" placeholder="公司 / 姓名" />
    </div>
    <div class="xfm-toolbar__select">
      <XfSelect v-model="typeFilter" :options="typeOptions" />
    </div>
    <XfButton variant="secondary" @click="search">查询</XfButton>
    <div class="xfm-toolbar__grow"></div>
  </div>

  <XfPanel>
    <XfErrorState v-if="error && !rows.length" :message="error" @retry="load" />
    <XfTable v-else :columns="columns" :data="rows" :loading="loading && !loaded" row-key="id">
      <template #type="{ row }">
        <XfTag :tone="toneOf('creditType', row.type as string | number | undefined)">
          {{ labelOf('creditType', row.type as string | number | undefined) }}
        </XfTag>
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
        <XfTag :tone="toneOf('credit', row.status as string | number | undefined)">
          {{ labelOf('credit', row.status as string | number | undefined) }}
        </XfTag>
      </template>
      <template #actions="{ row }">
        <div class="xfm-actions">
          <XfButton size="sm" variant="secondary" @click="openEdit(row as CreditRow)">编辑</XfButton>
          <XfButton size="sm" variant="secondary" @click="openRepay(row as CreditRow)">还款</XfButton>
          <XfButton size="sm" :variant="row.status === 1 ? 'danger' : 'secondary'" @click="askToggleStatus(row as CreditRow)">
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
  </template>

  <!-- ============ 挂账流水 ============ -->
  <template v-else>
    <div class="xfm-toolbar">
      <div class="xfm-toolbar__select xfm-toolbar__select--wide">
        <XfSelect v-model="logCreditId" :options="logSubjects" placeholder="选择挂账主体" />
      </div>
      <XfField label="开始日期">
        <template #default>
          <input v-model="logStart" class="xfm-date" type="date" />
        </template>
      </XfField>
      <XfField label="结束日期">
        <template #default>
          <input v-model="logEnd" class="xfm-date" type="date" />
        </template>
      </XfField>
      <XfButton variant="secondary" @click="searchLogs">查询</XfButton>
      <div class="xfm-toolbar__grow"></div>
    </div>

    <XfPanel>
      <XfErrorState v-if="logError && !logRows.length" :message="logError" @retry="loadLogs" />
      <XfTable
        v-else
        :columns="logColumns"
        :data="logRows"
        :loading="logLoading && !logLoaded"
        row-key="logId"
      >
        <template #type="{ row }">
          <XfTag :tone="toneOf('creditType', row.type as string | number | undefined)">
            {{ labelOf('creditType', row.type as string | number | undefined) }}
          </XfTag>
        </template>
        <template #orderAmount="{ row }">
          <span class="xfm-num">{{ formatYuan(Number(row.orderAmount)) }}</span>
        </template>
        <template #receivedAmount="{ row }">
          <span class="xfm-num">{{ formatYuan(Number(row.receivedAmount)) }}</span>
        </template>
        <template #creditAmount="{ row }">
          <span class="xfm-num">{{ formatYuan(Number(row.creditAmount)) }}</span>
        </template>
      </XfTable>

      <div v-if="logLoaded" class="xfm-pager">
        <span class="xfm-pager__summary">共 {{ logTotal }} 条流水</span>
        <XfPagination v-model:page="logPage" :total="logTotal" :page-size="LOG_PAGE_SIZE" />
      </div>
    </XfPanel>
  </template>

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
        <span>剩余欠款额度</span>
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

  <!-- 停用 / 启用二次确认 -->
  <XfModal v-model:open="statusConfirmOpen" title="状态确认" width="440px">
    <p v-if="statusRow" class="xfm-confirm">
      <template v-if="statusRow.status === 1">
        确认停用「{{ statusRow.name }}」？停用后该主体不允许新挂账，已有挂账与还款记录保留。
      </template>
      <template v-else> 确认启用「{{ statusRow.name }}」？启用后恢复该主体的挂账资格。 </template>
    </p>
    <template #footer>
      <XfButton variant="secondary" @click="statusConfirmOpen = false">取消</XfButton>
      <XfButton :variant="statusRow?.status === 1 ? 'danger' : undefined" @click="confirmToggleStatus">
        确认{{ statusRow?.status === 1 ? '停用' : '启用' }}
      </XfButton>
    </template>
  </XfModal>

</template>
