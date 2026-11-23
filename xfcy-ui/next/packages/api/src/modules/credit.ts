/**
 * 挂账模块（store 域，BFF enterprise-web）。
 * 契约：CreditController @RequestMapping("/credit")
 * - POST /credit/add        新增（body: CreditPayload / CreditVO）
 * - GET  /credit/page/{n}/{s}?userName=
 * - GET  /credit/{id}       详情回显（CreditVO）
 * - PUT  /credit            修改（body: CreditPayload / CreditVO）
 * - POST /credit/repayment  还款（body: CreditRepaymentPayload）
 * - GET  /credit/log/page/{n}/{s}/{creditId}  挂账流水分页
 * - GET  /credit/export/creditId/{id}/start/{start}/end/{end}  流水导出（xlsx）
 * 网关：/credit/** → enterprise-web（收银端读主体列表复用同一端点）。
 */

import { ApiError, getApiConfig, getToken, http, unwrap } from '../http'
import { isMockMode } from '../mock'
import type {
  Credit,
  CreditLog,
  CreditPayload,
  CreditRepaymentPayload,
  PageResult,
} from '../types'

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

/** 挂账流水分页：GET /credit/log/page/{pageNum}/{pageSize}/{creditId} */
export async function logPage(
  pageNum: number,
  pageSize: number,
  creditId: string,
): Promise<PageResult<CreditLog>> {
  const { data } = await http.get(`/credit/log/page/${pageNum}/${pageSize}/${encodeURIComponent(creditId)}`)
  return unwrap<PageResult<CreditLog>>(data)
}

/** 触发浏览器下载（仅 B 端浏览器环境可用）。 */
function triggerDownload(blob: Blob, fileName: string): void {
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = fileName
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(url)
}

function csvCell(value: string): string {
  return /[",\n]/.test(value) ? `"${value.replace(/"/g, '""')}"` : value
}

/**
 * 挂账流水导出。
 * - real：下载 BFF 生成的 xlsx（GET /credit/export/...，带 token）。
 * - mock：用本地流水生成 CSV（同样走 logPage，保持演示行为一致）。
 */
export async function exportLogs(creditId: string, start: string, end: string): Promise<void> {
  if (isMockMode()) {
    const result = await logPage(1, 500, creditId)
    const header = '订单号,挂账人,类型,订单金额(元),应收金额(元),挂账金额(元),挂账时间'
    const lines = result.records.map((row) =>
      [
        row.orderId ?? '',
        row.userName ?? '',
        row.type === 1 ? '公司' : '个人',
        ((row.orderAmount ?? 0) / 100).toFixed(2),
        ((row.receivedAmount ?? 0) / 100).toFixed(2),
        ((row.creditAmount ?? 0) / 100).toFixed(2),
        row.lastUpdateTime ?? '',
      ]
        .map(csvCell)
        .join(','),
    )
    const blob = new Blob(['\ufeff' + [header, ...lines].join('\n')], {
      type: 'text/csv;charset=utf-8',
    })
    triggerDownload(blob, `挂账流水_${creditId}.csv`)
    return
  }

  const cfg = getApiConfig()
  const base = cfg.baseURL.replace(/\/$/, '')
  const url = `${base}/credit/export/creditId/${encodeURIComponent(creditId)}/start/${encodeURIComponent(start)}/end/${encodeURIComponent(end)}`
  const token = getToken()
  const response = await fetch(url, {
    headers: token ? { [cfg.tokenHeader]: token } : {},
  })
  if (!response.ok) throw new ApiError('挂账流水导出失败', response.status)
  triggerDownload(await response.blob(), `挂账流水_${creditId}.xlsx`)
}
