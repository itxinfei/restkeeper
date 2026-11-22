<template>
  <div class="dashboard-container">
    <HeadLable :title="'品牌管理'" />
    <div class="container">
      <div class="items" v-for="item in brandList.items">
        <img src="../../assets/img_brand01@2x.png" width="100%" alt="">
        <div class="tit">{{item.brandName}}</div>
        <div class="des">{{item.info}}</div>
      </div>
      <div class="items items-add">
        <div class="add" @click="addBrand"> + </div>
        <div class="tit">添加品牌</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import HeadLable from '@/components/HeadLable'
import { getBrandList } from '@/api/brand'
import { IbrandListData } from '@/api/types'

@Component({
  name: 'brand',
  components: {
    HeadLable
  }
})

export default class extends Vue {
  private brandList: IbrandListData[] = []
  private counts: number = 0
  private page: number = 1
  private pageSize: number = 10
  private async getItemDataes() {
    const { data } = await getBrandList({ page: this.page, pageSize: this.pageSize })
    this.brandList = data.data
    this.counts = data.data.counts
  }

  created() {
    this.getItemDataes()
  }

  private addBrand() {
    this.$router.push({ path: '/brand/add' })
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  &-container {
    margin: 30px;
    .container{
      display: flex;
      flex-wrap: wrap;
      position: relative;
      z-index: 1;
      .items{
        text-align: center;
        padding: 15px;
        width: 18%;
        min-height: 270px;
        margin: 1%;
        border-radius: 3px;
        box-shadow: 2px 2px 12px rgba(0,0,0,0.1);
        background: #fff;
        line-height: 2;
        img{
          box-shadow: 1px 1px 3px rgba(0,0,0,0.06);
        }
        &-add:hover{
          cursor:pointer;
          .add{
            background: $mine;
          }
          .tit{
            color: $mine;
          }
        }
        .add{
          background: $gray-2;
          display: inline-block;
          width: 66px;
          height: 66px;
          border-radius: 100%;
          font-size: 52px;
          color:#fff;
          line-height: 55px;
          margin-top: 60px;
        }
        .tit{
          font-size: 16px;
          line-height: 50px;
          color: $gray-1;
        }
        .des{
          font-size: 13px;
          line-height: 20px;
          color: $gray-2;
        }
      }
    }
  }
}
</style>
