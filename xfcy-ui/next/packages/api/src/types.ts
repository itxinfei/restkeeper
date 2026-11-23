/**
 * 后端契约核心类型（手写，字段以 Java 实体为准）。
 *
 * 约定：
 * - 金额字段单位统一为「分」（Integer），展示层自行换算为元。
 * - 主键除 Staff（UUID）外均为雪花 ID，JSON 序列化为字符串。
 * - 时间字段为 ISO 字符串（LocalDateTime / LocalDate）。
 */

/** 通用分页结果，对应后端 PageResult<T> */
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

/** provider 层裸返回信封，对应 com.restkeeper.utils.Result */
export interface ProviderResult<T = unknown> {
  status: number
  desc?: string
  data?: T
  token?: string
}

/** BFF 层统一信封，对应 web_common BaseResponse */
export interface BffResponse<T = unknown> {
  success: boolean
  message?: string
  code: number
  data?: T
}

/* ----------------  operator 域 ---------------- */

/** t_enterprise_account */
export interface EnterpriseAccount {
  enterpriseId: string
  enterpriseName: string
  password?: string
  /** 商户号（8 位随机数字） */
  shopId: string
  applicant?: string
  phone: string
  province?: string
  city?: string
  area?: string
  address?: string
  applicationTime?: string
  expireTime?: string
  /** 0 试用 / 1 正式 / -1 停用 */
  status?: number
  lastUpdateTime?: string
  isDeleted?: number
}

/** t_operator_user */
export interface OperatorUser {
  uid: string
  loginname: string
  loginpass?: string
}

/** t_sys_dict */
export interface SysDictionary {
  dictId: string
  dictName: string
  /** 字典数据（编码） */
  dictData?: string
  /** 分类 */
  category?: string
  info?: string
}

/* ----------------  shop 域 ---------------- */

/** t_brand */
export interface Brand {
  brandId: string
  brandName: string
  category?: string
  logo?: string
  contact?: string
  contactPhone?: string
  storeCount?: number
  cityCount?: number
  info?: string
  shopId?: string
  lastUpdateTime?: string
  isDeleted?: number
}

/** t_store */
export interface Store {
  storeId: string
  storeName: string
  brandId?: string
  province?: string
  city?: string
  area?: string
  contact?: string
  contactPhone?: string
  storeManagerId?: string
  address?: string
  /** 1 营业 / 0 停业 */
  status?: number
  brand?: Brand
  lastUpdateTime?: string
  isDeleted?: number
}

/** t_store_manager */
export interface StoreManager {
  storeManagerId: string
  storeManagerName: string
  storeManagerPhone: string
  /** 1 启用 / 0 停用 */
  status?: number
  password?: string
  stores?: Store[]
  shopId?: string
  lastUpdateTime?: string
  isDeleted?: number
}

/** 门店列表轻量 DTO，对应 StoreDTO */
export interface StoreDTO {
  storeId: string
  storeName: string
  storeManagerId?: string
  address?: string
}

/* ----------------  store 域：菜品 / 套餐 ---------------- */

/** t_category（1 菜品 / 2 套餐） */
export interface DishCategory {
  categoryId?: string
  type?: number
  name: string
  torder?: number
  dishList?: Dish[]
  lastUpdateTime?: string
  isDeleted?: number
  shopId?: string
  storeId?: string
}

/** t_dish_flavor */
export interface DishFlavor {
  id?: string
  dishId?: string
  flavorName?: string
  flavorValue?: string
  shopId?: string
  storeId?: string
  lastUpdateTime?: string
}

/** t_dish */
export interface Dish {
  /** 新增时省略，由后端雪花 ID 生成 */
  id?: string
  name: string
  categoryId?: string
  /** 价格，单位「分」 */
  price?: number
  code?: string
  image?: string
  description?: string
  /** 1 起售 / 0 停售 */
  status?: number
  dishCategory?: DishCategory
  flavorList?: DishFlavor[]
  shopId?: string
  storeId?: string
  lastUpdateTime?: string
  isDeleted?: number
}

/** 菜品保存/修改请求体，对应 DishWithFlavors */
export interface DishWithFlavors {
  dish: Dish
  flavorList: DishFlavor[]
}

/** t_setmeal_dish */
export interface SetMealDish {
  id?: string
  setMealId?: string
  dishId?: string
  dishName?: string
  dishCopies?: number
  index?: number
  shopId?: string
  storeId?: string
  lastUpdateTime?: string
}

/** t_setmeal */
export interface SetMeal {
  id?: string
  categoryId?: string
  name: string
  /** 价格，单位「分」 */
  price?: number
  /** 1 起售 / 0 停售 */
  status?: number
  code?: string
  description?: string
  image?: string
  category?: DishCategory
  dishList?: SetMealDish[]
  shopId?: string
  storeId?: string
  lastUpdateTime?: string
  isDeleted?: number
}

/** 套餐保存/修改请求体，对应 SetMealWithDishes */
export interface SetMealWithDishes {
  setMeal: SetMeal
  dishList: SetMealDish[]
}

/* ----------------  store 域：桌台 ---------------- */

/** t_table_area */
export interface TableArea {
  areaId?: string
  areaName: string
  indexNumber: number
  shopId?: string
  storeId?: string
  lastUpdateTime?: string
  isDeleted?: number
}

/** t_table */
export interface Table {
  tableId?: string
  areaId?: string
  tableName: string
  tableSeatNumber?: number
  /** 0 空闲 / 1 开台 / 2 锁定 */
  status?: number
  area?: TableArea
  shopId?: string
  storeId?: string
  lastUpdateTime?: string
  isDeleted?: number
}

