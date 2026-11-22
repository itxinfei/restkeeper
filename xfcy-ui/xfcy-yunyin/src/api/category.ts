import request from '@/utils/request'
/**
 *
 * 分类管理
 *
 **/

// 分类列表获取
export const getClassList = (params: any) =>
  request({
    url: `/dishCategory/pageList/${params.page}/${params.pageSize}`,
    method: 'get',
    params
  })

// 添加分类
export const addCategory = (params: any) =>
  request({
    url: `/dishCategory/add`,
    method: 'post',
    params
  })

// 修改分类
export const editCategory = (params: any) =>
  request({
    url: `/dishCategory/update/${params.id}`,
    method: 'put',
    params
  })

// 删除套餐
export const delCategory = (params: any) =>
  request({
    url: `/dishCategory/delete/${params.id}`,
    method: 'DELETE',
    params
  })
