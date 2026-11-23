<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { XfLoginPage } from '@xfcy/ui'
import { isMockMode } from '@xfcy/api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

async function login(username: string, password: string) {
  await user.login(username, password)
}

function onSuccess() {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  router.replace(redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/enterprises')
}
</script>

<template>
  <XfLoginPage
    brand-title="心飞运营中心"
    :brand-subtitle="
      isMockMode() ? '平台运营 · 示意登录（任意账号密码可进入）' : '平台运营 · 请登录运营人员账号'
    "
    :login="login"
    @success="onSuccess"
  />
</template>
