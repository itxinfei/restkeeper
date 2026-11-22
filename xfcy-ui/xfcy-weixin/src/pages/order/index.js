import Taro, { Component } from '@tarojs/taro'
import { connect } from '@tarojs/redux'
import * as actions from '../../actions/menu'
import { View, Text, ScrollView, Image } from '@tarojs/components'
import modeImg from './../../assets/img/imgDemo.png'
import logoImg from './../../assets/img/logo.png'
import './../menu/index.scss'

@connect(state => state, { ...actions })
class ShopCards extends Component {
  config = {
    navigationBarTitleText: '提交订单'
  }

  constructor () {
    super(...arguments)
    this.state = { menuNum: '' }
    this.changNum = this.changNum.bind(this)
  }
  componentDidMount(){
    const { tableId } = this.props.app.tableInfo
    this.props.dispatchMenuCart({url: `order/shopCart/${tableId}`})
  }
  changNum(act){
    let {menuNum} = this.state
    if (menuNum > 0 && act !== 'add') {
      menuNum--
    }
    act === 'add' ? menuNum++ : null
    this.setState({ menuNum })
    console.log(menuNum)
  }

  code () {
    Taro.scanCode()
  }

  get totalPrice() {
    let totalPrice = 0
    if (!this.props.menu.cartList) {
      return totalPrice
    }
    const dishList = this.props.menu.cartList.dishList
    dishList.map(dish => {
      totalPrice += (dish.price * dish.number)
    })
    return (totalPrice / 100).toFixed(2)
  }

  /**
   * 微信支付
   * 参考文档：https://nervjs.github.io/taro/docs/apis/open-api/login/login.html
   * TODO: 1.各种异常系考虑 2.await async写法
   */
  taroLogin () {
    const { tableId } = this.props.app.tableInfo

    // 调用接口获取登录凭证（code）
    Taro.login({
      success: (loginRes) => {
        console.log(loginRes.code)

        if (!loginRes.code) {
          Taro.showToast({
            title: '登录失败',
            icon: 'none',
            duration: 2000
          })
          console.log('登录失败！' + loginRes.errMsg)

          return
        }

        // 支付下单
        this.props.dispatchOrderPay({url: `order/pay/${tableId}/${loginRes.code}`}).then(prepayRes => {
          // TODO: 异常系处理
          // {success: false, message: "该桌有人正在支付", code: 400}
          console.log(prepayRes)
          if (!prepayRes) {
            Taro.showToast({
              title: '该桌有人正在支付',
              icon: 'none',
              duration: 2000
            })
            return
          }
          let temp = JSON.parse(prepayRes)

          // 发起微信支付
          Taro.requestPayment({
            timeStamp: temp.timeStamp,
            nonceStr: temp.nonceStr,
            package: temp.package,
            signType: 'MD5',
            paySign: temp.paySign,
            success: (res) => {
              console.log('支付成功')
              console.log(res)

              // TODO: 异常系处理
              Taro.navigateTo({url: '/pages/order/success'})
            },
            fail: function (res) {
              console.log('支付失败')
              console.log(res)
            }
          })
        })
      },
      fail: (loginRes) => {
        console.log('登录失败！' + loginRes.errMsg)
      }
    })
  }

  render () {
    // const { menuNum } = this.state
    const { menu } = this.props
    return (
      // TODO: 购物车列表被遮挡，添加ScrollView
      <View className='shopCards' >
        <View className='topInfo'>
          <Image className='logo' src={logoImg} />
          <View className='info'>
            <View>{menu.tableStatus.tableName}</View>
            <View className='tabPostion'>大厅</View>
          </View>
        </View>
        <View className='addMenu'>
          <Text className='addBut' onClick={() => Taro.navigateTo({url: '/pages/menu/index'})} >继续加菜</Text>
          {/* <Text>清空</Text> */}
        </View>
        <View className='menuItemsBox'>
          {menu.cartList && menu.cartList.dishList && menu.cartList.dishList.length > 0
            ? menu.cartList.dishList.map((item, index) =>(
                <View className='menuItems' key={index}>
                  <Image className='itemImg' src={modeImg} />
                  <View className='cont'>
                    <View>{item.name}</View>
                    <View className='num'>x{item.number}</View>
                  </View>
                  <View className='addNum'>
                    <Text className='price'>￥{(item.price / 100).toFixed(2)}</Text>
                  </View>
                </View>
              ))
             : null
          }
          <View className='submitBut'><View className='price' >￥ {this.totalPrice}</View><View className='submit' onClick={this.taroLogin.bind(this)} >提交订单</View></View>
        </View>
      </View>
    )
  }
}
export default ShopCards
