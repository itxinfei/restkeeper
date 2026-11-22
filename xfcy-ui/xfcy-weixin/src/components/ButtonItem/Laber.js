import Taro, { Component } from '@tarojs/taro'
import { View, Text } from '@tarojs/components'
import classNames from 'classnames';
import './ButtonItem.scss'

class Laber extends Component {
  constructor () {
    super(...arguments)
  }
  render () {
    const {
      value,
      type
    } = this.props
    return (
      <Text
        onClick={() => (this.props.onClick())}
        className={classNames(
          {
            'lab_main': true,
            'lab_Yellow':type === 'yellow',
            'lab_Red':type === 'red',
            'lab_Green':type === 'green',
            'lab_black':type === 'black',
          }
        )} >{value}</Text>
    )
  }
}

export default Laber

Laber.defaultProps = {
  onClick:() => {},
  type: 'yellow'
}
