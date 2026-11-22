<template>
  <div class="dashboard-container">
    <HeadLable :but-list="true">
      <div class="headBut">
        <span
          :class="{act: act === 'day'}"
          @click="dateAct('day')"
        >日报</span>
        <span
          :class="{act: act === 'week'}"
          @click="dateAct('week')"
        >周报</span>
        <span
          :class="{act: act === 'mouth'}"
          @click="dateAct('mouth')"
        >月报</span>
      </div>
    </HeadLable>
    <div class="topLable">
      <div class="tit">
        统计时间：00：00 - 24：00
      </div>
      <div
        v-if="act === 'day'"
        class="dataSelect"
      >
        <div>
          <span>前一天</span>
          <el-date-picker
            v-model="dataTime"
            size="mini"
            type="date"
            placeholder="选择日期"
          />
          <span>后一天</span>
        </div>
        <div><span class="but">查看今日数据</span></div>
      </div>
      <div
        v-if="act === 'week'"
        class="dataSelect"
      >
        <div>
          <span>前一周</span>
          <el-date-picker
            v-model="dataTime"
            size="mini"
            type="week"
            format="yyyy 第 WW 周"
            placeholder="选择周"
          />
          <span>后一周</span>
        </div>
        <div><span class="but">查看本周数据</span></div>
      </div>
      <div
        v-if="act === 'mouth'"
        class="dataSelect"
      >
        <div>
          <span>前一月</span>
          <el-date-picker
            v-model="dataTime"
            size="mini"
            type="month"
            placeholder="选择月"
          />
          <span>后一月</span>
        </div>
        <div><span class="but">查看本月数据</span></div>
      </div>
    </div>
    <div class="container">
      <div class="topDataBox">
        <div class="box nowData">
          <div class="icon">
            <img
              src="./../../assets/icons/xiangmujine@2x.png"
              width="45"
              height="43"
              alt=""
            >
          </div>
          <div class="item">
            <div>实收金额</div>
            <div>{{ topData.payTotal || 0 }}元</div>
            <div>较前一日 0%</div>
          </div>
        </div>
        <div class="box noData">
          <div class="icon">
            <img
              src="./../../assets/icons/jine_m-2@2x.png"
              width="50"
              alt=""
            >
          </div>
          <div class="item">
            <div>未收金额</div>
            <div>{{ topData.noPayTotal || 0 }}元</div>
            <div>较前一日 0%</div>
          </div>
        </div>
        <div class="box member">
          <div class="icon">
            <img
              src="./../../assets/icons/renshu@2x.png"
              width="46"
              alt=""
            >
          </div>
          <div class="item">
            <div>就餐人数</div>
            <div>{{ topData.totalPerson || 0 }}人</div>
            <div>较前一日 0%</div>
          </div>
        </div>
      </div>
      <div class="memberChart">
        <Basic
          id="line"
          :chart-data="chartDataA"
          title="时段销售趋势"
        />
      </div>
    </div>
    <div class="container">
      <div class="chartBox">
        <div>
          <BarChart
            :chart-data="chartDataB"
            title="菜品分类占比"
          />
        </div>
        <div>
          <MixedChart
            :chart-data="chartDataA"
            title="菜单销售排行"
          />
        </div>
      </div>
    </div>
    <div class="container">
      <div class="chartBox">
        <div>
          <BarChart
            id="bar"
            :chart-data="chartDataB"
            title="店内收款构成"
          />
        </div>
        <div class="itemList">
          <div class="title">
            优惠指标
          </div>
          <div class="item topLab">
            <span>优惠合计</span><span>190元</span><span>20%</span>
          </div>
          <div class="item">
            <span>免单</span><span>190元</span><span>20%</span>
          </div>
          <div class="item">
            <span>赠菜</span><span>190元</span><span>20%</span>
          </div>
          <div class="item">
            <span>抹零</span><span>190元</span><span>20%</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import HeadLable from '@/components/HeadLable';
import Basic from '@/components/Charts/Basic.vue';
import BarChart from '@/components/Charts/BarChart';
import MixedChart from '@/components/Charts/MixedChart';
import moment from 'moment';
import { getDataes, getDayPayType,getDayDataes,getSalesRanking,getDayRanking,getTimeQuantumDataes,getTimeQuantumReceivables,getTimeQuantumType,getTimeQuantumDishes,getTimeQuantumDiscount } from '@/api/charts';

