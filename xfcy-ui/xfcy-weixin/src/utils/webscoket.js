import Taro from '@tarojs/taro'
import { Stomp } from './stomp'
import { getCartList } from './../actions/menu'

  /**
   * WebSocket和Stomp协议连接RabbitMQ
   * 
   * 小程序没有window对象，需要改点stompjs的源码
   * https://zhuanlan.zhihu.com/p/24825459
   * https://blog.csdn.net/kinsxkins/article/details/79303843
   * 
   * TODO: 有时间可以研究下用stompjs npm package
   * http://jmesnil.net/stomp-websocket/doc/
   */
export default function initWebScoket(tableId) {
    var socketOpen = false
    var socketMsgQueue = []
    function sendSocketMessage(msg) {
      console.log('send msg:')
      console.log(msg)
      if (socketOpen) {
        Taro.sendSocketMessage({
          data: msg
        })
      } else {
        socketMsgQueue.push(msg)
      }
    }

    var ws = {
      send: sendSocketMessage,
      onopen: null,
      onmessage: null
    }

    Taro.connectSocket({
      url: 'ws://161.189.152.255:15674/ws'
    })
    Taro.onSocketOpen(res => {
      console.log('WebSocket连接已打开！')

      socketOpen = true
      for (var i = 0; i < socketMsgQueue.length; i++) {
        sendSocketMessage(socketMsgQueue[i])
      }
      socketMsgQueue = []

      ws.onopen && ws.onopen()
    })

    Taro.onSocketMessage(res => {
      console.log('收到onmessage事件:', res)
      ws.onmessage && ws.onmessage(res)
    })

    Taro.onSocketClose(res => {
      console.log('收到onclose事件:', res)
    })

    Taro.onSocketError(res => {
      console.log('收到onerror事件:', res)
    })

    // var Stomp = require('./stomp').Stomp
    var client = Stomp.over(ws)

    var destination = `/exchange/micro_app_exchange/${tableId}`
    client.connect('guest', 'guest', sessionId => {
      console.log('sessionId', sessionId)

      client.subscribe(destination, (body, headers) => {
        console.log('From MQ:', body)

        Taro.$store.dispatch(getCartList(JSON.parse(body.body)))
      })
    })
  }