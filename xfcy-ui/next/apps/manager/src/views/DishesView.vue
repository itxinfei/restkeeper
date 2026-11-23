<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  XfButton,
  XfEmpty,
  XfErrorState,
  XfField,
  XfInput,
  XfModal,
  XfPagination,
  XfPanel,
  XfSelect,
  XfTable,
  XfTabs,
  XfTag,
  useToast,
} from '@xfcy/ui'
import { dish as dishApi, formatYuan } from '@xfcy/api'
import type {
  Dish as ApiDish,
  DishCategory as ApiDishCategory,
  DishWithFlavors,
  SetMeal as ApiSetMeal,
} from '@xfcy/api'

type TabKey = 'dish' | 'combo' | 'category' | 'flavor'

type Column = {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  width?: string
}

type DishStatus = '起售' | '停售'
type DishRow = {
  id: string
  name: string
  desc: string
  category: string
  code: string
  price: string
  remain: string
  remainDanger: boolean
  status: DishStatus
  thumb: string
  flavor?: string
}

type CategoryStatus = '启用' | '停用'
type CategoryRow = {
  id: string
  name: string
  sort: number
  dishCount: number
  status: CategoryStatus
}

type ComboStatus = '起售' | '停售'
type ComboRow = {
  id: string
  name: string
  code: string
  price: string
  dishCount: number
  status: ComboStatus
  items: string[]
}

type FlavorRow = {
  id: number
  name: string
  type: string
  dishCount: number
  status: CategoryStatus
}

const tabs: Array<{ key: TabKey; label: string }> = [
  { key: 'dish', label: '菜品' },
  { key: 'combo', label: '套餐' },
  { key: 'category', label: '菜品分类' },
  { key: 'flavor', label: '口味库' },
]

const activeTab = ref<TabKey>('dish')

/* ---------------- 通用 ---------------- */

const toast = useToast()

function openNotice(text: string) {
  toast.info(text)
}

function enableTone(status: string) {
  // 语义固定：启用 / 起售 = 成功绿，停用 / 停售 = 危险红（DESIGN.md §2）
  return status === '启用' || status === '起售' ? ('success' as const) : ('danger' as const)
}

/* ---------------- 数据加载（mock 经 api mockAdapter，real 走 HTTP） ---------------- */

function toDishRow(dish: ApiDish): DishRow {
  return {
    id: dish.id ?? '',
    name: dish.name,
    desc: dish.description ?? '',
    category: dish.dishCategory?.name ?? '',
    code: dish.code ?? '',
    price: formatYuan(dish.price),
    remain: '不限',
    remainDanger: false,
    status: dish.status === 1 ? '起售' : '停售',
    thumb: dish.image ?? '🍽️',
  }
}

function toCategoryRow(category: ApiDishCategory): CategoryRow {
  return {
    id: category.categoryId ?? '',
    name: category.name,
    sort: category.torder ?? 0,
    dishCount: 0,
    status: '启用',
  }
}

function toComboRow(setMeal: ApiSetMeal): ComboRow {
  const items = (setMeal.dishList ?? []).map((item) => {
    const copies = item.dishCopies ?? 1
    return `${item.dishName ?? ''}${copies > 1 ? ` ×${copies}` : ''}`
  })
  return {
    id: setMeal.id ?? '',
    name: setMeal.name,
    code: setMeal.code ?? '',
    price: formatYuan(setMeal.price),
    dishCount: items.length,
    status: setMeal.status === 1 ? '起售' : '停售',
    items,
  }
}

const dishes = ref<DishRow[]>([])
const error = ref('')
const categories = ref<CategoryRow[]>([])
const combos = ref<ComboRow[]>([])

async function load() {
  error.value = ''
  try {
    const [dishResult, categoryResult, comboResult] = await Promise.all([
      dishApi.page(1, 100),
      dishApi.categoryPage(1, 100),
      dishApi.setMealPage(1, 100),
    ])
    dishes.value = dishResult.records.map(toDishRow)
    categories.value = categoryResult.records.map(toCategoryRow)
    combos.value = comboResult.records.map(toComboRow)
    selectedComboId.value = comboResult.records[0]?.id ?? null
  } catch (err) {
    error.value = err instanceof Error ? err.message : '菜品数据加载失败'
  }
}

onMounted(load)

/* ---------------- 菜品 ---------------- */

const keyword = ref('')
const categoryFilter = ref('all')
const statusFilter = ref('all')
const dishPage = ref(1)
const dishPageSize = 10

