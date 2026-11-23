<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { XfButton, XfInput, XfModal, XfSelect } from '@xfcy/ui'
import { credit, formatYuan } from '@xfcy/api'
import type { Credit } from '@xfcy/api'
import type { CreditSelection, PayMethod } from '../stores/order'

const props = defineProps<{
  open: boolean
  total: number
  tableName: string
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'confirm', method: PayMethod, credit?: CreditSelection): void
}>()

const methods: { key: PayMethod; label: string }[] = [
  { key: '现金', label: '现金' },
  { key: '微信', label: '微信' },
  { key: '支付宝', label: '支付宝' },
  { key: '挂账', label: '挂账' },
]

const method = ref<PayMethod>('现金')
const received = ref('')
const error = ref('')

/* ---------------- 挂账主体 ---------------- */

const creditSubjects = ref<Credit[]>([])
const creditId = ref('')
const creditUserName = ref('')
const creditLoading = ref(false)

const creditOptions = computed(() =>
  creditSubjects.value.map((item) => ({
    label:
      item.creditType === 1
        ? `${item.companyName ?? '-'}（公司）`
        : `${item.userName ?? '-'}（个人）`,
    value: item.creditId ?? '',
  })),
)

const selectedCredit = computed(
  () => creditSubjects.value.find((item) => item.creditId === creditId.value) ?? null,
)

/** 与后端 payByCredit 同口径：停用 / 超额 / 缺姓名一律前置拦住 */
const creditProblem = computed(() => {
  if (method.value !== '挂账') return ''
  if (!creditId.value) return '请选择挂账主体'
  const subject = selectedCredit.value
  if (!subject) return '挂账主体不存在，请重新选择'
  if (subject.status !== 1) return '该挂账主体已停用，不允许挂账'
  if (!creditUserName.value.trim()) return '请填写挂账人姓名'
  if ((subject.creditAmount ?? 0) < props.total) {
    return `挂账金额超过剩余欠款额度（剩余 ${formatYuan(subject.creditAmount ?? 0)} 元）`
  }
  return ''
})

async function loadCredits() {
  if (creditLoading.value) return
  creditLoading.value = true
  try {
    const result = await credit.page(1, 200)
    creditSubjects.value = result.records.filter((item) => item.creditId)
    if (creditSubjects.value.length === 1) creditId.value = creditSubjects.value[0].creditId ?? ''
  } catch {
    creditSubjects.value = []
  } finally {
    creditLoading.value = false
  }
}

const receivedNum = computed(() => (received.value.trim() === '' ? 0 : Number(received.value) || 0))
/** 实收金额统一换算为「分」，与应收 total 对齐 */
const receivedCents = computed(() => Math.round(receivedNum.value * 100))
const change = computed(() => receivedCents.value - props.total)
const canConfirm = computed(
  () =>
    props.total > 0 &&
    (method.value !== '现金' || receivedCents.value >= props.total) &&
    !creditProblem.value,
)

watch(
  () => props.open,
  (open) => {
    if (open) {
      method.value = '现金'
      received.value = ''
      error.value = ''
      creditId.value = ''
      creditUserName.value = ''
      void loadCredits()
    }
  },
)

function close() {
  emit('update:open', false)
}

function confirm() {
  if (!canConfirm.value) {
    error.value = creditProblem.value || '实收金额不足'
    return
  }
  error.value = ''
  const subject = selectedCredit.value
  emit(
    'confirm',
    method.value,
    method.value === '挂账' && subject
      ? {
          creditId: subject.creditId ?? '',
          creditUserName: creditUserName.value.trim(),
          creditAmount: props.total,
        }
      : undefined,
  )
}
</script>

<template>
  <XfModal
    :open="open"
    :title="`结账 · ${tableName || '未选桌台'}`"
    width="520px"
    @update:open="close"
  >
    <div class="xfc-checkout">
      <div class="xfc-checkout__amount">
        <span>应收</span>
        <b class="xfc-checkout__total">{{ formatYuan(total) }}</b>
      </div>

      <div class="xfc-checkout__label">支付方式</div>
      <div class="xfc-checkout__methods" role="group" aria-label="支付方式">
        <button
          v-for="item in methods"
          :key="item.key"
          type="button"
          class="xfc-paymethod"
          :class="{ 'is-active': method === item.key }"
          :aria-pressed="method === item.key"
          @click="method = item.key"
        >
          {{ item.label }}
        </button>
      </div>

      <template v-if="method === '挂账'">
        <div class="xfc-checkout__label">挂账主体</div>
        <XfSelect
          v-model="creditId"
          :options="creditOptions"
          :placeholder="creditLoading ? '挂账主体加载中…' : '选择挂账主体'"
        />
        <XfInput
          v-model="creditUserName"
          placeholder="挂账人姓名（须为该公司关联用餐人或本人）"
        />
        <div v-if="selectedCredit" class="xfc-checkout__quota">
          <span>剩余欠款额度</span>
          <b class="xfc-num">{{ formatYuan(selectedCredit.creditAmount ?? 0) }}</b>
        </div>
      </template>

      <template v-if="method === '现金'">
        <XfInput
          v-model="received"
          prefix="¥"
          type="number"
          placeholder="输入实收金额"
        />
        <div class="xfc-checkout__change" :class="{ 'is-short': receivedCents < total }">
          <span>找零</span>
          <b class="xfc-num">{{ formatYuan(Math.max(0, change)) }}</b>
        </div>
      </template>
      <p v-if="error" class="xfc-checkout__error" role="alert">{{ error }}</p>
    </div>

    <template #footer>
      <XfButton variant="secondary" size="lg" @click="close">取消</XfButton>
      <XfButton size="lg" :disabled="!canConfirm" @click="confirm">确认结账</XfButton>
    </template>
  </XfModal>
</template>
