import axios from 'axios';
import { Message, MessageBox } from 'element-ui';
import { UserModule } from '@/store/modules/user';

const service = axios.create({
    'baseURL': 'http://canzg-wsl.itheima.net:8081/', // 'http://161.189.152.255:8081/',  // 'http://canzg-wsl.itheima.net/enterpise', // https://mock.boxuegu.com/mock/872/', // 'http://172.16.43.139:8081/', // 'http://39.98.133.55:8081/', // https://mock.boxuegu.com/mock/872/', // process.env.VUE_APP_BASE_API,
    'timeout': 5000
});

// Request interceptors
service.interceptors.request.use(
    (config) => {
    // config.data = config.params
    // Add X-Access-Token header to every request, you can add other custom headers here
        if (UserModule.token) {
            config.headers['authorization'] = UserModule.token;
        }
        config.headers['Access-Control-Allow-Origin'] = '*';
        config.headers['Content-Type'] = 'application/json;';
        return config;
    },
    (error) => {
        Promise.reject(error);
    }
);

// Response interceptors
service.interceptors.response.use(
    (response) => {
        if (response.data.code === 200 || response.data.status === 200){
            const res = response.data;
            return response;
        }
        return Promise.reject(error);

    },
    (error) => {
        Message({
            'message': error.message,
            'type': 'error',
            'duration': 5 * 1000
        });
        return Promise.reject(error);
    }
);

export default service;