const categoryOptions = computed(() => [
  { label: '分类：全部', value: 'all' },
  ...categories.value.map((item) => ({ label: item.name, value: item.name })),
])
const statusOptions = [
  { label: '状态：全部', value: 'all' },
  { label: '起售', value: 'on' },
  { label: '停售', value: 'off' },
]
const dishFormStatusOptions = [
  { label: '起售', value: '起售' },
  { label: '停售', value: '停售' },
]

const filteredDishes = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return dishes.value.filter((row) => {
    if (categoryFilter.value !== 'all' && row.category !== categoryFilter.value) return false
    if (statusFilter.value === 'on' && row.status !== '起售') return false
    if (statusFilter.value === 'off' && row.status !== '停售') return false
    if (kw && !row.name.toLowerCase().includes(kw) && !row.code.toLowerCase().includes(kw)) {
      return false
    }
    return true
  })
})

const pagedDishes = computed(() => {
  const start = (dishPage.value - 1) * dishPageSize
  return filteredDishes.value.slice(start, start + dishPageSize)
})

const dishStat = computed(() => ({
  total: dishes.value.length,
  onSale: dishes.value.filter((row) => row.status === '起售').length,
  offSale: dishes.value.filter((row) => row.status === '停售').length,
}))

const dishColumns: Column[] = [
  { key: 'thumb', label: '', width: '56px' },
  { key: 'name', label: '菜品' },
  { key: 'category', label: '分类' },
  { key: 'code', label: '编码', align: 'center' },
  { key: 'price', label: '价格', align: 'right', sortable: true },
  { key: 'remain', label: '沽清余量', align: 'right' },
  { key: 'status', label: '状态' },
  { key: 'action', label: '操作' },
]

function statusTone(status: DishStatus) {
  return status === '起售' ? ('success' as const) : ('danger' as const)
}

async function toggleDish(row: DishRow) {
  if (row.status === '起售') await dishApi.forbidden([row.id])
  else await dishApi.enable([row.id])
  await load()
}

/* -------- 菜品新增 / 编辑弹窗 -------- */

type DishForm = {
  id: string | null
  name: string
  category: string | null
  code: string
  price: string
  flavor: string | null
  status: DishStatus
}

function emptyDishForm(): DishForm {
  return { id: null, name: '', category: null, code: '', price: '', flavor: null, status: '起售' }
}

const dishModalOpen = ref(false)
const dishForm = ref<DishForm>(emptyDishForm())
const dishNameError = ref('')
const dishPriceError = ref('')

const dishCategoryOptions = computed(() =>
  categories.value.map((item) => ({ label: item.name, value: item.name })),
)
const flavorOptions = computed(() =>
  flavors.value
    .filter((item) => item.status === '启用')
    .map((item) => ({ label: item.name, value: item.name })),
)

function openDishModal() {
  dishForm.value = emptyDishForm()
  dishNameError.value = ''
  dishPriceError.value = ''
  dishModalOpen.value = true
}

function editDish(row: DishRow) {
  dishForm.value = {
    id: row.id,
    name: row.name,
    category: row.category,
    code: row.code,
    price: row.price.replace('¥', '').replace(/,/g, ''),
    flavor: row.flavor ?? null,
    status: row.status,
  }
  dishNameError.value = ''
  dishPriceError.value = ''
  dishModalOpen.value = true
}

async function saveDish() {
  const name = dishForm.value.name.trim()
  const price = Number.parseFloat(dishForm.value.price.replace(/,/g, ''))
  dishNameError.value = ''
  dishPriceError.value = ''

  if (!name) {
    dishNameError.value = '请填写菜品名称'
    return
  }
  if (!Number.isFinite(price) || price < 0) {
    dishPriceError.value = '请填写有效的价格'
    return
  }

  const code = dishForm.value.code.trim()
  const category = categories.value.find((item) => item.name === dishForm.value.category)

  const payload: DishWithFlavors = {
    dish: {
      id: dishForm.value.id ?? undefined,
      name,
      categoryId: category?.id,
      code: code || undefined,
      price: Math.round(price * 100),
      status: dishForm.value.status === '起售' ? 1 : 0,
      description: '示意菜品',
      image: '🍽️',
    },
    flavorList: [],
  }

  if (dishForm.value.id) await dishApi.update(payload)
  else await dishApi.save(payload)

  dishModalOpen.value = false
  await load()
  dishPage.value = 1
}

