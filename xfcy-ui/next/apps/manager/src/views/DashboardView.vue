<script setup lang="ts">
import { XfButton, XfKpi, XfPanel, XfTable, XfTag } from '@xfcy/ui'
import { dashboardKpis, latestOrders, soldoutRows, tableStats } from '../api/mock'
import type { OrderStatus } from '../api/mock'

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

function statusTone(status: OrderStatus) {
  if (status === '已结') return 'success' as const
  if (status === '挂账') return 'warning' as const
  if (status === '未结') return 'danger' as const
  return 'neutral' as const
}
</script>

<template>
  <section class="xfm-kpis" aria-label="今日经营 KPI（示意）">
    <XfKpi
      v-for="kpi in dashboardKpis"
      :key="kpi.key"
      :label="kpi.label"
      :value="kpi.value"
      :trend="kpi.trend"
      :trend-text="kpi.trendText"
    />
  </section>

  <div class="xfm-cols">
    <XfPanel title="最新订单（示意）">
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
          <XfTag :tone="statusTone(row.status as OrderStatus)">{{ row.status }}</XfTag>
        </template>
        <template #time="{ row }">
          <span class="xfm-num">{{ row.time }}</span>
        </template>
      </XfTable>
    </XfPanel>

    <div class="xfm-side">
      <XfPanel title="桌台盘（示意）">
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

      <XfPanel title="沽清余量（示意）">
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
