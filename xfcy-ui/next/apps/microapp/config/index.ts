import { defineConfig } from '@tarojs/cli'
import devConfig from './dev'
import prodConfig from './prod'

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
      publicPath: '/',
      staticDirectory: 'static',
      esnextModules: [],
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
