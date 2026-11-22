<template>
  <div class="dashboard-container">
    <HeadLable :title="'账号管理'"/>
    <div class="container">
      <div class="tableBar">
        <el-input placeholder="请输入企业关键词" style="width: 250px" size="small" v-model="input" clearable>
          <i slot="prefix" class="el-input__icon el-icon-search" style="cursor: pointer" @click="init"></i>
        </el-input>
        <el-button  type="primary" @click="addMemberHandle('add')" size="small"> +新增账号 </el-button>
      </div>
      <el-table :data="tableData" stripe class="tableBox">
        <el-table-column prop="enterpriseId" label="ID"  > </el-table-column>
        <el-table-column prop="enterpriseName" label="企业名称" > </el-table-column>
        <el-table-column prop="shopId" label="商户号" > </el-table-column>
        <el-table-column prop="phone" label="账号" width="113"> </el-table-column>
        <el-table-column prop="applicant" label="申请人" > </el-table-column>
        <el-table-column prop="phone" label="手机号" width="113"> </el-table-column>
        <el-table-column prop="applicationTime" label="申请日期" >
          <template slot-scope="scope">
            {{moment(scope.row.applicationTime)}}
          </template>
        </el-table-column>
        <el-table-column prop="expireTime" label="到期日期" >
          <template slot-scope="scope">
            {{moment(scope.row.applicationTime)}}
          </template>
        </el-table-column>
        <el-table-column label="账号状态">
          <template slot-scope="scope">
            <span v-if="scope.row.status == 0">试用中</span>
            <span v-if="scope.row.status == -1">已停用</span>
            <span v-if="scope.row.status == 1">正式</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="addMemberHandle(scope.row.enterpriseId)">编辑</el-button>
            <el-button type="text" size="small" @click="statusHandle(scope.row)">{{scope.row.status == '-1' ? '启用' : '禁用'}}</el-button>
            <el-button type="text" size="small" @click="rePassword(scope.row.enterpriseId)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="counts > 10"
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
import {getMemberList, rePassword, memberOpen, memberClose} from '@/api/member'
import moment from 'moment'


@Component({
  name: 'member',
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
    private changeRow = []
    private rmId = ''

    created() {
        this.init()
    }

    private async init() {
        await getMemberList({page: this.page, pageSize: this.pageSize, name:this.input || undefined}).then(res => {
            const {data} = res.data
            this.tableData = data.items
            this.counts = Number(data.counts)
        }).catch(err => {
            this.$message.error('请求出错了：' + err)
        })
    }

    private moment(dat: any){
        return moment(dat).format('YYYY-MM-DD HH:mm:ss')
    }
    // 添加
    private addMemberHandle(st:string) {
        if (st == 'add'){
            this.$router.push({ path: '/member/add' })
        }else{
            this.$router.push({ path: '/member/add', query: {id: st}})
        }
    }
    // 重置密码
    private rePassword(id:string) {
        rePassword({id}).then(res => {
            this.$message.success('密码重置短信已发送，请注意查收')
        }).catch(err => {
            this.$message.error('请求出错了：' + err)
        })
    }

    //状态修改
    private statusHandle(row:object) {
        this.changeRow = row
        this.$confirm('确认调整该账号的状态?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            if (this.changeRow.status == -1){
                memberOpen({id: this.changeRow.enterpriseId}).then(res => {
                    this.$message.success('账号已起用成功！')
                    this.init()
                }).catch(err => {
                    this.$message.error('请求出错了：' + err)
                })
            } else {
                memberClose({id: this.changeRow.enterpriseId}).then(res => {
                    this.$message.success('账号已禁用成功！')
                    this.init()
                }).catch(err => {
                    this.$message.error('请求出错了：' + err)
                })
            }
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
