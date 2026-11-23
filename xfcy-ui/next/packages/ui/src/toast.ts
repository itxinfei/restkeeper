import { reactive, readonly } from 'vue'
import type { Tone } from './tone'

export type ToastItem = {
  id: number
  text: string
  tone: Tone
}

type ToastState = {
  toasts: ToastItem[]
}

const state = reactive<ToastState>({ toasts: [] })
let seq = 0
let timer: number | undefined

function dismiss(id: number) {
  const index = state.toasts.findIndex((item) => item.id === id)
  if (index >= 0) state.toasts.splice(index, 1)
}

function push(text: string, tone: Tone = 'info', duration = 2600) {
  seq += 1
  const item: ToastItem = { id: seq, text, tone }
  state.toasts.push(item)
  if (state.toasts.length > 4) state.toasts.splice(0, state.toasts.length - 4)

  if (timer !== undefined) window.clearTimeout(timer)
  timer = window.setTimeout(() => dismiss(item.id), duration)
}

export function useToast() {
  return {
    toasts: readonly(state.toasts),
    success: (text: string) => push(text, 'success'),
    error: (text: string) => push(text, 'danger'),
    info: (text: string) => push(text, 'info'),
    dismiss,
  }
}

export function resetToastState() {
  state.toasts.splice(0, state.toasts.length)
  if (timer !== undefined) {
    window.clearTimeout(timer)
    timer = undefined
  }
}
