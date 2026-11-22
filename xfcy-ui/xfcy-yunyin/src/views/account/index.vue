<template>
  <div class="dashboard-container">
    <HeadLable :title="'挂账管理'"/>
    <div class="container">
      <div class="tableBar">
        <el-input placeholder="请输入内容" style="width: 250px" size="small" v-model="input" clearable>
          <i slot="prefix" class="el-input__icon el-icon-search" style="cursor: pointer" @click="init"></i>
        </el-input>
        <el-button  type="primary" @click="addAccount('add')" size="small"> +添加挂账 </el-button>
      </div>
      <el-table :data="tableData" stripe class="tableBox">
        <el-table-column prop="creditType" label="挂账类型"  width="110">
          <template slot-scope="scope">
            {{scope.row.creditType == '1' ? '公司' : '个人'}}
          </template>
        </el-table-column>
        <el-table-column prop="companyName" label="挂账公司" align="center">
          <template slot-scope="scope">
            {{scope.row.companyName || '--'}}
          </template>
        </el-table-column>
          <el-table-column label="挂账人" >
            <template slot-scope="scope">
              <div v-if="scope.row.creditType == '1'">
                <div v-for="(it, index) in scope.row.users">{{scope.row.userName || '--'}}</div>
              </div>
              <div v-if="scope.row.creditType == '2'">{{scope.row.userName || '--'}}</div>
            </template>
          </el-table-column>
          <el-table-column label="挂账电话">
            <template slot-scope="scope">
              <div v-if="scope.row.creditType == '1'">
                <div v-for="(it, index) in scope.row.users">{{it.phone || '--'}}</div>
              </div>
              <div v-if="scope.row.creditType == '2'">{{scope.row.phone || '--'}}</div>
            </template>
          </el-table-column>
        <el-table-column prop="creditAmount" label="欠款金额">
          <template slot-scope="scope">
            {{scope.row.creditAmount/100}}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
            <template slot-scope="scope">
              {{scope.row.status == '0' ? '停用' : '有效'}}
            </template>
        </el-table-column>
        <el-table-column prop="address" label="操作" width="180" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="addAccount(scope.row.id)">修改</el-button>
            <el-button type="text" size="small" @click="stopHandle(scope.row)">{{scope.row.status == '1' ? '停用' : '启用'}}</el-button>
            <el-button type="text" size="small" @click="() => $router.push({path: '/accounts/repayment', query: {id: scope.row.id}})"> 还款 </el-button>
            <el-button type="text" size="small" @click="() => $router.push({path: '/accounts/order', query: {id: scope.row.id}})">订单明细</el-button>
          </template>
        </el-table-column>
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
import { getAccountList, accountStatus, delAccount } from '@/api/account'

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
        await getAccountList({page: this.page, pageSize: this.pageSize,name:this.input || undefined}).then(res => {
            const {data} = res.data
            this.tableData = data.items
            this.counts = Number(data.counts)
        }).catch(err => {
            this.$message.error('请求出错了：' + err)
        })
    }
    // 添加
    private addAccount(st:string) {
        if (st == 'add'){
            this.$router.push({ path: '/accounts/add' })
        }else{
            this.$router.push({ path: '/accounts/add', query: {id: st}})
        }
    }

    //删除
    private deleteHandle(id:any) {
        this.$confirm('此操作将永久删除该挂账, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            delAccount({id}).then(res => {
                this.$message.success('删除成功！')
                this.init()
            }).catch(err => {
                this.$message.error('请求出错了：' + err)
            })
        })
    }
    //停业
    private stopHandle(row:object) {
        this.$confirm('确认修改该挂账人账号?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            accountStatus({id: row.id, status: row.status == 0  ? 1 : 0}).then(res => {
                this.$message.success('该门店已停业！')
                this.init()
            }).catch(err => {
                this.$message.error('请求出错了：' + err)
            })
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
