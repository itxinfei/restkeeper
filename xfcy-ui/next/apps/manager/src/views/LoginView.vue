<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { XfButton, XfField, XfInput } from '@xfcy/ui'
import { useShopStore } from '../stores/shop'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const shop = useShopStore()

const username = ref('')
const password = ref('')
const error = ref('')

function submit() {
  if (!username.value.trim()) {
    error.value = '请输入账号'
    return
  }
  if (!password.value) {
    error.value = '请输入密码'
    return
  }
  error.value = ''
  user.login(username.value.trim(), password.value)

  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  router.replace(redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/dashboard')
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
        <XfField label="账号" required>
          <template #default="{ id }">
            <XfInput
              :id="id"
              v-model="username"
              placeholder="请输入账号"
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

        <XfButton size="lg" block>登录</XfButton>
      </form>
    </div>
  </div>
</template>
