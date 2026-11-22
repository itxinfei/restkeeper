<template>
  <div class="dashboard-container">
    <HeadLable :title="'支付方式'" />
    <div class="container">
      <el-table
        :data="tableData"
        stripe
        class="tableBox"
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          prop="name"
          label="付款方式"
          width="110"
        />
        <el-table-column
          prop="info"
          label="付款说明"
        />
        <el-table-column
          prop="name"
          label="状态"
          align="center"
          width="100"
        >
          <template slot-scope="scope">
            <el-switch v-model="value" />
          </template>
        </el-table-column>
        <el-table-column
          prop="address"
          label="配置"
          align="center"
          width="100"
        >
          <template slot-scope="scope">
            配置
          </template>
        </el-table-column>
        <el-table-column
          prop="address"
          label="排序"
          width="160"
          align="center"
        >
          <template slot-scope="scope">
            ...
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import HeadLable from '@/components/HeadLable';

@Component({
    'name': 'foodType',
    'components': {
        HeadLable
    }
})
export default class extends Vue {
    private multipleSelection = ''
    private value = ''
    get tableData() {
        return [{
            'date': '2016-05-02',
            'name': '王小虎',
            'info': '顾客使用现金买单时使用。（仅记账，不涉及与本平台结算）',
            'address': '上海市普陀区'
        }, {
            'date': '2016-05-04',
            'name': '王小虎',
            'info': '顾客使用现金买单时使用。（仅记账，不涉及与本平台结算）',
            'address': '上海市普陀区'
        }, {
            'date': '2016-05-01',
            'name': '王小虎',
            'info': '顾客使用现金买单时使用。（仅记账，不涉及与本平台结算）',
            'address': '上海市普陀区'
        }, {
            'date': '2016-05-03',
            'name': '王小虎',
            'info': '顾客使用现金买单时使用。（仅记账，不涉及与本平台结算）',
            'address': '上海市普陀区'
        }];
    }

    get input() {
        return '';
    }

    private addPackage() {
        this.$router.push({ 'path': '/setmeal/add' });
    }

    private handleSelectionChange(val) {
        this.multipleSelection = val;
    }

    private handleSizeChange() {

    }

    private handleCurrentChange() {

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
