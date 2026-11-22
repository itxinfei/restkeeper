<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { categories, dishes } from '../api/mock'
import type { Dish } from '../api/mock'
import { useOrderStore } from '../stores/order'
import type { PayMethod } from '../stores/order'
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
  categories
    .map((category) => ({
      id: category.id,
      name: category.name,
      dishes: dishes.filter((d) => d.categoryId === category.id),
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

function onAddDish(dish: Dish) {
  order.addDish(dish.id)
}

const checkoutOpen = ref(false)

function onCheckout(method: PayMethod) {
  order.checkout(method)
  checkoutOpen.value = false
  router.replace({ name: 'tables' })
}
</script>

<template>
  <div class="xfc-pos">
    <div class="xfc-pos__left">
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
