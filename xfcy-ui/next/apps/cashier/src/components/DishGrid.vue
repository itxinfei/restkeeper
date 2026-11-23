<script setup lang="ts">
import { XfTag, remainLabel, remainTone } from '@xfcy/ui'
import { formatYuan } from '@xfcy/api'
import type { PosDish } from '../api/pos'

defineProps<{ dishes: PosDish[] }>()
const emit = defineEmits<{ (e: 'add', dish: PosDish): void }>()

function soldOut(dish: PosDish): boolean {
  return dish.status === '停售' || dish.remain === 0
}

function remainText(dish: PosDish): string {
  return remainLabel(dish.remain, dish.status === '停售')
}

function remainTagTone(dish: PosDish) {
  return remainTone(dish.remain, dish.status === '停售')
}

function onAdd(dish: PosDish) {
  if (soldOut(dish)) return
  emit('add', dish)
}
</script>

<template>
  <div class="xfc-dgrid">
    <button
      v-for="dish in dishes"
      :key="dish.id"
      type="button"
      class="xfc-dish"
      :class="{ 'is-disabled': soldOut(dish) }"
      :disabled="soldOut(dish)"
      :aria-disabled="soldOut(dish)"
      @click="onAdd(dish)"
    >
      <span class="xfc-dish__thumb" aria-hidden="true">{{ dish.thumb }}</span>
      <span class="xfc-dish__name">{{ dish.name }}</span>
      <span class="xfc-dish__desc">{{ dish.desc }}</span>
      <span class="xfc-dish__row">
        <span class="xfc-dish__price">{{ formatYuan(dish.price) }}</span>
        <XfTag :tone="remainTagTone(dish)">{{ remainText(dish) }}</XfTag>
      </span>
    </button>
  </div>
</template>
