/**
 * 系统字典 / 运营人员模块（operator 域）。
 * 契约：
 * - ISysDictService     @HttpExchange("/sysDict")    仅只读：GET /sysDict/list/{category}
 * - IOperatorUserService @HttpExchange("/operator/user")  仅只读：GET /operator/user/page/{pageNum}/{pageSize}?name=
 *
 * 说明：后端当前只提供查询契约，未提供字典/运营人员的增删改接口，
 * 因此 operator 端的写操作仅在 mock 模式下可用（见 apps/operator 说明）。
 */

import { http, unwrap } from '../http'
import type { OperatorUser, PageResult, SysDictionary } from '../types'

/** 字典分类（与后端 t_sys_dict.category 对齐） */
export const DICT_CATEGORIES = ['dish_category', 'order_status', 'pay_type', 'shop_tag'] as const

/** 分类展示名 */
export const DICT_CATEGORY_LABELS: Record<string, string> = {
  dish_category: '菜品分类',
  order_status: '订单状态',
  pay_type: '支付方式',
  shop_tag: '门店标签',
}

/** 按分类查询字典项：GET /sysDict/list/{category} */
export async function dictList(category: string): Promise<SysDictionary[]> {
  const { data } = await http.get(`/sysDict/list/${category}`)
  return unwrap<SysDictionary[]>(data)
}

/** 一次拉取全部已知分类字典（展示层便利方法，逐分类调用） */
export async function dictListAll(): Promise<SysDictionary[]> {
  const lists = await Promise.all(DICT_CATEGORIES.map((category) => dictList(category)))
  return lists.flat()
}

/** 运营人员分页：GET /operator/user/page/{pageNum}/{pageSize}?name= */
export async function operatorPage(
  pageNum: number,
  pageSize: number,
  name?: string,
): Promise<PageResult<OperatorUser>> {
  const { data } = await http.get(`/operator/user/page/${pageNum}/${pageSize}`, {
    params: { name },
  })
  return unwrap<PageResult<OperatorUser>>(data)
}

/* ---------------- 写操作（后端暂无契约，仅 mock 模式可用） ---------------- */

/** 演示扩展字段（真实 t_operator_user 仅有 uid/loginname/loginpass；新增时 uid 由后端生成） */
export type OperatorDraft = Omit<OperatorUser, 'uid'> & {
  uid?: string
  name?: string
  role?: string
  status?: string
  lastLogin?: string
}

/** 新增字典：POST /sysDict/add（mock 专用） */
export async function dictSave(item: SysDictionary): Promise<boolean> {
  const { data } = await http.post('/sysDict/add', item)
  return unwrap<boolean>(data)
}

/** 修改字典：PUT /sysDict（mock 专用） */
export async function dictUpdate(item: SysDictionary): Promise<boolean> {
  const { data } = await http.put('/sysDict', item)
  return unwrap<boolean>(data)
}

/** 删除字典：DELETE /sysDict/{dictId}（mock 专用） */
export async function dictRemove(dictId: string): Promise<boolean> {
  const { data } = await http.delete(`/sysDict/${dictId}`)
  return unwrap<boolean>(data)
}

/** 新增运营人员：POST /operator/user/add（mock 专用） */
export async function operatorSave(user: OperatorDraft): Promise<boolean> {
  const { data } = await http.post('/operator/user/add', user)
  return unwrap<boolean>(data)
}

/** 修改运营人员：PUT /operator/user（mock 专用） */
export async function operatorUpdate(user: OperatorDraft): Promise<boolean> {
  const { data } = await http.put('/operator/user', user)
  return unwrap<boolean>(data)
}

/** 重置运营人员密码：PUT /operator/user/resetPwd（mock 专用） */
export async function operatorResetPwd(uid: string, password: string): Promise<boolean> {
  const { data } = await http.put('/operator/user/resetPwd', null, { params: { uid, password } })
  return unwrap<boolean>(data)
}
