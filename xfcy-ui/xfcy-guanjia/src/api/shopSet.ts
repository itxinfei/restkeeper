import request from '@/utils/request';
/**
 *
 * 门店设置
 *
 **/
// 店铺切换
export const changStore = (params: any) =>
    request({
        'url': `/store/switchStore/${params.storeId}`,
        'method': 'get',
        params
    });

// 初始化信息获取
export const getShopSetData = (params: any) =>
    request({
        'url': '/settings/getSysSettings',
        'method': 'get',
        params
    });

// 设置信息保存
export const editShopSet = (params: any) =>
    request({
        'url': '/settings/update',
        'method': 'put',
        'data': params
    });
