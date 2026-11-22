import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

const uiSrc = fileURLToPath(new URL('../../packages/ui/src', import.meta.url))
const apiSrc = fileURLToPath(new URL('../../packages/api/src', import.meta.url))

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // 本地网关地址，可用 VITE_API_PROXY_TARGET 覆盖
  const proxyTarget = env.VITE_API_PROXY_TARGET ?? 'http://127.0.0.1:8080'

  return {
    plugins: [vue()],
    resolve: {
      alias: [
        { find: /^@xfcy\/ui$/, replacement: `${uiSrc}/index.ts` },
        { find: /^@xfcy\/ui\/(.*)$/, replacement: `${uiSrc}/$1` },
        { find: /^@xfcy\/api$/, replacement: `${apiSrc}/index.ts` },
        { find: /^@xfcy\/api\/(.*)$/, replacement: `${apiSrc}/$1` },
      ],
    },
    server: {
      // 5184/5185 已被 manager/operator 占用，cashier 使用 5186 避免启动冲突
      port: 5186,
      proxy: {
        // 网关未配置 StripPrefix，这里剥掉 /api 前缀，按 gateway.yml 真实路径转发
        '/api': {
          target: proxyTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
      },
    },
  }
})
