<script setup lang="ts">
import { XfTag } from '@xfcy/ui'
import { formatYuan } from '../api/mock'
import type { Dish } from '../api/mock'

defineProps<{ dishes: Dish[] }>()
const emit = defineEmits<{ (e: 'add', dish: Dish): void }>()

function soldOut(dish: Dish): boolean {
  return dish.status === '停售' || dish.remain === 0
}

function remainText(dish: Dish): string {
  if (dish.status === '停售') return '停售'
  if (dish.remain === 0) return '沽清'
  if (dish.remain === null) return '不限'
  return `余 ${dish.remain}`
}

function remainTone(dish: Dish): 'danger' | 'neutral' {
  if (dish.status === '停售') return 'danger'
  return 'neutral'
}

function onAdd(dish: Dish) {
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
        <XfTag :tone="remainTone(dish)">{{ remainText(dish) }}</XfTag>
      </span>
    </button>
  </div>
</template>
