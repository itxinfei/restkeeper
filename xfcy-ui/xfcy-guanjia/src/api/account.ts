import request from '@/utils/request';
/**
 *
 * 挂账管理
 *
 **/

// 获取挂账列表
export const getAccountList = (params: any) =>
    request({
        'url': `/credit/pageList/${params.page}/${params.pageSize}`,
        'method': 'get',
        params
    });

// 获取挂账详情
export const getAccountDet = (params: any) =>
    request({
        'url': `/credit/${params.id}`,
        'method': 'get',
        params
    });

// 添加挂账
export const addAccount = (params: any) =>
    request({
        'url': '/credit/add',
        'method': 'post',
        'data': params
    });

// 修改挂账
export const putAccount = (params: any) =>
    request({
        'url': `/credit/update/${params.id}`,
        'method': 'put',
        'data': params
    });

// 状态更改
export const accountStatus = (params: any) =>
    request({
        'url': `/credit/update/${params.id}/status/${params.status}`,
        'method': 'put',
        'data': params
    });

// 还款
export const repayment = (params: any) =>
    request({
        'url': '/credit/repayment',
        'method': 'post',
        'data': params
    });

// 删除挂账
export const delAccount = (params: any) =>
    request({
        'url': `/credit/update/${params.id}`,
        'method': 'delete',
        params
    });

// 订单详情
export const getOrderList = (params: any) =>
    request({
        'url': `/credit/creditLog/${params.page}/${params.pageSize}/${params.id}`,
        'method': 'get',
        params
    });

// 报表下载
export const getList = (params: any) =>
    request({
        'url': `/credit/export/creditId/${params.creditId}/start/${params.start}/end/${params.end}`,
        'method': 'get',
        params
    });
