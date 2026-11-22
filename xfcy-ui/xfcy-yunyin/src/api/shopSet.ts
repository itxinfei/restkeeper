import request from '@/utils/request'
/**
 *
 * 门店设置
 *
 **/

// 初始化信息获取
export const getShopSetData = (params: any) =>
  request({
    url: `/remark/getRemarks`,
    method: 'get',
    params
  })

// 设置信息保存
export const editShopSet = (params: any) =>
  request({
    url: `/remark/update`,
    method: 'put',
    params
  })
