import request from '@/utils/request';
/**
 *
 * 员工管理
 *
 **/

// 获取员工列表
export const getMemberList = (params: any) =>
    request({
        'url': `/staff/pageList/${params.page}/${params.pageSize}`,
        'method': 'get',
        params
    });
// 获取员工详情
export const getMemberDetile = (params: any) =>
    request({
        'url': `/staff/${params.id}`,
        'method': 'get',
        params
    });
// 添加员工
export const addMember = (params: any) =>
    request({
        'url': '/staff/add',
        'method': 'post',
        'data': params
    });
// 编辑员工
export const editMember = (params: any) =>
    request({
        'url': '/staff/update', // /${params.staffId}
        'method': 'put',
        'data': params
    });
// 员工状态修改
export const memberStatus = (params: any) =>
    request({
        'url': `/staff/${params.id}/${params.status}`,
        'method': 'put',
        'data': params
    });
