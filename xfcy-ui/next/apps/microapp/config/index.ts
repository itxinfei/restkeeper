import path from 'node:path'
import { defineConfig } from '@tarojs/cli'
import devConfig from './dev'
import prodConfig from './prod'

// @xfcy/api 直接引用源码（与 B 端 Vite alias 一致），避免依赖 dist 构建产物
const apiSrcDir = path.resolve(__dirname, '../../../packages/api/src')
const apiSrc = path.resolve(apiSrcDir, 'index.ts')
// 真实模式本地联调网关（可用 Node 环境变量 TARO_APP_API_PROXY_TARGET 覆盖）
const proxyTarget = process.env.TARO_APP_API_PROXY_TARGET || 'http://127.0.0.1:8080'

// 心飞餐饮 C 端点餐 · Taro 4 编译配置
// 设计宽度 390（DESIGN.md §5.3 / §7 C 端基准 390，兼容 360–430）
export default defineConfig(async (merge, { command, mode }) => {
  console.log(`[microapp] taro build: command=${command}, mode=${mode}`)
  const baseConfig = {
    projectName: 'microapp',
    date: '2026-11-22',
    designWidth: 390,
    deviceRatio: {
      390: 750 / 390,
      750: 1,
    },
    sourceRoot: 'src',
    outputRoot: 'dist',
    plugins: ['@tarojs/plugin-framework-react'],
    alias: {
      '@xfcy/api': apiSrc,
    },
    defineConstants: {},
    copy: {
      patterns: [],
      options: {},
    },
    framework: 'react',
    compiler: 'webpack5',
    cache: {
      enable: false,
    },
    mini: {
      // 共享层源码在 src 之外，需加入 babel 编译白名单（script rule 默认只编译 src）
      compile: {
        include: [apiSrcDir],
      },
      postcss: {
        pxtransform: {
          enable: true,
          config: {},
        },
        cssModules: {
          enable: false,
        },
      },
    },
    h5: {
      // 共享层源码在 src 之外，需加入 babel 编译白名单（script rule 默认只编译 src）
      compile: {
        include: [apiSrcDir],
      },
      publicPath: '/',
      staticDirectory: 'static',
      esnextModules: [],
      devServer: {
        // 真实模式下把 /microApp 代理到本地网关（microapp-web 路由）
        proxy: {
          '/microApp': {
            target: proxyTarget,
            changeOrigin: true,
          },
        },
      },
      postcss: {
        autoprefixer: {
          enable: true,
          config: {},
        },
        pxtransform: {
          enable: true,
          config: {},
        },
        cssModules: {
          enable: false,
        },
      },
    },
  }
  if (process.env.NODE_ENV === 'development') {
    return merge({}, baseConfig, devConfig)
  }
  return merge({}, baseConfig, prodConfig)
})
