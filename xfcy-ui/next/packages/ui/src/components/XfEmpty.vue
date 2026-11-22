<script setup lang="ts">
import { useSlots } from 'vue'

withDefaults(
  defineProps<{
    title?: string
    description?: string
  }>(),
  { title: '', description: '' },
)

const slots = useSlots()
</script>

<template>
  <div class="xf-empty">
    <div class="xf-empty__icon">
      <slot name="icon">
        <svg width="48" height="48" viewBox="0 0 48 48" fill="none" aria-hidden="true">
          <rect x="9" y="11" width="30" height="24" rx="4" stroke="currentColor" stroke-width="2" />
          <path d="M9 21h30" stroke="currentColor" stroke-width="2" />
          <path d="M18 29h12" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
        </svg>
      </slot>
    </div>
    <p v-if="title" class="xf-empty__title">{{ title }}</p>
    <p v-if="description" class="xf-empty__description">{{ description }}</p>
    <div v-if="slots.action" class="xf-empty__action">
      <slot name="action" />
    </div>
  </div>
</template>

<style scoped>
.xf-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-10) var(--space-4);
  text-align: center;
}

.xf-empty__icon {
  width: 48px;
  height: 48px;
  color: var(--c-text-3);
}

.xf-empty__title {
  font-size: var(--text-h3);
  font-weight: var(--weight-title);
  line-height: var(--leading-title);
  color: var(--c-text);
}

.xf-empty__description {
  font-size: var(--text-caption);
  line-height: var(--leading-body);
  color: var(--c-text-2);
}

.xf-empty__action {
  margin-top: var(--space-2);
}
</style>
