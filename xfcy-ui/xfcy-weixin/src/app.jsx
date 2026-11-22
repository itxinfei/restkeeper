import Taro, { Component } from '@tarojs/taro'
import { Provider } from '@tarojs/redux'

import 'taro-ui/dist/style/index.scss'
import Index from './pages/home/home'

import configStore from './store'

import './app.scss'

import { initTableInfo } from './actions/app'

import initWebScoket from './utils/webscoket'

// 如果需要在 h5 环境中开启 React Devtools
// 取消以下注释：
// if (process.env.NODE_ENV !== 'production' && process.env.TARO_ENV === 'h5')  {
//   require('nerv-devtools')
// }

const store = configStore()

class App extends Component {

  config = {
    pages: [
      'pages/menu/index'
    ],
    window: {
      backgroundTextStyle: 'light',
      navigationBarBackgroundColor: '#fff',
      navigationBarTitleText: 'WeChat',
      navigationBarTextStyle: 'black'
    },
    subpackages: [
      {
        'root': 'pages/order/',
        'pages': [
          'index',
          'success'
        ]
      }
    ]
  }

  componentDidMount () {
    Taro.$store = store

    Taro.$store.dispatch(initTableInfo({
      shopId: 'f3deb',
      storeId: '1205130634395324417',
      tableId: '1227868395689308161'
    }))
    // WebSocket和Stomp协议连接RabbitMQ
    initWebScoket(Taro.$store.getState().app.tableInfo.tableId)
  }

  componentDidShow () {}

  componentDidHide () {}

  componentDidCatchError () {}

  // 在 App 类中的 render() 函数没有实际作用
  // 请勿修改此函数
  render () {
    return (
      <Provider store={store}>
        <Index />
      </Provider>
    )
  }
}

Taro.render(<App />, document.getElementById('app'))
