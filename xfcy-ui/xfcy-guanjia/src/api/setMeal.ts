import request from '@/utils/request';
/**
 *
 * 套餐管理
 *
 **/

// 获取套餐列表
export const getSetMealList = (params: any) =>
    request({
        'url': `/setMeal/pageList/${params.page}/${params.pageSize}`,
        'method': 'get',
        params
    });

// 获取套餐详情
export const getSetMealDel = (params: any) =>
    request({
        'url': `/setMeal/${params.id}`,
        'method': 'get',
        params
    });

// 添加套餐
export const addSetMeal = (params: any) =>
    request({
        'url': '/setMeal/add',
        'method': 'post',
        'data': params
    });

// 更新套餐
export const editSetMeal = (params: any) =>
    request({
        'url': '/setMeal/update',
        'method': 'put',
        'data': params
    });

// 套餐状态更改
export const setMealStatus = (params: any) =>
    request({
        'url': '/setMeal/updateStatus',
        'method': 'put',
        'data': params
    });

// 删除套餐
export const delSetMeal = (params: any) =>
    request({
        'url': '/setMeal/delete',
        'method': 'delete',
        'data':params
    });
