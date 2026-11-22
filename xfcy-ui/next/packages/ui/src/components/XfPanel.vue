<script setup lang="ts">
import { useSlots } from 'vue'

defineProps<{ title?: string }>()

const slots = useSlots()
</script>

<template>
  <section class="xf-panel">
    <header v-if="title || slots.actions" class="xf-panel__header">
      <h3 class="xf-panel__title">{{ title }}</h3>
      <div class="xf-panel__actions">
        <slot name="actions" />
      </div>
    </header>
    <div class="xf-panel__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.xf-panel {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.xf-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--c-border);
}

.xf-panel__title {
  font-size: var(--text-body);
  font-weight: var(--weight-title);
  line-height: var(--leading-body);
  color: var(--c-text);
}

.xf-panel__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.xf-panel__body {
  padding: var(--space-4);
}
</style>
