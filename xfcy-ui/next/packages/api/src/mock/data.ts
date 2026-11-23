/**
 * mock 演示数据源。
 *
 * 由 apps/manager 首版 mock 迁移而来，字段口径保持不变：
 * - 契约实体字段（Dish/SetMeal/Table 等）按后端实体形状，金额为「分」。
 * - 工作台/菜品页展示用数组（demo*）沿用原型示意数据，仅 mock 模式使用。
 *
 * 注意：以下均为演示「示意」数据，非真实经营数据。
 */

import type {
  Credit,
  CreditLog,
  CurrentHourCollectDTO,
  Dish,
  DishCategory,
  EnterpriseAccount,
  OperatorUser,
  OrderEntity,
  PayTypeCollectDTO,
  PrivilegeDTO,
  SellCalculation,
  SetMeal,
  Staff,
  SysDictionary,
  Table,
  TableArea,
} from '../types'

/* ---------------- 菜品分类（示意） ---------------- */

export const mockCategories: DishCategory[] = [
  { categoryId: 'cat-rec', type: 1, name: '招牌推荐', torder: 1 },
  { categoryId: 'cat-1', type: 1, name: '热菜', torder: 2 },
  { categoryId: 'cat-2', type: 1, name: '凉菜', torder: 3 },
  { categoryId: 'cat-3', type: 1, name: '主食', torder: 4 },
  { categoryId: 'cat-4', type: 1, name: '饮品', torder: 5 },
  { categoryId: 'cat-combo', type: 2, name: '套餐', torder: 6 },
  { categoryId: 'cat-liquor', type: 1, name: '酒水', torder: 7 },
  { categoryId: 'cat-5', type: 1, name: '甜点', torder: 8 },
]

/* ---------------- 菜品（示意，价格单位：分） ---------------- */

/** 演示扩展：沽清余量（真实契约无此字段，-1/undefined 表示不限） */
export type MockDish = Dish & { remain?: number | null }

export const mockDishes: MockDish[] = [
  { id: 'D001', name: '招牌红烧肉', description: '五花肉 · 冰糖慢炖', categoryId: 'cat-rec', code: 'D001', price: 4800, status: 1, image: '🍖', remain: 3, dishCategory: mockCategories[0] },
  { id: 'D002', name: '清蒸鲈鱼', description: '鲜活 · 粤式蒸法', categoryId: 'cat-rec', code: 'D002', price: 8800, status: 1, image: '🐟', remain: 0, dishCategory: mockCategories[0] },
  { id: 'D003', name: '毛血旺', description: '麻辣 · 现煮', categoryId: 'cat-rec', code: 'D003', price: 6800, status: 1, image: '🍲', remain: 5, dishCategory: mockCategories[0] },
  { id: 'D004', name: '蒜蓉时蔬', description: '时令 · 清炒', categoryId: 'cat-1', code: 'D004', price: 2200, status: 1, image: '🥬', remain: null, dishCategory: mockCategories[1] },
  { id: 'D005', name: '宫保鸡丁', description: '经典 · 微辣', categoryId: 'cat-1', code: 'D005', price: 3800, status: 1, image: '🍗', remain: null, dishCategory: mockCategories[1] },
  { id: 'D006', name: '凉拌黄瓜', description: '蒜香 · 爽脆', categoryId: 'cat-2', code: 'D006', price: 1800, status: 1, image: '🥒', remain: null, dishCategory: mockCategories[2] },
  { id: 'D007', name: '夫妻肺片', description: '红油 · 卤香', categoryId: 'cat-2', code: 'D007', price: 3200, status: 1, image: '🌶️', remain: 4, dishCategory: mockCategories[2] },
  { id: 'D008', name: '腊味煲仔饭', description: '丝苗米 · 锅巴香', categoryId: 'cat-3', code: 'D008', price: 3600, status: 0, image: '🍚', remain: null, dishCategory: mockCategories[3] },
  { id: 'D009', name: '手工水饺', description: '猪肉白菜 · 12 只', categoryId: 'cat-3', code: 'D009', price: 2800, status: 1, image: '🥟', remain: null, dishCategory: mockCategories[3] },
  { id: 'D010', name: '米饭', description: '东北大米', categoryId: 'cat-3', code: 'D010', price: 300, status: 1, image: '🍙', remain: null, dishCategory: mockCategories[3] },
  { id: 'D011', name: '酸梅汤', description: '冰镇 · 解腻', categoryId: 'cat-4', code: 'D011', price: 1200, status: 1, image: '🍹', remain: null, dishCategory: mockCategories[4] },
  { id: 'D012', name: '红糖冰粉', description: '手工 · 冰爽', categoryId: 'cat-4', code: 'D012', price: 1600, status: 1, image: '🍨', remain: null, dishCategory: mockCategories[4] },
  { id: 'D013', name: '双人小聚套餐', description: '红烧肉 + 时蔬 + 酸梅汤 ×2', categoryId: 'cat-combo', code: 'D013', price: 12800, status: 1, image: '🍱', remain: null, dishCategory: mockCategories[5] },
  { id: 'D014', name: '家庭四人餐', description: '鲈鱼 + 红烧肉 + 时蔬 + 主食 + 饮品', categoryId: 'cat-combo', code: 'D014', price: 26800, status: 1, image: '🎉', remain: 2, dishCategory: mockCategories[5] },
  { id: 'D015', name: '青岛啤酒', description: '500ml · 冰镇', categoryId: 'cat-liquor', code: 'D015', price: 1000, status: 1, image: '🍺', remain: 20, dishCategory: mockCategories[6] },
]

