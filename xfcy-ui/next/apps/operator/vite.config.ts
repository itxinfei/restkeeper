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
    // 5184 已被 manager 占用，operator 使用 5185 避免启动冲突
    port: 5185,
  },
})
