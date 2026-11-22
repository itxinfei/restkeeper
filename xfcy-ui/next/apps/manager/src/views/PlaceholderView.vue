<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { XfEmpty, XfPanel } from '@xfcy/ui'

const route = useRoute()
const title = computed(() => String(route.meta.title ?? '模块'))
</script>

<template>
  <XfPanel>
    <XfEmpty
      :title="`${title} · 建设中`"
      description="管家端首版优先落地工作台与菜品管理，该模块为占位页，后续按高保真原型补齐。"
    />
  </XfPanel>
</template>
