<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  XfButton,
  XfField,
  XfInput,
  XfModal,
  XfPanel,
  XfSelect,
  XfTable,
  XfTag,
  XfErrorState,
} from '@xfcy/ui'
import { isMockMode, system } from '@xfcy/api'
import type { SysDictionary } from '@xfcy/api'

type Column = {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  width?: string
}

type DictStatus = '启用' | '停用'

type DictRow = {
  id: string
  type: string
  code: string
  value: string
  sort: number
  status: DictStatus
}

const columns: Column[] = [
  { key: 'type', label: '字典类型' },
  { key: 'code', label: '编码' },
  { key: 'value', label: '值' },
  { key: 'sort', label: '排序', align: 'center', sortable: true },
  { key: 'status', label: '状态' },
  { key: 'action', label: '操作', width: '160px' },
]

const store = system
const rows = ref<DictRow[]>([])
const items = ref<SysDictionary[]>([])
const keyword = ref('')
const loading = ref(false)
const loaded = ref(false)
const error = ref('')
/** 后端暂无字典写接口，写操作仅 mock 模式可用 */
const writable = isMockMode()

const typeOptions = Object.entries(system.DICT_CATEGORY_LABELS).map(([value, label]) => ({
  label,
  value,
}))
const statusOptions = [
  { label: '启用', value: '启用' },
  { label: '停用', value: '停用' },
]

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return rows.value
  return rows.value.filter(
    (row) =>
      row.type.toLowerCase().includes(kw) ||
      row.code.toLowerCase().includes(kw) ||
      row.value.toLowerCase().includes(kw),
  )
})

function enableTone(status: string) {
  // 语义固定：启用 = 成功绿，停用 = 危险红（DESIGN.md §2）
  return status === '启用' ? ('success' as const) : ('danger' as const)
}

function categoryLabel(category: string): string {
  return system.DICT_CATEGORY_LABELS[category] ?? category
}

function categoryOf(label: string): string {
  const hit = Object.entries(system.DICT_CATEGORY_LABELS).find(([, value]) => value === label)
  return hit?.[0] ?? label
}

async function load() {
  if (loading.value) return
  loading.value = true
  error.value = ''
  try {
    items.value = await store.dictListAll()
    const counters: Record<string, number> = {}
    rows.value = items.value.map((item) => {
      const category = item.category ?? ''
      counters[category] = (counters[category] ?? 0) + 1
      return {
        id: item.dictId,
        type: categoryLabel(category),
        code: category,
        value: item.dictData ?? '',
        sort: counters[category],
        status: item.info === '停用' ? '停用' : '启用',
      }
    })
    loaded.value = true
  } catch (err) {
    error.value = err instanceof Error ? err.message : '字典加载失败'
  } finally {
    loading.value = false
  }
}

async function toggle(row: DictRow) {
  if (!writable) return
  const item = items.value.find((entry) => entry.dictId === row.id)
  if (!item) return
  item.info = row.status === '启用' ? '停用' : '启用'
  try {
    await store.dictUpdate(item)
    await load()
  } catch {
    item.info = row.status
  }
}

/* -------- 新增 / 编辑弹窗 -------- */

type DictForm = {
  id: string | null
  type: string
  value: string
  sort: string
  status: DictStatus
}

const modalOpen = ref(false)
const form = ref<DictForm>({ id: null, type: '', value: '', sort: '', status: '启用' })
const typeError = ref('')
const valueError = ref('')
const saveError = ref('')

function openCreate() {
  form.value = { id: null, type: '', value: '', sort: '', status: '启用' }
  typeError.value = ''
  valueError.value = ''
  saveError.value = ''
  modalOpen.value = true
}

function openEdit(row: DictRow) {
  form.value = {
    id: row.id,
    type: row.type,
    value: row.value,
    sort: String(row.sort),
    status: row.status,
  }
  typeError.value = ''
  valueError.value = ''
  saveError.value = ''
  modalOpen.value = true
}

