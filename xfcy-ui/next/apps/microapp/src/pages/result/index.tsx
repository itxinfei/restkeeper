import { Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getLastOrder } from '../../store/cart'
import { getStoreInfo } from '../../api/service'
import { yuan } from '../../utils/format'
import './index.scss'

/**
 * 支付结果（原型 microapp-result.html）
 * 成功态 + 订单号 + 金额 + 进度步骤 + 返回/查看按钮。
 */
export default function Result() {
  const order = getLastOrder()
  const store = getStoreInfo()

  const backToMenu = () => {
    Taro.reLaunch({ url: '/pages/menu/index' })
  }

  const viewOrder = () => {
    // 示意：订单详情页待后端就绪后接入
    Taro.showToast({ title: '示意：订单详情待接入', icon: 'none' })
  }

  if (!order) {
    return (
      <View className="xf-page result">
        <View className="xf-empty">
          <View className="xf-empty__icon">🧾</View>
          <Text className="xf-empty__title">暂无支付结果</Text>
          <Text className="xf-empty__desc">请先从菜单下单</Text>
          <View className="result__back" onClick={backToMenu}>
            返回菜单
          </View>
        </View>
      </View>
    )
  }

  return (
    <View className="xf-page result">
      {/* 成功态 */}
      <View className="result__hero">
        <View className="result__ok">✓</View>
        <Text className="result__title">支付成功</Text>
        <Text className="result__sub">厨房已接单，请稍候上菜</Text>
        <Text className="result__demo">开发演示环境 · 支付为模拟打桩，不会真实扣款</Text>
      </View>

      {/* 订单信息 */}
      <View className="result__card">
        <View className="result__row">
          <Text>订单号</Text>
          <Text className="xf-num">{order.orderNumber}</Text>
        </View>
        <View className="result__row">
          <Text>桌台</Text>
          <Text>
            {store.tableName} · {store.areaName}
          </Text>
        </View>
        <View className="result__row">
          <Text>下单时间</Text>
          <Text className="xf-num">{order.createTime}</Text>
        </View>
        <View className="result__row">
          <Text>支付时间</Text>
          <Text className="xf-num">{order.payTime}</Text>
        </View>
        <View className="result__row result__row--amount">
          <Text>实付金额</Text>
          <Text className="result__amount xf-num">{yuan(order.totalAmount)}</Text>
        </View>
      </View>

      {/* 进度步骤 */}
      <View className="result__steps">
        <View className="result__step is-done">
          <View className="result__dot">✓</View>
          <Text>下单</Text>
        </View>
        <View className="result__step is-done">
          <View className="result__dot">✓</View>
          <Text>支付</Text>
        </View>
        <View className="result__step">
          <View className="result__dot">3</View>
          <Text>制作</Text>
        </View>
        <View className="result__step">
          <View className="result__dot">4</View>
          <Text>上菜</Text>
        </View>
      </View>

      {/* 底部按钮 */}
      <View className="result__bar">
        <View className="result__btn result__btn--ghost" onClick={backToMenu}>
          返回菜单
        </View>
        <View className="result__btn result__btn--primary" onClick={viewOrder}>
          查看订单
        </View>
      </View>
    </View>
  )
}
