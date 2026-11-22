<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { XfButton, XfField, XfInput } from '@xfcy/ui'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

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
  router.replace(redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/enterprises')
}
</script>

<template>
  <div class="xfo-login">
    <div class="xfo-login__card">
      <div class="xfo-login__brand">
        <span class="xfo-login__logo" aria-hidden="true">心</span>
        <span>
          <div class="xfo-login__title">心飞运营中心</div>
          <div class="xfo-login__sub">平台运营 · 示意登录（任意账号密码可进入）</div>
        </span>
      </div>

      <form class="xfo-login__form" @submit.prevent="submit">
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

        <p v-if="error" class="xfo-login__error" role="alert">{{ error }}</p>

        <XfButton size="lg" block>登录</XfButton>
      </form>
    </div>
  </div>
</template>
