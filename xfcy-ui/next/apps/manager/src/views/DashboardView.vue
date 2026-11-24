<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { XfEmpty, XfKpi, XfPageHead, XfPanel, XfTable, XfTag, labelOf, payTypeLabel, toneOf } from '@xfcy/ui'
import {
  demoDashboardKpis,
  demoLatestOrders,
  demoSoldoutRows,
  demoTableStats,
  formatYuan,
  isMockMode,
  order,
  soldout,
  table,
} from '@xfcy/api'
import type {
  DemoKpi,
  DemoOrderRow,
  DemoOrderStatus,
  DemoSoldoutRow,
  DemoTableStat,
} from '@xfcy/api'

type Column = {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  width?: string
}

const columns: Column[] = [
  { key: 'orderNo', label: '单号', sortable: true },
  { key: 'table', label: '桌台' },
  { key: 'guests', label: '人数', align: 'center' },
  { key: 'amount', label: '金额', align: 'right', sortable: true },
  { key: 'pay', label: '支付' },
  { key: 'status', label: '状态' },
  { key: 'time', label: '时间' },
]

/* mock 模式用迁移自 api 的演示数据；real 模式按契约拉取，失败显式报错 */
const kpis = ref<DemoKpi[]>(demoDashboardKpis)
const latestOrders = ref<DemoOrderRow[]>(demoLatestOrders)
const tableStats = ref<DemoTableStat[]>(demoTableStats)
const soldoutRows = ref<DemoSoldoutRow[]>(demoSoldoutRows)

const error = ref('')
const router = useRouter()

