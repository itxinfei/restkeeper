<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { XfButton, XfKpi, XfPanel, XfTable, XfTag } from '@xfcy/ui'
import {
  demoDashboardKpis,
  demoLatestOrders,
  demoSoldoutRows,
  demoTableStats,
  formatYuan,
  isMockMode,
  order,
} from '@xfcy/api'
import type { DemoKpi, DemoOrderRow, DemoOrderStatus } from '@xfcy/api'

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

// mock 模式直接使用迁移自 api 的演示数据；real 模式按契约拉取实时统计
const kpis = ref<DemoKpi[]>(demoDashboardKpis)
const latestOrders = ref<DemoOrderRow[]>(demoLatestOrders)
const tableStats = ref(demoTableStats)
const soldoutRows = ref(demoSoldoutRows)

function isoDate(d: Date): string {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

onMounted(async () => {
  if (isMockMode()) return
  const now = new Date()
  const start = isoDate(now)
  const end = isoDate(new Date(now.getTime() + 24 * 60 * 60 * 1000))
  try {
    // 契约：GET /order/report/currentAmount?start=&end=（区间 >=start 且 <end）
    const stat = await order.currentAmount(start, end)
    const avg =
      stat.payTotal && stat.payTotalCount ? Math.round(stat.payTotal / stat.payTotalCount) : 0
    kpis.value = [
      { key: 'revenue', label: '今日营业额', value: formatYuan(stat.payTotal), trend: 'flat', trendText: '' },
      { key: 'orders', label: '今日单量', value: String(stat.payTotalCount ?? 0), trend: 'flat', trendText: '' },
      { key: 'avg', label: '客单价', value: formatYuan(avg), trend: 'flat', trendText: '' },
      { key: 'turnover', label: '翻台率', value: '—', trend: 'flat', trendText: '' },
    ]
  } catch {
    // 保留演示数据，避免报表接口不可用时页面空白
  }
})

function statusTone(status: DemoOrderStatus) {
  if (status === '已结') return 'success' as const
  if (status === '挂账') return 'warning' as const
  if (status === '未结') return 'danger' as const
  return 'neutral' as const
}
</script>

<template>
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
      <template #actions>
        <XfButton size="sm" variant="secondary">全部订单</XfButton>
      </template>
      <XfTable :columns="columns" :data="latestOrders" row-key="id">
        <template #guests="{ row }">
          <span class="xfm-num">{{ row.guests }}</span>
        </template>
        <template #amount="{ row }">
          <span class="xfm-num">{{ row.amount }}</span>
        </template>
        <template #status="{ row }">
          <XfTag :tone="statusTone(row.status as DemoOrderStatus)">{{ row.status }}</XfTag>
        </template>
        <template #time="{ row }">
          <span class="xfm-num">{{ row.time }}</span>
        </template>
      </XfTable>
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
            <b class="xfm-num">{{ stat.value }}</b>
            {{ stat.label }}
          </div>
        </div>
      </XfPanel>

      <XfPanel title="沽清余量">
        <template #actions>
          <XfButton size="sm" variant="ghost">登记</XfButton>
        </template>
        <div v-for="row in soldoutRows" :key="row.id" class="xfm-rowline">
          <span>{{ row.name }}</span>
          <b class="xfm-num" :style="row.danger ? { color: 'var(--c-danger)' } : undefined">
            {{ row.remain }}
          </b>
        </div>
      </XfPanel>
    </div>
  </div>
</template>
