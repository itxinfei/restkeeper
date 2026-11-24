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
  XfTag,
  labelOf,
  remainLabel,
  toneOf,
  useToast,
} from '@xfcy/ui'
import { dish, soldout } from '@xfcy/api'
import type { SellCalculation } from '@xfcy/api'

const PAGE_SIZE = 10

type SoldoutRow = {
  id: string
  dishId: string
  name: string
  type: number
  limit: number
  /** null 表示不限量 */
  remainder: number | null
}

type DishOption = { id: string; name: string; type: number }

const rows = ref<SoldoutRow[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const loaded = ref(false)
const error = ref('')
const keyword = ref('')
const typeFilter = ref<'all' | '1' | '2'>('all')
const dishes = ref<DishOption[]>([])

const typeOptions = [
  { label: '类型：全部', value: 'all' },
  { label: '普通菜品', value: '1' },
  { label: '套餐', value: '2' },
]

const columns = [
  { key: 'name', label: '菜品名称', width: '220px', wrap: true },
  { key: 'type', label: '类型', width: '100px' },
  { key: 'limit', label: '沽清总量', align: 'right' as const, width: '120px' },
  { key: 'remainder', label: '剩余', align: 'right' as const, width: '110px' },
  { key: 'actions', label: '操作', align: 'right' as const, width: '190px' },
]

function toRow(item: SellCalculation): SoldoutRow {
  const remainder = item.remainder
  return {
    id: item.id ?? '',
    dishId: item.dishId ?? '',
    name: item.dishName ?? '-',
    type: item.dishType ?? 1,
    limit: item.sellLimitTotal ?? 0,
    remainder: remainder === undefined || remainder < 0 ? null : remainder,
  }
}

async function loadDishes() {
  const result = await dish.page(1, 200)
  dishes.value = result.records.map((item) => ({
    id: item.id ?? '',
    name: item.name,
    type: item.dishCategory?.type === 2 ? 2 : 1,
  }))
}

async function load() {
  if (loading.value) return
  loading.value = true
  error.value = ''
  try {
    const result = await soldout.page(
      page.value,
      PAGE_SIZE,
      keyword.value.trim() || undefined,
      typeFilter.value === 'all' ? undefined : Number(typeFilter.value),
    )
    rows.value = result.records.map(toRow)
    total.value = result.total
    loaded.value = true
  } catch (err) {
    error.value = err instanceof Error ? err.message : '沽清列表加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadDishes()
  await load()
})
watch(page, () => void load())
watch(typeFilter, () => {
  page.value = 1
  void load()
})

function search() {
  page.value = 1
  void load()
}

/* ---------------- 新增沽清 ---------------- */

const modalOpen = ref(false)
const form = ref({ dishId: '', limit: '' })
const errors = ref({ dishId: '', limit: '' })

const dishOptions = computed(() =>
  dishes.value.map((item) => ({ label: item.name, value: item.id })),
)

function openCreate() {
  form.value = { dishId: '', limit: '' }
  errors.value = { dishId: '', limit: '' }
  modalOpen.value = true
}

async function save() {
  const limit = Number.parseInt(form.value.limit, 10)
  errors.value = { dishId: '', limit: '' }
  if (!form.value.dishId) errors.value.dishId = '请选择菜品'
  if (!Number.isFinite(limit) || limit <= 0) errors.value.limit = '请填写有效的沽清总量'
  if (errors.value.dishId || errors.value.limit) return

  const selected = dishes.value.find((item) => item.id === form.value.dishId)
  await soldout.save({
    dishId: form.value.dishId,
    dishName: selected?.name,
    dishType: selected?.type ?? 1,
    sellLimitTotal: limit,
    remainder: limit,
  })
  modalOpen.value = false
  await load()
}

/* ---------------- 数量调整 / 删除 ---------------- */

const toast = useToast()

function notify(text: string, tone: 'info' | 'success' | 'warning' = 'info') {
  if (tone === 'success') toast.success(text)
  else if (tone === 'warning') toast.warning(text)
  else toast.info(text)
}

async function change(row: SoldoutRow, delta: number) {
  if (row.remainder === null) {
    notify('该菜品未设置沽清数量，可先删除后重新添加')
    return
  }
  if (delta > 0) await soldout.increase(row.dishId, 1)
  else await soldout.decrease(row.dishId, 1)
  await load()
}

async function remove(row: SoldoutRow) {
  await soldout.remove([row.id])
  notify(`已删除「${row.name}」的沽清设置`, 'success')
  await load()
}
</script>

<template>
  <XfPageHead title="沽清管理" description="菜品 / 套餐限量与余量">
    <template #actions>
      <XfButton @click="openCreate">＋ 新增沽清</XfButton>
    </template>
  </XfPageHead>

  <div class="xf-toolbar">
    <div class="xf-toolbar__search">
      <XfInput v-model="keyword" placeholder="菜品名称" />
    </div>
    <div class="xf-toolbar__select">
      <XfSelect v-model="typeFilter" :options="typeOptions" />
    </div>
    <XfButton variant="secondary" @click="search">查询</XfButton>
    <div class="xf-toolbar__grow"></div>
  </div>

  <XfPanel>
    <XfErrorState v-if="error && !rows.length" :message="error" @retry="load" />
    <XfTable v-else :columns="columns" :data="rows" :loading="loading && !loaded" row-key="id">
      <template #type="{ row }">
        <XfTag :tone="toneOf('itemType', row.type as string | number | undefined)">
          {{ labelOf('itemType', row.type as string | number | undefined) }}
        </XfTag>
      </template>
      <template #limit="{ row }">
        <span class="xf-num">{{ row.limit }}</span>
      </template>
      <template #remainder="{ row }">
        <span class="xf-num" :class="{ 'xfm-tag-text': row.remainder === null }">
          {{ remainLabel(row.remainder as number | null | undefined) }}
        </span>
      </template>
      <template #actions="{ row }">
        <div class="xfm-actions">
          <XfButton
            size="sm"
            variant="secondary"
            :disabled="row.remainder === null || row.remainder === 0"
            :title="row.remainder === null ? '未设置沽清数量' : '余量已封底为 0'"
            @click="change(row as SoldoutRow, -1)"
          >
            −1
          </XfButton>
          <XfButton
            size="sm"
            variant="secondary"
            :disabled="row.remainder === null"
            :title="row.remainder === null ? '未设置沽清数量' : undefined"
            @click="change(row as SoldoutRow, 1)"
          >
            ＋1
          </XfButton>
          <XfButton size="sm" variant="danger" @click="remove(row as SoldoutRow)">删除</XfButton>
        </div>
      </template>
    </XfTable>

    <div v-if="loaded" class="xfm-pager">
      <span class="xfm-pager__summary">共 {{ total }} 条沽清</span>
      <XfPagination v-model:page="page" :total="total" :page-size="PAGE_SIZE" />
    </div>
  </XfPanel>

  <!-- 新增沽清 -->
  <XfModal v-model:open="modalOpen" title="新增沽清">
    <div class="xfm-form">
      <XfField label="菜品" required :error="errors.dishId || ''">
        <template #default="{ id }">
          <XfSelect :id="id" v-model="form.dishId" :options="dishOptions" placeholder="请选择菜品" />
        </template>
      </XfField>
      <XfField label="沽清总量" required :error="errors.limit || ''" hint="售出后剩余数量会相应扣减">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.limit" placeholder="如 20" />
        </template>
      </XfField>
    </div>
    <template #footer>
      <XfButton variant="secondary" @click="modalOpen = false">取消</XfButton>
      <XfButton @click="save">保存</XfButton>
    </template>
  </XfModal>

</template>
