/**
 * 订单 / 报表模块（order 域）。
 * 契约：
 * - IOrderService      @HttpExchange("/order")
 * - IReportPayService  @HttpExchange("/report/pay")
 * - IReportDishService @HttpExchange("/report/dish")
 * 网关：/order/**、/report/** → enterprise-web。
 *
 * 报表查询参数 start/end 为 LocalDate（yyyy-MM-dd，必填），
 * 区间语义为 lastUpdateTime >= start AND < end（传「今天/明天」才是当日）。
 */

import { http, unwrap } from '../http'
import type {
  CurrentAmountCollectDTO,
  CurrentHourCollectDTO,
  OrderEntity,
  PayTypeCollectDTO,
  PrivilegeDTO,
} from '../types'

/* ---------------- 订单操作 ---------------- */

/** 下单 / 加菜（同一端点，body 带 orderId 为加菜）：POST /order/add */
export async function addOrder(order: OrderEntity): Promise<string> {
  const { data } = await http.post('/order/add', order)
  return unwrap<string>(data)
}

/** 退菜：POST /order/returnDish（body: DetailDTO，一次退一份） */
export async function returnDish(detailId: string, remarks: string[]): Promise<boolean> {
  const { data } = await http.post('/order/returnDish', { detailId, remarks })
  return unwrap<boolean>(data)
}

/** 现金结账：POST /order/pay（body: OrderEntity，金额字段前端透传） */
export async function pay(order: OrderEntity): Promise<boolean> {
  const { data } = await http.post('/order/pay', order)
  return unwrap<boolean>(data)
}

/** 挂账结账：POST /order/pay/credit（body: CreditPayRequest） */
export async function payByCredit(
  order: OrderEntity,
  credit: { creditId: string; creditAmount: number; creditUserName: string },
): Promise<boolean> {
  const { data } = await http.post('/order/pay/credit', { order, credit })
  return unwrap<boolean>(data)
}

/** 换台：PUT /order/changeTable?orderId=&targetTableId= */
export async function changeTable(orderId: string, targetTableId: string): Promise<boolean> {
  const { data } = await http.put('/order/changeTable', null, {
    params: { orderId, targetTableId },
  })
  return unwrap<boolean>(data)
}

/** 订单详情：GET /order/{id} */
export async function getById(id: string): Promise<OrderEntity> {
  const { data } = await http.get(`/order/${id}`)
  return unwrap<OrderEntity>(data)
}

/** 按流水号查已付订单（反结账入口）：GET /order/number/{orderNumber} */
export async function getByOrderNumber(orderNumber: string): Promise<OrderEntity | null> {
  const { data } = await http.get(`/order/number/${orderNumber}`)
  return unwrap<OrderEntity | null>(data)
}

/* ---------------- 经营报表（读模型） ---------------- */

/** 实时金额统计：GET /order/report/currentAmount?start=&end= */
export async function currentAmount(start: string, end: string): Promise<CurrentAmountCollectDTO> {
  const { data } = await http.get('/order/report/currentAmount', { params: { start, end } })
  return unwrap<CurrentAmountCollectDTO>(data)
}

/** 分时统计：GET /order/report/hourCollect?start=&end=&type=（type 1 金额 / 2 单数） */
export async function hourCollect(
  start: string,
  end: string,
  type: number,
): Promise<CurrentHourCollectDTO[]> {
  const { data } = await http.get('/order/report/hourCollect', {
    params: { start, end, type },
  })
  return unwrap<CurrentHourCollectDTO[]>(data)
}

/** 收款方式构成：GET /order/report/payType?start=&end= */
export async function payTypeCollect(
  start: string,
  end: string,
): Promise<PayTypeCollectDTO[]> {
  const { data } = await http.get('/order/report/payType', { params: { start, end } })
  return unwrap<PayTypeCollectDTO[]>(data)
}

/** 优惠统计：GET /order/report/privilege?start=&end= */
export async function privilegeCollect(start: string, end: string): Promise<PrivilegeDTO> {
  const { data } = await http.get('/order/report/privilege', { params: { start, end } })
  return unwrap<PrivilegeDTO>(data)
}
