import request from '@/utils/request'
/**
 *
 * 账号管理
 *
 **/

// 获取账号管理列表
export const getMemberList = (params: any) =>
  request({
    url: `/enterprise/pageList/${params.page}/${params.pageSize}`,
    method: 'get',
    params
  })
// 获取账号管理详情
export const getMemberDetile = (params: any) =>
  request({
    url: `/enterprise/getById/${params.id}`,
    method: 'get',
    params
  })

// 添加账号
export const addMember = (params: any) =>
  request({
    url: `/enterprise/add`,
    method: 'post',
    data:params
  })
// 编辑账号
export const editMember = (params: any) =>
  request({
    url: `/enterprise/update`,
    method: 'put',
    data:params
  })
// 账号状态启用
export const memberOpen = (params: any) =>
  request({
    url: `/enterprise/enable/${params.id}`,
    method: 'put',
    data:params
  })
// 账号状态禁用
export const memberClose = (params: any) =>
  request({
    url: `/enterprise/forbidden/${params.id}`,
    method: 'put',
    data:params
  })
// 账号删除
export const memberDel = (params: any) =>
  request({
    url: `/enterprise/deleteById/${params.id}`,
    method: 'delete',
    params
  })
// 密码重置
export const rePassword = (params: any) =>
  request({
    url: `/enterprise/deleteById/${params.id}`,
    method: 'delete',
    params
  })
