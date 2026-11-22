import Taro from '@tarojs/taro'
import {
  CART_LIST, TABLE_STATUS, OPEN_TABLE, DISH_FLAVOR, MENU, MENU_TYPE, MENU_CART, MENU_MEAL_DET, MENU_CART_ADD, MENU_CART_CLEAN, ORDER_PAY
} from './../constants/menu'

import { createAction } from './../utils/redux'

/**
 * 获取WebScoket的推送
 */
export const getCartList = (cartList) => {
  return  {
    type: CART_LIST,
    cartList: cartList
  }
}

/**
 * 获取桌台状态
 */
export const dispatchTableStatus = payload => createAction({
  url: payload.url,
  type: TABLE_STATUS,
  payload: payload.params
})

/**
 * 开桌
 */
export const dispatchOpenTable = payload => createAction({
  url: payload.url,
  type: OPEN_TABLE,
  payload: payload.params
})

/**
 * 口味列表
 */
export const dispatchDishFlavor = payload => createAction({
  url: payload.url,
  type: DISH_FLAVOR,
  payload: payload.params
})

/**
 * 菜单列表
 */
export const dispatchMenu = payload => createAction({
  url: payload.url,
  type: MENU,
  payload: payload.params
})

/**
 * 菜单分类列表
 */
export const dispatchMenuType = payload => createAction({
  url: payload.url,
  type: MENU_TYPE,
  payload: payload.params
})

/**
 * 购物车列表
 */
export const dispatchMenuCart = payload => createAction({
  url: payload.url,
  type: MENU_CART,
  payload: payload.params
})

/**
 * 套餐详情
 */
export const dispatchMenuMealDet = payload => createAction({
  url: payload.url,
  type: MENU_MEAL_DET,
  payload: payload.params
})

/**
 * 添加购物车
 */
export const dispatchCartAdd = payload => createAction({
  url: payload.url,
  type: MENU_CART_ADD,
  payload: payload.params,
  method: payload.method
})

/**
 * 清空购物车
 */
export const dispatchCartClean = payload => createAction({
  url: payload.url,
  type: MENU_CART_CLEAN,
  payload: payload.params,
  method: payload.method
})

/**
 * 订单支付
 */
export const dispatchOrderPay = payload => createAction({
  url: payload.url,
  type: ORDER_PAY,
  payload: payload.params
})
