/**
 * 认证模块：B 端四套登录 + 登出 + 当前用户缓存 + C 端占位。
 *
 * 契约路径（provider @HttpExchange）：
 * - 企业账号  POST /enterprise/account/login  （shopId/phone/loginPass）
 * - 门店经理  POST /store/manager/login       （shopId/phone/loginPass）
 * - 运营人员  POST /operator/user/login       （loginName/loginPass）
 * - 门店员工  POST /staff/login               （shopId/loginName/password）
 *
 * 网关路由（deploy/nacos/gateway.yml）：
 * - /enterprise/account/** → operator-web
 * - /store/manager/**      → enterprise-web
 * - /operator/**、/sysDict/** → operator-web
 * - /staff/login、/table/**、/order/** 等 → cash-web
 * 网关未配置 StripPrefix，路径原样透传。
 */

import { http, setToken, unwrapLogin } from '../http'
import type {
  EnterpriseAccount,
  LoginPayload,
  LoginResult,
  OperatorUser,
  Staff,
  StoreManager,
} from '../types'

const USER_CACHE_KEY = 'xfcy.user'

function cacheUser(user: unknown, userType: string): void {
  localStorage.setItem(USER_CACHE_KEY, JSON.stringify({ user, userType }))
}

/** 读取本地缓存的当前用户（后端无专用「当前用户」端点，见报告决策点） */
export function getCurrentUser(): { user: unknown; userType: string } | null {
  const raw = localStorage.getItem(USER_CACHE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as { user: unknown; userType: string }
  } catch {
    return null
  }
}

/** 登出：契约层无登出端点，仅清理本地会话 */
export function logout(): void {
  setToken('')
  localStorage.removeItem(USER_CACHE_KEY)
}

function postParams(url: string, params: Record<string, string>) {
  return http.post(url, null, { params })
}

/** 企业账号登录：POST /enterprise/account/login */
export async function loginEnterprise(
  payload: LoginPayload,
): Promise<LoginResult<EnterpriseAccount>> {
  const { data } = await postParams('/enterprise/account/login', {
    shopId: payload.shopId,
    phone: payload.phone,
    loginPass: payload.loginPass,
  })
  const { token, user } = unwrapLogin<EnterpriseAccount>(data)
  setToken(token)
  cacheUser(user, 'shop')
  return { token, user, userType: 'shop' }
}

/** 门店经理登录：POST /store/manager/login */
export async function loginManager(payload: LoginPayload): Promise<LoginResult<StoreManager>> {
  const { data } = await postParams('/store/manager/login', {
    shopId: payload.shopId,
    phone: payload.phone,
    loginPass: payload.loginPass,
  })
  const { token, user } = unwrapLogin<StoreManager>(data)
  setToken(token)
  cacheUser(user, 'store_manager')
  return { token, user, userType: 'store_manager' }
}

/** 运营人员登录：POST /operator/user/login */
export async function loginOperator(
  loginName: string,
  loginPass: string,
): Promise<LoginResult<OperatorUser>> {
  const { data } = await postParams('/operator/user/login', { loginName, loginPass })
  const { token, user } = unwrapLogin<OperatorUser>(data)
  setToken(token)
  cacheUser(user, 'operator')
  return { token, user, userType: 'operator' }
}

/** 门店员工登录：POST /staff/login（注意参数名为 password，非 loginPass） */
export async function loginStaff(
  shopId: string,
  loginName: string,
  password: string,
): Promise<LoginResult<Staff>> {
  const { data } = await postParams('/staff/login', { shopId, loginName, password })
  const { token, user } = unwrapLogin<Staff>(data)
  setToken(token)
  cacheUser(user, 'staff')
  return { token, user, userType: 'staff' }
}

/**
 * C 端登录占位（§4.4）：xfcy-microapp-web 使用独立 StpLogic，
 * 网关对 /microApp/** 放行，鉴权在 C 端 BFF 内部完成，契约层暂无对应端点。
 */
export async function loginCustomer(): Promise<LoginResult> {
  throw new Error('C 端登录尚未接入：等待 microapp-web 契约（/microApp）落地')
}
