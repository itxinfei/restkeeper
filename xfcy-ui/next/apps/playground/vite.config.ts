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
    port: 5173,
  },
})
