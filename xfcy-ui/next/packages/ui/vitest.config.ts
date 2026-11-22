import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['src/components/__tests__/*.spec.ts'],
    coverage: {
      provider: 'v8',
      include: ['src/components/**/*.vue', 'src/index.ts'],
      reporter: ['text', 'html'],
    },
  },
})
