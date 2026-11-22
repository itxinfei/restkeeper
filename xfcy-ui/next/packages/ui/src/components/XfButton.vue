<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    variant?: 'primary' | 'secondary' | 'ghost' | 'danger'
    size?: 'sm' | 'md' | 'lg' | 'xl'
    block?: boolean
    disabled?: boolean
    loading?: boolean
    type?: 'button' | 'submit' | 'reset'
  }>(),
  {
    variant: 'primary',
    size: 'md',
    block: false,
    disabled: false,
    loading: false,
    type: 'button',
  },
)

const emit = defineEmits<{ (e: 'click', event: MouseEvent): void }>()

const inactive = computed(() => props.disabled || props.loading)

function onClick(event: MouseEvent) {
  if (inactive.value) return
  emit('click', event)
}
</script>

<template>
  <button
    class="xf-btn"
    :class="[`xf-btn--${variant}`, `xf-btn--${size}`, { 'xf-btn--block': block }]"
    :type="type"
    :disabled="inactive"
    :aria-disabled="inactive"
    :aria-busy="loading || undefined"
    @click="onClick"
  >
    <span v-if="loading" class="xf-btn__spinner" aria-hidden="true"></span>
    <span class="xf-btn__content">
      <slot />
    </span>
  </button>
</template>

<style scoped>
.xf-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  font-family: var(--font-sans);
  font-weight: var(--weight-body);
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}

.xf-btn:focus-visible {
  outline: none;
  box-shadow: var(--focus-ring);
}

.xf-btn--block {
  display: flex;
  width: 100%;
}

/* 尺寸：DESIGN.md §6 按钮 */
.xf-btn--sm {
  height: 32px;
  padding: 6px 12px;
  font-size: var(--text-caption);
}

.xf-btn--md {
  height: 40px;
  padding: 8px 16px;
  font-size: var(--text-body);
}

.xf-btn--lg {
  height: 48px;
  padding: 12px 20px;
  font-size: var(--text-body);
  font-weight: var(--weight-emphasis);
}

.xf-btn--xl {
  height: 56px;
  padding: 14px 24px;
  font-size: var(--text-h3);
  font-weight: var(--weight-emphasis);
}

/* 类型 */
.xf-btn--primary {
  background: var(--c-primary);
  color: #ffffff;
}

.xf-btn--primary:not(:disabled):hover {
  background: var(--c-primary-hover);
}

.xf-btn--secondary {
  background: var(--c-surface);
  border-color: var(--c-border-strong);
  color: var(--c-text);
}

.xf-btn--secondary:not(:disabled):hover {
  background: var(--c-surface-2);
  border-color: var(--c-primary);
  color: var(--c-primary);
}

.xf-btn--ghost {
  background: transparent;
  color: var(--c-primary);
}

.xf-btn--ghost:not(:disabled):hover {
  background: var(--c-primary-soft);
}

.xf-btn--danger {
  background: var(--c-danger);
  color: #ffffff;
}

.xf-btn--danger:not(:disabled):hover {
  background: #991b1c;
}

/* 禁用态：--c-text-3 文字 + --c-bg 底（§6） */
.xf-btn:disabled {
  background: var(--c-bg);
  border-color: var(--c-border);
  color: var(--c-text-3);
  cursor: not-allowed;
}

.xf-btn__spinner {
  width: 14px;
  height: 14px;
  flex: none;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  opacity: 0.75;
  animation: xf-btn-spin 0.7s linear infinite;
}

@keyframes xf-btn-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
