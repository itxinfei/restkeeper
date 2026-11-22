<template>
  <div class="dashboard-container">
    <HeadLable :title="'员工管理'" />
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
            @click="init"
          />
        </el-input>
        <el-button
          type="primary"
          size="small"
          @click="addMemberHandle('add')"
        >
          +添加员工
        </el-button>
      </div>
      <el-table
        :data="tableData"
        stripe
        class="tableBox"
      >
        <el-table-column
          prop="staffName"
          label="员工姓名"
        />
        <el-table-column
          prop="phone"
          label="账号"
        />
        <el-table-column
          prop="phone"
          label="手机号"
        />
        <el-table-column label="账号状态">
          <template slot-scope="scope">
            {{ scope.row.status == '0' ? '已禁用' : '正常' }}
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
              @click="addMemberHandle(scope.row.staffId)"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="statusHandle(scope.row)"
            >
              {{ scope.row.status == '1' ? '禁用' : '启用' }}
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
import { Component, Vue } from 'vue-property-decorator';
import HeadLable from '@/components/HeadLable';
import {getMemberList, memberStatus} from '@/api/member';

@Component({
    'name': 'member',
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
    private id = ''
    private status = ''

    created() {
        this.init();
    }

    private async init() {
        await getMemberList({'page': this.page, 'pageSize': this.pageSize, 'name':this.input || undefined}).then(res => {
            const {data} = res.data;
            this.tableData = data.items;
            this.counts = Number(data.counts);
        }).catch(err => {
            this.$message.error('请求出错了：' + err);
        });
    }
    // 添加
    private addMemberHandle(st:string) {
        if (st == 'add'){
            this.$router.push({ 'path': '/member/add' });
        } else {
            this.$router.push({ 'path': '/member/add', 'query': {'id': st}});
        }
    }

    //状态修改
    private statusHandle(row:object) {
        console.log(row);
        this.id = row.staffId;
        this.status = row.status;
        this.$confirm('确认调整该账号的状态?', '提示', {
            'confirmButtonText': '确定',
            'cancelButtonText': '取消',
            'type': 'warning'
        }).then(() => {
            memberStatus({'id': this.id, 'status': this.status == 0 ? 1 : 0}).then(res => {
                this.$message.success('账号状态更改成功！');
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
