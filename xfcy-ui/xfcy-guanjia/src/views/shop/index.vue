<template>
  <div class="dashboard-container">
    <HeadLable :title="'门店管理'" />
    <div class="container">
      <div class="tableBar">
        <el-input
          v-model="input"
          placeholder="请输入内容"
          style="width: 250px"
          size="small"
          clearable
        >
          <i
            slot="prefix"
            class="el-input__icon el-icon-search"
            style="cursor: pointer"
            @click="searchHandle"
          />
        </el-input>
        <el-button
          type="primary"
          size="small"
          @click="addShop('add')"
        >
          添加
        </el-button>
      </div>
      <el-table
        :data="tableData"
        stripe
        class="tableBox"
      >
        <el-table-column
          prop="storeName"
          label="店长名"
          width="120"
        />
        <el-table-column
          prop="brand.brandName"
          label="所属品牌"
          width="110"
        />
        <el-table-column
          prop="brand.category"
          label="所属业态"
          width="110"
        />
        <el-table-column
          prop="address"
          label="详细地址"
        />
        <el-table-column
          prop="contact"
          label="门店负责人"
        />
        <el-table-column
          prop="contactPhone"
          label="门店电话"
        />
        <el-table-column
          prop="status"
          label="门店状态"
        >
          <template slot-scope="scope">
            {{ scope.row.status == 1 ? '正常' : '停业' }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="160"
          align="center"
        >
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="stopHandle(scope.row)"
            >
              {{ scope.row.status == '1' ? '停业' : '开业' }}
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="addShop(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="deleteHandle(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="counts > 10"
        class="pageList"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="page"
        layout="total, sizes, prev, pager, next, jumper"
        :total="counts"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import HeadLable from '@/components/HeadLable';
import {getShopList, seachShopList, stopShop, delShop} from '@/api/brand';

    @Component({
        'name': 'shop',
        'components': {
            HeadLable
        }
    })

export default class extends Vue {
        private input: any = ''
        private counts: number = 0
        private page: number = 1
        private pageSize: number = 10
        private tableData = []
        private changeId = ''
        private status = ''

        created() {
            this.init();
        }

        private async init() {
            await getShopList({'page': this.page, 'pageSize': this.pageSize}).then(res => {
                const {data} = res.data;
                this.tableData = data.items;
                this.counts = Number(data.counts);
            }).catch(err => {
                this.$message.error('请求出错了：' + err);
            });
        }
        //搜索
        private async searchHandle() {
            if (!this.input){
                return;
            }
            this.page = 1;
            this.pageSize = 10;
            await seachShopList({'page': this.page, 'pageSize': this.pageSize, 'name': this.input}).then(res => {
                const {data} = res.data;
                this.tableData = data.items;
                this.counts = Number(data.counts);
            }).catch(err => {
                this.$message.error('请求出错了：' + err);
            });
        }
        // 添加
        private addShop(st:string) {
            if (st == 'add'){
                this.$router.push({ 'path': '/shop/add' });
            } else {
                this.$router.push({ 'path': '/shop/add', 'query': {'id': st.storeId}});
            }
        }

        //删除
        private deleteHandle(row:any) {
            this.changeId = row.storeId;
            this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
                'confirmButtonText': '确定',
                'cancelButtonText': '取消',
                'type': 'warning'
            }).then(() => {
                delShop({'id': this.changeId}).then(res => {
                    this.$message.success('删除成功！');
                    this.init();
                }).catch(err => {
                    this.$message.error('请求出错了：' + err);
                });
            });
        }
        //停业
        private stopHandle(row:object) {
            this.changeId = row.storeId;
            this.status = row.status == 1 ? 0 :1;
            console.log(this.status);
            this.$confirm('确认更改该门店营业状态?', '提示', {
                'confirmButtonText': '确定',
                'cancelButtonText': '取消',
                'type': 'warning'
            }).then(() => {
                stopShop({'id': this.changeId, 'status': this.status}).then(res => {
                    this.$message.success('该门店已状态更新成功！');
                    this.init();
                }).catch(err => {
                    this.$message.error('请求出错了：' + err);
                });
            });
        }

        private handleSizeChange(val: any) {
            this.pageSize = val;
            this.init();
        }

        private handleCurrentChange(val: any) {
            this.page = val;
            this.init();
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
