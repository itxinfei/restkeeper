import { Text, View } from '@tarojs/components'
import { Dish } from '../types'
import { setQty, useCart } from '../store/cart'
import { yuan } from '../utils/format'
import './DishCard.scss'

interface Props {
  dish: Dish
}

/**
 * 菜品大图卡（DESIGN.md §5.3：大图 + 名称 + 价格；价格用主色 700）
 * 图片位为「示意」渐变 + emoji（§10），真实环境替换为 Image。
 */
export default function DishCard({ dish }: Props) {
  const { items } = useCart()
  const qty = items.find((i) => i.dish.id === dish.id)?.qty ?? 0
  const soldOut = !!dish.soldOut

  return (
    <View className={`dish-card ${soldOut ? 'is-soldout' : ''}`}>
      <View className={`dish-card__pic dish-card__pic--${dish.gradient}`}>
        <Text className="dish-card__emoji">{dish.emoji}</Text>
        <Text className="dish-card__tag">示意</Text>
        {dish.signature && <Text className="dish-card__sig">招牌</Text>}
      </View>
      <View className="dish-card__info">
        <Text className="dish-card__name">{dish.name}</Text>
        <Text className="dish-card__desc">{dish.desc}</Text>
        <View className="dish-card__buy">
          <Text className="dish-card__price">
            {yuan(dish.price)}
            <Text className="dish-card__unit">/{dish.unit}</Text>
          </Text>
          {soldOut ? (
            <View className="dish-card__add is-disabled">沽清</View>
          ) : qty === 0 ? (
            <View className="dish-card__add" onClick={() => setQty(dish, 1)}>
              +
            </View>
          ) : (
            <View className="dish-card__step">
              <View className="dish-card__stepbtn" onClick={() => setQty(dish, qty - 1)}>
                −
              </View>
              <Text className="dish-card__num">{qty}</Text>
              <View className="dish-card__stepbtn" onClick={() => setQty(dish, qty + 1)}>
                ＋
              </View>
            </View>
          )}
        </View>
      </View>
    </View>
  )
}
