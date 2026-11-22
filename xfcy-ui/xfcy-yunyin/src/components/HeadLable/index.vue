<template>
    <div class="HeadLable">
      <span class="goBack" v-if="goback" @click="goBack()"><img src="@/assets/icons/btn_back@2x.png" alt=""> 返回</span>
      <span>{{title}}</span>
    </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'

@Component({
  name: 'Hamburger'
})
export default class extends Vue {
  @Prop({ default: false }) private goback!: boolean
  @Prop({ default: '集团管理' }) private title!: string

  private toggleClick() {
    this.$emit('toggleClick')
  }

  private goBack() {
    this.$router.go(-1)
  }
}
</script>

<style lang="scss" scoped>
  .HeadLable{
    position: absolute;
    background: #fff;
    height: 50px;
    width: 300px;
    padding-left: 20px;
    line-height: 50px;
    top:0px;
    left: 0px;
    opacity: 0;
    animation: opacity 500ms ease-out 800ms forwards;
    .goBack{
      border-right: solid 1px #ccc;
      padding-right: 14px;
      margin-right: 10px;
      color: #666;
      cursor: pointer;
      img{
        position: relative;
        top:16px;
        margin-right: 5px;
        width: 18px;
        height: 18px;
        float: left;
      }
    }
  }
  @keyframes opacity {
     0% {
       opacity: 0;
       left: 80px;
     }
     100% {
       opacity: 1;
       left: 0;
     }
   }
</style>
