import { PropsWithChildren } from 'react'
import { useLaunch } from '@tarojs/taro'
import './app.scss'

function App({ children }: PropsWithChildren) {
  useLaunch(() => {
    console.log('[microapp] 心飞点餐已启动（示意数据，未接后端）')
  })
  return children
}

export default App
