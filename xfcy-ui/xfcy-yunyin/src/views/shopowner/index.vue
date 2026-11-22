<template>
  <div class="dashboard-container">
    <HeadLable :title="'店长管理'"/>
    <div class="container">
      <div class="tableBar">
        <el-input placeholder="请输入内容" style="width: 250px" size="small" @:keyup.enter="searchHandle" v-model="input" clearable>
          <i slot="prefix" class="el-input__icon el-icon-search"style="cursor: pointer"  @click="searchHandle('add')" ></i>
        </el-input>
        <el-button type="primary" size="small" @click="addShopowner">添加</el-button>
      </div>
      <el-table :data="tableData" stripe class="tableBox">
        <el-table-column prop="storeManagerName" label="店长姓名" width="160"></el-table-column>
        <el-table-column prop="storeManagerPhone" label="联系方式" width="160"></el-table-column>
        <el-table-column label="所属店面">
          <template slot-scope="scope">
            <span v-for="(item,index) in scope.row.stores" :key="index" >{{item.storeName}}、</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="账号状态" width="160">
          <template slot-scope="scope">
            <span>{{scope.status == 0 ? '停业' : '正常'}}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="addShopowner(scope.row)">编辑</el-button>
            <el-button type="text" size="small" @click="stopHandle">暂停</el-button>
            <el-button type="text" size="small" @click="deleteHandle">删除</el-button>
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
    import {Component, Vue} from 'vue-property-decorator'
    import HeadLable from '@/components/HeadLable'
    import {getShopOwner, stopShopOwner, delShopOwner} from '@/api/brand'

    @Component({
        name: 'shopowner',
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
            await getShopOwner({page: this.page, pageSize: this.pageSize, criteria: this.input || undefined}).then(res => {
                const {data} = res.data
                console.log(data)
                this.tableData = data.items
                this.counts = Number(data.counts)
            }).catch(err => {
                this.$message.error('请求出错了：' + err)
            })
        }
        //搜索
        private searchHandle(val) {
          if (!this.input){
              return
          }
          this.init()
        }
        // 添加
        private addShopowner(st:string) {
            if (st == 'add'){
                this.$router.push({ path: '/shopowner/addShopowner' })
            }else{
                console.log(st)
                this.$router.push({ path: '/shopowner/addShopowner', query: {id: st.storeManagerId}})
            }
        }

        //删除
        private deleteHandle() {
            this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                delShopOwner({page: this.page, pageSize: this.pageSize}).then(res => {
                    this.$message.success('删除成功！')
                    this.init()
                }).catch(err => {
                    this.$message.error('请求出错了：' + err)
                })
            })
        }
        //停业
        private stopHandle() {
            this.$confirm('确认该门店停业?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                stopShopOwner({page: this.page, pageSize: this.pageSize}).then(res => {
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

<style lang="scss" scoped>
  .dashboard {
    &-container {
      margin: 30px;

      .container {
        background: #fff;
        position: relative;
        z-index: 1;
        padding: 30px 28px;
        border-radius: 4px;
        min-height: 500px;

        .tableBar {
          display: flex;
          margin-bottom: 20px;
          justify-content: space-between;
        }

        .tableBox {
          width: 100%;
          border: solid 2px $gray-5;
          border-radius: 2px;
        }

        .pageList {
          text-align: center;
          margin-top: 30px;
        }
      }
    }
  }
</style>