/* ---------------- 套餐（示意，价格单位：分） ---------------- */

export const mockSetMeals: SetMeal[] = [
  {
    id: 'combo-1',
    name: '双人小聚套餐（示意）',
    code: 'C001',
    price: 12800,
    status: 1,
    categoryId: 'cat-1',
    dishList: [
      { id: 'sd-1', setMealId: 'combo-1', dishId: 'D001', dishName: '招牌红烧肉', dishCopies: 1, index: 0 },
      { id: 'sd-2', setMealId: 'combo-1', dishId: 'D004', dishName: '蒜蓉时蔬', dishCopies: 1, index: 1 },
      { id: 'sd-3', setMealId: 'combo-1', dishId: 'D011', dishName: '酸梅汤', dishCopies: 2, index: 2 },
    ],
  },
  {
    id: 'combo-2',
    name: '家庭四人餐（示意）',
    code: 'C002',
    price: 26800,
    status: 1,
    categoryId: 'cat-1',
    dishList: [
      { id: 'sd-4', setMealId: 'combo-2', dishId: 'D002', dishName: '清蒸鲈鱼', dishCopies: 1, index: 0 },
      { id: 'sd-5', setMealId: 'combo-2', dishId: 'D001', dishName: '招牌红烧肉', dishCopies: 1, index: 1 },
      { id: 'sd-6', setMealId: 'combo-2', dishId: 'D004', dishName: '蒜蓉时蔬', dishCopies: 1, index: 2 },
      { id: 'sd-7', setMealId: 'combo-2', dishId: 'D008', dishName: '腊味煲仔饭', dishCopies: 1, index: 3 },
      { id: 'sd-8', setMealId: 'combo-2', dishId: 'D011', dishName: '酸梅汤', dishCopies: 4, index: 4 },
    ],
  },
  {
    id: 'combo-3',
    name: '单人商务餐（示意）',
    code: 'C003',
    price: 5800,
    status: 0,
    categoryId: 'cat-1',
    dishList: [
      { id: 'sd-9', setMealId: 'combo-3', dishId: 'D008', dishName: '腊味煲仔饭', dishCopies: 1, index: 0 },
      { id: 'sd-10', setMealId: 'combo-3', dishId: 'D011', dishName: '酸梅汤', dishCopies: 1, index: 1 },
    ],
  },
]

/* ---------------- 桌台区域 / 桌台（示意） ---------------- */

