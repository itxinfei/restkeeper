<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { XfLoginPage } from '@xfcy/ui'
import { isMockMode } from '@xfcy/api'
import { useShopStore } from '../stores/shop'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const shop = useShopStore()

// 真实后端登录需要 shopId（企业/店长身份），mock 模式不展示该字段
const showShopId = !isMockMode()

async function login(username: string, password: string, shopId: string) {
  await user.login(username, password, showShopId ? shopId : undefined)
}

function onSuccess() {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  router.replace(redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/dashboard')
}
</script>

<template>
  <XfLoginPage
    brand-title="心飞管家端"
    :brand-subtitle="shop.currentShop.name"
    :show-shop-id="showShopId"
    shop-id-label="商户号"
    shop-id-placeholder="8 位商户号（shopId）"
    :login="login"
    @success="onSuccess"
  >
    <template #extra>
      <p class="xf-login__extra">初次使用：由运营中心「开通企业」生成初始密码。</p>
    </template>
  </XfLoginPage>
</template>
