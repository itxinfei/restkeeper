<script setup lang="ts">
withDefaults(
  defineProps<{
    tone?: 'success' | 'warning' | 'danger' | 'info' | 'neutral' | 'primary'
  }>(),
  { tone: 'neutral' },
)
</script>

<template>
  <span class="xf-tag" :class="`xf-tag--${tone}`"><slot /></span>
</template>

<style scoped>
.xf-tag {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 2px 8px;
  border: 1px solid transparent;
  border-radius: var(--radius-pill);
  font-size: var(--text-mini);
  font-weight: var(--weight-body);
  line-height: 1.2;
  white-space: nowrap;
}

.xf-tag--success {
  background: var(--c-success-soft);
  border-color: var(--c-success);
  color: var(--c-success);
}

.xf-tag--warning {
  background: var(--c-warning-soft);
  border-color: var(--c-warning);
  color: var(--c-warning);
}

.xf-tag--danger {
  background: var(--c-danger-soft);
  border-color: var(--c-danger);
  color: var(--c-danger);
}

/* 规范未单列 info 淡底色，由 --c-info 派生，避免引入新色值 */
.xf-tag--info {
  background: color-mix(in srgb, var(--c-info) 8%, #ffffff);
  border-color: var(--c-info);
  color: var(--c-info);
}

.xf-tag--neutral {
  background: var(--c-neutral-soft);
  border-color: var(--c-border);
  color: var(--c-text-2);
}

.xf-tag--primary {
  background: var(--c-primary-soft);
  border-color: var(--c-primary);
  color: var(--c-primary);
}
</style>
