<template>
  <div class="dashboard-container">
    <HeadLable :title="'桌台管理'"/>
    <div class="container">
      <div class="tableBar">
        <div>地区：
          <el-select v-model="seachId" size="mini" @change="init" placeholder="请选择">
              <el-option v-for="item in areaData" :key="item.value" :label="item.areaName" :value="item.areaId"> </el-option>
          </el-select>
        </div>
        <el-button  type="primary" @click="() => tableDataes.dialogVisible = true" size="small"> +新建台桌 </el-button>
      </div>
      <el-table :data="tableData" stripe class="tableBox">
        <el-table-column prop="tableName" label="台桌名称" > </el-table-column>
        <el-table-column prop="seatNumber" label="座位数"> </el-table-column>
        <el-table-column prop="area.areaName" label="所属区域" > </el-table-column>
        <el-table-column prop="address" label="操作" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="editHandle(scope.row)">修改</el-button>
            <el-button type="text" size="small" @click="deleteHandle(scope.row.tableId)">删除</el-button>
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
    <el-dialog
      :title="tableDataes.title"
      :visible.sync="tableDataes.dialogVisible"
      width="30%"
      :before-close="handleClose">
      <el-form  :inline="true" :model="tableDataes" ref="tableDataes" :rules="rules" class="demo-form-inline" label-width="110px">
        <el-form-item label="所属区域：" prop="areaId">
          <el-select v-model="tableDataes.areaId" size="mini" @change="init" placeholder="请选择">
            <el-option v-for="item in areaData" :key="item.value" :label="item.areaName" :value="item.areaId"> </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="桌台名称：" prop="name">
          <el-input v-model="tableDataes.name" size="small" placeholder="不超过20个汉字"></el-input>
        </el-form-item>
        <el-form-item label="座位数："  prop="seatNumber">
          <el-input v-model="tableDataes.seatNumber" size="small" placeholder="请输入1-99"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="tableDataes.dialogVisible = false">取 消</el-button>
        <el-button type="primary" size="small" @click="submitForm('tableDataes')">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import HeadLable from '@/components/HeadLable'
import {getTableList, getAreaList, addTable, editTable, delTable} from '@/api/table'


@Component({
  name: 'foodType',
  components: {
    HeadLable
  }
})
export default class extends Vue {
    private seachId: any = ''
    private areaData: [] = []
    private counts: number = 0
    private page: number = 1
    private pageSize: number = 10
    private tableData = []
    private action = ''
    private tableDataes: any = {
        title: '添加桌台',
        dialogVisible: false,
        name: '',
        areaId: '',
        seatNumber: ''
    }

    get rules() {
        return {
            name: [
                {required: true, message: '请输入桌台名称', trigger: 'blur'},
            ],
            seatNumber: [
                {required: true, message: '请输入座位数', trigger: 'blur'},
            ],
            areaId: [
                {required: true, message: '请选择品牌区域', trigger: 'blur'},
            ],
        }
    }

    created() {
        this.init()
        this.getAreaData()
    }

    private async init() {
        await getTableList({areaId: this.seachId || 0, page: this.page, pageSize: this.pageSize}).then(res => {
            const { data } = res.data
            this.tableData = data.items
            this.counts = Number(data.counts)
        }).catch(err => {
            this.$message.error('请求桌台列表出错了：' + err)
        })
    }
    // 获取区域列表
    private getAreaData(){
        getAreaList({page: this.page, pageSize: this.pageSize}).then(res => {
            const { data } = res.data
            this.areaData = data.items
        }).catch(err => {
            this.$message.error('获取区域信息出错了：' + err)
        })
    }

    // 关闭弹窗
    private handleClose(st: string) {
        this.tableDataes.dialogVisible = false
    }

    // 修改
    private editHandle(dat: any) {
        this.tableDataes.title = '修改分类'
        this.action = 'edit'
        this.tableDataes.id = dat.tableId
        this.tableDataes.name = dat.tableName
        this.tableDataes.seatNumber = dat.seatNumber
        this.tableDataes.dialogVisible = true
    }

    //删除
    private deleteHandle(tableId:any) {
        this.$confirm('此操作将永久删除该桌台, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            delTable({tableId}).then(res => {
                this.$message.success('删除成功！')
                this.init()
            }).catch(err => {
                this.$message.error('请求出错了：' + err)
            })
        })
    }

    //数据提交
    submitForm(formName: any) {
        this.$refs[formName].validate((valid: any) => {
            if (valid) {
                if (this.action == 'add') {
                    const {areaId, name, seatNumber} = this.tableDataes
                    addTable({areaId, name, seatNumber}).then(res => {
                        this.$message.success('桌台添加成功！')
                        this.tableDataes.dialogVisible = false
                        this.init()
                    }).catch(err => {
                        this.$message.error('请求出错了：' + err)
                    })
                } else {
                    const {areaId, id, name, seatNumber} = this.tableDataes
                    editTable({areaId, id, name, seatNumber}).then(res => {
                        this.$message.success('桌台修改成功！')
                        this.tableDataes.dialogVisible = false
                        this.init()
                    }).catch(err => {
                        this.$message.error('请求出错了：' + err)
                    })
                }
            } else {
                console.log('error submit!!')
                return false
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
