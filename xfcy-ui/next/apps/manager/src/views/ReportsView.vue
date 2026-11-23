<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { XfButton, XfField, XfInput, XfKpi, XfPageHead, XfPanel, XfTabs, useToast } from '@xfcy/ui'
import { formatYuan, order } from '@xfcy/api'
import type {
  CurrentAmountCollectDTO,
  CurrentHourCollectDTO,
  PayTypeCollectDTO,
  PrivilegeDTO,
} from '@xfcy/api'

type Kpi = { key: string; label: string; value: string }
type HourRow = { hour: number; value: number }
type PayRow = { payType: number; payName: string; count: number; ratio: string }

function isoDate(d: Date): string {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const now = new Date()
const start = ref(isoDate(now))
const end = ref(isoDate(new Date(now.getTime() + 24 * 60 * 60 * 1000)))
/** 分时统计口径：1 金额 / 2 单数 */
const metric = ref<number>(1)

const metricTabs = [
  { key: 1, label: '分时金额' },
  { key: 2, label: '分时单数' },
]

const loading = ref(false)
const kpis = ref<Kpi[]>([])
const hourRows = ref<HourRow[]>([])
const payRows = ref<PayRow[]>([])
const privilege = ref<PrivilegeDTO>({})
const error = ref('')

const maxHour = computed(() => Math.max(1, ...hourRows.value.map((row) => row.value)))
const totalCount = computed(() => payRows.value.reduce((sum, row) => sum + row.count, 0))
const hourHasData = computed(() => hourRows.value.some((row) => row.value > 0))

function buildKpis(amount: CurrentAmountCollectDTO): Kpi[] {
  const total = amount.payTotal ?? 0
  const count = amount.payTotalCount ?? 0
  const avg = count > 0 ? Math.round(total / count) : 0
  return [
    { key: 'revenue', label: '营业额（已付）', value: formatYuan(total) },
    { key: 'orders', label: '单量（已付）', value: String(count) },
    { key: 'avg', label: '客单价', value: formatYuan(avg) },
    { key: 'noPay', label: '未付金额', value: formatYuan(amount.noPayTotal) },
    { key: 'person', label: '就餐人数', value: String(amount.totalPerson ?? 0) },
  ]
}

function buildHours(rows: CurrentHourCollectDTO[]): HourRow[] {
  const map = new Map<number, number>()
  rows.forEach((row) => {
    const hour = row.currentDateHour ?? -1
    if (hour >= 0 && hour <= 23) map.set(hour, row.totalAmount ?? 0)
  })
  // 固定 24 小时轴，无数据的时段高度为 0，保证图表稳定
  return Array.from({ length: 24 }, (_, hour) => ({ hour, value: map.get(hour) ?? 0 }))
}

function buildPayRows(rows: PayTypeCollectDTO[]): PayRow[] {
  const total = rows.reduce((sum, row) => sum + (row.totalCount ?? 0), 0)
  return rows.map((row) => ({
    payType: row.payType ?? 0,
    payName: row.payName || `方式 ${row.payType ?? '-'}`,
    count: row.totalCount ?? 0,
    ratio: total > 0 ? `${Math.round(((row.totalCount ?? 0) / total) * 100)}%` : '0%',
  }))
}

async function load() {
  if (loading.value) return
  loading.value = true
  error.value = ''
  try {
    const [amount, hours, pay, privilegeResult] = await Promise.all([
      order.currentAmount(start.value, end.value),
      order.hourCollect(start.value, end.value, metric.value),
      order.payTypeCollect(start.value, end.value),
      order.privilegeCollect(start.value, end.value),
    ])
    kpis.value = buildKpis(amount)
    hourRows.value = buildHours(hours)
    payRows.value = buildPayRows(pay)
    privilege.value = privilegeResult
  } catch (e) {
    error.value = e instanceof Error ? e.message : '报表加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)

/* ---------------- 提示 ---------------- */

const toast = useToast()

// 切换金额 / 单数口径时重新拉取分时数据
watch(metric, () => {
  void load()
})

/* ---------------- 导出 CSV ---------------- */

function csvCell(value: string | number): string {
  const text = String(value)
  return /[",\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text
}

function exportCsv() {
  if (!kpis.value.length) {
    toast.warning('暂无可导出的报表数据')
    return
  }
  const lines: string[] = [`报表区间,${start.value} ~ ${end.value}`, '', 'KPI']
  kpis.value.forEach((kpi) => lines.push([kpi.label, kpi.value].map(csvCell).join(',')))
  lines.push('', `分时（${metric.value === 1 ? '金额' : '单数'}）`)
  hourRows.value.forEach((row) => lines.push([`${row.hour}时`, row.value].map(csvCell).join(',')))
  lines.push('', '收款方式')
  payRows.value.forEach((row) =>
    lines.push([row.payName, row.count, row.ratio].map(csvCell).join(',')),
  )
  const blob = new Blob(['\ufeff' + lines.join('\n')], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = `经营报表_${start.value}_${end.value}.csv`
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(url)
  toast.success('报表已导出 CSV')
}
</script>

<template>
  <XfPageHead title="经营报表" description="营业数据、分时与收款构成">
    <template #actions>
      <XfButton variant="secondary" @click="toast.info('报表区间为左闭右开，传「今天/明天」即当日。')">
        口径说明
      </XfButton>
      <XfButton @click="exportCsv">导出 CSV</XfButton>
    </template>
  </XfPageHead>

  <div class="xfm-toolbar">
    <XfField label="开始日期">
      <template #default="{ id }">
        <XfInput :id="id" v-model="start" type="date" />
      </template>
    </XfField>
    <XfField label="结束日期">
      <template #default="{ id }">
        <XfInput :id="id" v-model="end" type="date" />
      </template>
    </XfField>
    <div class="xfm-toolbar__grow"></div>
    <span v-if="loading" class="xfm-muted">加载中…</span>
    <XfButton @click="load">查询</XfButton>
  </div>

  <p v-if="error" class="xfm-report__error" role="alert">{{ error }}</p>

  <p class="xfm-report__note">
    口径提示：历史订单仅归档在 <code>t_his_order</code> / <code>t_his_order_detail</code>；
    报表区间跨归档日期时，历史段数据可能不连续。
  </p>

  <section class="xfm-kpis" aria-label="经营 KPI">
    <XfKpi v-for="kpi in kpis" :key="kpi.key" :label="kpi.label" :value="kpi.value" />
  </section>

  <div class="xfm-cols">
    <XfPanel title="分时销售">
      <template #actions>
        <XfTabs v-model="metric" :tabs="metricTabs" />
      </template>
      <div class="xfm-chart" role="img" aria-label="分时销售柱状图">
        <div v-for="row in hourRows" :key="row.hour" class="xfm-chart__col" tabindex="0" :aria-label="`${row.hour}时 ${row.value}`">
          <span class="xfm-chart__value xf-num">{{ row.value }}</span>
          <div
            class="xfm-chart__bar"
            :style="{ height: `${Math.round((row.value / maxHour) * 100)}%` }"
          ></div>
          <span class="xfm-chart__hour xf-num">{{ row.hour }}时</span>
        </div>
        <div v-if="!hourHasData" class="xfm-chart__empty">该区间暂无分时数据</div>
      </div>
    </XfPanel>

    <div class="xfm-side">
      <XfPanel title="收款方式构成">
        <div v-for="row in payRows" :key="row.payType" class="xfm-rowline">
          <span>{{ row.payName }}</span>
          <span>
            <b class="xfm-num">{{ row.count }}</b> 笔 ·
            <span class="xfm-num">{{ row.ratio }}</span>
          </span>
        </div>
        <div v-if="payRows.length === 0" class="xfm-muted">该区间暂无收款数据</div>
        <div class="xfm-rowline" style="border-top: 1px solid var(--c-border); margin-top: 8px; padding-top: 8px">
          <span>合计</span>
          <b class="xfm-num">{{ totalCount }}</b>
        </div>
      </XfPanel>

      <XfPanel title="优惠统计">
        <div class="xfm-rowline"><span>赠菜金额</span><b class="xfm-num">{{ formatYuan(privilege.presentAmount) }}</b></div>
        <div class="xfm-rowline"><span>免单金额</span><b class="xfm-num">{{ formatYuan(privilege.freeAmount) }}</b></div>
        <div class="xfm-rowline"><span>抹零金额</span><b class="xfm-num">{{ formatYuan(privilege.smallAmount) }}</b></div>
      </XfPanel>
    </div>
  </div>

</template>
