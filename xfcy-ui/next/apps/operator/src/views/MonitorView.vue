<script setup lang="ts">
import { monitorKpis, serviceList } from '../api/mock'
import type { ServiceRow } from '../api/mock'
import { XfKpi, XfPageHead, XfPanel, XfTable, XfTag, labelOf, toneOf } from '@xfcy/ui'

type Column = {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  width?: string
}

const columns: Column[] = [
  { key: 'name', label: '服务名' },
  { key: 'group', label: '分组' },
  { key: 'status', label: '状态' },
  { key: 'instances', label: '实例数', align: 'center' },
  { key: 'version', label: '版本', align: 'center' },
  { key: 'heartbeat', label: '心跳', align: 'center' },
]

const kpiTrend: Record<string, 'up' | 'down' | 'flat'> = {
  total: 'flat',
  online: 'flat',
  offline: 'flat',
  alert: 'down',
}

</script>

<template>
  <XfPageHead title="服务监控" description="Nacos 服务注册、心跳与告警（示意）" />

  <section class="xfo-monitor" aria-label="服务监控 KPI（示意）">
    <XfKpi
      v-for="kpi in monitorKpis"
      :key="kpi.key"
      :label="kpi.label"
      :value="kpi.value"
      :trend="kpiTrend[kpi.key] ?? 'flat'"
      :trend-text="kpi.trendText"
    />
  </section>

  <XfPanel title="Nacos 服务状态（示意）">
    <XfTable
      :columns="columns"
      :data="serviceList"
      row-key="key"
      empty-text="暂无服务（示意）"
      empty-description="服务注册信息将在此展示"
    >
      <template #name="{ row }">
        <span class="xfo-cell-name">{{ (row as ServiceRow).name }}</span>
      </template>
      <template #group="{ row }">
        <span class="xf-muted">{{ (row as ServiceRow).group }}</span>
      </template>
      <template #status="{ row }">
        <XfTag :tone="toneOf('service', (row as ServiceRow).status)">{{ labelOf('service', (row as ServiceRow).status) }}</XfTag>
      </template>
      <template #instances="{ row }">
        <span class="xf-num">{{ (row as ServiceRow).instances }}</span>
      </template>
      <template #version="{ row }">
        <span class="xf-num">{{ (row as ServiceRow).version }}</span>
      </template>
      <template #heartbeat="{ row }">
        <span class="xf-num">{{ (row as ServiceRow).heartbeat }}</span>
      </template>
    </XfTable>
    <p class="xfo-note">以上为演示「示意」数据，非真实 Nacos 探测结果。</p>
  </XfPanel>
</template>
