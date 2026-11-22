<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { XfButton, XfEmpty, XfModal, XfTag } from '@xfcy/ui'
import { formatYuan } from '@xfcy/api'
import { useOrderStore } from '../stores/order'
import type { OrderStatus } from '../stores/order'

const order = useOrderStore()
const emit = defineEmits<{ (e: 'checkout'): void }>()

const statusTone = computed<'danger' | 'warning' | 'success'>(() => {
  const map: Record<OrderStatus, 'danger' | 'warning' | 'success'> = {
    未结: 'danger',
    挂账: 'warning',
    已结: 'success',
  }
  return map[order.status]
})

/* 轻提示（示意操作反馈） */
const notice = ref('')
let noticeTimer: number | undefined
function showNotice(text: string) {
  notice.value = text
  if (noticeTimer !== undefined) window.clearTimeout(noticeTimer)
  noticeTimer = window.setTimeout(() => {
    notice.value = ''
  }, 2500)
}
onBeforeUnmount(() => {
  if (noticeTimer !== undefined) window.clearTimeout(noticeTimer)
})

function onHang() {
  order.markCredit()
  showNotice('已挂账（示意），订单状态：挂账；真实模式在结账时选择「挂账」提交')
}

function onPrint() {
  showNotice('已发送后厨打印（示意）')
}

function onRemoveLast() {
  const last = order.items[order.items.length - 1]
  if (!last) {
    showNotice('订单为空，无可退菜品')
    return
  }
  order.removeLastItem()
  showNotice(`已退 ${last.name} ×1（示意）`)
}

/* 换台：仅可选择空闲桌台 */
const moveOpen = ref(false)
const freeTables = computed(() => order.tables.filter((t) => t.status === '空闲'))

async function onMove(id: string) {
  await order.moveTable(id)
  moveOpen.value = false
  showNotice(`已换台至 ${id}`)
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
      <XfTag :tone="statusTone">{{ order.status }}</XfTag>
    </header>

    <p v-if="notice" class="xfc-order__notice" role="status">{{ notice }}</p>

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
  </aside>
</template>
