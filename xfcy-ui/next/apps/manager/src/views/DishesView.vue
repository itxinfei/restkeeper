<script setup lang="ts">
import { ref } from 'vue'
import {
  XfButton,
  XfEmpty,
  XfField,
  XfInput,
  XfModal,
  XfPagination,
  XfPanel,
  XfSelect,
  XfTable,
  XfTabs,
  XfTag,
} from '@xfcy/ui'
import { dishCategories, dishList, dishSummary } from '../api/mock'
import type { DishStatus } from '../api/mock'

type TabKey = 'dish' | 'combo' | 'category' | 'flavor'

type Column = {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  width?: string
}

const tabs: Array<{ key: TabKey; label: string }> = [
  { key: 'dish', label: '菜品' },
  { key: 'combo', label: '套餐' },
  { key: 'category', label: '菜品分类' },
  { key: 'flavor', label: '口味库' },
]

const columns: Column[] = [
  { key: 'thumb', label: '', width: '56px' },
  { key: 'name', label: '菜品' },
  { key: 'category', label: '分类' },
  { key: 'code', label: '编码', align: 'center' },
  { key: 'price', label: '价格', align: 'right', sortable: true },
  { key: 'remain', label: '沽清余量', align: 'right' },
  { key: 'status', label: '状态' },
  { key: 'action', label: '操作' },
]

const activeTab = ref<TabKey>('dish')
const keyword = ref('')
const category = ref<string>('all')
const status = ref<string>('all')

const categoryOptions = [
  { label: '分类：全部', value: 'all' },
  ...dishCategories.map((item) => ({ label: item, value: item })),
]
const statusOptions = [
  { label: '状态：全部', value: 'all' },
  { label: '起售', value: 'on' },
  { label: '停售', value: 'off' },
]

const page = ref(1)
const pageSize = 10

const noticeOpen = ref(false)
const noticeText = ref('')

const dishModalOpen = ref(false)
const dishForm = ref({ name: '', category: null as string | null, price: '' })
const dishFormError = ref('')

function statusTone(status: DishStatus) {
  // 语义固定：起售=成功绿，停售=危险红（DESIGN.md §2）
  return status === '起售' ? ('success' as const) : ('danger' as const)
}

function toggleText(value: DishStatus) {
  return value === '起售' ? '停售' : '起售'
}

function openNotice(text: string) {
  noticeText.value = text
  noticeOpen.value = true
}

function openDishModal() {
  dishForm.value = { name: '', category: null, price: '' }
  dishFormError.value = ''
  dishModalOpen.value = true
}

function saveDish() {
  if (!dishForm.value.name.trim()) {
    dishFormError.value = '请填写菜品名称'
    return
  }
  // 示意：首版不落库，仅关闭弹窗
  dishModalOpen.value = false
}
</script>

<template>
  <div class="xfm-tabs">
    <XfTabs v-model="activeTab" :tabs="tabs" />
  </div>

  <div class="xfm-toolbar">
    <div class="xfm-toolbar__search">
      <XfInput v-model="keyword" placeholder="搜索菜名 / 编码" />
    </div>
    <div class="xfm-toolbar__select">
      <XfSelect v-model="category" :options="categoryOptions" />
    </div>
    <div class="xfm-toolbar__select">
      <XfSelect v-model="status" :options="statusOptions" />
    </div>
    <div class="xfm-toolbar__grow"></div>
    <XfButton variant="secondary" @click="openNotice('示意：请先勾选菜品，再执行批量停售。')">
      批量停售
    </XfButton>
    <XfButton @click="openDishModal">＋ 新增菜品</XfButton>
  </div>

  <XfPanel>
    <XfTable v-if="activeTab === 'dish'" :columns="columns" :data="dishList" row-key="id">
      <template #thumb="{ row }">
        <span class="xfm-thumb" aria-hidden="true">{{ row.thumb }}</span>
      </template>
      <template #name="{ row }">
        <div class="xfm-dish-name">{{ row.name }}</div>
        <div class="xfm-dish-desc">{{ row.desc }}</div>
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
          <XfButton size="sm" variant="ghost">编辑</XfButton>
          <XfButton
            size="sm"
            :variant="row.status === '起售' ? 'danger' : 'secondary'"
          >
            {{ toggleText(row.status as DishStatus) }}
          </XfButton>
        </div>
      </template>
    </XfTable>

    <XfEmpty
      v-else
      title="该模块为占位"
      description="管家端首版仅落地菜品列表，套餐 / 分类 / 口味库后续按原型补齐。"
    />

    <div v-if="activeTab === 'dish'" class="xfm-pager">
      <span class="xfm-pager__summary">
        共 {{ dishSummary.total }} 道 · 起售 {{ dishSummary.onSale }} · 停售 {{ dishSummary.offSale }}
      </span>
      <XfPagination v-model:page="page" :total="dishSummary.total" :page-size="pageSize" />
    </div>
  </XfPanel>

  <XfModal v-model:open="noticeOpen" title="提示">
    <p class="xfm-muted">{{ noticeText }}</p>
    <template #footer>
      <XfButton variant="secondary" @click="noticeOpen = false">知道了</XfButton>
    </template>
  </XfModal>

  <XfModal v-model:open="dishModalOpen" title="新增菜品（示意）">
    <div class="xfm-form">
      <XfField label="菜品名称" required>
        <template #default="{ id }">
          <XfInput :id="id" v-model="dishForm.name" placeholder="如 招牌红烧肉" />
        </template>
      </XfField>
      <XfField label="分类">
        <template #default="{ id }">
          <XfSelect
            :id="id"
            v-model="dishForm.category"
            :options="dishCategories.map((item) => ({ label: item, value: item }))"
          />
        </template>
      </XfField>
      <XfField label="价格" required :error="dishFormError || ''">
        <template #default="{ id }">
          <XfInput :id="id" v-model="dishForm.price" prefix="¥" placeholder="0.00" />
        </template>
      </XfField>
    </div>
    <template #footer>
      <XfButton variant="secondary" @click="dishModalOpen = false">取消</XfButton>
      <XfButton @click="saveDish">保存</XfButton>
    </template>
  </XfModal>
</template>
