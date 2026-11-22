<script setup lang="ts">
import { ref } from 'vue'
import {
  XfButton,
  XfEmpty,
  XfField,
  XfInput,
  XfKpi,
  XfModal,
  XfPagination,
  XfPanel,
  XfSelect,
  XfTable,
  XfTabs,
  XfTag,
} from '@xfcy/ui'

const navItems = [
  { id: 'tokens', label: '色彩 token' },
  { id: 'type', label: '字阶' },
  { id: 'shape', label: '圆角 / 阴影' },
  { id: 'button', label: '按钮' },
  { id: 'tag', label: '标签' },
  { id: 'kpi', label: 'KPI' },
  { id: 'panel', label: 'Panel + Table' },
  { id: 'tabs', label: 'Tabs' },
  { id: 'modal', label: 'Modal' },
  { id: 'empty', label: 'Empty' },
  { id: 'pagination', label: 'Pagination' },
  { id: 'form', label: 'Form' },
]

const colorTokens = [
  { name: '--c-bg', value: '#f7f8fa' },
  { name: '--c-surface', value: '#ffffff' },
  { name: '--c-surface-2', value: '#f9fafb' },
  { name: '--c-border', value: '#e5e7eb' },
  { name: '--c-border-strong', value: '#d1d5db' },
  { name: '--c-text', value: '#111827' },
  { name: '--c-text-2', value: '#4b5563' },
  { name: '--c-text-3', value: '#6b7280' },
  { name: '--c-primary', value: '#2563eb' },
  { name: '--c-primary-hover', value: '#1d4ed8' },
  { name: '--c-primary-soft', value: '#eff6ff' },
  { name: '--c-brand', value: '#c2410c' },
  { name: '--c-brand-hover', value: '#9a3412' },
  { name: '--c-brand-soft', value: '#fff7ed' },
  { name: '--c-success', value: '#047857' },
  { name: '--c-success-soft', value: '#ecfdf5' },
  { name: '--c-warning', value: '#b45309' },
  { name: '--c-warning-soft', value: '#fffbeb' },
  { name: '--c-danger', value: '#b91c1c' },
  { name: '--c-danger-soft', value: '#fef2f2' },
  { name: '--c-info', value: '#0369a1' },
  { name: '--c-neutral-soft', value: '#f3f4f6' },
]

const typeScale = [
  { name: 'Display', spec: '32px / 700', sample: '今日经营', style: { fontSize: '32px', fontWeight: 700, lineHeight: 1.3 } },
  { name: 'H1', spec: '24px / 600', sample: '页面标题', style: { fontSize: '24px', fontWeight: 600, lineHeight: 1.3 } },
  { name: 'H2', spec: '20px / 600', sample: '区块标题', style: { fontSize: '20px', fontWeight: 600, lineHeight: 1.3 } },
  { name: 'H3', spec: '16px / 600', sample: '小组标题', style: { fontSize: '16px', fontWeight: 600, lineHeight: 1.3 } },
  { name: 'Body', spec: '14px / 400', sample: '正文内容', style: { fontSize: '14px', fontWeight: 400, lineHeight: 1.5 } },
  { name: 'Caption', spec: '13px / 400', sample: '辅助说明', style: { fontSize: '13px', fontWeight: 400, lineHeight: 1.5 } },
  { name: 'Mini', spec: '12px / 400', sample: '标签 / 表格辅助', style: { fontSize: '12px', fontWeight: 400, lineHeight: 1.4 } },
]

const radiusTokens = [
  { name: '--radius-xs', value: '4px' },
  { name: '--radius-sm', value: '6px' },
  { name: '--radius-md', value: '8px' },
  { name: '--radius-lg', value: '12px' },
  { name: '--radius-pill', value: '999px' },
]

const shadowTokens = [
  { name: '--shadow-sm', value: '0 1px 2px rgba(17,24,39,.04)' },
  { name: '--shadow-md', value: '0 4px 12px rgba(17,24,39,.08)' },
  { name: '--shadow-lg', value: '0 12px 32px rgba(17,24,39,.14)' },
]

const buttonVariants = ['primary', 'secondary', 'ghost', 'danger'] as const
const buttonSizes = ['sm', 'md', 'lg', 'xl'] as const
const tagTones = ['success', 'warning', 'danger', 'info', 'neutral', 'primary'] as const

