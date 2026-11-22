<script setup lang="ts">
import { computed } from 'vue'

type OptionValue = string | number
type Option = OptionValue | { label: string; value: OptionValue }

const props = withDefaults(
  defineProps<{
    modelValue?: OptionValue | null
    options?: Option[]
    disabled?: boolean
    placeholder?: string
    id?: string
  }>(),
  {
    modelValue: null,
    options: () => [],
    disabled: false,
    placeholder: '请选择',
    id: '',
  },
)

const emit = defineEmits<{ (e: 'update:modelValue', value: OptionValue | null): void }>()

interface NormalizedOption {
  label: string
  value: OptionValue
}

function normalize(option: Option): NormalizedOption {
  if (option !== null && typeof option === 'object') {
    return { label: option.label, value: option.value }
  }
  return { label: String(option), value: option as OptionValue }
}

const normalizedOptions = computed<NormalizedOption[]>(() => props.options.map(normalize))

const hasValue = computed(() => props.modelValue !== null && props.modelValue !== '')

function onChange(event: Event) {
  const value = (event.target as HTMLSelectElement).value
  const match = normalizedOptions.value.find((option) => String(option.value) === value)
  emit('update:modelValue', match ? match.value : null)
}
</script>

<template>
  <div class="xf-select" :class="{ 'is-disabled': disabled, 'is-placeholder': !hasValue }">
    <select
      :id="id || undefined"
      class="xf-select__control"
      :value="modelValue ?? ''"
      :disabled="disabled"
      @change="onChange"
    >
      <option value="" disabled>{{ placeholder }}</option>
      <option
        v-for="option in normalizedOptions"
        :key="String(option.value)"
        :value="String(option.value)"
      >
        {{ option.label }}
      </option>
    </select>
    <span class="xf-select__arrow" aria-hidden="true"></span>
  </div>
</template>

<style scoped>
.xf-select {
  position: relative;
  display: inline-flex;
  align-items: center;
  width: 100%;
}

.xf-select__control {
  appearance: none;
  -webkit-appearance: none;
  width: 100%;
  height: var(--control-height);
  padding: 0 32px 0 var(--space-3);
  background: var(--c-surface);
  border: 1px solid var(--c-border-strong);
  border-radius: var(--radius-sm);
  color: var(--c-text);
  font-size: var(--text-body);
  cursor: pointer;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background-color 0.15s ease;
}

.xf-select__control:focus-visible {
  outline: none;
  border-color: var(--c-primary);
  box-shadow: var(--focus-ring);
}

.xf-select.is-placeholder .xf-select__control {
  color: var(--c-text-3);
}

.xf-select__arrow {
  position: absolute;
  right: var(--space-3);
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 5px solid var(--c-text-3);
  pointer-events: none;
}

.xf-select.is-disabled .xf-select__control {
  background: var(--c-bg);
  color: var(--c-text-3);
  cursor: not-allowed;
}
</style>
