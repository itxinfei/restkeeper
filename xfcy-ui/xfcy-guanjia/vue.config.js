const path = require('path');
const name = 'Vue Typescript Admin';

module.exports = {
    'publicPath': process.env.NODE_ENV === 'production' ? './' : './', // TODO: Remember to change this to fit your need
    'lintOnSave': process.env.NODE_ENV === 'development',
    'pwa': {
        'name': name
    },
    'pluginOptions': {
        'style-resources-loader': {
            'preProcessor': 'scss',
            'patterns': [
                path.resolve(__dirname, 'src/styles/_variables.scss'),
                path.resolve(__dirname, 'src/styles/_mixins.scss')
            ]
        }
    },
    // devServer: {
    //   proxy: {
    //     '/Api/': {     //这里最好有一个 /
    //       target: 'http://canzg-wsl.itheima.net/enterpise',  // 后台接口域名
    //       ws: true,        //如果要代理 websockets，配置这个参数
    //       secure: false,  // 如果是https接口，需要配置这个参数
    //       changeOrigin: true,  //是否跨域
    //       pathRewrite:{
    //         '^/Api':'/'
    //       }
    //     }
    //   }
    // },
    chainWebpack(config) {
    // Provide the app's title in webpack's name field, so that
    // it can be accessed in index.html to inject the correct title.
        config.set('name', name);
    }
};