const columns = [
  { key: 'orderNo', label: '单号', sortable: true },
  { key: 'amount', label: '金额', align: 'right' as const, sortable: true },
  { key: 'status', label: '状态' },
  { key: 'time', label: '时间' },
]

const tableData = [
  { id: 1, orderNo: 'FK20261122-018', amount: '¥328.00', status: '已结账', time: '2026-11-22 12:30' },
  { id: 2, orderNo: 'FK20261122-019', amount: '¥86.50', status: '挂账', time: '2026-11-22 12:42' },
  { id: 3, orderNo: 'FK20261122-020', amount: '¥152.00', status: '未结', time: '2026-11-22 13:05' },
  { id: 4, orderNo: 'FK20261122-021', amount: '¥46.00', status: '已结账', time: '2026-11-22 13:20' },
]

function statusTone(status: string): 'success' | 'warning' | 'danger' | 'neutral' {
  if (status === '已结账') return 'success'
  if (status === '挂账') return 'warning'
  if (status === '未结') return 'danger'
  return 'neutral'
}

const activeTab = ref<string>('order')
const tabs = [
  { key: 'order', label: '订单明细' },
  { key: 'dish', label: '菜品' },
  { key: 'member', label: '会员' },
]

const modalOpen = ref(false)

const total = ref(86)
const page = ref(1)
const pageSize = 10

const form = ref({ table: '', amount: '', channel: null as string | null })
const formError = ref('')
const channels = [
  { label: '堂食', value: 'dine-in' },
  { label: '外卖', value: 'takeaway' },
  { label: '自提', value: 'pickup' },
]

function submitForm() {
  formError.value = form.value.table ? '' : '请填写桌台号'
}
</script>

