import Taro from '@tarojs/taro'
import { TABLE_INFO } from './../constants/app'

/**
 * 获取桌台信息
 */
export const initTableInfo = (tableInfo) => ({
    type: TABLE_INFO,
    tableInfo: tableInfo
})