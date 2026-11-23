/**
 * 挂账模块（store 域，BFF enterprise-web）。
 * 契约：CreditController @RequestMapping("/credit")
 * - POST /credit/add        新增（body: CreditPayload / CreditVO）
 * - GET  /credit/page/{n}/{s}?userName=
 * - GET  /credit/{id}       详情回显（CreditVO）
 * - PUT  /credit            修改（body: CreditPayload / CreditVO）
 * - POST /credit/repayment  还款（body: CreditRepaymentPayload）
 * 网关：/credit/** → enterprise-web。
 */

import { http, unwrap } from '../http'
import type { Credit, CreditPayload, CreditRepaymentPayload, PageResult } from '../types'

/** 挂账分页（按用餐人名称模糊）：GET /credit/page/{pageNum}/{pageSize}?userName= */
export async function page(
  pageNum: number,
  pageSize: number,
  userName?: string,
): Promise<PageResult<Credit>> {
  const { data } = await http.get(`/credit/page/${pageNum}/${pageSize}`, {
    params: { userName },
  })
  return unwrap<PageResult<Credit>>(data)
}

/** 挂账详情回显（含关联用餐人）：GET /credit/{id} */
export async function getById(id: string): Promise<CreditPayload> {
  const { data } = await http.get(`/credit/${id}`)
  return unwrap<CreditPayload>(data)
}

/** 新增挂账：POST /credit/add */
export async function add(payload: CreditPayload): Promise<boolean> {
  const { data } = await http.post('/credit/add', payload)
  return unwrap<boolean>(data)
}

/** 修改挂账：PUT /credit */
export async function update(payload: CreditPayload): Promise<boolean> {
  const { data } = await http.put('/credit', payload)
  return unwrap<boolean>(data)
}

/** 还款：POST /credit/repayment */
export async function repayment(payload: CreditRepaymentPayload): Promise<boolean> {
  const { data } = await http.post('/credit/repayment', payload)
  return unwrap<boolean>(data)
}