export const mockTableAreas: TableArea[] = [
  { areaId: 'area-1', areaName: '大厅', indexNumber: 1 },
  { areaId: 'area-2', areaName: '卡座', indexNumber: 2 },
  { areaId: 'area-3', areaName: '包间', indexNumber: 3 },
]

export const mockTables: Table[] = [
  { tableId: 'A01', areaId: 'area-1', tableName: 'A01 大厅', tableSeatNumber: 2, status: 0 },
  { tableId: 'A02', areaId: 'area-1', tableName: 'A02 大厅', tableSeatNumber: 4, status: 0 },
  { tableId: 'A03', areaId: 'area-1', tableName: 'A03 大厅', tableSeatNumber: 4, status: 1 },
  { tableId: 'A04', areaId: 'area-1', tableName: 'A04 大厅', tableSeatNumber: 3, status: 0 },
  { tableId: 'A05', areaId: 'area-1', tableName: 'A05 大厅', tableSeatNumber: 2, status: 0 },
  { tableId: 'C01', areaId: 'area-2', tableName: 'C01 卡座', tableSeatNumber: 6, status: 1 },
  { tableId: 'C02', areaId: 'area-2', tableName: 'C02 卡座', tableSeatNumber: 6, status: 0 },
  { tableId: 'C03', areaId: 'area-2', tableName: 'C03 卡座', tableSeatNumber: 4, status: 0 },
  { tableId: 'B01', areaId: 'area-3', tableName: 'B01 包间', tableSeatNumber: 8, status: 1 },
  { tableId: 'B02', areaId: 'area-3', tableName: 'B02 包间', tableSeatNumber: 10, status: 0 },
]

/* ---------------- 报表（示意，金额单位：分） ---------------- */

/** 分时统计（type=1 金额为分；type=2 时 totalAmount 为单数） */
export const mockHourCollect: CurrentHourCollectDTO[] = [
  { currentDateHour: 10, totalAmount: 42800 },
  { currentDateHour: 11, totalAmount: 96400 },
  { currentDateHour: 12, totalAmount: 218600 },
  { currentDateHour: 13, totalAmount: 152300 },
  { currentDateHour: 14, totalAmount: 64800 },
  { currentDateHour: 17, totalAmount: 38600 },
  { currentDateHour: 18, totalAmount: 128400 },
  { currentDateHour: 19, totalAmount: 246800 },
  { currentDateHour: 20, totalAmount: 187200 },
  { currentDateHour: 21, totalAmount: 96400 },
]

/** 收款方式构成 */
export const mockPayTypeCollect: PayTypeCollectDTO[] = [
  { payType: 2, payName: '微信', totalCount: 20 },
  { payType: 1, payName: '现金', totalCount: 12 },
  { payType: 3, payName: '支付宝', totalCount: 8 },
  { payType: 5, payName: '挂账', totalCount: 2 },
]

/** 优惠统计 */
export const mockPrivilege: PrivilegeDTO = {
  presentAmount: 12800,
  freeAmount: 0,
  smallAmount: 640,
}

/* ---------------- 订单（示意，金额单位：分） ---------------- */

export const mockOrders: OrderEntity[] = [
  {
    orderId: '1',
    orderNumber: 'FK-018',
    tableId: 'A03',
    payType: 2,
    payStatus: 0,
    payAmount: 32800,
    totalAmount: 32800,
    personNumbers: 4,
    createTime: '2026-11-22T12:41:00',
    operatorName: '李国栋',
  },
  {
    orderId: '2',
    orderNumber: 'FK-017',
    tableId: 'B01',
    payType: 5,
    payStatus: 0,
    payAmount: 124600,
    totalAmount: 124600,
    personNumbers: 8,
    createTime: '2026-11-22T12:18:00',
    operatorName: '李国栋',
  },
  {
    orderId: '3',
    orderNumber: 'FK-015',
    tableId: 'A01',
    payType: 1,
    payStatus: 1,
    payAmount: 9600,
    totalAmount: 9600,
    personNumbers: 2,
    createTime: '2026-11-22T11:52:00',
    operatorName: '李国栋',
  },
]

