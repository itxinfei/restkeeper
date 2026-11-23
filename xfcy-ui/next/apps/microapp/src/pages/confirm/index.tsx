import { useState } from 'react'
import { Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { clearCart, setLastOrder, useCart } from '../../store/cart'
import { getPayMethods, getStoreInfo, submitOrder } from '../../api/service'
import { yuan } from '../../utils/format'
import './index.scss'

/**
 * 下单确认（原型 microapp-cart.html 的「确认订单」用餐信息 + 提交订单部分）
 * 提交后调用 /order/submit → /pay/mock，跳转支付结果页。
 */
export default function Confirm() {
  const { items, count, total } = useCart()
  const payMethods = getPayMethods()
  const store = getStoreInfo()
  const [payMethod, setPayMethod] = useState(payMethods[0].id)
  const [submitting, setSubmitting] = useState(false)

  const submit = async () => {
    if (submitting || count === 0) return
    setSubmitting(true)
    try {
      const order = await submitOrder(items, '', payMethod)
      setLastOrder(order)
      clearCart()
      // redirectTo：结果页返回时不回到确认页
      Taro.redirectTo({ url: '/pages/result/index' })
    } catch {
      Taro.showToast({ title: '提交失败，请重试', icon: 'none' })
      setSubmitting(false)
    }
  }

  return (
    <View className="xf-page confirm">
      <View className="xf-nav">
        <View className="xf-nav__back" onClick={() => Taro.navigateBack()}>
          ‹
        </View>
        <Text className="xf-nav__title">确认订单</Text>
      </View>

      <View className="confirm__body">
        {/* 用餐信息 */}
        <View className="xf-block">
          <Text className="xf-block__title">用餐信息</Text>
          <View className="xf-sumrow">
            <Text>门店</Text>
            <b>{store.name}</b>
          </View>
          <View className="xf-sumrow">
            <Text>桌台</Text>
            <b>
              {store.tableName} · {store.areaName}
            </b>
          </View>
          <View className="xf-sumrow">
            <Text>人数</Text>
            <b>{store.people} 人</b>
          </View>
        </View>

        {/* 已选菜品摘要 */}
        <View className="xf-block">
          <Text className="xf-block__title">已选菜品 · {count} 份</Text>
          {items.map((item) => (
            <View key={item.dish.id} className="xf-line">
              <Text className="xf-line__name">
                {item.dish.name} × {item.qty}
              </Text>
              <Text className="xf-line__sum">{yuan(item.qty * item.dish.price)}</Text>
            </View>
          ))}
        </View>

        {/* 支付方式（dev 打桩） */}
        <View className="xf-block">
          <Text className="xf-block__title">支付方式</Text>
          {payMethods.map((m) => (
            <View
              key={m.id}
              className={`confirm__pay ${payMethod === m.id ? 'is-on' : ''}`}
              onClick={() => setPayMethod(m.id)}
            >
              <View className="confirm__pay-text">
                <Text className="confirm__pay-name">{m.name}</Text>
                <Text className="confirm__pay-desc">{m.desc}</Text>
              </View>
              <View className="confirm__radio">
                {payMethod === m.id && <View className="confirm__radio-dot" />}
              </View>
            </View>
          ))}
        </View>

        {/* 金额 */}
        <View className="xf-block">
          <View className="xf-sumrow xf-sumrow--big">
            <Text>应付</Text>
            <Text className="xf-num">{yuan(total)}</Text>
          </View>
        </View>
      </View>

      <View className="xf-bar">
        <View className="xf-bar__total">
          <small>合计</small>
          <b>{yuan(total)}</b>
        </View>
        <View className={`xf-bar__go ${submitting ? 'is-disabled' : ''}`} onClick={submit}>
          {submitting ? '提交中…' : '提交订单'}
        </View>
      </View>
    </View>
  )
}
