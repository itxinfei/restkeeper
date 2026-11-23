import { PropsWithChildren } from 'react'
import { useLaunch } from '@tarojs/taro'
import { setupApi } from './api/setup'
import './app.scss'

// 装配 @xfcy/api：注入 Taro 传输 / 存储与 mock 开关（默认 mock）
setupApi()

function App({ children }: PropsWithChildren) {
  useLaunch(() => {
    console.log('[microapp] 心飞点餐已启动（@xfcy/api，默认 mock）')
  })
  return children
}

export default App
