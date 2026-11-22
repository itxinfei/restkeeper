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
  router.replace(redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/tables')
}
</script>

<template>
  <div class="xfc-login">
    <div class="xfc-login__card">
      <div class="xfc-login__brand">
        <span class="xfc-login__logo" aria-hidden="true">心</span>
        <span>
          <div class="xfc-login__title">心飞收银端</div>
          <div class="xfc-login__sub">触屏点餐 · 示意登录（任意账号密码可进入）</div>
        </span>
      </div>

      <form class="xfc-login__form" @submit.prevent="submit">
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

        <p v-if="error" class="xfc-login__error" role="alert">{{ error }}</p>

        <XfButton type="submit" size="lg" block>登录</XfButton>
      </form>
    </div>
  </div>
</template>
