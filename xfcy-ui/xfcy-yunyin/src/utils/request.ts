import axios from 'axios'
import { Message, MessageBox } from 'element-ui'
import { UserModule } from '@/store/modules/user'

const service = axios.create({
  baseURL: 'http://canzg-wsl.itheima.net:8083/', //'https://mock.boxuegu.com/mock/866/', // 'http://39.98.133.55:8083',  // process.env.VUE_APP_BASE_API,
  timeout: 5000
})

// Request interceptors
service.interceptors.request.use(
  (config) => {
    if (UserModule.token) {
      config.headers['authorization'] = UserModule.token
    }
    config.headers['Access-Control-Allow-Origin'] = '*'

    return config
  },
  (error) => {
    Promise.reject(error)
  }
)

// Response interceptors
service.interceptors.response.use(
  (response) => {
    if (response.status == 200 || response.code == 200){
      return response
    } else {
      Promise.reject(error)
    }
  },
  (error) => {
    Message({
      message: error.message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service
