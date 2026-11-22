import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { dish, isMockMode, order, table } from '@xfcy/api'
import type { OrderEntity } from '@xfcy/api'
import type { Area, Category, PayMethod, PosDish, PosTable } from '../api/pos'
import { payTypeFromMethod, tableStatusText } from '../api/pos'

export type OrderStatus = '未结' | '挂账' | '已结'
export type { PayMethod }

export type OrderItem = {
  dishId: string
  name: string
  /** 单价，单位「分」 */
  price: number
  qty: number
}

const PAGE_SIZE = 1000

type AreaRow = { areaId: string; areaName: string; indexNumber?: number }

/**
 * 收银台状态：桌台 / 菜品 / 当前订单。
 *
 * 数据源：@xfcy/api（默认 mock，VITE_USE_MOCK=0 走真实后端）。
 * - mock：与首版演示行为一致，全部本地状态；
 * - real：开台 table.openTable、加菜 order.addOrder、结账 order.pay/payByCredit、
 *   换台 order.changeTable；数量微调/退菜仍为本地示意。
 */
export const useOrderStore = defineStore('order', () => {
  const areas = ref<Area[]>([])
  const categories = ref<Category[]>([])
  const dishes = ref<PosDish[]>([])
  const tables = ref<PosTable[]>([])
  const loading = ref(false)
  const loaded = ref(false)

  const currentTableId = ref('')
  const guests = ref(0)
  const items = ref<OrderItem[]>([])
  const discount = ref(0)
  const status = ref<OrderStatus>('未结')
  const lastPayMethod = ref<PayMethod | ''>('')

  /** 真实模式当前订单号（order.addOrder 返回） */
  const currentOrderId = ref('')
  const currentOrderNumber = ref('')

  const dishMap = computed(() => new Map(dishes.value.map((item) => [item.id, item])))

  const currentTable = computed(
    () => tables.value.find((t) => t.id === currentTableId.value) ?? null,
  )
  const itemCount = computed(() => items.value.reduce((sum, it) => sum + it.qty, 0))
  const subtotal = computed(() =>
    items.value.reduce((sum, it) => sum + it.price * it.qty, 0),
  )
  const total = computed(() => Math.max(0, subtotal.value - discount.value))
  const isEmpty = computed(() => items.value.length === 0)

  function setTableStatus(id: string, next: '空闲' | '开台' | '锁定') {
    const target = tables.value.find((t) => t.id === id)
    if (target) target.status = next
  }

  async function load() {
    if (loading.value) return
    loading.value = true
    try {
      const [areaRows, categoryPage, dishPage] = await Promise.all([
        table.areaList(),
        dish.categoryPage(1, PAGE_SIZE),
        dish.page(1, PAGE_SIZE),
      ])

      const areaList: AreaRow[] = areaRows
        .map((row) => ({
          areaId: String(row.areaId ?? ''),
          areaName: String(row.areaName ?? ''),
          indexNumber: Number(row.indexNumber ?? 0),
        }))
        .filter((row) => row.areaId)
        .sort((a, b) => a.indexNumber - b.indexNumber)

      areas.value = [
        { id: 'all', name: '全部' },
        ...areaList.map((row) => ({ id: row.areaId, name: row.areaName })),
      ]

      const areaName = new Map(areaList.map((row) => [row.areaId, row.areaName]))
      const tablePages = await Promise.all(
        areaList.map((row) => table.pageByArea(row.areaId, 1, PAGE_SIZE)),
      )
      tables.value = tablePages
        .flatMap((page) => page.records)
        .map((item) => ({
          id: item.tableId ?? '',
          name: item.tableName,
          areaId: item.areaId ?? '',
          area: areaName.get(item.areaId ?? '') ?? '',
          seats: item.tableSeatNumber ?? 0,
          status: tableStatusText(item.status),
        }))
        .filter((item) => item.id)

      categories.value = [...categoryPage.records]
        .sort((a, b) => (a.torder ?? 0) - (b.torder ?? 0))
        .map((item) => ({ id: item.categoryId ?? '', name: item.name }))
        .filter((item) => item.id)

      dishes.value = dishPage.records
        .map((item) => {
          const mock = item as typeof item & { remain?: number | null }
          return {
            id: item.id ?? '',
            categoryId: item.categoryId ?? '',
            name: item.name,
            desc: item.description ?? '',
            price: item.price ?? 0,
            remain: mock.remain ?? null,
            status: item.status === 0 ? ('停售' as const) : ('起售' as const),
            thumb: item.image ?? '🍽️',
          }
        })
        .filter((item) => item.id)

      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  function resetOrder() {
    items.value = []
    discount.value = 0
    status.value = '未结'
    lastPayMethod.value = ''
    currentOrderId.value = ''
    currentOrderNumber.value = ''
  }

  /** 开台并进入点餐 */
  async function openTable(id: string, guestCount: number) {
    const target = tables.value.find((t) => t.id === id)
    if (!target) return
    if (!isMockMode()) {
      await table.openTable(id, guestCount)
    }
    setTableStatus(id, '开台')
    currentTableId.value = id
    guests.value = guestCount
    resetOrder()
  }

  /** 已进入开台的桌台：恢复点餐（示意：不恢复历史明细） */
  function resumeTable(id: string) {
    const target = tables.value.find((t) => t.id === id)
    if (!target || target.status !== '开台') return
    currentTableId.value = id
    guests.value = target.seats
    resetOrder()
  }

  function buildOrderPayload(orderId: string): OrderEntity {
    return {
      orderId,
      tableId: currentTableId.value,
      personNumbers: guests.value,
      payType: 0,
      payStatus: 0,
      orderSource: 0,
      totalAmount: total.value,
      orderDetails: items.value.map((item) => ({
        detailId: '',
        orderId,
        dishId: item.dishId,
        dishName: item.name,
        dishPrice: item.price,
        dishNumber: item.qty,
        dishAmount: item.price * item.qty,
        dishType: 1,
        detailStatus: 1,
      })),
    }
  }

  /** 真实模式：加菜提交（同一端点，带 orderId 即加菜） */
  let orderQueue: Promise<void> = Promise.resolve()
  function submitOrderAsync() {
    orderQueue = orderQueue
      .then(async () => {
        const orderId = await order.addOrder(buildOrderPayload(currentOrderId.value))
        if (orderId) currentOrderId.value = orderId
      })
      .catch(() => {
        /* 网络失败时保留本地明细，交由用户重试 */
      })
    return orderQueue
  }

  async function addDish(id: string) {
    const dish = dishMap.value.get(id)
    if (!dish || dish.status === '停售' || dish.remain === 0) return
    const existing = items.value.find((it) => it.dishId === id)
    if (existing) existing.qty += 1
    else items.value.push({ dishId: id, name: dish.name, price: dish.price, qty: 1 })

    if (!isMockMode()) {
      await submitOrderAsync()
    }
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
  async function moveTable(id: string) {
    const target = tables.value.find((t) => t.id === id)
    if (!target || target.status !== '空闲') return
    if (!isMockMode() && currentOrderId.value) {
      await order.changeTable(currentOrderId.value, id)
    }
    if (currentTableId.value) setTableStatus(currentTableId.value, '空闲')
    setTableStatus(id, '开台')
    currentTableId.value = id
  }

  /** 结账：真实模式调用 order.pay / order.payByCredit */
  async function checkout(method: PayMethod) {
    lastPayMethod.value = method
    status.value = '已结'

    if (!isMockMode() && currentOrderId.value) {
      const payload = buildOrderPayload(currentOrderId.value)
      payload.payType = payTypeFromMethod(method)
      payload.payStatus = 1
      payload.payAmount = total.value
      payload.totalAmount = total.value
      if (method === '挂账') {
        await order.payByCredit(payload, {
          creditId: 'credit-demo',
          creditAmount: total.value,
          creditUserName: '挂账客户（示意）',
        })
      } else {
        await order.pay(payload)
      }
    }

    if (currentTableId.value) setTableStatus(currentTableId.value, '空闲')
    items.value = []
    discount.value = 0
    guests.value = 0
    currentTableId.value = ''
    currentOrderId.value = ''
    currentOrderNumber.value = ''
  }

  return {
    areas,
    categories,
    dishes,
    tables,
    loading,
    loaded,
    currentTableId,
    guests,
    items,
    discount,
    status,
    lastPayMethod,
    currentOrderId,
    currentOrderNumber,
    currentTable,
    dishMap,
    itemCount,
    subtotal,
    total,
    isEmpty,
    load,
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
