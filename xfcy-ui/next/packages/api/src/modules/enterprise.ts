/**
 * 企业账号模块（operator 域）。
 * 契约：IEnterpriseAccountService，类级 @HttpExchange("/enterprise/account")。
 */

import { http, unwrap } from '../http'
import type { EnterpriseAccount, PageResult } from '../types'

/** 分页查询（仅未删除）：GET /enterprise/account/page/{pageNum}/{pageSize}?enterpriseName=&status= */
export async function page(
  pageNum: number,
  pageSize: number,
  enterpriseName?: string,
  status?: number | null,
): Promise<PageResult<EnterpriseAccount>> {
  const { data } = await http.get(`/enterprise/account/page/${pageNum}/${pageSize}`, {
    params: { enterpriseName, status },
  })
  return unwrap<PageResult<EnterpriseAccount>>(data)
}

/** 回收站分页：GET /enterprise/account/removed/page/{pageNum}/{pageSize}?enterpriseName= */
export async function removedPage(
  pageNum: number,
  pageSize: number,
  enterpriseName?: string,
): Promise<PageResult<EnterpriseAccount>> {
  const { data } = await http.get(`/enterprise/account/removed/page/${pageNum}/${pageSize}`, {
    params: { enterpriseName },
  })
  return unwrap<PageResult<EnterpriseAccount>>(data)
}

/** 开通企业：POST /enterprise/account/add（body + validityDay?） */
export async function add(account: EnterpriseAccount, validityDay?: number): Promise<boolean> {
  const { data } = await http.post('/enterprise/account/add', account, {
    params: { validityDay },
  })
  return unwrap<boolean>(data)
}

/** 账号详情：GET /enterprise/account/{id} */
export async function getById(id: string): Promise<EnterpriseAccount> {
  const { data } = await http.get(`/enterprise/account/${id}`)
  return unwrap<EnterpriseAccount>(data)
}

/** 账号编辑：PUT /enterprise/account（body + validityDay?） */
export async function update(account: EnterpriseAccount, validityDay?: number): Promise<boolean> {
  const { data } = await http.put('/enterprise/account', account, {
    params: { validityDay },
  })
  return unwrap<boolean>(data)
}

/** 逻辑删除：DELETE /enterprise/account/{id} */
export async function remove(id: string): Promise<boolean> {
  const { data } = await http.delete(`/enterprise/account/${id}`)
  return unwrap<boolean>(data)
}

/** 还原：PUT /enterprise/account/recovery/{id} */
export async function recovery(id: string): Promise<boolean> {
  const { data } = await http.put(`/enterprise/account/recovery/${id}`)
  return unwrap<boolean>(data)
}

/** 停用：PUT /enterprise/account/forbidden/{id} */
export async function forbidden(id: string): Promise<boolean> {
  const { data } = await http.put(`/enterprise/account/forbidden/${id}`)
  return unwrap<boolean>(data)
}

/** 重置密码：PUT /enterprise/account/resetPwd?id=&password= */
export async function resetPwd(id: string, password: string): Promise<boolean> {
  const { data } = await http.put('/enterprise/account/resetPwd', null, {
    params: { id, password },
  })
  return unwrap<boolean>(data)
}
