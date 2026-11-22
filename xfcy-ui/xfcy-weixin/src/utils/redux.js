/**
 * 适当封装 Redux，简化调用
 */
/* eslint-disable import/prefer-default-export */
import Taro from '@tarojs/taro'
import fetch from './request'

export function createAction(options) {
  // TODO: host 抽出文件或者放入配置文件中 'https://mock.boxuegu.com/mock/869'
  const host = 'http://canzg-wsl.itheima.net:8087/microApp'//HOST // Taro.getEnv() == 'WEB' ? '/api' : HOST
  let { url, payload, method, fetchOptions, cb = null, type } = options
  url = `${host}/${url}`
  return (dispatch) => {
    return fetch({ url, payload, method, ...fetchOptions }).then((res) => {
      dispatch({ type, payload: cb ? cb(res) : res })
      return res
    })
  }
}