async function save() {
  const category = categoryOf(form.value.type)
  const value = form.value.value.trim()
  typeError.value = ''
  valueError.value = ''
  saveError.value = ''

  if (!category) {
    typeError.value = '请选择字典类型'
    return
  }
  if (!value) {
    valueError.value = '请填写值'
    return
  }

  const label = categoryLabel(category)

  try {
    if (form.value.id != null) {
      const item = items.value.find((entry) => entry.dictId === form.value.id)
      if (!item) throw new Error('未找到该字典项')
      await store.dictUpdate({
        ...item,
        category,
        dictName: label,
        dictData: value,
        info: form.value.status,
      })
    } else {
      const maxId = items.value.reduce((max, entry) => Math.max(max, Number(entry.dictId) || 0), 0)
      await store.dictSave({
        dictId: String(maxId + 1),
        category,
        dictName: label,
        dictData: value,
        info: form.value.status,
      })
    }
    // 排序仅影响展示，重新拉取后按分类内顺序计算
    modalOpen.value = false
    await load()
  } catch (error) {
    saveError.value = error instanceof Error ? error.message : '保存失败'
  }
}

onMounted(load)
</script>

<template>
  <div class="xfo-toolbar">
    <div class="xfo-toolbar__search">
      <XfInput v-model="keyword" placeholder="搜索类型 / 编码 / 值" />
    </div>
    <div class="xfo-toolbar__grow"></div>
    <span class="xfo-muted">
      共 {{ rows.length }} 项 · 数据源：{{ writable ? '@xfcy/api mock' : '真实后端' }}
    </span>
    <XfButton :disabled="!writable" @click="openCreate">＋ 新增字典</XfButton>
  </div>

  <XfPanel>
    <p v-if="!writable" class="xfo-note">后端未提供字典写接口，新增/编辑/停用仅在 mock 模式可用。</p>
    <XfErrorState v-if="error && !rows.length" :message="error" @retry="load" />
    <XfTable
      v-else
      :columns="columns"
      :data="filtered"
      :loading="loading && !loaded"
      row-key="id"
      empty-text="暂无字典"
      empty-description="点击右上角「新增字典」创建"
    >
      <template #type="{ row }">
        <span class="xfo-cell-name">{{ (row as DictRow).type }}</span>
      </template>
      <template #code="{ row }">
        <span class="xfo-num">{{ (row as DictRow).code }}</span>
      </template>
      <template #sort="{ row }">
        <span class="xfo-num">{{ (row as DictRow).sort }}</span>
      </template>
      <template #status="{ row }">
        <XfTag :tone="enableTone((row as DictRow).status)">{{ (row as DictRow).status }}</XfTag>
      </template>
      <template #action="{ row }">
        <div class="xfo-actions">
          <XfButton size="sm" variant="ghost" :disabled="!writable" @click="openEdit(row as DictRow)">
            编辑
          </XfButton>
          <XfButton
            size="sm"
            :disabled="!writable"
            :variant="(row as DictRow).status === '启用' ? 'danger' : 'secondary'"
            @click="toggle(row as DictRow)"
          >
            {{ (row as DictRow).status === '启用' ? '停用' : '启用' }}
          </XfButton>
        </div>
      </template>
    </XfTable>
  </XfPanel>

  <XfModal
    v-model:open="modalOpen"
    :title="form.id ? '编辑字典' : '新增字典'"
  >
    <div class="xfo-form">
      <XfField label="字典类型" required :error="typeError || ''">
        <template #default="{ id }">
          <XfSelect :id="id" v-model="form.type" :options="typeOptions" placeholder="请选择字典类型" />
        </template>
      </XfField>
      <XfField label="值" required :error="valueError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.value" placeholder="如 热菜" />
        </template>
      </XfField>
      <XfField label="排序" hint="数字越小越靠前（展示用）">
        <template #default="{ id }">
          <XfInput :id="id" v-model="form.sort" placeholder="如 1" />
        </template>
      </XfField>
      <XfField label="状态">
        <template #default="{ id }">
          <XfSelect :id="id" v-model="form.status" :options="statusOptions" />
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
