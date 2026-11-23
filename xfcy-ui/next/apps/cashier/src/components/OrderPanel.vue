<script setup lang="ts">
import { computed, ref } from 'vue'
import { XfButton, XfEmpty, XfField, XfInput, XfModal, XfTag, labelOf, toneOf, useToast } from '@xfcy/ui'
import { formatYuan } from '@xfcy/api'
import type { OrderEntity } from '@xfcy/api'
import { useOrderStore } from '../stores/order'

const order = useOrderStore()
const { success, warning, error, info } = useToast()
const emit = defineEmits<{ (e: 'checkout'): void }>()

function errText(e: unknown) {
  return e instanceof Error && e.message ? e.message : '服务异常，请稍后重试'
}

function onHang() {
  order.markCredit()
  info('已挂账，订单状态：挂账；真实模式请在结账时选择「挂账」提交')
}

function onPrint() {
  info('已发送后厨打印（示意）')
}

/**
 * 退菜：真实模式调 order.returnDish；已付订单退菜只冲 totalAmount，
 * 会产生实收/应收差额，必须明确提示「差额需人工处理」。
 */
async function onRemoveLast() {
  const last = order.items[order.items.length - 1]
  if (!last) {
    info('订单为空，无可退菜品')
    return
  }
  try {
    const result = await order.removeLastItem()
    if (result === 'empty') {
      info('订单为空，无可退菜品')
    } else if (result === 'paid') {
      warning(`已退 ${last.name} ×1；该订单已结账，差额需人工处理（不自动退款）`)
    } else {
      success(`已退 ${last.name} ×1`)
    }
  } catch (e) {
    error(`退菜失败：${errText(e)}`)
  }
}

/* 换台：仅可选择空闲桌台 */
const moveOpen = ref(false)
const freeTables = computed(() => order.tables.filter((t) => t.status === '空闲'))

async function onMove(id: string) {
  try {
    await order.moveTable(id)
    moveOpen.value = false
    success(`已换台至 ${id}`)
  } catch (e) {
    error(`换台失败：${errText(e)}`)
  }
}

/* 反结账：仅已付订单；入口用流水号反查，确认框明确告知影响 */
const reverseOpen = ref(false)
const reverseNumber = ref('')
const reverseRemark = ref('')
const reverseTarget = ref<OrderEntity | null>(null)
const reverseBusy = ref(false)
const reverseError = ref('')

const reverseTableName = computed(() => {
  const target = reverseTarget.value
  if (!target?.tableId) return ''
  return order.tables.find((t) => t.id === target.tableId)?.name ?? target.tableId
})

function openReverse() {
  reverseNumber.value = ''
  reverseRemark.value = ''
  reverseTarget.value = null
  reverseError.value = ''
  reverseOpen.value = true
}

async function onLookupReverse() {
  reverseError.value = ''
  reverseTarget.value = null
  try {
    const found = await order.lookupPaidOrder(reverseNumber.value)
    if (!found) {
      reverseError.value = '未找到已结账订单，仅已付订单可反结账'
      return
    }
    reverseTarget.value = found
  } catch (e) {
    reverseError.value = `查询失败：${errText(e)}`
  }
}

async function onConfirmReverse() {
  const target = reverseTarget.value
  if (!target?.orderId) return
  reverseBusy.value = true
  try {
    const ok = await order.reverseOrder({
      orderId: target.orderId,
      remark: reverseRemark.value.trim() || undefined,
    })
    if (ok) {
      success(`反结账成功：${target.orderNumber ?? ''} 已恢复为未结账`)
      reverseOpen.value = false
    } else {
      error('反结账失败，请稍后重试')
    }
  } catch (e) {
    error(`反结账失败：${errText(e)}`)
  } finally {
    reverseBusy.value = false
  }
}
</script>

