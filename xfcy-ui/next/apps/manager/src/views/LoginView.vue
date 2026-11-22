<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { XfButton, XfField, XfInput } from '@xfcy/ui'
import { isMockMode } from '@xfcy/api'
import { useShopStore } from '../stores/shop'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const shop = useShopStore()

const username = ref('')
const password = ref('')
const shopId = ref('')
const error = ref('')
const loading = ref(false)
// 真实后端登录需要 shopId（企业/店长身份），mock 模式不展示该字段
const showShopId = !isMockMode()

async function submit() {
  if (!username.value.trim()) {
    error.value = '请输入账号'
    return
  }
  if (!password.value) {
    error.value = '请输入密码'
    return
  }
  if (showShopId && !shopId.value.trim()) {
    error.value = '请输入商户号（shopId）'
    return
  }
  error.value = ''
  loading.value = true
  try {
    await user.login(
      username.value.trim(),
      password.value,
      showShopId ? shopId.value.trim() : undefined,
    )
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    router.replace(redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/dashboard')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="xfm-login">
    <div class="xfm-login__card">
      <div class="xfm-login__brand">
        <span class="xfm-login__logo" aria-hidden="true">心</span>
        <span>
          <div class="xfm-login__title">心飞管家端</div>
          <div class="xfm-login__sub">{{ shop.currentShop.name }}</div>
        </span>
      </div>

      <form class="xfm-login__form" @submit.prevent="submit">
        <XfField v-if="showShopId" label="商户号" required>
          <template #default="{ id }">
            <XfInput
              :id="id"
              v-model="shopId"
              placeholder="8 位商户号（shopId）"
              autocomplete="off"
            />
          </template>
        </XfField>

        <XfField label="账号" required>
          <template #default="{ id }">
            <XfInput
              :id="id"
              v-model="username"
              placeholder="请输入手机号"
              autocomplete="username"
            />
          </template>
        </XfField>

        <XfField label="密码" required>
          <template #default="{ id }">
            <XfInput
              :id="id"
              v-model="password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
          </template>
        </XfField>

        <p v-if="error" class="xfm-login__error" role="alert">{{ error }}</p>

        <XfButton size="lg" block type="submit" :disabled="loading">{{ loading ? '登录中…' : '登录' }}</XfButton>
      </form>
    </div>
  </div>
</template>
