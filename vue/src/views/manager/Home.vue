<template>
  <div>
    <div style="display: flex; margin: 10px 0">
      <div style="width: 50%" class="card">
        <div style="margin-bottom: 30px">
          <el-select style="width: 150px" v-model="days" @change="loadLine">
            <el-option :value="7" label="last week"></el-option>
            <el-option :value="14" label="last two weeks"></el-option>
            <el-option :value="30" label="last month"></el-option>
          </el-select>
        </div>
        <div style="width: 100%; height: 400px" id="line"></div>
      </div>

      <div style="width: 50%;" class="card">
        <div style="margin-bottom: 30px; font-size: 20px; font-weight: bold">Notice Lists</div>
        <div >
          <el-timeline  reverse slot="reference">
            <el-timeline-item v-for="item in notices" :key="item.id" :timestamp="item.time">
              <el-popover
                  placement="right"
                  width="200"
                  trigger="hover"
                  :content="item.content">
                <span slot="reference">{{ item.title }}</span>
              </el-popover>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
const lineOption = {
  title: {
    text: 'File Upload Trends',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    left: 'left'
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      data: [],
      type: 'line',
      smooth: true
    },
  ]
}

export default {
  name: 'Home',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      notices: [],
      days: 7
    }
  },
  created() {
    this.$request.get('/notice/selectAll').then(res => {
      this.notices = res.data || []
    })
  },
  mounted() {
    this.loadLine()
  },
  methods: {
    loadLine() {
      // 折线图
      let linetDom = document.getElementById('line');  // div id=line
      let lineChart = echarts.init(linetDom);
      // 参考
      this.$request.get('/diskFiles/count', { params: { days: this.days } }).then(res => {
        lineOption.xAxis.data = res.data?.map(v => v.date) || []
        lineOption.series[0].data = res.data.map(v => v.count) || []
        lineChart.setOption(lineOption)
      })
    }
  }
}
</script>
