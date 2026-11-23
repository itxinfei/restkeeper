<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { XfButton, XfEmpty, XfInput } from '@xfcy/ui'
import { isMockMode } from '@xfcy/api'
import type { PosDish } from '../api/pos'
import { useOrderStore } from '../stores/order'
import type { CreditSelection, PayMethod } from '../stores/order'
import DishGrid from '../components/DishGrid.vue'
import OrderPanel from '../components/OrderPanel.vue'
import CheckoutModal from '../components/CheckoutModal.vue'

const route = useRoute()
const router = useRouter()
const order = useOrderStore()

const tableId = computed(() => String(route.params.tableId ?? ''))

/** 进入订单页时确保桌台与订单状态一致（支持直接访问/刷新） */
function ensureTable() {
  const table = order.tables.find((t) => t.id === tableId.value)
  if (!table) {
    router.replace({ name: 'tables' })
    return
  }
  if (order.currentTableId !== tableId.value) {
    if (table.status === '开台') order.resumeTable(table.id)
    else order.openTable(table.id, table.seats)
  }
}

ensureTable()
watch(tableId, ensureTable)

const groups = computed(() =>
  order.categories
    .map((category) => ({
      id: category.id,
      name: category.name,
      dishes: order.dishes.filter((d) => d.categoryId === category.id),
    }))
    .filter((group) => group.dishes.length > 0),
)

/* 分类横向锚点：点击滚动到对应分组，滚动时同步高亮 */
const scrollRef = ref<HTMLElement | null>(null)
const sectionEls = ref<Record<string, HTMLElement | null>>({})
const activeCat = ref(groups.value[0]?.id ?? '')

function setSection(id: string) {
  return (el: unknown) => {
    sectionEls.value[id] = el as HTMLElement | null
  }
}

function sectionTop(sec: HTMLElement, container: HTMLElement): number {
  return (
    sec.getBoundingClientRect().top - container.getBoundingClientRect().top + container.scrollTop
  )
}

function jump(id: string) {
  activeCat.value = id
  const container = scrollRef.value
  const sec = sectionEls.value[id]
  if (container && sec) {
    container.scrollTo({ top: sectionTop(sec, container) - 4, behavior: 'smooth' })
  }
}

function onScroll() {
  const container = scrollRef.value
  if (!container) return
  const top = container.scrollTop + 120
  let current = activeCat.value
  for (const group of groups.value) {
    const sec = sectionEls.value[group.id]
    if (sec && sectionTop(sec, container) <= top) current = group.id
  }
  if (current !== activeCat.value) activeCat.value = current
}

async function onAddDish(dish: PosDish) {
  await order.addDish(dish.id)
}

/* 编码/名称检索：真实模式走 /search/dish/code（ES），mock 本地过滤 */
const keyword = ref('')

const searchHint = computed(() => {
  if (!order.searched || order.searchLoading || order.searchResults.length > 0) return ''
  return isMockMode()
    ? '换个编码或名称试试。'
    : '真实模式未检索到菜品：可能是 ES 索引未同步（当前无写入代码），可切换 mock 演示或稍后再试。'
})

async function runSearch() {
  await order.searchDishes(keyword.value)
}

function clearSearch() {
  keyword.value = ''
  void order.searchDishes('')
}

const checkoutOpen = ref(false)

async function onCheckout(method: PayMethod, credit?: CreditSelection) {
  await order.checkout(method, credit)
  checkoutOpen.value = false
  router.replace({ name: 'tables' })
}
</script>

<template>
  <div class="xfc-pos">
    <div class="xfc-pos__left">
      <div class="xfc-search">
        <XfInput
          v-model="keyword"
          class="xfc-search__input"
          placeholder="输入菜品编码 / 名称检索"
          @keyup.enter="runSearch"
        />
        <XfButton :loading="order.searchLoading" @click="runSearch">检索</XfButton>
        <XfButton v-if="order.searched" variant="ghost" size="sm" @click="clearSearch">
          清除
        </XfButton>
      </div>

      <div v-if="order.searched" class="xfc-dishes">
        <p class="xfc-search__meta" :class="{ 'is-error': order.searchError }">
          检索「{{ order.searchKeyword }}」 ·
          {{ order.searchLoading ? '检索中…' : order.searchError ? '检索失败' : `${order.searchResults.length} 条结果` }}
        </p>
        <p v-if="order.searchError" class="xfc-search__meta is-error" role="alert">
          检索失败：{{ order.searchError }}
        </p>
        <XfEmpty
          v-if="!order.searchLoading && !order.searchError && order.searchResults.length === 0"
          title="未找到菜品"
          :description="searchHint"
        />
        <DishGrid
          v-else-if="!order.searchLoading"
          :dishes="order.searchResults"
          @add="onAddDish"
        />
      </div>

      <template v-else>
      <nav class="xfc-cats" aria-label="菜品分类">
        <button
          v-for="group in groups"
          :key="group.id"
          type="button"
          class="xfc-cat"
          :class="{ 'is-active': activeCat === group.id }"
          @click="jump(group.id)"
        >
          {{ group.name }}
        </button>
      </nav>

      <div ref="scrollRef" class="xfc-dishes" @scroll="onScroll">
        <section v-for="group in groups" :key="group.id" :ref="setSection(group.id)" class="xfc-group">
          <h3 class="xfc-group__title">{{ group.name }}</h3>
          <DishGrid :dishes="group.dishes" @add="onAddDish" />
        </section>
      </div>
      </template>
    </div>

    <OrderPanel @checkout="checkoutOpen = true" />

    <CheckoutModal
      v-model:open="checkoutOpen"
      :total="order.total"
      :table-name="order.currentTable?.name ?? ''"
      @confirm="onCheckout"
    />
  </div>
</template>