<template>
  <aside class="xfc-order" aria-label="订单面板">
    <header class="xfc-order__head">
      <div class="xfc-order__headtext">
        <h2 class="xfc-order__title">订单明细</h2>
        <span v-if="order.currentTable" class="xfc-order__table">
          {{ order.currentTable.name }} · {{ order.currentTable.area }} · {{ order.guests }} 人
        </span>
      </div>
      <XfTag :tone="toneOf('pay', order.status)">{{ labelOf('pay', order.status) }}</XfTag>
    </header>

    <div class="xfc-order__lines">
      <XfEmpty
        v-if="order.isEmpty"
        title="尚未点餐"
        description="点击左侧菜品加入订单（示意）"
      />
      <div v-for="item in order.items" :key="item.dishId" class="xfc-line">
        <div class="xfc-line__info">
          <span class="xfc-line__name">{{ item.name }}</span>
          <span class="xfc-line__sub xfc-num">{{ formatYuan(item.price) }} / 份</span>
        </div>
        <div class="xfc-line__qty">
          <button
            type="button"
            class="xfc-step"
            :aria-label="`减少 ${item.name}`"
            @click="order.decQty(item.dishId)"
          >
            −
          </button>
          <b class="xfc-line__num xfc-num">{{ item.qty }}</b>
          <button
            type="button"
            class="xfc-step"
            :aria-label="`增加 ${item.name}`"
            @click="order.incQty(item.dishId)"
          >
            ＋
          </button>
        </div>
        <b class="xfc-line__sum xfc-num">{{ formatYuan(item.price * item.qty) }}</b>
      </div>
    </div>

    <div class="xfc-order__total">
      <div class="xfc-row">
        <span>数量合计</span>
        <b class="xfc-num">{{ order.itemCount }} 份</b>
      </div>
      <div class="xfc-row">
        <span>优惠</span>
        <b class="xfc-num is-success">−{{ formatYuan(order.discount) }}</b>
      </div>
      <div class="xfc-row xfc-row--big">
        <span>应收</span>
        <b class="xfc-num">{{ formatYuan(order.total) }}</b>
      </div>
    </div>

    <div class="xfc-order__ops">
      <XfButton variant="secondary" size="lg" @click="onHang">挂账</XfButton>
      <XfButton variant="secondary" size="lg" @click="moveOpen = true">换台</XfButton>
      <XfButton variant="secondary" size="lg" @click="onRemoveLast">退菜</XfButton>
      <XfButton variant="secondary" size="lg" @click="onPrint">打印</XfButton>
      <XfButton variant="secondary" size="lg" @click="openReverse">反结账</XfButton>
      <XfButton
        size="xl"
        block
        class="xfc-pay-btn"
        :disabled="order.isEmpty"
        @click="emit('checkout')"
      >
        结账 {{ formatYuan(order.total) }}
      </XfButton>
    </div>

    <XfModal
      :open="moveOpen"
      title="换台（示意）"
      width="440px"
      @update:open="moveOpen = $event"
    >
      <p class="xfc-order__muted">仅可选择空闲桌台，原桌台将转为空闲。</p>
      <div class="xfc-movetables">
        <button
          v-for="table in freeTables"
          :key="table.id"
          type="button"
          class="xfc-movetable"
          @click="onMove(table.id)"
        >
          <b>{{ table.name }}</b>
          <span>{{ table.area }} · {{ table.seats }} 座</span>
        </button>
        <p v-if="freeTables.length === 0" class="xfc-order__muted">当前没有空闲桌台（示意）</p>
      </div>
    </XfModal>

    <XfModal
      v-model:open="reverseOpen"
      title="反结账（仅已结账订单）"
      width="520px"
    >
      <div class="xfc-reverse">
        <XfField label="订单流水号" required>
          <template #default="{ id }">
            <div class="xfc-reverse__lookup">
              <XfInput
                :id="id"
                v-model="reverseNumber"
                placeholder="输入已结账订单流水号"
                @keyup.enter="onLookupReverse"
              />
              <XfButton variant="secondary" :disabled="reverseBusy" @click="onLookupReverse">
                查询
              </XfButton>
            </div>
          </template>
        </XfField>

        <p v-if="reverseError" class="xfc-reverse__error" role="alert">{{ reverseError }}</p>

        <div v-if="reverseTarget" class="xfc-reverse__summary">
          <div class="xfc-row">
            <span>流水号</span>
            <b>{{ reverseTarget.orderNumber || '—' }}</b>
          </div>
          <div class="xfc-row">
            <span>桌台</span>
            <b>{{ reverseTableName || '—' }}</b>
          </div>
          <div class="xfc-row">
            <span>实收金额</span>
            <b class="xfc-num">
              {{ formatYuan(reverseTarget.payAmount ?? reverseTarget.totalAmount ?? 0) }}
            </b>
          </div>
        </div>

        <XfField label="反结账原因">
          <template #default="{ id }">
            <textarea
              :id="id"
              v-model="reverseRemark"
              class="xfc-reverse__textarea"
              rows="3"
              placeholder="选填，如：收银员操作失误"
            ></textarea>
          </template>
        </XfField>

        <p class="xfc-reverse__warn" role="alert">
          反结账仅把订单支付状态恢复为「未结账」，不会恢复桌台状态、不会回冲沽清数量，也不会自动退款。
        </p>
      </div>

      <template #footer>
        <XfButton variant="secondary" :disabled="reverseBusy" @click="reverseOpen = false">
          取消
        </XfButton>
        <XfButton :disabled="!reverseTarget || reverseBusy" @click="onConfirmReverse">
          确认反结账
        </XfButton>
      </template>
    </XfModal>
  </aside>
</template>
