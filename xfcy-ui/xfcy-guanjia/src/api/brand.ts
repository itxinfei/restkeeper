import request from '@/utils/request';
/**
 *
 * 集团管理下全部接口调用包含有
 * 品牌管理
 * 门店管理
 * 店长管理
 **/

// 获取品牌列表
export const getBrandList = (params: any) =>
    request({
        'url': `/brand/pageList/${params.page}/${params.pageSize}`,
        'method': 'get',
        params
    });
// 添加品牌
export const addBrand = (params: any) =>
    request({
        'url': '/brand/add',
        'method': 'POST',
        'data': params
    });
// 获取门店列表
export const getShopList = (params: any) =>
    request({
        'url': `/store/pageList/${params.page}/${params.pageSize}`,
        'method': 'get',
        params
    });
// 获取门店详情
export const getShopDetails = (params: any) =>
    request({
        'url': `/store/${params.id}`,
        'method': 'get',
        params
    });
// 获取门店搜索
export const seachShopList = (params: any) =>
    request({
        'url': `/store/search/${params.page}/${params.pageSize}/${params.name}`,
        'method': 'get',
        params
    });
// 门店添加
export const addShop = (params: any) =>
    request({
        'url': '/store/add',
        'method': 'post',
        'data': params
    });
// 门店编辑
export const editShop = (params: any) =>
    request({
        'url': '/store/update/', // ${params.storeId}
        'method': 'put',
        'data': params
    });
// 门店停业
export const stopShop = (params: any) =>
    request({
        'url': `/store/disabled/${params.id}/${params.status}`,
        'method': 'put',
        'data': params
    });
// 门店删除
export const delShop = (params: any) =>
    request({
        'url': `/store/del/${params.id}`,
        'method': 'delete',
        'data': params
    });
// 店长列表
export const getShopOwner = (params: any) =>
    request({
        'url': `/storeManager/pageList/${params.page}/${params.pageSize}/`,
        'method': 'get',
        params
    });
// 店长详情
export const getShopOwnerDetails = (params: any) =>
    request({
        'url': `/storeManager/${params.id}`,
        'method': 'get',
        params
    });
// 店长添加
export const addShopOwner = (params: any) =>
    request({
        'url': '/storeManager/add',
        'method': 'post',
        'data': params
    });
// 店长编辑
export const editShopOwner = (params: any) =>
    request({
        'url': '/storeManager/update',
        'method': 'put',
        'data': params
    });
// 店长暂停
export const stopShopOwner = (params: any) =>
    request({
        'url': `/storeManager/${params.id}/${params.status}`,
        'method': 'put',
        'data': params
    });
// 店长删除
export const delShopOwner = (params: any) =>
    request({
        'url': `/storeManager/del/${params.id}`,
        'method': 'delete',
        params
    });
// 获取门店省份信息
export const getProvince = (params: any) =>
    request({
        'url': '/storeManager/listProvince',
        'method': 'get',
        params
    });

// 获取当前省份的门店信息
export const getProvinceStopList = (params: any) =>
    request({
        'url': `/storeManager/getStoreByProvince/${params.region}`,
        'method': 'get',
        params
    });

