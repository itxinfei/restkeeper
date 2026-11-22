import { Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useCart } from '../store/cart'
import { yuan } from '../utils/format'
import './CartBar.scss'

/**
 * 底部购物车条（固定，避让安全区 env(safe-area-inset-bottom)）
 * 依据 DESIGN.md §5.3 底部操作条。
 */
export default function CartBar() {
  const { count, total } = useCart()
  const disabled = count === 0

  const goCart = () => {
    if (disabled) return
    Taro.navigateTo({ url: '/pages/cart/index' })
  }

  return (
    <View className="cart-bar">
      <View className="cart-bar__icon">
        <Text className="cart-bar__emoji">🛒</Text>
        {count > 0 && <Text className="cart-bar__badge">{count}</Text>}
      </View>
      <View className="cart-bar__info">
        <Text className="cart-bar__label">已选 {count} 道</Text>
        <Text className="cart-bar__total">{yuan(total)}</Text>
      </View>
      <View className={`cart-bar__go ${disabled ? 'is-disabled' : ''}`} onClick={goCart}>
        去结算
      </View>
    </View>
  )
}
