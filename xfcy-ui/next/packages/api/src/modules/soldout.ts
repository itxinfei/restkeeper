/**
 * 沽清模块（store 域）。
 * 契约：
 * - ISellCalculationService @HttpExchange("/sellCalculation")
 * 网关：/sellCalculation → cash-web。
 *
 * 注意：cash-web BFF 当前仅暴露 POST /sellCalculation 与 DELETE /sellCalculation；
 * 分页 / 余量 / 增减走 provider 契约路径，BFF 端点待补（mock 模式不受影响）。
 *
 * 金额无关，数量为整数；remainder 为 -1 / undefined 表示不限量。
 */

import { http, unwrap } from '../http'
import type { PageResult, SellCalculation } from '../types'

/** 沽清分页（菜品名模糊、类型精确）：GET /sellCalculation/page/{pageNum}/{pageSize}?dishName=&dishType= */
export async function page(
  pageNum: number,
  pageSize: number,
  dishName?: string,
  dishType?: number,
): Promise<PageResult<SellCalculation>> {
  const { data } = await http.get(`/sellCalculation/page/${pageNum}/${pageSize}`, {
    params: { dishName, dishType },
  })
  return unwrap<PageResult<SellCalculation>>(data)
}

/** 按菜品查剩余沽清数量：GET /sellCalculation/remainder/{dishId} */
export async function remainder(dishId: string): Promise<number> {
  const { data } = await http.get(`/sellCalculation/remainder/${dishId}`)
  return unwrap<number>(data)
}

/** 扣减数量（下单内部调用）：PUT /sellCalculation/decrease?dishId=&dishNumber= */
export async function decrease(dishId: string, dishNumber: number): Promise<void> {
  const { data } = await http.put('/sellCalculation/decrease', null, {
    params: { dishId, dishNumber },
  })
  unwrap<void>(data)
}

/** 增加数量（退菜内部调用，无记录则新建）：POST /sellCalculation/add?dishId=&dishNumber= */
export async function increase(dishId: string, dishNumber: number): Promise<void> {
  const { data } = await http.post('/sellCalculation/add', null, {
    params: { dishId, dishNumber },
  })
  unwrap<void>(data)
}

/** 保存沽清记录：POST /sellCalculation */
export async function save(record: SellCalculation): Promise<boolean> {
  const { data } = await http.post('/sellCalculation', record)
  return unwrap<boolean>(data)
}

/** 批量删除：DELETE /sellCalculation（body: string[] ids） */
export async function remove(ids: string[]): Promise<boolean> {
  const { data } = await http.delete('/sellCalculation', { data: ids })
  return unwrap<boolean>(data)
}