/** t_table_log */
export interface TableLog {
  logId?: string
  tableId?: string
  userId?: string
  /** 1 开桌 / 2 锁桌 */
  tableStatus?: number
  userNumbers?: number
  createTime?: string
  shopId?: string
  storeId?: string
  lastUpdateTime?: string
  isDeleted?: number
}

/* ----------------  store 域：员工 / 沽清 ---------------- */

/** t_staff */
export interface Staff {
  staffId: string
  staffName: string
  password?: string
  phone?: string
  sex?: string
  idNumber?: string
  /** 1 在职 / 0 停用 */
  status?: number
  shopId?: string
  storeId?: string
  lastUpdateTime?: string
  isDeleted?: number
}

/** t_sell_calculation（remainder 无记录约定 -1 表示不限量） */
export interface SellCalculation {
  id: string
  dishId?: string
  dishName?: string
  dishType?: number
  /** 剩余数量；-1 表示不限量 */
  remainder?: number
  shopId?: string
  storeId?: string
  lastUpdateTime?: string
}

/* ----------------  order 域 ---------------- */

/** t_order_detail */
export interface OrderDetail {
  detailId: string
  orderId?: string
  orderNumber?: string
  tableId?: string
  /** 1 正常/加菜 / 2 退菜 */
  detailStatus?: number
  dishRemark?: string
  dishId?: string
  /** 1 菜品 / 2 套餐 */
  dishType?: number
  dishName?: string
  /** 单价，单位「分」 */
  dishPrice?: number
  dishNumber?: number
  /** 金额，单位「分」 */
  dishAmount?: number
  flavorRemark?: string
  presentRemark?: string
  returnRemark?: string
  addRemark?: string
  dishCategoryName?: string
  shopId?: string
  storeId?: string
  lastUpdateTime?: string
  isDeleted?: number
}

/** t_order */
export interface OrderEntity {
  orderId: string
  orderNumber?: string
  tableId?: string
  /** 0 免单 / 1 现金 / 2 微信 / 3 支付宝 / 4 银行卡 / 5 挂账 */
  payType?: number
  /** 0 未付 / 1 已付 */
  payStatus?: number
  /** 实付金额，单位「分」 */
  payAmount?: number
  /** 抹零金额，单位「分」 */
  smallAmount?: number
  personNumbers?: number
  /** 应收金额，单位「分」 */
  totalAmount?: number
  /** 赠菜金额，单位「分」 */
  presentAmount?: number
  /** 免单金额，单位「分」 */
  freeAmount?: number
  orderRemark?: string
  createTime?: string
  operatorName?: string
  buyId?: string
  /** 0 收银端 / 1 App */
  orderSource?: number
  orderDetails?: OrderDetail[]
  shopId?: string
  storeId?: string
  lastUpdateTime?: string
  isDeleted?: number
}

/** 退菜请求体，对应 DetailDTO */
export interface DetailDTO {
  detailId: string
  remarks: string[]
}

/** 工作台实时金额统计，对应 CurrentAmountCollectDTO */
export interface CurrentAmountCollectDTO {
  /** 已付款总金额，单位「分」 */
  payTotal?: number
  payTotalCount?: number
  /** 未付款总金额，单位「分」 */
  noPayTotal?: number
  noPayTotalCount?: number
  totalPerson?: number
  currentPerson?: number
}

/** 收款方式构成项，对应 PayTypeCollectDTO（金额单位「分」） */
export interface PayTypeCollectDTO {
  payType?: number
  /** 金额，单位「分」 */
  amount?: number
  count?: number
}

/** 优惠统计，对应 PrivilegeDTO（金额单位「分」） */
export interface PrivilegeDTO {
  /** 赠菜金额，单位「分」 */
  presentAmount?: number
  /** 免单金额，单位「分」 */
  freeAmount?: number
  /** 抹零金额，单位「分」 */
  smallAmount?: number
}

/* ----------------  auth ---------------- */

/** 登录成功结果：token + 身份信息 */
export interface LoginResult<T = unknown> {
  token: string
  user: T
  userType?: string
}

/** B 端身份类型，对应 SystemCode 中的 userType */
export type BEndIdentity = 'shop' | 'store_manager' | 'operator' | 'staff'

/** 登录参数（企业/店长/运营为 loginPass，员工为 password） */
export interface LoginPayload {
  shopId: string
  /** 手机号（员工为姓名） */
  phone: string
  loginPass: string
}

/**
 * C 端登录用户（xfcy-microapp-web dev 打桩）。
 * 真实环境 openid 由微信 code2session 换取，shopId/storeId 由扫码上下文透传。
 */
export interface MicroappLoginUser {
  openid?: string
  shopId?: string
  storeId?: string
  userType?: string
}

/** C 端菜品检索文档（ES 索引 dish，字段名为下划线） */
export interface DishEsItem {
  id: string
  code?: string
  dishName?: string
  image?: string
  /** 1 菜品 / 2 套餐 */
  type?: number
  /** 单价，单位「分」 */
  price?: number
  /** 菜品描述（mock 检索附带，真实 DishEs 无此字段） */
  description?: string
  lastUpdateTime?: string
  storeId?: string
  shopId?: string
}

/** C 端检索结果包装 */
export interface MicroappSearchResult<T = DishEsItem> {
  records: T[]
  total: number
}

/** C 端检索响应：菜品与套餐合并返回 */
export interface MicroappSearchResponse {
  dish?: MicroappSearchResult
  setMeal?: MicroappSearchResult
}