<template>
  <div class="playground">
    <nav class="playground__nav" aria-label="分区导航">
      <span class="playground__brand">心飞餐饮 UI</span>
      <a v-for="item in navItems" :key="item.id" :href="`#${item.id}`">{{ item.label }}</a>
    </nav>

    <main class="playground__main">
      <section id="tokens" class="playground__section">
        <h2 class="playground__section-title">色彩 token</h2>
        <p class="playground__note">来源 DESIGN.md §2，语义固定；组件只引用变量，不写死色值。</p>
        <div class="playground__grid playground__grid--tokens">
          <div v-for="token in colorTokens" :key="token.name" class="swatch">
            <div class="swatch__color" :style="{ background: token.value }"></div>
            <div class="swatch__meta">
              <div class="swatch__name">{{ token.name }}</div>
              <div class="swatch__value">{{ token.value }}</div>
            </div>
          </div>
        </div>
      </section>

      <section id="type" class="playground__section">
        <h2 class="playground__section-title">字阶</h2>
        <p class="playground__note">字号 12 / 13 / 14 / 16 / 18 / 20 / 24 / 28 / 32；行高与字重见 DESIGN.md §3。</p>
        <div class="playground__grid playground__grid--types">
          <div v-for="item in typeScale" :key="item.name" class="type-sample">
            <div :style="item.style">{{ item.sample }}</div>
            <div class="type-sample__meta">{{ item.name }} · {{ item.spec }}</div>
          </div>
        </div>
      </section>

      <section id="shape" class="playground__section">
        <h2 class="playground__section-title">圆角 / 阴影</h2>
        <div class="playground__row">
          <div
            v-for="item in radiusTokens"
            :key="item.name"
            class="type-sample"
            :style="{ borderRadius: item.value, padding: '16px 24px' }"
          >
            <div class="type-sample__meta">{{ item.name }} · {{ item.value }}</div>
          </div>
        </div>
        <div class="playground__row">
          <div
            v-for="item in shadowTokens"
            :key="item.name"
            class="type-sample"
            :style="{ boxShadow: item.value, padding: '16px 24px' }"
          >
            <div class="type-sample__meta">{{ item.name }}</div>
          </div>
        </div>
      </section>

      <section id="button" class="playground__section">
        <h2 class="playground__section-title">按钮</h2>
        <div class="playground__row">
          <XfButton v-for="variant in buttonVariants" :key="variant" :variant="variant">
            {{ variant }}
          </XfButton>
        </div>
        <div class="playground__row">
          <XfButton v-for="size in buttonSizes" :key="size" :size="size">尺寸 {{ size }}</XfButton>
        </div>
        <div class="playground__row">
          <XfButton loading>加载中</XfButton>
          <XfButton disabled>禁用</XfButton>
          <XfButton block>块级按钮</XfButton>
        </div>
      </section>

      <section id="tag" class="playground__section">
        <h2 class="playground__section-title">标签</h2>
        <div class="playground__row">
          <XfTag v-for="tone in tagTones" :key="tone" :tone="tone">{{ tone }}</XfTag>
        </div>
        <div class="playground__row">
          <XfTag tone="success">已结账</XfTag>
          <XfTag tone="warning">挂账</XfTag>
          <XfTag tone="danger">未结 / 停售</XfTag>
          <XfTag tone="neutral">沽清</XfTag>
          <XfTag tone="info">进行中</XfTag>
          <XfTag tone="primary">启用</XfTag>
        </div>
      </section>

      <section id="kpi" class="playground__section">
        <h2 class="playground__section-title">KPI</h2>
        <p class="playground__note">以下为演示示意数据，非真实经营数据。</p>
        <div class="playground__grid playground__grid--kpi">
          <XfKpi label="今日营业额（示意）" value="¥12,860.00" trend="up" trend-text="+3.2%" />
          <XfKpi label="订单数（示意）" value="186" trend="up" trend-text="+12" />
          <XfKpi label="客单价（示意）" value="¥69.14" trend="down" trend-text="-1.8%" />
          <XfKpi label="翻台率（示意）" value="2.4" trend="flat" trend-text="持平" />
        </div>
      </section>

      <section id="panel" class="playground__section">
        <h2 class="playground__section-title">Panel + Table</h2>
        <p class="playground__note">示例数据均为演示示意，禁止对外表述为真实经营数据。</p>
        <XfPanel title="订单列表（示意）">
          <template #actions>
            <XfButton size="sm" variant="secondary">导出</XfButton>
          </template>
          <XfTable :columns="columns" :data="tableData" row-key="id">
            <template #amount="{ row }">
              <span class="xf-num">{{ row.amount }}</span>
            </template>
            <template #status="{ row }">
              <XfTag :tone="statusTone(String(row.status))">{{ row.status }}</XfTag>
            </template>
          </XfTable>
        </XfPanel>
      </section>

      <section id="tabs" class="playground__section">
        <h2 class="playground__section-title">Tabs</h2>
        <XfTabs v-model="activeTab" :tabs="tabs" />
        <p class="playground__note">当前选中：{{ activeTab }}（下划线式，支持方向键切换）</p>
      </section>

      <section id="modal" class="playground__section">
        <h2 class="playground__section-title">Modal</h2>
        <XfButton @click="modalOpen = true">打开弹窗</XfButton>
        <XfModal v-model:open="modalOpen" title="订单详情（示意）">
          <p class="playground__note">单号 FK20261122-018，金额 ¥328.00，状态：已结账。</p>
          <template #footer>
            <XfButton variant="secondary" @click="modalOpen = false">取消</XfButton>
            <XfButton @click="modalOpen = false">确认</XfButton>
          </template>
        </XfModal>
      </section>

      <section id="empty" class="playground__section">
        <h2 class="playground__section-title">Empty</h2>
        <XfPanel>
          <XfEmpty title="暂无订单" description="当前筛选条件下没有数据，请调整筛选条件后重试。">
            <template #action>
              <XfButton variant="secondary">清除筛选</XfButton>
            </template>
          </XfEmpty>
        </XfPanel>
      </section>

      <section id="pagination" class="playground__section">
        <h2 class="playground__section-title">Pagination</h2>
        <XfPagination v-model:page="page" :total="total" :page-size="pageSize" />
      </section>

      <section id="form" class="playground__section">
        <h2 class="playground__section-title">Form</h2>
        <XfPanel>
          <div class="form-grid">
            <XfField label="桌台号" required>
              <template #default="{ id }">
                <XfInput :id="id" v-model="form.table" placeholder="如 A12" />
              </template>
            </XfField>
            <XfField label="金额" required :error="formError || ''">
              <template #default="{ id }">
                <XfInput
                  :id="id"
                  v-model="form.amount"
                  prefix="¥"
                  placeholder="0.00"
                  :error="!!formError"
                />
              </template>
            </XfField>
            <XfField label="渠道">
              <template #default="{ id }">
                <XfSelect :id="id" v-model="form.channel" :options="channels" />
              </template>
            </XfField>
          </div>
          <div class="playground__row" style="margin-top: var(--space-4)">
            <XfButton @click="submitForm">提交（演示）</XfButton>
          </div>
        </XfPanel>
      </section>
    </main>
  </div>
</template>
