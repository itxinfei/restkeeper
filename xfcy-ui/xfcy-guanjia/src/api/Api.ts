import request from '@/utils/request';

export const getBrandList = (params: any) =>
    request({
        'url': `/brand/pageList/${params.page}/${params.pageSize}`,
        'method': 'get',
        params
    });
