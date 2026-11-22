import Taro from '@tarojs/taro'
/* eslint-disable */
// const CODE_SUCCESS = '200'
 const CODE_AUTH_EXPIRED = '600'
//



function getStorage(key) {
  return Taro.getStorage({ key }).then(res => res.data).catch(() => '')
}
//
// function updateStorage(data = {}) {
//   return Promise.all([
//     Taro.setStorage({ key: 'token', data: data['3rdSession'] || '' }),
//     Taro.setStorage({ key: 'uid', data: data['uid'] || ''})
//   ])
// }

/**
 * 简易封装网络请求
 * // NOTE 需要注意 RN 不支持 *StorageSync，此处用 async/await 解决
 * @param {*} options
 */
export default async function fetch(options) {

  const { url, payload, method = 'GET' } = options
  // const token = await getStorage('token')
  const { shopId, storeId } = Taro.$store.getState().app.tableInfo
  const header = {
    shopId: shopId,
    storeId: storeId
  } //token ? { 'WX-PIN-SESSION': token, 'X-WX-3RD-Session': token } : {}
  if (method === 'POST') {
    header['content-type'] = 'application/json'
  }
  return Taro.request({
    url,
    method,
    data: payload,
    header
  }).then(async (res) => {
    const { data } = res
    return data.data
  }).catch((err) => {
     const defaultMsg = err.code === CODE_AUTH_EXPIRED ? '登录失效' : '请求异常'
    // if (showToast) {
    //   Taro.showToast({
    //     title: err && err.errorMsg || defaultMsg,
    //     icon: 'none'
    //   })
    // }
    //
    // if (err.code === CODE_AUTH_EXPIRED && autoLogin) {
    //   Taro.navigateTo({
    //     url: '/pages/user-login/user-login'
    //   })
    // }
    return  Promise.reject({ message: defaultMsg, ...err })
  })
}
