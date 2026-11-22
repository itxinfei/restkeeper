<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, useId, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    open: boolean
    title?: string
    mask?: boolean
    closable?: boolean
    width?: string
  }>(),
  {
    title: '',
    mask: true,
    closable: true,
    width: '520px',
  },
)

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'close'): void
}>()

const uid = useId().replace(/[^a-zA-Z0-9]/g, '')
const titleId = `xf-modal-${uid}-title`

const dialogRef = ref<HTMLElement | null>(null)

function close() {
  if (!props.closable) return
  emit('update:open', false)
  emit('close')
}

function onMaskPointerDown(event: MouseEvent) {
  if (props.mask && event.target === event.currentTarget) close()
}

const FOCUSABLE =
  'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])'

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    event.preventDefault()
    close()
    return
  }
  if (event.key !== 'Tab' || !dialogRef.value) return

  const nodes = Array.from(dialogRef.value.querySelectorAll<HTMLElement>(FOCUSABLE)).filter(
    (node) => node.offsetParent !== null,
  )
  if (nodes.length === 0) return
  const first = nodes[0]
  const last = nodes[nodes.length - 1]

  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault()
    last.focus()
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault()
    first.focus()
  }
}

watch(
  () => props.open,
  async (open) => {
    if (open) {
      document.body.style.overflow = 'hidden'
      await nextTick()
      dialogRef.value?.focus()
    } else {
      document.body.style.overflow = ''
    }
  },
)

onBeforeUnmount(() => {
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <Transition name="xf-modal">
      <div
        v-if="open"
        class="xf-modal__overlay"
        @mousedown="onMaskPointerDown"
        @keydown="onKeydown"
      >
        <div
          ref="dialogRef"
          class="xf-modal"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="title ? titleId : undefined"
          tabindex="-1"
          :style="{ width }"
        >
          <header class="xf-modal__header">
            <h2 :id="titleId" class="xf-modal__title">{{ title }}</h2>
            <button
              v-if="closable"
              type="button"
              class="xf-modal__close"
              aria-label="关闭"
              @click="close"
            >
              ×
            </button>
          </header>
          <div class="xf-modal__body">
            <slot />
          </div>
          <footer v-if="$slots.footer" class="xf-modal__footer">
            <slot name="footer" />
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.xf-modal__overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
  background: var(--overlay);
}

.xf-modal {
  max-width: 100%;
  max-height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--c-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  outline: none;
}

.xf-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid var(--c-border);
}

.xf-modal__title {
  font-size: var(--text-h3);
  font-weight: var(--weight-title);
  line-height: var(--leading-title);
  color: var(--c-text);
}

.xf-modal__close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--c-text-3);
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

.xf-modal__close:hover {
  background: var(--c-surface-2);
  color: var(--c-text);
}

.xf-modal__close:focus-visible {
  outline: none;
  box-shadow: var(--focus-ring);
}

.xf-modal__body {
  padding: var(--space-4);
  overflow-y: auto;
}

.xf-modal__footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--c-border);
}

.xf-modal-enter-active,
.xf-modal-leave-active {
  transition: opacity 0.18s ease;
}

.xf-modal-enter-active .xf-modal,
.xf-modal-leave-active .xf-modal {
  transition: transform 0.18s ease;
}

.xf-modal-enter-from,
.xf-modal-leave-to {
  opacity: 0;
}

.xf-modal-enter-from .xf-modal,
.xf-modal-leave-to .xf-modal {
  transform: translateY(8px);
}
</style>
