import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { dishes, tables as mockTables } from '../api/mock'
import type { PosTable, TableStatus } from '../api/mock'

export type OrderStatus = '未结' | '挂账' | '已结'
export type PayMethod = '现金' | '微信' | '支付宝' | '挂账'

export type OrderItem = {
  dishId: string
  name: string
  price: number
  qty: number
}

/**
 * 收银台状态：桌台 + 当前订单。
 * 首版为本地示意状态，后续接入真实点餐/结账接口时替换 actions 实现。
 */
export const useOrderStore = defineStore('order', () => {
  const tables = ref<PosTable[]>(mockTables.map((t) => ({ ...t })))
  const currentTableId = ref<string>('')
  const guests = ref(0)
  const items = ref<OrderItem[]>([])
  const discount = ref(0)
  const status = ref<OrderStatus>('未结')
  const lastPayMethod = ref<PayMethod | ''>('')

  const dishMap = new Map(dishes.map((d) => [d.id, d]))

  const currentTable = computed(
    () => tables.value.find((t) => t.id === currentTableId.value) ?? null,
  )
  const itemCount = computed(() => items.value.reduce((sum, it) => sum + it.qty, 0))
  const subtotal = computed(() =>
    items.value.reduce((sum, it) => sum + it.price * it.qty, 0),
  )
  const total = computed(() => Math.max(0, subtotal.value - discount.value))
  const isEmpty = computed(() => items.value.length === 0)

  function setTableStatus(id: string, next: TableStatus) {
    const table = tables.value.find((t) => t.id === id)
    if (table) table.status = next
  }

  /** 开台并进入点餐 */
  function openTable(id: string, guestCount: number) {
    setTableStatus(id, '开台')
    currentTableId.value = id
    guests.value = guestCount
    items.value = []
    discount.value = 0
    status.value = '未结'
    lastPayMethod.value = ''
  }

  /** 已进入开台的桌台：恢复点餐（示意：不恢复历史明细） */
  function resumeTable(id: string) {
    const table = tables.value.find((t) => t.id === id)
    if (!table || table.status !== '开台') return
    currentTableId.value = id
    guests.value = table.seats
    items.value = []
    discount.value = 0
    status.value = '未结'
    lastPayMethod.value = ''
  }

  function addDish(id: string) {
    const dish = dishMap.get(id)
    if (!dish || dish.status === '停售' || dish.remain === 0) return
    const existing = items.value.find((it) => it.dishId === id)
    if (existing) existing.qty += 1
    else items.value.push({ dishId: id, name: dish.name, price: dish.price, qty: 1 })
  }

  function setQty(id: string, qty: number) {
    const index = items.value.findIndex((it) => it.dishId === id)
    if (index < 0) return
    if (qty <= 0) items.value.splice(index, 1)
    else items.value[index].qty = qty
  }

  function incQty(id: string) {
    const item = items.value.find((it) => it.dishId === id)
    if (item) item.qty += 1
  }

  function decQty(id: string) {
    const item = items.value.find((it) => it.dishId === id)
    if (item) setQty(id, item.qty - 1)
  }

  function removeItem(id: string) {
    items.value = items.value.filter((it) => it.dishId !== id)
  }

  function removeLastItem() {
    const last = items.value[items.value.length - 1]
    if (last) removeItem(last.dishId)
  }

  function clearItems() {
    items.value = []
  }

  function markCredit() {
    status.value = '挂账'
  }

  /** 换台：原桌台转空闲，新桌台转开台 */
  function moveTable(id: string) {
    const target = tables.value.find((t) => t.id === id)
    if (!target || target.status !== '空闲') return
    if (currentTableId.value) setTableStatus(currentTableId.value, '空闲')
    setTableStatus(id, '开台')
    currentTableId.value = id
  }

  /** 结账：订单置「已结」，桌台转空闲（DESIGN.md §2 状态语义） */
  function checkout(method: PayMethod) {
    lastPayMethod.value = method
    status.value = '已结'
    if (currentTableId.value) setTableStatus(currentTableId.value, '空闲')
    items.value = []
    discount.value = 0
    guests.value = 0
    currentTableId.value = ''
  }

  return {
    tables,
    currentTableId,
    guests,
    items,
    discount,
    status,
    lastPayMethod,
    currentTable,
    itemCount,
    subtotal,
    total,
    isEmpty,
    openTable,
    resumeTable,
    addDish,
    setQty,
    incQty,
    decQty,
    removeItem,
    removeLastItem,
    clearItems,
    markCredit,
    moveTable,
    checkout,
  }
})
