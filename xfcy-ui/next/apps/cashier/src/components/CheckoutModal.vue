<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { XfButton, XfInput, XfModal } from '@xfcy/ui'
import { formatYuan } from '@xfcy/api'
import type { PayMethod } from '../stores/order'

const props = defineProps<{
  open: boolean
  total: number
  tableName: string
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'confirm', method: PayMethod): void
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

const receivedNum = computed(() => (received.value.trim() === '' ? 0 : Number(received.value) || 0))
/** 实收金额统一换算为「分」，与应收 total 对齐 */
const receivedCents = computed(() => Math.round(receivedNum.value * 100))
const change = computed(() => receivedCents.value - props.total)
const canConfirm = computed(
  () => props.total > 0 && (method.value !== '现金' || receivedCents.value >= props.total),
)

watch(
  () => props.open,
  (open) => {
    if (open) {
      method.value = '现金'
      received.value = ''
      error.value = ''
    }
  },
)

function close() {
  emit('update:open', false)
}

function confirm() {
  if (!canConfirm.value) {
    error.value = '实收金额不足'
    return
  }
  error.value = ''
  emit('confirm', method.value)
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

      <template v-if="method === '现金'">
        <XfInput
          v-model="received"
          prefix="¥"
          type="number"
          placeholder="输入实收金额"
          :error="error"
        />
        <div class="xfc-checkout__change" :class="{ 'is-short': !canConfirm }">
          <span>找零</span>
          <b class="xfc-num">{{ formatYuan(Math.max(0, change)) }}</b>
        </div>
      </template>
      <p v-else-if="error" class="xfc-checkout__error" role="alert">{{ error }}</p>
    </div>

    <template #footer>
      <XfButton variant="secondary" size="lg" @click="close">取消</XfButton>
      <XfButton size="lg" :disabled="!canConfirm" @click="confirm">确认结账</XfButton>
    </template>
  </XfModal>
</template>
