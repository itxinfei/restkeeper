/**
 * 员工模块（store 域）。
 * 契约：
 * - IStaffService @HttpExchange("/staff")
 * 网关：/staff/** → enterprise-web（分页 / 更新 / 停用端点已补齐）。
 */

import { http, unwrap } from '../http'
import type { PageResult, Staff } from '../types'

/** 员工分页（姓名 / 手机号模糊）：GET /staff/page/{pageNum}/{pageSize}?staffName=&phone= */
export async function page(
  pageNum: number,
  pageSize: number,
  staffName?: string,
  phone?: string,
): Promise<PageResult<Staff>> {
  const { data } = await http.get(`/staff/page/${pageNum}/${pageSize}`, {
    params: { staffName, phone },
  })
  return unwrap<PageResult<Staff>>(data)
}

/** 新增员工：POST /staff/add */
export async function add(staff: Staff): Promise<boolean> {
  const { data } = await http.post('/staff/add', staff)
  return unwrap<boolean>(data)
}

/** 按主键更新员工（含停用后重新启用：status=1）：PUT /staff */
export async function update(staff: Staff): Promise<boolean> {
  const { data } = await http.put('/staff', staff)
  return unwrap<boolean>(data)
}

/** 停用员工（status 置 0）：PUT /staff/pause/{id} */
export async function pause(id: string): Promise<boolean> {
  const { data } = await http.put(`/staff/pause/${id}`)
  return unwrap<boolean>(data)
}