@Component({
    'name': 'chart',
    'components': {
        HeadLable,
        Basic,
        BarChart,
        MixedChart
    }
})

export default class extends Vue {
    private dataTime: any = ''
    private moment = moment
    private Today = moment().format('YYYY-MM-DD')
    private week = [moment().startOf('month').format('YYYY-MM-DD'), moment().endOf('month').format('YYYY-MM-DD')]
    private month = [moment().startOf('week').add(1,'day').format('YYYY-MM-DD'),moment().endOf('week').add(1,'day').format('YYYY-MM-DD')]
    private topData = {}
    private act = 'day'
    private chartDataA = {
        'xData': ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        'yData': [709, 1917, 2455, 2610, 1719, 1433, 1544, 3285, 5208, 3372, 2484, 4078]
    }
    private chartDataB = {
        'legendData': ['凉菜', '热菜'],
        'seriesData': [{'name': '凉菜','value': 33755}, {'name': '热菜','value': 66286}],
        'selected': {'凉菜': true, '热菜': true}
    }

    created() {
        this.init();
    }

    private init(){
        this.dataTime = this.Today;
        getDayPayType({ 'date': this.Today }).then(res => {
            this.topData = res.data;
        }).catch(err => {
            this.$message.error('请求出错了：' + err);
        });
    }

    private dateAct (val:string) {
        this.act = val;
    }
}

</script>
<style lang="scss" scoped>
  .dashboard {
    &-container {
      margin: 30px;
      .headBut{
        span{
          cursor:pointer;
          margin: 0 10px;
          font-size: 18px;
          padding: 16px 0px;
        }
        .act{
          border-bottom: solid 4px $mine;
          font-weight: bold;
        }
      }
      .topLable{
        font-size: 16px;
        background: #fff;
        position: relative;
        z-index: 1;
        padding: 15px 28px;
        margin-bottom:30px;
        border-radius: 4px;
        .tit{
          line-height: 40px;
          color:$mine;
        }
        .dataSelect{
          display:flex;
          justify-content: space-between;
          line-height: 40px;
          div:first-child{
            span:first-child{
              margin-right: 10px;
              cursor: pointer;
            }
            span:last-child{
              margin-left: 10px;
              cursor: pointer;
            }
          }
        }
        .but{
          background: $mine;
          cursor: pointer;
          display: inline-block;
          color:#fff;
          padding: 0px 20px;
          line-height: 30px;
          height: 30px;
          border-radius: 4px;
          font-size: 14px;
        }
      }
      .container {
        background: #fff;
        position: relative;
        z-index: 1;
        padding: 30px 28px;
        border-radius: 4px;
        margin-bottom: 30px;
        .topDataBox{
          display: flex;
          margin-bottom: 15px;
          .icon{
            width: 80px;
            height: 80px;
            /*padding-top: 15px;*/
            border-radius: 100%;
            background: #fff;
            margin-right: 10px;
            text-align: center;
            display: flex;
            justify-content: center;
            align-items: center;
          }
          .box{
            margin-right: 15px;
            display: flex;
            padding: 15px;
            flex:1;
                border-radius: 4px;
                color:#fff;
                .item{
                  div:nth-child(2){
                    font-size: 18px;
                    line-height: 36px;
              }
            }
          }
          .nowData{
            background: linear-gradient(to right, #55A9FF , #379AFF);
          }
          .noData{
            background: linear-gradient(to right, #FFA868 , #FF903D);
          }
          .member{
            margin-right:0;
            background: linear-gradient(to right, #FF8888 , #F56C6C);
          }
        }
        .chartBox{
          display: flex;
          div{
            flex:1
          }
          .itemList{
              .title{
                font-size: 18px;
                font-weight: bold;
                margin-bottom: 20px;
              }
              .item{
                display: flex;
                line-height: 45px;
                padding: 0 20px;
                border-bottom: solid 1px #F0F1F4;
                span{
                  flex: 1;
                  text-align: center;
                }
                span:first-child{
                  text-align: left;
                }
              }
            .topLab{
                background: #F0F1F4;
                border-radius: 4px;
              }
          }
        }
      }
    }
  }
</style>
