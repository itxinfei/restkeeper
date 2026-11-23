<script setup lang="ts">
import { useToast } from '../toast'
import type { Tone } from '../tone'

const { toasts, dismiss } = useToast()

defineProps<{ role?: 'status' | 'alert' }>()

const ICONS: Record<Tone, string> = {
  success: 'M20 6 9 17l-5-5',
  warning: 'M12 9v4M12 17h.01M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z',
  danger: 'M18 6 6 18M6 6l12 12',
  info: 'M12 16v-4M12 8h.01M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0z',
  neutral: 'M20 6 9 17l-5-5',
  primary: 'M20 6 9 17l-5-5',
}
</script>

<template>
  <div class="xf-toast-region" :role="role ?? 'status'" aria-live="polite">
    <TransitionGroup name="xf-toast">
      <div
        v-for="item in toasts"
        :key="item.id"
        class="xf-toast"
        :class="`xf-toast--${item.tone}`"
      >
        <svg
          class="xf-toast__icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          aria-hidden="true"
        >
          <path :d="ICONS[item.tone]" />
        </svg>
        <span class="xf-toast__text">{{ item.text }}</span>
        <button
          type="button"
          class="xf-toast__close"
          aria-label="关闭提示"
          @click="dismiss(item.id)"
        >
          ×
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.xf-toast-region {
  position: fixed;
  right: var(--space-6);
  bottom: var(--space-6);
  z-index: 2000;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  max-width: 360px;
  pointer-events: none;
}

.xf-toast {
  pointer-events: auto;
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
  min-width: 220px;
  padding: 10px 12px;
  background: var(--c-surface);
  border: 1px solid var(--c-border-strong);
  border-left-width: 3px;
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-md);
  font-size: var(--text-body);
  line-height: var(--leading-compact);
  color: var(--c-text);
}

.xf-toast__icon {
  flex: none;
  width: 16px;
  height: 16px;
  margin-top: 1px;
}

.xf-toast__text {
  flex: 1 1 auto;
  min-width: 0;
}

.xf-toast__close {
  flex: none;
  border: none;
  background: none;
  padding: 0 2px;
  font-size: 16px;
  line-height: 1;
  color: var(--c-text-3);
  cursor: pointer;
}

.xf-toast__close:hover {
  color: var(--c-text);
}

.xf-toast__close:focus-visible {
  outline: none;
  box-shadow: var(--focus-ring);
  border-radius: var(--radius-xs);
}

.xf-toast--success {
  border-left-color: var(--c-success);
}
.xf-toast--success .xf-toast__icon {
  color: var(--c-success);
}
.xf-toast--warning {
  border-left-color: var(--c-warning);
}
.xf-toast--warning .xf-toast__icon {
  color: var(--c-warning);
}
.xf-toast--danger {
  border-left-color: var(--c-danger);
}
.xf-toast--danger .xf-toast__icon {
  color: var(--c-danger);
}
.xf-toast--info,
.xf-toast--primary {
  border-left-color: var(--c-info);
}
.xf-toast--info .xf-toast__icon,
.xf-toast--primary .xf-toast__icon {
  color: var(--c-info);
}
.xf-toast--neutral {
  border-left-color: var(--c-border-strong);
}
.xf-toast--neutral .xf-toast__icon {
  color: var(--c-text-3);
}

.xf-toast-enter-active,
.xf-toast-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.xf-toast-enter-from,
.xf-toast-leave-to {
  opacity: 0;
  transform: translateY(6px);
}
</style>
