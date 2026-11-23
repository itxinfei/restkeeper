<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { XfButton, XfEmpty, XfModal } from '@xfcy/ui'
import type { PosTable } from '../api/pos'
import { useOrderStore } from '../stores/order'
import TableCard from '../components/TableCard.vue'

const router = useRouter()
const order = useOrderStore()

const areaId = ref('all')
const areas = computed(() => order.areas)

const filteredTables = computed(() =>
  order.tables.filter((t) => areaId.value === 'all' || t.areaId === areaId.value),
)

const counts = computed<Record<string, number>>(() => {
  const result: Record<string, number> = { 空闲: 0, 开台: 0, 锁定: 0 }
  for (const table of order.tables) result[table.status] += 1
  return result
})

function areaCount(id: string): number {
  return order.tables.filter((t) => t.areaId === id).length
}

/* 空闲桌台：先选人数再进入点餐 */
const guestTable = ref<PosTable | null>(null)
const guestCount = ref(2)

function onSelect(table: PosTable) {
  if (table.status === '锁定') return
  if (table.status === '开台') {
    router.push({ name: 'order', params: { tableId: table.id } })
    return
  }
  guestCount.value = Math.max(1, Math.min(table.seats, 2))
  guestTable.value = table
}

async function confirmGuests() {
  const table = guestTable.value
  if (!table) return
  await order.openTable(table.id, guestCount.value)
  guestTable.value = null
  router.push({ name: 'order', params: { tableId: table.id } })
}

onMounted(() => {
  if (!order.loaded) void order.load()
})

function closeGuestModal() {
  guestTable.value = null
}
</script>

<template>
  <div class="xfc-tables">
    <div class="xfc-tables__bar">
      <div class="xfc-areas" role="tablist" aria-label="区域筛选">
        <button
          v-for="area in areas"
          :key="area.id"
          type="button"
          class="xfc-area"
          :class="{ 'is-active': areaId === area.id }"
          :aria-selected="areaId === area.id"
          role="tab"
          @click="areaId = area.id"
        >
          {{ area.name }}
          <span class="xfc-area__count xfc-num">{{ area.id === 'all' ? order.tables.length : areaCount(area.id) }}</span>
        </button>
      </div>

      <div class="xfc-legend">
        <span><i class="xfc-dot is-free" aria-hidden="true"></i>空闲 {{ counts['空闲'] }}</span>
        <span><i class="xfc-dot is-opened" aria-hidden="true"></i>开台 {{ counts['开台'] }}</span>
        <span><i class="xfc-dot is-locked" aria-hidden="true"></i>锁定 {{ counts['锁定'] }}</span>
      </div>
    </div>

    <div v-if="filteredTables.length" class="xfc-tgrid">
      <TableCard
        v-for="table in filteredTables"
        :key="table.id"
        :table="table"
        @select="onSelect"
      />
    </div>
    <XfEmpty
      v-else
      title="该区域暂无桌台"
      description="请先在管家端「桌台管理」中创建区域与桌台，或切换其他区域查看。"
    />

    <XfModal
      :open="guestTable !== null"
      :title="guestTable ? `开台 · ${guestTable.name}（示意）` : '开台（示意）'"
      width="420px"
      @update:open="closeGuestModal"
    >
      <div class="xfc-guests">
        <span class="xfc-guests__label">用餐人数</span>
        <div class="xfc-guests__stepper">
          <button
            type="button"
            class="xfc-step"
            aria-label="减少人数"
            :disabled="guestCount <= 1"
            @click="guestCount = Math.max(1, guestCount - 1)"
          >
            −
          </button>
          <b class="xfc-guests__num xfc-num">{{ guestCount }}</b>
          <button
            type="button"
            class="xfc-step"
            aria-label="增加人数"
            :disabled="guestCount >= (guestTable?.seats ?? 12)"
            @click="guestCount = Math.min(guestTable?.seats ?? 12, guestCount + 1)"
          >
            ＋
          </button>
        </div>
        <div class="xfc-guests__quick">
          <button
            v-for="n in [1, 2, 4, 6, 8, 10]"
            :key="n"
            type="button"
            class="xfc-guests__quickbtn"
            :class="{ 'is-active': guestCount === n }"
            @click="guestCount = n"
          >
            {{ n }} 人
          </button>
        </div>
      </div>

      <template #footer>
        <XfButton variant="secondary" size="lg" @click="closeGuestModal">取消</XfButton>
        <XfButton size="lg" @click="confirmGuests">进入点餐</XfButton>
      </template>
    </XfModal>
  </div>
</template>
