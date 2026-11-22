<script setup lang="ts">
import { computed, useId } from 'vue'

const props = withDefaults(
  defineProps<{
    label?: string
    required?: boolean
    error?: string | boolean
    hint?: string
  }>(),
  { label: '', required: false, error: false, hint: '' },
)

const uid = useId().replace(/[^a-zA-Z0-9]/g, '')
const fieldId = `xf-field-${uid}`
const errorId = `${fieldId}-error`
const hintId = `${fieldId}-hint`

const hasError = computed(() => !!props.error)
const errorMessage = computed(() => (typeof props.error === 'string' ? props.error : ''))
</script>

<template>
  <div class="xf-field" :class="{ 'has-error': hasError }">
    <label v-if="label" :for="fieldId" class="xf-field__label">
      {{ label }}
      <span v-if="required" class="xf-field__required" aria-hidden="true">*</span>
    </label>
    <div class="xf-field__control">
      <slot :id="fieldId" />
    </div>
    <p v-if="errorMessage" :id="errorId" class="xf-field__error" role="alert">
      {{ errorMessage }}
    </p>
    <p v-else-if="hint" :id="hintId" class="xf-field__hint">{{ hint }}</p>
  </div>
</template>

<style scoped>
.xf-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.xf-field__label {
  font-size: var(--text-caption);
  font-weight: var(--weight-body);
  line-height: var(--leading-body);
  color: var(--c-text-2);
}

.xf-field__required {
  color: var(--c-danger);
}

.xf-field__control {
  display: block;
}

.xf-field__error {
  font-size: var(--text-caption);
  line-height: var(--leading-body);
  color: var(--c-danger);
}

.xf-field__hint {
  font-size: var(--text-caption);
  line-height: var(--leading-body);
  color: var(--c-text-3);
}
</style>
