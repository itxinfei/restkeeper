<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    description?: string
  }>(),
  { description: '' },
)
</script>

<template>
  <header class="xf-page__head">
    <div class="xf-page__text">
      <h1 class="xf-page__title">{{ title }}</h1>
      <p v-if="description" class="xf-page__desc">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="xf-page__actions">
      <slot name="actions" />
    </div>
  </header>
</template>
