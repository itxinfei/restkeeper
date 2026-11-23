<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  XfButton,
  XfEmpty,
  XfErrorState,
  XfField,
  XfInput,
  XfModal,
  XfPageHead,
  XfPanel,
  XfSelect,
  XfTabs,
  XfTag,
  labelOf,
  toneOf,
} from '@xfcy/ui'
import { table } from '@xfcy/api'
import type { TableArea } from '@xfcy/api'

type AreaRow = {
  areaId: string
  areaName: string
  indexNumber?: number
  tableCount?: number
}

type TableStatus = '空闲' | '开台' | '锁定'

type TableRow = {
  id: string
  name: string
  areaId: string
  areaName: string
  seats: number
  status: TableStatus
  statusValue?: number
}

const STATUS_TEXT: Record<number, TableStatus> = { 0: '空闲', 1: '开台', 2: '锁定' }

const areas = ref<AreaRow[]>([])
const tables = ref<TableRow[]>([])
const loading = ref(false)
const loaded = ref(false)
const error = ref('')

const activeAreaId = ref<string>('all')
const statusFilter = ref<'all' | 'free' | 'open' | 'locked'>('all')

const statusOptions = [
  { label: '状态：全部', value: 'all' },
  { label: '空闲', value: 'free' },
  { label: '开台', value: 'open' },
  { label: '锁定', value: 'locked' },
]

const areaTabs = computed<Array<{ key: string; label: string }>>(() => [
  { key: 'all', label: '全部区域' },
  ...areas.value.map((area) => ({
    key: area.areaId,
    label: `${area.areaName}（${area.tableCount ?? 0}）`,
  })),
])

const filteredTables = computed(() => {
  const area = activeAreaId.value
  const status = statusFilter.value
  return tables.value.filter((row) => {
    if (area !== 'all' && row.areaId !== area) return false
    if (status === 'free' && row.statusValue !== 0) return false
    if (status === 'open' && row.statusValue !== 1) return false
    if (status === 'locked' && row.statusValue !== 2) return false
    return true
  })
})

const stat = computed(() => ({
  total: tables.value.length,
  free: tables.value.filter((row) => row.statusValue === 0).length,
  open: tables.value.filter((row) => row.statusValue === 1).length,
  locked: tables.value.filter((row) => row.statusValue === 2).length,
}))

function toRow(item: {
  tableId?: string
  areaId?: string
  tableName: string
  tableSeatNumber?: number
  status?: number
}): TableRow {
  const area = areas.value.find((row) => row.areaId === item.areaId)
  return {
    id: item.tableId ?? '',
    name: item.tableName,
    areaId: item.areaId ?? '',
    areaName: area?.areaName ?? '未分区',
    seats: item.tableSeatNumber ?? 0,
    status: STATUS_TEXT[item.status ?? 0] ?? '空闲',
    statusValue: item.status ?? 0,
  }
}

