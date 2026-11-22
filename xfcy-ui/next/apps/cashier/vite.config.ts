import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const uiSrc = fileURLToPath(new URL('../../packages/ui/src', import.meta.url))

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: [
      { find: /^@xfcy\/ui$/, replacement: `${uiSrc}/index.ts` },
      { find: /^@xfcy\/ui\/(.*)$/, replacement: `${uiSrc}/$1` },
    ],
  },
  server: {
    // 5173/5184/5185 已被 playground/manager/operator 占用，cashier 使用 5186
    port: 5186,
  },
})
