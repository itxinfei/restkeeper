<template>
  <div class="dashboard-container">
    <HeadLable :title="'订单详情'"/>
    <div class="container">
      <div class="tableBar">
        <div></div>
        <el-button  type="primary" size="small"> 下载报表 </el-button>
      </div>
      <el-table :data="tableData" stripe class="tableBox">
        <el-table-column prop="orderId" label="订单号" align="center" width="175"></el-table-column>
        <el-table-column prop="userName" label="挂账人" align="center"></el-table-column>
        <el-table-column prop="creditType" label="挂账类型" >
          <template slot-scope="scope">
            {{scope.row.creditType == '1' ? '公司' : '个人'}}
          </template>
        </el-table-column>
        <el-table-column label="挂账公司" >
          <template slot-scope="scope">{{scope.row.companyName || '--'}}</template>
        </el-table-column>
        <el-table-column prop="creditAmount" label="订单金额" width="110">
          <template slot-scope="scope">
            {{scope.row.orderAmount/100}}
          </template>
        </el-table-column>
        <el-table-column prop="receivableAmount" label="应收金额" width="110">
          <template slot-scope="scope">
            {{scope.row.creditAmount/100}}
          </template>
        </el-table-column>
        <el-table-column prop="creditAmount" label="挂账金额" width="110">
          <template slot-scope="scope">
            {{scope.row.creditAmount/100}}
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdateTime" label="挂账时间" width="165"></el-table-column>
      </el-table>
      <el-pagination
        v-if="counts >= 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pageList"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="page"
        layout="total, sizes, prev, pager, next, jumper"
        :total="counts">
      </el-pagination>
    </div>
  </div>
</template>

<script lang="ts">
    import { Component, Vue } from 'vue-property-decorator'
    import HeadLable from '@/components/HeadLable'
    import { getOrderList, } from '@/api/account'

    @Component({
        name: 'foodType',
        components: {
            HeadLable
        }
    })

    export default class extends Vue {
        private input: any = ''
        private counts: number = 0
        private page: number = 1
        private pageSize: number = 10
        private tableData = []

        created() {
            this.init()
        }

        private async init() {
            await getOrderList({page: this.page, pageSize: this.pageSize,id:this.$route.query.id}).then(res => {
                const {data} = res.data
                this.tableData = data.items
                this.counts = Number(data.counts)
            }).catch(err => {
                this.$message.error('请求出错了：' + err)
            })
        }

        private handleSizeChange(val: any) {
            this.pageSize = val
            this.init()
        }

        private handleCurrentChange(val: any) {
            this.page = val
            this.init()
        }

    }
</script>
<style lang="scss">
  .el-table-column--selection .cell{
    padding-left: 10px;
  }
</style>
<style lang="scss" scoped>
  .dashboard {
    &-container {
      margin: 30px;
      .container{
        background: #fff;
        position: relative;
        z-index: 1;
        padding: 30px 28px;
        border-radius: 4px;
        min-height: 500px;
        .tableBar{
          display: flex;
          margin-bottom: 20px;
          justify-content: space-between;
        }
        .tableBox{
          width: 100%;
          border: solid 2px $gray-5;
          border-radius: 2px;
        }
        .pageList{
          text-align: center;
          margin-top: 30px;
        }
      }
    }
  }
</style>
