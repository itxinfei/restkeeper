<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { XfLoginPage } from '@xfcy/ui'
import { isMockMode } from '@xfcy/api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const mockMode = isMockMode()

async function login(username: string, password: string, shopId: string) {
  await user.login(username, password, shopId)
}

function onSuccess() {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  router.replace(redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/tables')
}
</script>

<template>
  <XfLoginPage
    brand-title="心飞收银端"
    :brand-subtitle="
      mockMode ? '触屏点餐 · 示意登录（任意账号密码可进入）' : '触屏点餐 · 请登录门店员工账号'
    "
    :show-shop-id="!mockMode"
    shop-id-label="门店号"
    shop-id-placeholder="请输入门店号"
    touch
    :login="login"
    @success="onSuccess"
  />
</template>