/* ---------------- 工作台展示数据（示意，仅 mock 使用） ---------------- */

export type Trend = 'up' | 'down' | 'flat'

export type DemoKpi = {
  key: string
  label: string
  value: string
  trend: Trend
  trendText: string
}

export const demoDashboardKpis: DemoKpi[] = [
  { key: 'revenue', label: '今日营业额（示意）', value: '¥8,642.00', trend: 'up', trendText: '▲ 12.4%' },
  { key: 'orders', label: '今日单量（示意）', value: '42', trend: 'up', trendText: '▲ 6 单' },
  { key: 'avg', label: '客单价（示意）', value: '¥205.80', trend: 'down', trendText: '▼ 3.2%' },
  { key: 'turnover', label: '翻台率（示意）', value: '1.8', trend: 'flat', trendText: '目标 2.0' },
]

export type DemoOrderStatus = '已结' | '挂账' | '未结'

export type DemoOrderRow = {
  id: number
  orderNo: string
  table: string
  guests: number
  amount: string
  pay: string
  status: DemoOrderStatus
  time: string
}

export const demoLatestOrders: DemoOrderRow[] = [
  { id: 1, orderNo: 'FK-018', table: 'A03 大厅', guests: 4, amount: '¥328.00', pay: '微信', status: '未结', time: '12:41' },
  { id: 2, orderNo: 'FK-017', table: 'B01 包间', guests: 8, amount: '¥1,246.00', pay: '挂账', status: '挂账', time: '12:18' },
  { id: 3, orderNo: 'FK-015', table: 'A01 大厅', guests: 2, amount: '¥96.00', pay: '现金', status: '已结', time: '11:52' },
  { id: 4, orderNo: 'FK-012', table: 'C02 卡座', guests: 6, amount: '¥512.00', pay: '支付宝', status: '已结', time: '11:20' },
  { id: 5, orderNo: 'FK-009', table: 'A05 大厅', guests: 3, amount: '¥178.00', pay: '微信', status: '已结', time: '10:47' },
]

export type DemoTableStat = {
  key: string
  label: string
  value: number
  tone: 'normal' | 'warn'
}

export const demoTableStats: DemoTableStat[] = [
  { key: 'free', label: '空闲', value: 12, tone: 'normal' },
  { key: 'opened', label: '开台', value: 7, tone: 'warn' },
  { key: 'locked', label: '锁定', value: 1, tone: 'normal' },
]

export type DemoSoldoutRow = {
  id: number
  name: string
  remain: string
  danger: boolean
}

export const demoSoldoutRows: DemoSoldoutRow[] = [
  { id: 1, name: '招牌红烧肉', remain: '余 3', danger: false },
  { id: 2, name: '清蒸鲈鱼', remain: '余 0', danger: true },
  { id: 3, name: '蒜蓉时蔬', remain: '不限', danger: false },
]

/* ---------------- 企业账号 / 回收站（operator 域，示意） ---------------- */

export type MockPlan = '基础版' | '标准版' | '旗舰版'

/** 演示扩展：套餐/门店数（真实契约无此字段） */
export type MockEnterpriseAccount = EnterpriseAccount & {
  plan?: MockPlan
  shopCount?: number
}

/** 演示扩展：删除时间/删除人（真实契约无此字段，仅回收站展示用） */
export type MockRemovedEnterpriseAccount = MockEnterpriseAccount & {
  deletedAt?: string
  deletedBy?: string
}

