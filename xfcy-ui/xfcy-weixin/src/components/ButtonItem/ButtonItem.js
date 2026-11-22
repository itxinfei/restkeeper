import Taro, { Component } from '@tarojs/taro'
import { View, Text } from '@tarojs/components'
import classNames from 'classnames';
import './ButtonItem.scss'

class ButtonItem extends Component {
  constructor () {
    super(...arguments)
  }
  render () {
    const {
      value,
      type,
      style,
      className
    } = this.props
    return (
      <Text
        onClick={() => (this.props.onClick())}
        style={style}
        className={`${classNames(
          {
            'but_main': true,
            'but_empty':type === 'empty',
            'but_solid':type === 'solid',
            'but_smSolid':type === 'smSolid',
            'but_big':type === 'big',
            'but_med':type === 'med',
          }
        ) } ${className}`
        }
      >{value}</Text>
    )
  }
}

export default ButtonItem

ButtonItem.defaultProps = {
  onClick:() => {}
}
