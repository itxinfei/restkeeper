<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    label: string
    value: string | number
    trend?: 'up' | 'down' | 'flat'
    trendText?: string
  }>(),
  { trend: 'flat', trendText: '' },
)

const trendMeta = computed(() => {
  if (props.trend === 'up') return { class: 'is-up', arrow: '↑', text: '上升' }
  if (props.trend === 'down') return { class: 'is-down', arrow: '↓', text: '下降' }
  return { class: 'is-flat', arrow: '—', text: '持平' }
})
</script>

<template>
  <div class="xf-kpi" role="group" :aria-label="label">
    <div class="xf-kpi__label">{{ label }}</div>
    <div class="xf-kpi__value xf-num">{{ value }}</div>
    <div class="xf-kpi__trend" :class="trendMeta.class" :aria-label="`同比${trendMeta.text}`">
      <span class="xf-kpi__arrow" aria-hidden="true">{{ trendMeta.arrow }}</span>
      <span>{{ trendText || '同比' }}</span>
    </div>
  </div>
</template>

<style scoped>
.xf-kpi {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  padding: var(--space-4);
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.xf-kpi__label {
  font-size: var(--text-caption);
  font-weight: var(--weight-body);
  line-height: var(--leading-body);
  color: var(--c-text-2);
}

.xf-kpi__value {
  font-size: var(--text-h1);
  font-weight: var(--weight-strong);
  line-height: var(--leading-title);
  color: var(--c-text);
}

.xf-kpi__trend {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-caption);
  line-height: var(--leading-body);
}

.xf-kpi__trend.is-up {
  color: var(--c-success);
}

.xf-kpi__trend.is-down {
  color: var(--c-danger);
}

.xf-kpi__trend.is-flat {
  color: var(--c-text-3);
}
</style>