async function load() {
  if (loading.value) return
  loading.value = true
  error.value = ''
  try {
    const areaRows = (await table.areaList()) as unknown as AreaRow[]
    areas.value = areaRows
    if (activeAreaId.value !== 'all' && !areaRows.some((row) => row.areaId === activeAreaId.value)) {
      activeAreaId.value = 'all'
    }
    const pages = await Promise.all(
      areaRows.map((area) => table.pageByArea(area.areaId, 1, 200)),
    )
    tables.value = pages.flatMap((page) => page.records.map(toRow))
    loaded.value = true
  } catch (err) {
    error.value = err instanceof Error ? err.message : '桌台数据加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)

/* ---------------- 开台 / 详情 ---------------- */

const detailOpen = ref(false)
const detailRow = ref<TableRow | null>(null)
const openPeople = ref('2')
const openError = ref('')

function openDetail(row: TableRow) {
  detailRow.value = row
  openPeople.value = String(row.seats || 2)
  openError.value = ''
  detailOpen.value = true
}

async function confirmOpen() {
  const row = detailRow.value
  if (!row) return
  const people = Number.parseInt(openPeople.value, 10)
  if (!Number.isFinite(people) || people <= 0) {
    openError.value = '请填写有效的就餐人数'
    return
  }
  openError.value = ''
  await table.openTable(row.id, people)
  detailOpen.value = false
  await load()
}

/* ---------------- 新增 / 编辑桌台 ---------------- */

type TableForm = {
  id: string | null
  name: string
  areaId: string | null
  seats: string
}

const tableModalOpen = ref(false)
const tableForm = ref<TableForm>({ id: null, name: '', areaId: null, seats: '' })
const tableNameError = ref('')
const tableSeatsError = ref('')

const areaOptions = computed(() =>
  areas.value.map((area) => ({ label: area.areaName, value: area.areaId })),
)

function openTableModal() {
  tableForm.value = {
    id: null,
    name: '',
    areaId: activeAreaId.value !== 'all' ? activeAreaId.value : (areas.value[0]?.areaId ?? null),
    seats: '4',
  }
  tableNameError.value = ''
  tableSeatsError.value = ''
  tableModalOpen.value = true
}

function editTable(row: TableRow) {
  detailOpen.value = false
  tableForm.value = { id: row.id, name: row.name, areaId: row.areaId, seats: String(row.seats) }
  tableNameError.value = ''
  tableSeatsError.value = ''
  tableModalOpen.value = true
}

async function saveTable() {
  const name = tableForm.value.name.trim()
  const seats = Number.parseInt(tableForm.value.seats, 10)
  tableNameError.value = ''
  tableSeatsError.value = ''
  if (!name) {
    tableNameError.value = '请填写桌台名称'
    return
  }
  if (!Number.isFinite(seats) || seats <= 0) {
    tableSeatsError.value = '请填写有效的座位数'
    return
  }
  const areaId = tableForm.value.areaId ?? areas.value[0]?.areaId ?? ''
  if (tableForm.value.id) {
    await table.update({
      tableId: tableForm.value.id,
      areaId,
      tableName: name,
      tableSeatNumber: seats,
      status: 0,
    })
  } else {
    await table.add({ areaId, tableName: name, tableSeatNumber: seats, status: 0 })
  }
  tableModalOpen.value = false
  await load()
}

/* ---------------- 新增区域 ---------------- */

const areaModalOpen = ref(false)
const areaName = ref('')
const areaNameError = ref('')

function openAreaModal() {
  areaName.value = ''
  areaNameError.value = ''
  areaModalOpen.value = true
}

async function saveArea() {
  const name = areaName.value.trim()
  if (!name) {
    areaNameError.value = '请填写区域名称'
    return
  }
  const payload: TableArea = { areaName: name, indexNumber: areas.value.length + 1 }
  await table.areaAdd(payload)
  areaModalOpen.value = false
  await load()
}

</script>

<template>
  <XfPageHead title="桌台管理" description="区域、桌台与状态总览">
    <template #actions>
      <XfButton variant="secondary" @click="openAreaModal">＋ 新增区域</XfButton>
      <XfButton @click="openTableModal">＋ 新增桌台</XfButton>
    </template>
  </XfPageHead>

  <div class="xfm-toolbar">
    <div class="xfm-toolbar__select">
      <XfSelect v-model="statusFilter" :options="statusOptions" />
    </div>
    <span class="xfm-muted">
      共 {{ stat.total }} 桌 · 空闲 {{ stat.free }} · 开台 {{ stat.open }} · 锁定 {{ stat.locked }}
    </span>
    <div class="xfm-toolbar__grow"></div>
  </div>

  <XfTabs v-model="activeAreaId" :tabs="areaTabs" />

  <XfPanel>
    <XfErrorState v-if="error && !tables.length" :message="error" @retry="load" />
    <div v-else-if="loading && !loaded" class="xfm-muted">加载中…</div>
    <div v-else-if="filteredTables.length === 0" class="xfm-tableempty">
      <XfEmpty title="暂无桌台" description="点击右上角「新增桌台」创建，或「新增区域」先建分区" />
    </div>
    <div v-else class="xfm-tablegrid">
      <button
        v-for="row in filteredTables"
        :key="row.id"
        type="button"
        class="xfm-tablecard"
        :class="`is-${row.statusValue}`"
        @click="openDetail(row)"
      >
        <div class="xfm-tablecard__name">{{ row.name }}</div>
        <div class="xfm-tablecard__meta">{{ row.areaName }} · {{ row.seats }} 座</div>
        <XfTag :tone="toneOf('table', row.status)">{{ labelOf('table', row.status) }}</XfTag>
      </button>
    </div>
  </XfPanel>

  <!-- 桌台详情 -->
  <XfModal v-model:open="detailOpen" title="桌台详情">
    <div v-if="detailRow" class="xfm-detail">
      <div class="xfm-detail__name">{{ detailRow.name }}</div>
      <div class="xfm-detail__line"><span>区域</span><b>{{ detailRow.areaName }}</b></div>
      <div class="xfm-detail__line"><span>座位数</span><b class="xfm-num">{{ detailRow.seats }}</b></div>
      <div class="xfm-detail__line">
        <span>状态</span>
        <XfTag :tone="toneOf('table', detailRow.status)">{{ labelOf('table', detailRow.status) }}</XfTag>
      </div>
    </div>

    <template v-if="detailRow?.statusValue === 0">
      <div class="xfm-form" style="margin-top: 16px">
        <XfField label="就餐人数" required :error="openError || ''">
          <template #default="{ id }">
            <XfInput :id="id" v-model="openPeople" placeholder="如 4" />
          </template>
        </XfField>
      </div>
    </template>

    <template #footer>
      <XfButton variant="secondary" @click="detailOpen = false">关闭</XfButton>
      <XfButton v-if="detailRow?.statusValue === 0" @click="confirmOpen">开台</XfButton>
      <XfButton v-else variant="secondary" @click="editTable(detailRow as TableRow)">编辑桌台</XfButton>
    </template>
  </XfModal>

  <!-- 新增 / 编辑桌台 -->
  <XfModal v-model:open="tableModalOpen" :title="tableForm.id ? '编辑桌台' : '新增桌台'">
    <div class="xfm-form">
      <XfField label="桌台名称" required :error="tableNameError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="tableForm.name" placeholder="如 A06 大厅" />
        </template>
      </XfField>
      <XfField label="所属区域" required>
        <template #default="{ id }">
          <XfSelect
            :id="id"
            v-model="tableForm.areaId"
            :options="areaOptions"
            placeholder="请选择区域"
          />
        </template>
      </XfField>
      <XfField label="座位数" required :error="tableSeatsError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="tableForm.seats" placeholder="如 4" />
        </template>
      </XfField>
    </div>
    <template #footer>
      <XfButton variant="secondary" @click="tableModalOpen = false">取消</XfButton>
      <XfButton @click="saveTable">保存</XfButton>
    </template>
  </XfModal>

  <!-- 新增区域 -->
  <XfModal v-model:open="areaModalOpen" title="新增区域">
    <div class="xfm-form">
      <XfField label="区域名称" required :error="areaNameError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="areaName" placeholder="如 露台" />
        </template>
      </XfField>
    </div>
    <template #footer>
      <XfButton variant="secondary" @click="areaModalOpen = false">取消</XfButton>
      <XfButton @click="saveArea">保存</XfButton>
    </template>
  </XfModal>

</template>
