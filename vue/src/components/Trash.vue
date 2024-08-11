<template>
  <div>
    <!--增删-->
    <div>
      <div style="padding: 15px; border-bottom: 1px solid #ddd">
        <el-button type="danger" plain @click="delBatch" :disabled="!tableData.length">Delete</el-button>
      </div>
    </div>
    <el-table :data="tableData" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column label="Name">
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
              <el-tooltip content="Restore" effect="light" :open-delay="1000">
                <i class="el-icon-refresh" style="margin-right: 10px; cursor: pointer" @click="restore(scope.row.fileId)"></i>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="time" label="Delete Time" width="300"></el-table-column>
      <el-table-column prop="size" label="Size(KB)" width="300"></el-table-column>
    </el-table>

  </div>
</template>
<script>
export default {
  name: "Trash",
  props:{
    typeList: null
  },
  data(){
    return{
      tableData: [],
    }
  },

  mounted() {
    this.load()
  },

  methods: {
    //还原文件
    restore(fileId) {
      this.$request.put('/diskFiles/restore/' + fileId).then(res => {
        if (res.code === '200') {   // 表示操作成功
          this.$message.success('Restore Successfully')
          this.load()
        } else {
          this.$message.error(res.msg)  // 弹出错误的信息
        }
      })
    },

    // del(fileId) {   // 单个删除
    //   this.$confirm('您确定删除吗？', '确认删除', {type: "warning"}).then(response => {
    //     this.$request.delete('/diskFiles/delete/' + fileId).then(res => {  // 这里传的是 trash的id  要传file_id
    //       if (res.code === '200') {   // 表示操作成功
    //         this.$message.success('操作成功')
    //         this.load()
    //       } else {
    //         this.$message.error(res.msg)  // 弹出错误的信息
    //       }
    //     })
    //   }).catch(() => {
    //   })
    // },

    //批量删除
    delBatch() {
      if (!this.ids.length) {
        this.$message.warning('Have no items to delete')
        return
      }
      this.$confirm('Are you sure you want to delete?', 'Delete Confirm', {type: "warning"}).then(response => {
        this.$request.delete('/diskFiles/delete/batch', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('Delete Successfully')
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
      this.$request.get('/diskFiles/selectTrash').then(res => {
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
      this.ids = rows.map(v => v.fileId)
    },

  }
}
</script>


<style scoped>

</style>