/// <reference types="@tarojs/taro" />

/**
 * 应用配置：4 个页面（对应原型 microapp-menu / cart / result）。
 * 使用自定义导航（navigationStyle: custom），以还原原型中的门店头 / 返回头。
 * defineAppConfig 为 @tarojs/taro 注入的全局帮助函数。
 */
export default defineAppConfig({
  pages: [
    'pages/menu/index',
    'pages/cart/index',
    'pages/confirm/index',
    'pages/result/index',
  ],
  window: {
    navigationStyle: 'custom',
    backgroundTextStyle: 'light',
    backgroundColor: '#f7f8fa',
    navigationBarBackgroundColor: '#ffffff',
    navigationBarTitleText: '心飞点餐',
    navigationBarTextStyle: 'black',
  },
})
