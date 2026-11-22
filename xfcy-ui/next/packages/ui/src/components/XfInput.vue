<script setup lang="ts">
import { computed, useId } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue?: string | number
    type?: string
    placeholder?: string
    disabled?: boolean
    /** 传入字符串作为错误文案；true 仅表示错误态 */
    error?: string | boolean
    /** 输入前缀，金额场景传 ¥ */
    prefix?: string
    autocomplete?: string
    id?: string
  }>(),
  {
    type: 'text',
    placeholder: '',
    disabled: false,
    error: false,
    prefix: '',
    autocomplete: 'off',
    id: '',
  },
)

const emit = defineEmits<{ (e: 'update:modelValue', value: string): void }>()

const uid = useId().replace(/[^a-zA-Z0-9]/g, '')
const inputId = computed(() => props.id || `xf-input-${uid}`)
const errorId = computed(() => `${inputId.value}-error`)

const hasError = computed(() => !!props.error)
const errorMessage = computed(() => (typeof props.error === 'string' ? props.error : ''))

function onInput(event: Event) {
  emit('update:modelValue', (event.target as HTMLInputElement).value)
}
</script>

<template>
  <div class="xf-input" :class="{ 'is-disabled': disabled, 'has-error': hasError }">
    <div class="xf-input__row">
      <span v-if="prefix" class="xf-input__prefix" aria-hidden="true">{{ prefix }}</span>
      <input
        :id="inputId"
        class="xf-input__control"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :autocomplete="autocomplete"
        :aria-invalid="hasError"
        :aria-describedby="errorMessage ? errorId : undefined"
        @input="onInput"
      />
    </div>
    <p v-if="errorMessage" :id="errorId" class="xf-input__error" role="alert">
      {{ errorMessage }}
    </p>
  </div>
</template>

<style scoped>
.xf-input__row {
  display: flex;
  align-items: center;
  height: var(--control-height);
  padding: 0 var(--space-3);
  background: var(--c-surface);
  border: 1px solid var(--c-border-strong);
  border-radius: var(--radius-sm);
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background-color 0.15s ease;
}

.xf-input:focus-within .xf-input__row {
  border-color: var(--c-primary);
  box-shadow: var(--focus-ring);
}

.xf-input__prefix {
  flex: none;
  margin-right: var(--space-2);
  padding-right: var(--space-2);
  color: var(--c-text-3);
  font-size: var(--text-body);
  border-right: 1px solid var(--c-border);
}

.xf-input__control {
  flex: 1 1 auto;
  min-width: 0;
  height: 100%;
  padding: 0;
  border: none;
  outline: none;
  background: transparent;
  color: var(--c-text);
  font-size: var(--text-body);
  font-variant-numeric: tabular-nums;
}

.xf-input__control::placeholder {
  color: var(--c-text-3);
}

.xf-input.is-disabled .xf-input__row {
  background: var(--c-bg);
  cursor: not-allowed;
}

.xf-input.is-disabled .xf-input__control {
  color: var(--c-text-3);
  cursor: not-allowed;
}

.xf-input.has-error .xf-input__row {
  border-color: var(--c-danger);
}

.xf-input.has-error:focus-within .xf-input__row {
  border-color: var(--c-danger);
  box-shadow: 0 0 0 2px var(--c-danger-soft);
}

.xf-input__error {
  margin-top: var(--space-1);
  color: var(--c-danger);
  font-size: var(--text-caption);
  line-height: var(--leading-body);
}
</style>
