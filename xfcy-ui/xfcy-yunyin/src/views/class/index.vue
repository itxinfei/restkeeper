<template>
  <div class="dashboard-container">
    <HeadLable :title="'分类管理'"/>
    <div class="container">
      <div class="tableBar" style="display: inline-block">
        <el-button type="primary" @click="addClass('class')" size="small" class="continue"> + 新增菜品分类</el-button>
        <el-button type="primary" @click="addClass('meal')" size="small"> + 新增套餐分类</el-button>
      </div>
      <el-table :data="tableData" stripe class="tableBox">
        <el-table-column prop="name" label="分类名称"></el-table-column>
        <el-table-column prop="type" label="分类类型">
          <template slot-scope="scope">
            <span>{{scope.row.type == '1' ? '菜品分类': '套餐分类'}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdateTime" label="操作时间"></el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="editHandle(scope.row)">修改</el-button>
            <el-button type="text" size="small" @click="deleteHandle(scope.row.categoryId)">删除</el-button>
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
      :title="classData.title"
      :visible.sync="classData.dialogVisible"
      width="30%"
      :before-close="handleClose">
      <el-form :inline="true" class="demo-form-inline">
        <el-form-item label="分类名称：">
          <el-input v-model="classData.categoryName" size="small" placeholder="请输入分类名称"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="classData.dialogVisible = false">取 消</el-button>
        <el-button type="primary" size="small" @click="submitForm()">确 定</el-button>
        <el-button v-if="action != 'edit'" type="primary" size="small" class="continue" @click="submitForm('go')"> 保存并继续添加 </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script lang="ts">
    import {Component, Vue} from 'vue-property-decorator'
    import HeadLable from '@/components/HeadLable'
    import {getClassList, addCategory, editCategory, delCategory} from '@/api/category'

    @Component({
        name: 'class',
        components: {
            HeadLable
        }
    })
    export default class extends Vue {
        private action: string = ''
        private counts: number = 0
        private page: number = 1
        private pageSize: number = 10
        private tableData = []
        private classData: any = {
            title: '添加菜品分类',
            dialogVisible: false,
            categoryId: '',
            categoryName: ''
        }

        created() {
            this.init()
        }

        // 初始化信息
        private async init() {
            await getClassList({page: this.page, pageSize: this.pageSize}).then(res => {
                const {data} = res.data
                this.tableData = data.items
                this.counts = Number(data.counts)
            }).catch(err => {
                this.$message.error('请求出错了：' + err)
            })
        }

        // 添加
        private addClass(st: any) {
            if (st == 'class') {
                this.classData.title = '新增菜品分类'
            } else {
                this.classData.title = '新增套餐分类'
            }
            this.action = 'add'
            this.classData.categoryName = ''
            this.classData.dialogVisible = true
        }

        // 修改
        private editHandle(dat: object) {
            this.classData.title = '修改分类'
            this.action = 'edit'
            this.classData.categoryName = dat.name
            this.classData.categoryId = dat.categoryId
            this.classData.dialogVisible = true
        }

        // 关闭弹窗
        private handleClose(st: string) {
            this.classData.dialogVisible = false
        }

        //删除
        private deleteHandle(id: any) {
            this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                delCategory({id}).then(res => {
                    this.$message.success('删除成功！')
                    this.init()
                }).catch(err => {
                    this.$message.error('请求出错了：' + err)
                })
            })
        }

        //数据提交
        submitForm(st: any) {
            if (this.action == 'add') {
                if (this.classData.categoryName) {
                    addCategory({categoryName: this.classData.categoryName}).then(res => {
                        this.$message.success('分类添加成功！')
                        if (!st) {
                            this.classData.dialogVisible = false
                        }
                        this.init()
                    }).catch(err => {
                        this.$message.error('请求出错了：' + err)
                    })
                } else {
                    this.$message.error('请输入分类名称')
                }
            } else {
                if (this.classData.categoryName) {
                    editCategory({id:this.classData.categoryid,categoryName: this.classData.categoryName}).then(res => {
                        this.$message.success('分类修改成功！')
                        this.classData.dialogVisible = false
                        this.init()
                    }).catch(err => {
                        this.$message.error('请求出错了：' + err)
                    })
                } else {
                    this.$message.error('请输入分类名称')
                }
            }
        }

        //分页
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
  .el-dialog__body {
    padding-bottom: 0px;
  }
</style>
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
