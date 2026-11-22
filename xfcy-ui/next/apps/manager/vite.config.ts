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
    // 5174/5175 在本机常被其他工程占用，这里改用 5184 避免启动冲突
    port: 5184,
  },
})