function isoDate(d: Date): string {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

async function load() {
  error.value = ''
  if (isMockMode()) {
    kpis.value = demoDashboardKpis
    latestOrders.value = demoLatestOrders
    tableStats.value = demoTableStats
    soldoutRows.value = demoSoldoutRows
    return
  }

  try {
    const now = new Date()
    // 契约：GET /order/report/currentAmount?start=&end=（区间 >=start 且 <end）
    const stat = await order.currentAmount(isoDate(now), isoDate(new Date(now.getTime() + 86_400_000)))
    const avg = stat.payTotal && stat.payTotalCount ? Math.round(stat.payTotal / stat.payTotalCount) : 0
    kpis.value = [
      { key: 'revenue', label: '今日营业额', value: formatYuan(stat.payTotal), trend: 'flat', trendText: '' },
      { key: 'orders', label: '今日单量', value: String(stat.payTotalCount ?? 0), trend: 'flat', trendText: '' },
      { key: 'avg', label: '客单价', value: formatYuan(avg), trend: 'flat', trendText: '' },
      // 翻台率无对应契约，显式标注口径缺失，不编造数值
      { key: 'turnover', label: '翻台率', value: '暂无口径', trend: 'flat', trendText: '契约未提供' },
    ]

    // 桌台盘：按区域聚合三种状态计数
    const areas = await table.areaList()
    const counts = await Promise.all(
      areas.flatMap((area) => {
        const id = String(area.areaId ?? '')
        return [0, 1, 2].map((status) => table.countByStatus(id, status))
      }),
    )
    let cursor = 0
    let free = 0
    let opened = 0
    let locked = 0
    for (const _area of areas) {
      free += Number(counts[cursor] ?? 0)
      opened += Number(counts[cursor + 1] ?? 0)
      locked += Number(counts[cursor + 2] ?? 0)
      cursor += 3
    }
    tableStats.value = [
      { key: 'free', label: '空闲', value: free, tone: 'normal' },
      { key: 'opened', label: '开台', value: opened, tone: 'warn' },
      { key: 'locked', label: '锁定', value: locked, tone: 'normal' },
    ]

    // 沽清余量：只取限量菜（remainder >= 0），按余量升序展示前 5
    const page = await soldout.page(1, 50)
    soldoutRows.value = (page.records ?? [])
      .filter((item) => typeof item.remainder === 'number' && item.remainder >= 0)
      .sort((a, b) => (a.remainder ?? 0) - (b.remainder ?? 0))
      .slice(0, 5)
      .map((item, index) => ({
        id: index + 1,
        name: item.dishName ?? '未命名',
        remain: `余 ${item.remainder ?? 0}`,
        danger: (item.remainder ?? 0) <= 2,
      }))

    // 最新订单：只读分页契约（时间区间与报表口径一致，左闭右开）
    const ordersPage = await order.page(1, 6, {
      start: isoDate(now),
      end: isoDate(new Date(now.getTime() + 86_400_000)),
    })
    latestOrders.value = (ordersPage.records ?? []).map((item, index) => {
      const paid = item.payStatus === 1
      const credit = item.payType === 5
      const status: DemoOrderStatus = paid ? (credit ? '挂账' : '已结') : '未结'
      return {
        id: index + 1,
        orderNo: item.orderNumber ?? item.orderId ?? '—',
        table: item.tableId ?? '—',
        guests: item.personNumbers ?? 0,
        amount: formatYuan(item.payAmount ?? item.totalAmount ?? 0),
        pay: payTypeLabel(item.payType),
        status,
        time: (item.lastUpdateTime ?? '').slice(11, 16) || '—',
      }
    })
  } catch (err) {
    error.value = err instanceof Error ? err.message : '数据加载失败'
  }
}

onMounted(load)

function goSoldout() {
  router.push('/soldout')
}
</script>

<template>
  <XfPageHead title="工作台" description="今日经营概览、桌台与沽清实时状态" />

  <XfEmpty
    v-if="error"
    title="工作台数据加载失败"
    :description="error"
    action-text="重试"
    @action="load"
  />

  <template v-else>
    <section class="xfm-kpis" aria-label="今日经营 KPI">
      <XfKpi
        v-for="kpi in kpis"
        :key="kpi.key"
        :label="kpi.label"
        :value="kpi.value"
        :trend="kpi.trend"
        :trend-text="kpi.trendText"
      />
    </section>

    <div class="xfm-cols">
      <XfPanel title="最新订单">
        <XfTable
          v-if="latestOrders.length"
          :columns="columns"
          :data="latestOrders"
          row-key="id"
        >
          <template #guests="{ row }">
            <span class="xf-num">{{ row.guests }}</span>
          </template>
          <template #amount="{ row }">
            <span class="xf-num">{{ row.amount }}</span>
          </template>
          <template #status="{ row }">
            <XfTag :tone="toneOf('pay', row.status as DemoOrderStatus)">{{ labelOf('pay', row.status as DemoOrderStatus) }}</XfTag>
          </template>
          <template #time="{ row }">
            <span class="xf-num">{{ row.time }}</span>
          </template>
        </XfTable>
        <XfEmpty
          v-else
          title="今日暂无订单"
          description="订单产生后将在此展示"
        />
      </XfPanel>

      <div class="xfm-side">
        <XfPanel title="桌台盘">
          <div class="xfm-tgrid">
            <div
              v-for="stat in tableStats"
              :key="stat.key"
              class="xfm-tcell"
              :class="{ 'is-warn': stat.tone === 'warn' }"
            >
              <b class="xf-num">{{ stat.value }}</b>
              {{ stat.label }}
            </div>
          </div>
        </XfPanel>

        <XfPanel title="沽清余量">
          <template #actions>
            <XfButton size="sm" variant="ghost" @click="goSoldout">登记</XfButton>
          </template>
          <div v-for="row in soldoutRows" :key="row.id" class="xfm-rowline">
            <span>{{ row.name }}</span>
            <b class="xf-num" :style="row.danger ? { color: 'var(--c-danger)' } : undefined">
              {{ row.remain }}
            </b>
          </div>
          <XfEmpty
            v-if="!soldoutRows.length"
            title="暂无限量菜品"
            description="未登记沽清的菜品默认不限量"
          />
        </XfPanel>
      </div>
    </div>
  </template>
</template>
