import request from '@/utils/request'
/**
 *
 * 菜品管理
 *
 **/

// 获取菜品列表
export const getDishList = (params: any) =>
  request({
    url: `/dish/pageList/${params.page}/${params.pageSize}`,
    method: 'get',
    params
  })
// 获取菜品详情
export const getDishDetails = (params: any) =>
  request({
    url: `/dish/${params.id}`,
    method: 'get',
    params
  })
// 添加菜品
export const addDish = (params: any) =>
  request({
    url: `/dish/add`,
    method: 'post',
    params
  })
//修改菜品
export const editDish = (params: any) =>
  request({
    url: `/dish/update`,
    method: 'put',
    params
  })
//修改状态
export const editDishState = (params: any) =>
  request({
    url: `/dish/updateSaleState`,
    method: 'put',
    params
  })
//删除菜品
export const delDish = (params: any) =>
  request({
    url: `/dish/delete`,
    method: 'delete',
    params
  })
// 口味列表获取
export const getFlavorList = (params: any) =>
  request({
    url: `/dish/flavorList`,
    method: 'get',
    params
  })
// 获取菜品分类列表
export const getDishTypeList = (params: any) =>
  request({
    url: `/dish/type/${params.type}`,
    method: 'get',
    params
  })
// 分类id获取菜品列表
export const getDishListType = (params: any) =>
  request({
    url: `/dish/findEnableDishList/${params.id}`,
    method: 'get',
    params
  })
