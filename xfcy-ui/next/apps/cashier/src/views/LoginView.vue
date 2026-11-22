<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { XfButton, XfField, XfInput } from '@xfcy/ui'
import { isMockMode } from '@xfcy/api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

const username = ref('')
const password = ref('')
const shopId = ref('')
const error = ref('')
const submitting = ref(false)
const mockMode = isMockMode()

async function submit() {
  if (!username.value.trim()) {
    error.value = '请输入账号'
    return
  }
  if (!password.value) {
    error.value = '请输入密码'
    return
  }
  if (!mockMode && !shopId.value.trim()) {
    error.value = '请输入门店号'
    return
  }
  error.value = ''
  submitting.value = true
  try {
    await user.login(username.value.trim(), password.value, shopId.value.trim())
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    router.replace(redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/tables')
  } catch (err) {
    error.value = err instanceof Error ? err.message : '登录失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="xfc-login">
    <div class="xfc-login__card">
      <div class="xfc-login__brand">
        <span class="xfc-login__logo" aria-hidden="true">心</span>
        <span>
          <div class="xfc-login__title">心飞收银端</div>
          <div class="xfc-login__sub">
            触屏点餐 · {{ mockMode ? '示意登录（任意账号密码可进入）' : '请登录门店员工账号' }}
          </div>
        </span>
      </div>

      <form class="xfc-login__form" @submit.prevent="submit">
        <XfField v-if="!mockMode" label="门店号" required>
          <template #default="{ id }">
            <XfInput
              :id="id"
              v-model="shopId"
              placeholder="请输入门店号"
              autocomplete="off"
            />
          </template>
        </XfField>

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

        <XfButton type="submit" size="lg" block :disabled="submitting">
          {{ submitting ? '登录中…' : '登录' }}
        </XfButton>
      </form>
    </div>
  </div>
</template>
