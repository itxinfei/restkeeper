<script setup lang="ts">
import XfTag from './XfTag.vue'

withDefaults(defineProps<{ show?: boolean; text?: string }>(), {
  show: false,
  text: '演示数据',
})
</script>

<template>
  <XfTag v-if="show" tone="warning" class="xf-demo-badge">{{ text }}</XfTag>
</template>

<style scoped>
.xf-demo-badge {
  flex: none;
}
</style>
