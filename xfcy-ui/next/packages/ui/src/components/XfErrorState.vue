<script setup lang="ts">
import XfButton from './XfButton.vue'

withDefaults(
  defineProps<{ title?: string; message?: string }>(),
  { title: '加载失败', message: '' },
)

const emit = defineEmits<{ (e: 'retry'): void }>()
</script>

<template>
  <div class="xf-error">
    <svg
      class="xf-error__icon"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="1.8"
      stroke-linecap="round"
      stroke-linejoin="round"
      aria-hidden="true"
    >
      <path d="M12 9v4M12 17h.01M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z" />
    </svg>
    <p class="xf-error__title">{{ title }}</p>
    <p v-if="message" class="xf-error__msg">{{ message }}</p>
    <XfButton size="sm" variant="secondary" @click="emit('retry')">重试</XfButton>
  </div>
</template>

<style scoped>
.xf-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-8) var(--space-4);
  text-align: center;
}

.xf-error__icon {
  width: 28px;
  height: 28px;
  color: var(--c-text-3);
}

.xf-error__title {
  font-size: var(--text-body);
  font-weight: var(--weight-title);
  color: var(--c-text);
}

.xf-error__msg {
  font-size: var(--text-caption);
  color: var(--c-text-2);
  word-break: break-word;
}
</style>
