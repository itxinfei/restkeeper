import request from '@/utils/request';
/**
 *
 * 桌台管理
 * 区域管理
 *
 **/

// 获取桌台列表
export const getTableList = (params: any) =>
    request({
        'url': `/table/search/${params.areaId}/${params.page}/${params.pageSize}`,
        'method': 'get',
        params
    });

// 根据区域搜索列表
export const seachTableList = (params: any) =>
    request({
        'url': `/dish/pageList/${params.areaId}/${params.page}/${params.pageSize}`,
        'method': 'get',
        params
    });

// 添加桌台
export const addTable = (params: any) =>
    request({
        'url': '/table/add',
        'method': 'POST',
        'data': params
    });

// 修改桌台
export const editTable = (params: any) =>
    request({
        'url': `/table/update/${params.areaId}`,
        'method': 'PUT',
        'data': params
    });

// 删除桌台
export const delTable = (params: any) =>
    request({
        'url': `/table/delete/${params.tableId}`,
        'method': 'DELETE',
        params
    });

// 获取区域列表
export const getAreaList = (params: any) =>
    request({
        'url': `/table/areaPageList/${params.page}/${params.pageSize}`,
        'method': 'get',
        params
    });

// 添加区域
export const addArea = (params: any) =>
    request({
        'url': '/table/addArea',
        'method': 'POST',
        params,
        'data': params
    });

// 修改区域
export const editArea = (params: any) =>
    request({
        'url': `/table/updateArea/${params.id}`,
        'method': 'PUT',
        params,
        'data': params
    });

// 删除区域
export const delArea = (params: any) =>
    request({
        'url': `/table/deleteArea/${params.id}`,
        'method': 'DELETE',
        params,
        'data':params
    });