export const mockEnterpriseAccounts: MockEnterpriseAccount[] = [
  { enterpriseId: '1', enterpriseName: '心飞餐饮（国贸店）', shopId: '88010023', applicant: '李国栋', phone: '13801002301', shopCount: 3, plan: '旗舰版', status: 1, applicationTime: '2025-06-30 10:00:00', expireTime: '2027-06-30', lastUpdateTime: '2026-11-22 09:12:00' },
  { enterpriseId: '2', enterpriseName: '心飞餐饮（望京店）', shopId: '88010024', applicant: '赵敏', phone: '13801002302', shopCount: 1, plan: '基础版', status: 0, applicationTime: '2026-11-15 10:00:00', expireTime: '2026-11-29', lastUpdateTime: '2026-11-20 18:40:00' },
  { enterpriseId: '3', enterpriseName: '老城根火锅', shopId: '88010025', applicant: '孙浩', phone: '13801002303', shopCount: 2, plan: '标准版', status: 1, applicationTime: '2025-09-10 10:00:00', expireTime: '2027-01-15', lastUpdateTime: '2026-11-21 11:05:00' },
  { enterpriseId: '4', enterpriseName: '川香居', shopId: '88010026', applicant: '周婷', phone: '138010024', shopCount: 1, plan: '基础版', status: -1, applicationTime: '2024-05-20 10:00:00', expireTime: '2026-05-20', lastUpdateTime: '2026-11-18 16:20:00' },
  { enterpriseId: '5', enterpriseName: '蓝湾咖啡', shopId: '88010027', applicant: '吴斌', phone: '13801002305', shopCount: 4, plan: '标准版', status: 1, applicationTime: '2025-11-22 10:00:00', expireTime: '2027-03-01', lastUpdateTime: '2026-11-22 08:30:00' },
]

export const mockRemovedEnterpriseAccounts: MockRemovedEnterpriseAccount[] = [
  { enterpriseId: '101', enterpriseName: '悦来小馆', shopId: '88010018', applicant: '郑爽', phone: '13801002306', shopCount: 1, plan: '基础版', status: -1, isDeleted: 1, applicationTime: '2025-03-11 10:00:00', expireTime: '2026-03-11', lastUpdateTime: '2026-11-18 16:20:00', deletedAt: '2026-11-18 16:20', deletedBy: '王运营' },
  { enterpriseId: '102', enterpriseName: '巷口面馆', shopId: '88010019', applicant: '冯远', phone: '13801002307', shopCount: 2, plan: '标准版', status: -1, isDeleted: 1, applicationTime: '2025-08-02 10:00:00', expireTime: '2026-08-02', lastUpdateTime: '2026-11-20 09:05:00', deletedAt: '2026-11-20 09:05', deletedBy: '王运营' },
]

/* ---------------- 系统字典 / 运营人员（operator 域，示意） ---------------- */

export const mockDictItems: SysDictionary[] = [
  { dictId: '1', category: 'dish_category', dictName: '菜品分类', dictData: '热菜', info: '启用' },
  { dictId: '2', category: 'dish_category', dictName: '菜品分类', dictData: '主食', info: '启用' },
  { dictId: '3', category: 'order_status', dictName: '订单状态', dictData: '已结', info: '启用' },
  { dictId: '4', category: 'order_status', dictName: '订单状态', dictData: '挂账', info: '启用' },
  { dictId: '5', category: 'pay_type', dictName: '支付方式', dictData: '微信', info: '启用' },
  { dictId: '6', category: 'shop_tag', dictName: '门店标签', dictData: '商场店', info: '停用' },
]

/** 演示扩展：角色/姓名/最近登录（真实契约仅有 uid/loginname） */
export type MockOperatorUser = OperatorUser & {
  name?: string
  role?: string
  status?: string
  lastLogin?: string
}

export const mockOperatorUsers: MockOperatorUser[] = [
  { uid: '1', loginname: 'admin', name: '王运营', role: '平台运营', status: '启用', lastLogin: '2026-11-22 09:12' },
  { uid: '2', loginname: 'ops01', name: '陈晓', role: '运营', status: '启用', lastLogin: '2026-11-21 18:40' },
  { uid: '3', loginname: 'ops02', name: '刘洋', role: '客服', status: '停用', lastLogin: '2026-10-30 11:05' },
]

/* ---------------- 员工（store 域，示意） ---------------- */

export const mockStaffs: Staff[] = [
  { staffId: 'S001', staffName: '李国栋', phone: '13801002301', sex: '男', idNumber: '110101199001011234', status: 1 },
  { staffId: 'S002', staffName: '张倩', phone: '13801002302', sex: '女', idNumber: '110101199203022345', status: 1 },
  { staffId: 'S003', staffName: '王强', phone: '13801002303', sex: '男', status: 0 },
  { staffId: 'S004', staffName: '刘敏', phone: '13801002304', sex: '女', status: 1 },
]

