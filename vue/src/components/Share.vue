<template>
  <div>
    <!--增删-->
    <div>
      <div style="padding: 15px; border-bottom: 1px solid #ddd">
        <el-button type="danger" plain @click="delBatch" :disabled="!tableData.length || !ids.length">Cancel Share</el-button>
      </div>
    </div>
    <el-table :data="tableData" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column label="Name" >
        <template v-slot="scope">
          <div style="display: flex; cursor: pointer" @mouseenter="mouseEnter(scope.row)"  @mouseleave="mouseLeave(scope.row)">
            <div style=" flex: 1">
              <i style="font-size:25px; color: #409EFF" :class="typeList.find(v => v.text === scope.row.type)?.icon || 'el-icon-file'"></i>
              <span v-if="!scope.row.unSave" style="margin-left: 5px">{{ scope.row.name }}</span>
            </div>
            <div style="color: #409EFF; font-size: 13px" v-if="scope.row.optShow">
              <!--              <el-tooltip content="Delete" effect="light" :open-delay="1000">-->
              <!--                <i class="el-icon-delete" style="margin-right: 10px; cursor: pointer" @click="del(scope.row.fileId)"></i>-->
              <!--              </el-tooltip>-->
              <el-tooltip content="copy link" effect="light" :open-delay="1000">
                <i class="el-icon-copy-document" style="margin-right: 10px; cursor: pointer" @click="copy(scope.row)"></i>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="shareTime" label="Share Time" width="300"></el-table-column>
      <el-table-column prop="status" label="Status" width="250">
        <template v-slot="scope">
          <span style="color: #f13b3b" v-if="scope.row.status.includes('hours')">{{scope.row.status}}</span>
          <span v-else>{{scope.row.status}}</span>
        </template>
      </el-table-column>
      <el-table-column prop="count" label="Share Count" width="150"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: "ShareComponent",
  props:{
    typeList: null
  },
  data(){
    return{
      tableData: [],
      ids: [],
    }
  },

  mounted() {
    this.load()
  },

  methods: {
    //复制分享链接
    copy(row) {
      let currentUrl = location.href.substring(0, location.href.indexOf('/front'))
      let shareLink = currentUrl + '/front/openShare?code=' + row.code + '&shareId=' + row.id

      let _input = document.createElement("input");   // 直接构建input
      _input.value = shareLink;  // 设置内容
      document.body.appendChild(_input);    // 添加临时实例
      _input.select();   // 选择实例内容
      document.execCommand("Copy");   // 执行复制
      document.body.removeChild(_input)
      this.$message.success("Copy Successfully")
    },

    //批量取消分享
    delBatch() {
      if (!this.ids.length) {
        this.$message.warning('Have no items to delete')
        return
      }
      this.$confirm('Are you sure you want to cancel share?', 'Cancel Share Confirm', {type: "warning"}).then(response => {
        this.$request.delete('/share/delete/batch', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('Cancel Successfully')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },


    // 加载数据
    load() {
      this.$request.get('/share/selectAll').then(res => {
        this.tableData = res.data || []
      })
    },

    mouseEnter(row) {
      this.$set(row, 'optShow', true)
    },
    mouseLeave(row) {
      this.$set(row, 'optShow', false)
    },

    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)
    },

  }
}
</script>


<style scoped>

</style>