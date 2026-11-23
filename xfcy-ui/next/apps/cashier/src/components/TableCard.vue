<script setup lang="ts">
import { computed } from 'vue'
import { XfTag, labelOf, toneOf } from '@xfcy/ui'
import type { PosTable, TableStatus } from '../api/pos'

const props = defineProps<{ table: PosTable }>()
const emit = defineEmits<{ (e: 'select', table: PosTable): void }>()

/** 状态语义统一走 toneOf('table')：空闲=中性、开台=主色、锁定=信息（均附文字） */
const classMap: Record<TableStatus, string> = {
  空闲: 'is-free',
  开台: 'is-opened',
  锁定: 'is-locked',
}

const locked = computed(() => props.table.status === '锁定')
const tone = computed(() => toneOf('table', props.table.status))
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
      <XfTag :tone="tone">{{ labelOf('table', table.status) }}</XfTag>
      <span v-if="locked" class="xfc-tcard__hint">暂不可用</span>
    </span>
  </button>
</template>
