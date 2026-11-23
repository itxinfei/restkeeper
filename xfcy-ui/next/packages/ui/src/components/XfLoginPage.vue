<script setup lang="ts">
import { ref } from 'vue'
import XfButton from './XfButton.vue'
import XfField from './XfField.vue'
import XfInput from './XfInput.vue'

/**
 * 三端统一登录页（原 manager/operator/cashier 三个 LoginView 的公共部分）。
 * 差异通过 props/默认资料注入：标题、副标题、是否展示商户号/门店号、触屏尺寸。
 * 登录动作由调用方传入（内部组合各端 useUserStore）。
 */
const props = withDefaults(
  defineProps<{
    brandTitle: string
    brandSubtitle?: string
    logoText?: string
    /** 是否展示商户号 / 门店号输入（real 模式） */
    showShopId?: boolean
    shopIdLabel?: string
    shopIdPlaceholder?: string
    /** 触屏端（收银）放大字号与卡片 */
    touch?: boolean
    login: (username: string, password: string, shopId: string) => Promise<void>
  }>(),
  {
    brandSubtitle: '',
    logoText: '心',
    showShopId: false,
    shopIdLabel: '商户号',
    shopIdPlaceholder: '',
    touch: false,
  },
)

const emit = defineEmits<{ (e: 'success'): void }>()

const username = ref('')
const password = ref('')
const shopId = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  if (!username.value.trim()) {
    error.value = '请输入账号'
    return
  }
  if (!password.value) {
    error.value = '请输入密码'
    return
  }
  if (props.showShopId && !shopId.value.trim()) {
    error.value = `请输入${props.shopIdLabel}`
    return
  }
  error.value = ''
  loading.value = true
  try {
    await props.login(username.value.trim(), password.value, shopId.value.trim())
    emit('success')
  } catch (err) {
    error.value = err instanceof Error ? err.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="xf-login" :class="{ 'is-touch': touch }">
    <div class="xf-login__card">
      <div class="xf-login__brand">
        <span class="xf-login__logo" aria-hidden="true">{{ logoText }}</span>
        <span class="xf-login__heading">
          <div class="xf-login__title">{{ brandTitle }}</div>
          <div v-if="brandSubtitle" class="xf-login__sub">{{ brandSubtitle }}</div>
        </span>
      </div>

      <form class="xf-login__form" @submit.prevent="submit">
        <XfField v-if="showShopId" :label="shopIdLabel" required>
          <template #default="{ id }">
            <XfInput
              :id="id"
              v-model="shopId"
              :placeholder="shopIdPlaceholder || `请输入${shopIdLabel}`"
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

        <p v-if="error" class="xf-login__error" role="alert">{{ error }}</p>

        <XfButton size="lg" block type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
        </XfButton>
      </form>

      <slot name="extra"></slot>
    </div>
  </div>
</template>
