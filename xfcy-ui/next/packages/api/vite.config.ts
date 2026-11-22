import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import dts from 'vite-plugin-dts'

export default defineConfig({
  plugins: [
    dts({
      include: ['src'],
      outDir: 'dist',
      insertTypesEntry: true,
    }),
  ],
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    sourcemap: true,
    lib: {
      entry: fileURLToPath(new URL('./src/index.ts', import.meta.url)),
      name: 'XfcyApi',
      formats: ['es'],
      fileName: () => 'index.js',
    },
    rollupOptions: {
      external: ['axios'],
      output: {
        exports: 'named',
      },
    },
  },
})
