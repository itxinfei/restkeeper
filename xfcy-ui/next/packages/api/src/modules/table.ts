/**
 * 桌台区域 / 桌台 / 开台模块（store 域）。
 * 契约：
 * - ITableAreaService  @HttpExchange("/table/area")
 * - ITableService      @HttpExchange("/table")
 * - ITableLogService   @HttpExchange("/table/log")
 * 网关：/table/** → enterprise-web。
 */

import { http, unwrap } from '../http'
import type { PageResult, Table, TableArea, TableLog } from '../types'

/* ---------------- 区域 ---------------- */

/** 新增区域：POST /table/area/add */
export async function areaAdd(area: TableArea): Promise<boolean> {
  const { data } = await http.post('/table/area/add', area)
  return unwrap<boolean>(data)
}

/** 区域列表（含桌台数量聚合）：GET /table/area/list */
export async function areaList(): Promise<Array<Record<string, unknown>>> {
  const { data } = await http.get('/table/area/list')
  return unwrap<Array<Record<string, unknown>>>(data)
}

/* ---------------- 桌台 ---------------- */

/** 新增桌台：POST /table/add */
export async function add(table: Table): Promise<boolean> {
  const { data } = await http.post('/table/add', table)
  return unwrap<boolean>(data)
}

/** 按区域分页：GET /table/area/{areaId}/page/{pageNum}/{pageSize} */
export async function pageByArea(
  areaId: string,
  pageNum: number,
  pageSize: number,
): Promise<PageResult<Table>> {
  const { data } = await http.get(`/table/area/${areaId}/page/${pageNum}/${pageSize}`)
  return unwrap<PageResult<Table>>(data)
}

/** 按区域与状态统计：GET /table/count/{areaId}/{status}（status 0/1/2） */
export async function countByStatus(areaId: string, status: number): Promise<number> {
  const { data } = await http.get(`/table/count/${areaId}/${status}`)
  return unwrap<number>(data)
}

/** 桌台详情：GET /table/{id} */
export async function getById(id: string): Promise<Table> {
  const { data } = await http.get(`/table/${id}`)
  return unwrap<Table>(data)
}

/** 更新桌台：PUT /table */
export async function update(table: Table): Promise<boolean> {
  const { data } = await http.put('/table', table)
  return unwrap<boolean>(data)
}

/* ---------------- 开台 ---------------- */

/** 开台：POST /table/log/open（body: TableLog，桌台联动由 provider 内部完成） */
export async function openTable(tableId: string, userNumbers: number): Promise<boolean> {
  const { data } = await http.post('/table/log/open', {
    tableId,
    userNumbers,
    tableStatus: 1,
  } satisfies TableLog)
  return unwrap<boolean>(data)
}

/** 按桌台查当前开台记录：GET /table/log/open/{tableId} */
export async function getOpenTableLog(tableId: string): Promise<TableLog | null> {
  const { data } = await http.get(`/table/log/open/${tableId}`)
  return unwrap<TableLog | null>(data)
}
