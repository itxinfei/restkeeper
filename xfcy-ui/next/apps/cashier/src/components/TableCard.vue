<script setup lang="ts">
import { computed } from 'vue'
import { XfTag } from '@xfcy/ui'
import type { PosTable, TableStatus } from '../api/pos'

const props = defineProps<{ table: PosTable }>()
const emit = defineEmits<{ (e: 'select', table: PosTable): void }>()

/** 状态语义（DESIGN.md §2）：空闲=成功绿、开台=警告橙、锁定=中性灰，均附文字 */
const toneMap: Record<TableStatus, 'success' | 'warning' | 'neutral'> = {
  空闲: 'success',
  开台: 'warning',
  锁定: 'neutral',
}

const classMap: Record<TableStatus, string> = {
  空闲: 'is-free',
  开台: 'is-opened',
  锁定: 'is-locked',
}

const locked = computed(() => props.table.status === '锁定')
const tone = computed(() => toneMap[props.table.status])
const modifier = computed(() => classMap[props.table.status])

function onClick() {
  if (locked.value) return
  emit('select', props.table)
}
</script>

<template>
  <button
    type="button"
    class="xfc-tcard"
    :class="modifier"
    :disabled="locked"
    :aria-disabled="locked"
    @click="onClick"
  >
    <span class="xfc-tcard__name">{{ table.name }}</span>
    <span class="xfc-tcard__meta">{{ table.area }} · {{ table.seats }} 座</span>
    <span class="xfc-tcard__foot">
      <XfTag :tone="tone">{{ table.status }}</XfTag>
      <span v-if="locked" class="xfc-tcard__hint">暂不可用</span>
    </span>
  </button>
</template>
