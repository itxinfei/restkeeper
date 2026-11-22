<template>
  <div class="dashboard-container">
    <HeadLable :title="'菜品管理'"/>
    <div class="container">
      <div class="tableBar">
        <el-input placeholder="请输入内容" style="width: 250px" size="small" v-model="input" clearable>
          <i slot="prefix" class="el-input__icon el-icon-search" style="cursor: pointer" @click="init"></i>
        </el-input>
        <div class="tableLab">
          <span @click="deleteHandle()">批量删除</span>
          <span @click="statusHandle('1')">批量启售</span>
          <span @click="statusHandle('0')" style="border:none;">批量停售</span>
          <el-button  type="primary" @click="addFoodtype('add')" size="small"> +新建菜品 </el-button>
        </div>
      </div>
      <el-table :data="tableData" stripe class="tableBox" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="25"> </el-table-column>
        <el-table-column prop="name" label="菜品名称"> </el-table-column>
        <el-table-column prop="categoryName" label="菜品分类" > </el-table-column>
        <el-table-column  label="售价 商品码 售卖状态">
          <template slot-scope="scope">
            <span style="margin-right: 10px;">{{scope.row.price/100}}</span>
            <span style="margin-right: 10px;">{{scope.row.code}}</span>
            <span style="margin-right: 10px;">{{scope.row.status == '0' ? '停售' : '启售'}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdateTime" label="最后操作时间"> </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="addFoodtype(scope.row.id)">修改</el-button>
            <el-button type="text" size="small" @click="statusHandle(scope.row)">{{scope.row.status == '0'  ? '启售' : '停售'}}</el-button>
            <el-button type="text" size="small" @click="deleteHandle(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
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
import {getDishList, editDishState, delDish} from '@/api/dish'

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
    private checkList:[] = []
    private tableData:[] = []

    created() {
        this.init()
    }

    private async init() {
        await getDishList({page: this.page, pageSize: this.pageSize, name: this.input || undefined}).then(res => {
            const {data} = res.data
            this.tableData = data.items
            this.counts = Number(data.counts)
        }).catch(err => {
            this.$message.error('请求出错了：' + err)
        })
    }

    // 添加
    private addFoodtype(st:string) {
        if (st == 'add'){
            this.$router.push({ path: '/fond/add' })
        }else{
            this.$router.push({ path: '/fond/add', query: {id: st}})
        }
    }

    // 删除
    private deleteHandle(id:any) {
        id ? this.checkList = [id] : null
        this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            delDish({ids:this.checkList}).then(res => {
                this.$message.success('删除成功！')
                this.init()
            }).catch(err => {
                this.$message.error('请求出错了：' + err)
            })
        })
    }

    //状态更改
    private statusHandle(row:any) {
        let params:any = {}
        if (typeof row == 'string' ){
            if (this.checkList.length == 0){
                this.$message.error('批量操作，请先勾选操作菜品！')
                return false
            }
            params.ids = this.checkList
            params.status = row
        } else {
            params.ids = [row.id]
            params.status = row.status ? '0' : '1'
        }

        this.$confirm('确认更改该菜品状态?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            editDishState(params).then(res => {
                this.$message.success('菜品状态已经更改成功！')
                this.init()
            }).catch(err => {
                this.$message.error('请求出错了：' + err)
            })
        })
    }

    // 全部操作
    private handleSelectionChange(val:any){
        let checkArr:[] = []
        val.forEach((n:any) => {
            checkArr.push(n.id)
        })
        this.checkList = checkArr
        console.log(this.checkList)
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
          .tableLab{
            span{
              cursor: pointer;
              display: inline-block;
              font-size: 14px;
              padding: 0 10px;
              color: $gray-2;
              border-right: solid 1px $gray-4;
            }
          }
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