/* ---------------- 套餐 ---------------- */

const comboKeyword = ref('')
const comboStatus = ref('all')
const comboPage = ref(1)
const comboPageSize = 10
const selectedComboId = ref<string | null>(null)

const comboStatusOptions = [
  { label: '状态：全部', value: 'all' },
  { label: '起售', value: 'on' },
  { label: '停售', value: 'off' },
]

const filteredCombos = computed(() => {
  const kw = comboKeyword.value.trim().toLowerCase()
  return combos.value.filter((row) => {
    if (comboStatus.value === 'on' && row.status !== '起售') return false
    if (comboStatus.value === 'off' && row.status !== '停售') return false
    if (kw && !row.name.toLowerCase().includes(kw) && !row.code.toLowerCase().includes(kw)) {
      return false
    }
    return true
  })
})

const pagedCombos = computed(() => {
  const start = (comboPage.value - 1) * comboPageSize
  return filteredCombos.value.slice(start, start + comboPageSize)
})

const selectedCombo = computed(
  () => combos.value.find((row) => row.id === selectedComboId.value) ?? combos.value[0] ?? null,
)

const comboColumns: Column[] = [
  { key: 'name', label: '套餐' },
  { key: 'code', label: '编码', align: 'center' },
  { key: 'price', label: '售价', align: 'right', sortable: true },
  { key: 'dishCount', label: '内含菜品数', align: 'center' },
  { key: 'status', label: '状态' },
  { key: 'action', label: '操作' },
]

/* ---------------- 菜品分类 ---------------- */

const categoryPage = ref(1)
const categoryPageSize = 10

const categoryStatusOptions = [
  { label: '启用', value: '启用' },
  { label: '停用', value: '停用' },
]

const pagedCategories = computed(() => {
  const start = (categoryPage.value - 1) * categoryPageSize
  return categories.value.slice(start, start + categoryPageSize)
})

const categoryColumns: Column[] = [
  { key: 'name', label: '分类名称' },
  { key: 'sort', label: '排序', align: 'center', sortable: true },
  { key: 'dishCount', label: '菜品数', align: 'center' },
  { key: 'status', label: '状态' },
  { key: 'action', label: '操作' },
]

function toggleCategory(row: CategoryRow) {
  // 契约层分类无启用/停用端点，首版仅本地切换（见报告决策点）
  const index = categories.value.findIndex((item) => item.id === row.id)
  if (index < 0) return
  const next: CategoryStatus = row.status === '启用' ? '停用' : '启用'
  categories.value[index] = { ...categories.value[index], status: next }
}

/* -------- 分类新增 / 编辑弹窗 -------- */

type CategoryForm = {
  id: string | null
  name: string
  sort: string
  status: CategoryStatus
}

const categoryModalOpen = ref(false)
const categoryForm = ref<CategoryForm>({ id: null, name: '', sort: '', status: '启用' })
const categoryNameError = ref('')

function openCategoryModal() {
  categoryForm.value = { id: null, name: '', sort: '', status: '启用' }
  categoryNameError.value = ''
  categoryModalOpen.value = true
}

function editCategory(row: CategoryRow) {
  categoryForm.value = { id: row.id, name: row.name, sort: String(row.sort), status: row.status }
  categoryNameError.value = ''
  categoryModalOpen.value = true
}

async function saveCategory() {
  const name = categoryForm.value.name.trim()
  categoryNameError.value = ''
  if (!name) {
    categoryNameError.value = '请填写分类名称'
    return
  }
  if (categoryForm.value.id) await dishApi.categoryUpdate(categoryForm.value.id, name)
  else await dishApi.categoryAdd(name, 1)

  categoryModalOpen.value = false
  await load()
  categoryPage.value = 1
}

/* ---------------- 口味库（无契约端点，保留本地示意） ---------------- */

const flavors = ref<FlavorRow[]>([
  { id: 1, name: '微辣', type: '辣度', dishCount: 2, status: '启用' },
  { id: 2, name: '中辣', type: '辣度', dishCount: 1, status: '启用' },
  { id: 3, name: '特辣', type: '辣度', dishCount: 0, status: '停用' },
  { id: 4, name: '少糖', type: '甜度', dishCount: 1, status: '启用' },
  { id: 5, name: '冰镇', type: '温度', dishCount: 2, status: '启用' },
  { id: 6, name: '去冰', type: '温度', dishCount: 1, status: '启用' },
  { id: 7, name: '加冰', type: '温度', dishCount: 0, status: '启用' },
  { id: 8, name: '酥脆', type: '口感', dishCount: 1, status: '启用' },
])
const flavorKeyword = ref('')
const flavorPage = ref(1)
const flavorPageSize = 10

