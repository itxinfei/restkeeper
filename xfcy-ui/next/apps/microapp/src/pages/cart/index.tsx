import { Input, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getRemark, setQty, setRemark, useCart } from '../../store/cart'
import { yuan } from '../../utils/format'
import './index.scss'

/**
 * 购物车（原型 microapp-cart.html 的明细 / 步进 / 备注 / 金额明细部分）
 * 底部「去结算」进入下单确认页。
 */
export default function Cart() {
  const { items, count, total } = useCart()
  const remark = getRemark()
  const packingFee = 0

  const goConfirm = () => {
    if (count === 0) return
    Taro.navigateTo({ url: '/pages/confirm/index' })
  }

  return (
    <View className="xf-page cart">
      <View className="xf-nav">
        <View className="xf-nav__back" onClick={() => Taro.navigateBack()}>
          ‹
        </View>
        <Text className="xf-nav__title">购物车</Text>
      </View>

      {items.length === 0 ? (
        <View className="xf-empty">
          <View className="xf-empty__icon">🛒</View>
          <Text className="xf-empty__title">购物车还是空的</Text>
          <Text className="xf-empty__desc">去菜单挑几道招牌菜吧</Text>
          <View className="cart__back" onClick={() => Taro.navigateTo({ url: '/pages/menu/index' })}>
            去点餐
          </View>
        </View>
      ) : (
        <View className="cart__body">
          {/* 已选菜品 */}
          <View className="xf-block">
            <Text className="xf-block__title">已选菜品 · {count} 份</Text>
            {items.map((item) => (
              <View key={item.dish.id} className="xf-line">
                <Text className="xf-line__name">{item.dish.name}</Text>
                <View className="xf-step">
                  <View className="xf-step__btn" onClick={() => setQty(item.dish, item.qty - 1)}>
                    −
                  </View>
                  <Text className="xf-step__num">{item.qty}</Text>
                  <View
                    className="xf-step__btn xf-step__btn--primary"
                    onClick={() => setQty(item.dish, item.qty + 1)}
                  >
                    ＋
                  </View>
                </View>
                <Text className="xf-line__sum">{yuan(item.qty * item.dish.price)}</Text>
              </View>
            ))}
          </View>

          {/* 备注 */}
          <View className="xf-block">
            <Text className="xf-block__title">备注</Text>
            <Input
              className="cart__remark"
              placeholder="口味 / 忌口，可留空"
              value={remark}
              onInput={(e: { detail: { value: string } }) => setRemark(e.detail.value)}
            />
          </View>

          {/* 金额明细 */}
          <View className="xf-block">
            <View className="xf-sumrow">
              <Text>菜品合计</Text>
              <Text className="xf-num">{yuan(total)}</Text>
            </View>
            <View className="xf-sumrow">
              <Text>打包费</Text>
              <Text className="xf-num">{yuan(packingFee)}</Text>
            </View>
            <View className="xf-sumrow xf-sumrow--big">
              <Text>应付</Text>
              <Text className="xf-num">{yuan(total + packingFee)}</Text>
            </View>
          </View>
        </View>
      )}

      {/* 底部结算条 */}
      {items.length > 0 && (
        <View className="xf-bar">
          <View className="xf-bar__total">
            <small>合计</small>
            <b>{yuan(total)}</b>
          </View>
          <View className="xf-bar__go" onClick={goConfirm}>
            去结算
          </View>
        </View>
      )}
    </View>
  )
}