/* ---------------- 挂账（store 域，示意，金额单位：分） ---------------- */

export const mockCredits: Credit[] = [
  {
    creditId: 'CR001',
    creditType: 1,
    companyName: '心飞科技有限公司',
    userName: '陈国栋',
    phone: '13901001101',
    status: 1,
    creditAmount: 500000,
    totalRepaymentAmount: 320000,
    users: [
      { id: 'CU001', creditId: 'CR001', userName: '陈国栋', phone: '13901001101' },
      { id: 'CU002', creditId: 'CR001', userName: '林晓', phone: '13901001102' },
    ],
  },
  {
    creditId: 'CR002',
    creditType: 2,
    userName: '赵敏',
    phone: '13901001103',
    status: 1,
    creditAmount: 80000,
    totalRepaymentAmount: 20000,
  },
  {
    creditId: 'CR003',
    creditType: 1,
    companyName: '蓝湾贸易',
    userName: '孙浩',
    phone: '13901001104',
    status: 0,
    creditAmount: 120000,
    totalRepaymentAmount: 120000,
  },
]

/** 挂账流水（示意，金额单位：分）。与 mockOrders 的 orderId 对应。 */
export const mockCreditLogs: CreditLog[] = [
  {
    logId: 'CL001',
    orderId: 'ORD001',
    creditId: 'CR001',
    type: 1,
    companyName: '心飞科技有限公司',
    userName: '陈国栋',
    phone: '13901001101',
    orderAmount: 26800,
    receivedAmount: 26800,
    creditAmount: 26800,
    lastUpdateTime: '2026-11-18T12:30:00',
  },
  {
    logId: 'CL002',
    orderId: 'ORD002',
    creditId: 'CR001',
    type: 1,
    companyName: '心飞科技有限公司',
    userName: '林晓',
    phone: '13901001102',
    orderAmount: 15600,
    receivedAmount: 15600,
    creditAmount: 15600,
    lastUpdateTime: '2026-11-19T19:10:00',
  },
  {
    logId: 'CL003',
    orderId: 'ORD003',
    creditId: 'CR002',
    type: 2,
    userName: '赵敏',
    phone: '13901001103',
    orderAmount: 9800,
    receivedAmount: 9800,
    creditAmount: 9800,
    lastUpdateTime: '2026-11-20T13:05:00',
  },
  {
    logId: 'CL004',
    orderId: 'ORD005',
    creditId: 'CR002',
    type: 2,
    userName: '赵敏',
    phone: '13901001103',
    orderAmount: 4200,
    receivedAmount: 4200,
    creditAmount: 4200,
    lastUpdateTime: '2026-11-21T18:40:00',
  },
  {
    logId: 'CL005',
    orderId: 'ORD007',
    creditId: 'CR003',
    type: 1,
    companyName: '蓝湾贸易',
    userName: '孙浩',
    phone: '13901001104',
    orderAmount: 31000,
    receivedAmount: 31000,
    creditAmount: 31000,
    lastUpdateTime: '2026-11-16T12:00:00',
  },
]

/* ---------------- 沽清（store 域，示意） ---------------- */

export const mockSellCalculations: SellCalculation[] = [
  { id: 'SC001', dishId: 'D001', dishName: '招牌红烧肉', dishType: 1, sellLimitTotal: 20, remainder: 3 },
  { id: 'SC002', dishId: 'D002', dishName: '清蒸鲈鱼', dishType: 1, sellLimitTotal: 10, remainder: 0 },
  { id: 'SC003', dishId: 'D007', dishName: '夫妻肺片', dishType: 1, sellLimitTotal: 15, remainder: 4 },
  { id: 'SC004', dishId: 'D014', dishName: '家庭四人餐', dishType: 2, sellLimitTotal: 8, remainder: 2 },
]
