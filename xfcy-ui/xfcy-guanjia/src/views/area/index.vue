<template>
  <div class="dashboard-container">
    <HeadLable :title="'区域管理'" />
    <div class="container">
      <div class="tableBar">
        <div />
        <el-button
          type="primary"
          size="small"
          @click="addHandle"
        >
          +添加区域
        </el-button>
      </div>
      <el-table
        :data="tableData"
        stripe
        class="tableBox"
      >
        <el-table-column
          prop="areaName"
          label="区域名称"
          align="center"
        />
        <el-table-column
          label="操作"
          align="center"
        >
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="editHandle(scope.row)"
            >
              修改
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="deleteHandle(scope.row.areaId)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="counts > 10 "
        class="pageList"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="page"
        layout="total, sizes, prev, pager, next, jumper"
        :total="counts"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    <el-dialog
      :title="areaData.title"
      :visible.sync="areaData.dialogVisible"
      width="30%"
      :before-close="handleClose"
    >
      <el-form
        :inline="true"
        class="demo-form-inline"
      >
        <el-form-item label="区域名称：">
          <el-input
            v-model="areaData.areaName"
            size="small"
            placeholder="请输入区域名称"
          />
        </el-form-item>
      </el-form>
      <span
        slot="footer"
        class="dialog-footer"
      >
        <el-button
          size="small"
          @click="areaData.dialogVisible = false"
        >取 消</el-button>
        <el-button
          type="primary"
          size="small"
          @click="submitForm()"
        >确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import HeadLable from '@/components/HeadLable';
import {getAreaList, addArea, editArea, delArea} from '@/api/table';

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
    private action = ''
    private delAreaId = ''
    private areaData: any = {
        'title': '添加区域',
        'dialogVisible': false,
        'areaName': ''
    }
    created() {
        this.init();
    }

    private async init() {
        await getAreaList({'page': this.page, 'pageSize': this.pageSize, 'name':this.input || undefined}).then(res => {
            const { data } = res.data;
            this.tableData = data.items;
            this.counts = Number(data.counts);
        }).catch(err => {
            this.$message.error('请求出错了：' + err);
        });
    }

    // 关闭弹窗
    private handleClose(st: string) {
        this.areaData.dialogVisible = false;
    }

    // 添加
    private addHandle(){
        this.action = 'add';
        this.areaData.dialogVisible = true;
    }

    // 修改
    private editHandle(dat: object) {
        this.areaData.title = '修改分类';
        this.action = 'edit';
        this.areaData.areaName = dat.areaName;
        this.areaData.areaId = dat.areaId;
        this.areaData.dialogVisible = true;
    }

    //删除
    private deleteHandle(areaId:any) {
        this.delAreaId = areaId;
        this.$confirm('此操作将永久删除该桌台, 是否继续?', '提示', {
            'confirmButtonText': '确定',
            'cancelButtonText': '取消',
            'type': 'warning'
        }).then(() => {
            delArea({'id':this.delAreaId}).then(res => {
                this.$message.success('删除成功！');
                this.init();
            }).catch(err => {
                this.$message.error('请求出错了：' + err);
            });
        });
    }

    //数据提交
    submitForm(st: any) {
        if (this.action == 'add') {
            if (this.areaData.areaName) {
                addArea({'name': this.areaData.areaName}).then(res => {
                    this.$message.success('区域添加成功！');
                    if (!st) {
                        this.areaData.dialogVisible = false;
                    }
                    this.init();
                }).catch(err => {
                    this.$message.error('请求出错了：' + err);
                });
            } else {
                this.$message.error('请输入区域名称');
            }
        } else if (this.areaData.areaName) {
            editArea({'id':this.areaData.areaId,'name': this.areaData.areaName}).then(res => {
                this.$message.success('区域修改成功！');
                this.areaData.dialogVisible = false;
                this.init();
            }).catch(err => {
                this.$message.error('请求出错了：' + err);
            });
        } else {
            this.$message.error('请输入分类名称');
        }
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