const filteredFlavors = computed(() => {
  const kw = flavorKeyword.value.trim().toLowerCase()
  if (!kw) return flavors.value
  return flavors.value.filter(
    (row) => row.name.toLowerCase().includes(kw) || row.type.toLowerCase().includes(kw),
  )
})

const pagedFlavors = computed(() => {
  const start = (flavorPage.value - 1) * flavorPageSize
  return filteredFlavors.value.slice(start, start + flavorPageSize)
})

const flavorColumns: Column[] = [
  { key: 'name', label: '口味名称' },
  { key: 'type', label: '类型' },
  { key: 'dishCount', label: '关联菜品数', align: 'center' },
  { key: 'status', label: '状态' },
  { key: 'action', label: '操作' },
]

function toggleFlavor(row: FlavorRow) {
  const index = flavors.value.findIndex((item) => item.id === row.id)
  if (index < 0) return
  const next: CategoryStatus = row.status === '启用' ? '停用' : '启用'
  flavors.value[index] = { ...flavors.value[index], status: next }
}
</script>

<template>
  <div class="xfm-tabs">
    <XfTabs v-model="activeTab" :tabs="tabs" />
  </div>

  <XfErrorState v-if="error && !dishes.length" :message="error" @retry="load" />

  <!-- ============ 菜品 ============ -->
  <template v-if="activeTab === 'dish'">
    <div class="xfm-toolbar">
      <div class="xfm-toolbar__search">
        <XfInput v-model="keyword" placeholder="搜索菜名 / 编码" />
      </div>
      <div class="xfm-toolbar__select">
        <XfSelect v-model="categoryFilter" :options="categoryOptions" />
      </div>
      <div class="xfm-toolbar__select">
        <XfSelect v-model="statusFilter" :options="statusOptions" />
      </div>
      <div class="xfm-toolbar__grow"></div>
      <XfButton variant="secondary" @click="openNotice('示意：请先勾选菜品，再执行批量停售。')">
        批量停售
      </XfButton>
      <XfButton @click="openDishModal">＋ 新增菜品</XfButton>
    </div>

    <XfPanel>
      <XfTable
        :columns="dishColumns"
        :data="pagedDishes"
        row-key="id"
        empty-text="暂无菜品"
        empty-description="点击右上角「新增菜品」创建"
      >
        <template #thumb="{ row }">
          <span class="xfm-thumb" aria-hidden="true">{{ row.thumb }}</span>
        </template>
        <template #name="{ row }">
          <div class="xfm-dish-name">{{ row.name }}</div>
          <div class="xfm-dish-desc">{{ row.desc }}</div>
          <div v-if="row.flavor" class="xfm-dish-desc">口味：{{ row.flavor }}</div>
        </template>
        <template #code="{ row }">
          <span class="xfm-num">{{ row.code }}</span>
        </template>
        <template #price="{ row }">
          <span class="xfm-num">{{ row.price }}</span>
        </template>
        <template #remain="{ row }">
          <span class="xfm-num" :style="row.remainDanger ? { color: 'var(--c-danger)' } : undefined">
            {{ row.remain }}
          </span>
        </template>
        <template #status="{ row }">
          <XfTag :tone="statusTone(row.status as DishStatus)">{{ row.status }}</XfTag>
        </template>
        <template #action="{ row }">
          <div class="xfm-actions">
            <XfButton size="sm" variant="ghost" @click="editDish(row as DishRow)">编辑</XfButton>
            <XfButton
              size="sm"
              :variant="row.status === '起售' ? 'danger' : 'secondary'"
              @click="toggleDish(row as DishRow)"
            >
              {{ row.status === '起售' ? '停售' : '起售' }}
            </XfButton>
          </div>
        </template>
      </XfTable>

      <div class="xfm-pager">
        <span class="xfm-pager__summary">
          共 {{ dishStat.total }} 道 · 起售 {{ dishStat.onSale }} · 停售 {{ dishStat.offSale }}
        </span>
        <XfPagination v-model:page="dishPage" :total="filteredDishes.length" :page-size="dishPageSize" />
      </div>
    </XfPanel>
  </template>

  <!-- ============ 套餐 ============ -->
  <template v-else-if="activeTab === 'combo'">
    <div class="xfm-toolbar">
      <div class="xfm-toolbar__search">
        <XfInput v-model="comboKeyword" placeholder="搜索套餐名 / 编码" />
      </div>
      <div class="xfm-toolbar__select">
        <XfSelect v-model="comboStatus" :options="comboStatusOptions" />
      </div>
      <div class="xfm-toolbar__grow"></div>
      <span class="xfm-muted">点击「明细」查看内含菜品</span>
    </div>

    <div class="xfm-combo">
      <XfPanel>
        <XfTable
          :columns="comboColumns"
          :data="pagedCombos"
          row-key="id"
          empty-text="暂无套餐"
          empty-description="后续版本支持新增套餐"
        >
          <template #name="{ row }">
            <div class="xfm-dish-name">{{ row.name }}</div>
          </template>
          <template #code="{ row }">
            <span class="xfm-num">{{ row.code }}</span>
          </template>
          <template #price="{ row }">
            <span class="xfm-num">{{ row.price }}</span>
          </template>
          <template #dishCount="{ row }">
            <span class="xfm-num">{{ row.dishCount }}</span>
          </template>
          <template #status="{ row }">
            <XfTag :tone="enableTone(row.status as string)">{{ row.status }}</XfTag>
          </template>
          <template #action="{ row }">
            <div class="xfm-actions">
              <XfButton size="sm" variant="ghost" @click="selectedComboId = row.id as string">
                明细
              </XfButton>
            </div>
          </template>
        </XfTable>

        <div class="xfm-pager">
          <span class="xfm-pager__summary">共 {{ filteredCombos.length }} 个套餐</span>
          <XfPagination
            v-model:page="comboPage"
            :total="filteredCombos.length"
            :page-size="comboPageSize"
          />
        </div>
      </XfPanel>

      <XfPanel title="套餐明细">
        <template v-if="selectedCombo">
          <div class="xfm-combo__head">
            <div class="xfm-combo__name">{{ selectedCombo.name }}</div>
            <div class="xfm-combo__meta">
              <span class="xfm-num">{{ selectedCombo.code }}</span>
              <span class="xfm-num">{{ selectedCombo.price }}</span>
              <XfTag :tone="enableTone(selectedCombo.status)">{{ selectedCombo.status }}</XfTag>
            </div>
          </div>
          <div class="xfm-combo__items">
            <div v-for="(item, index) in selectedCombo.items" :key="index" class="xfm-combo__item">
              <span>{{ item }}</span>
            </div>
          </div>
        </template>
        <XfEmpty v-else title="未选择套餐" description="点击左侧「明细」查看内含菜品" />
      </XfPanel>
    </div>
  </template>

  <!-- ============ 菜品分类 ============ -->
  <template v-else-if="activeTab === 'category'">
    <div class="xfm-toolbar">
      <span class="xfm-muted">共 {{ categories.length }} 个分类</span>
      <div class="xfm-toolbar__grow"></div>
      <XfButton @click="openCategoryModal">＋ 新增分类</XfButton>
    </div>

    <XfPanel>
      <XfTable
        :columns="categoryColumns"
        :data="pagedCategories"
        row-key="id"
        empty-text="暂无分类"
        empty-description="点击右上角「新增分类」创建"
      >
        <template #name="{ row }">
          <div class="xfm-dish-name">{{ row.name }}</div>
        </template>
        <template #sort="{ row }">
          <span class="xfm-num">{{ row.sort }}</span>
        </template>
        <template #dishCount="{ row }">
          <span class="xfm-num">{{ row.dishCount }}</span>
        </template>
        <template #status="{ row }">
          <XfTag :tone="enableTone(row.status as string)">{{ row.status }}</XfTag>
        </template>
        <template #action="{ row }">
          <div class="xfm-actions">
            <XfButton size="sm" variant="ghost" @click="editCategory(row as CategoryRow)">编辑</XfButton>
            <XfButton
              size="sm"
              :variant="row.status === '启用' ? 'danger' : 'secondary'"
              @click="toggleCategory(row as CategoryRow)"
            >
              {{ row.status === '启用' ? '停用' : '启用' }}
            </XfButton>
          </div>
        </template>
      </XfTable>

      <div class="xfm-pager">
        <span class="xfm-pager__summary">共 {{ categories.length }} 个分类</span>
        <XfPagination
          v-model:page="categoryPage"
          :total="categories.length"
          :page-size="categoryPageSize"
        />
      </div>
    </XfPanel>
  </template>

  <!-- ============ 口味库 ============ -->
  <template v-else-if="activeTab === 'flavor'">
    <div class="xfm-toolbar">
      <div class="xfm-toolbar__search">
        <XfInput v-model="flavorKeyword" placeholder="搜索口味名称 / 类型" />
      </div>
      <div class="xfm-toolbar__grow"></div>
      <span class="xfm-muted">共 {{ flavors.length }} 个口味</span>
    </div>

    <XfPanel>
      <XfTable
        :columns="flavorColumns"
        :data="pagedFlavors"
        row-key="id"
        empty-text="暂无口味"
        empty-description="口味库用于点餐规格，后续版本支持维护"
      >
        <template #name="{ row }">
          <div class="xfm-dish-name">{{ row.name }}</div>
        </template>
        <template #type="{ row }">
          <span class="xfm-tag-text">{{ row.type }}</span>
        </template>
        <template #dishCount="{ row }">
          <span class="xfm-num">{{ row.dishCount }}</span>
        </template>
        <template #status="{ row }">
          <XfTag :tone="enableTone(row.status as string)">{{ row.status }}</XfTag>
        </template>
        <template #action="{ row }">
          <div class="xfm-actions">
            <XfButton
              size="sm"
              :variant="row.status === '启用' ? 'danger' : 'secondary'"
              @click="toggleFlavor(row as FlavorRow)"
            >
              {{ row.status === '启用' ? '停用' : '启用' }}
            </XfButton>
          </div>
        </template>
      </XfTable>

      <div class="xfm-pager">
        <span class="xfm-pager__summary">共 {{ flavors.length }} 个口味</span>
        <XfPagination
          v-model:page="flavorPage"
          :total="filteredFlavors.length"
          :page-size="flavorPageSize"
        />
      </div>
    </XfPanel>
  </template>

  <!-- ============ 菜品新增 / 编辑弹窗 ============ -->
  <XfModal
    v-model:open="dishModalOpen"
    :title="dishForm.id ? '编辑菜品' : '新增菜品'"
  >
    <div class="xfm-form xfm-form--grid">
      <XfField class="xfm-form__full" label="菜品名称" required :error="dishNameError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="dishForm.name" placeholder="如 招牌红烧肉" />
        </template>
      </XfField>
      <XfField label="分类">
        <template #default="{ id }">
          <XfSelect
            :id="id"
            v-model="dishForm.category"
            :options="dishCategoryOptions"
            placeholder="请选择分类"
          />
        </template>
      </XfField>
      <XfField label="编码">
        <template #default="{ id }">
          <XfInput :id="id" v-model="dishForm.code" placeholder="如 D006" />
        </template>
      </XfField>
      <XfField label="价格" required :error="dishPriceError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="dishForm.price" prefix="¥" placeholder="0.00" />
        </template>
      </XfField>
      <XfField label="口味">
        <template #default="{ id }">
          <XfSelect
            :id="id"
            v-model="dishForm.flavor"
            :options="flavorOptions"
            placeholder="请选择口味（可简化）"
          />
        </template>
      </XfField>
      <XfField label="状态">
        <template #default="{ id }">
          <XfSelect :id="id" v-model="dishForm.status" :options="dishFormStatusOptions" />
        </template>
      </XfField>
    </div>
    <template #footer>
      <XfButton variant="secondary" @click="dishModalOpen = false">取消</XfButton>
      <XfButton @click="saveDish">保存</XfButton>
    </template>
  </XfModal>

  <!-- ============ 分类新增 / 编辑弹窗 ============ -->
  <XfModal
    v-model:open="categoryModalOpen"
    :title="categoryForm.id ? '编辑分类' : '新增分类'"
  >
    <div class="xfm-form">
      <XfField label="分类名称" required :error="categoryNameError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="categoryForm.name" placeholder="如 热菜" />
        </template>
      </XfField>
      <XfField label="排序" hint="数字越小越靠前">
        <template #default="{ id }">
          <XfInput :id="id" v-model="categoryForm.sort" placeholder="如 1" />
        </template>
      </XfField>
      <XfField label="状态">
        <template #default="{ id }">
          <XfSelect :id="id" v-model="categoryForm.status" :options="categoryStatusOptions" />
        </template>
      </XfField>
    </div>
    <template #footer>
      <XfButton variant="secondary" @click="categoryModalOpen = false">取消</XfButton>
      <XfButton @click="saveCategory">保存</XfButton>
    </template>
  </XfModal>
</template>
