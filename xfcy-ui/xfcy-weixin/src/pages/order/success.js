import Taro, { Component } from '@tarojs/taro'
import { View, Image} from '@tarojs/components'
import ButtomItem from './../../components/ButtonItem/ButtonItem'
import success from './../../assets/img/success.png'
import './../menu/index.scss'

class OrderSuccess extends Component {
  constructor () {
    super(...arguments)
  }
  render () {
    return (
      <View className='orderSucc' >
        <Image src={success} className='successLog' />
        <View className='title'>下单成功</View>
        <View className='cont'>掌柜的 正在为您制作，请耐心稍等一下哈~</View>
        <View><ButtomItem onClick={() => Taro.navigateTo({url: '/pages/menu/index'})} value='继续点餐' type='empty' /></View>
      </View>
    )
  }
}
export default OrderSuccess
