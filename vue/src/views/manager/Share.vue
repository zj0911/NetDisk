<template>
  <div>
    <div class="search">
      <el-input placeholder="Enter File Name" style="width: 200px" v-model="name"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">Search</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">Reset</el-button>
    </div>

    <div class="operation">
      <el-button type="danger" plain @click="delBatch">Delete</el-button>
    </div>

    <div class="table">
      <el-table :data="tableData" strip @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="ID" width="70" align="center" sortable></el-table-column>
        <el-table-column prop="fileId" label="File ID"></el-table-column>
        <el-table-column prop="shareTime" label="Share Time"></el-table-column>
        <el-table-column prop="endTime" label="End Time"></el-table-column>
        <el-table-column prop="count" label="Count"></el-table-column>
        <el-table-column prop="code" label="Code"></el-table-column>
<!--        <el-table-column label="Operation" align="center" width="180">-->
<!--          <template v-slot="scope">-->
<!--            <el-button size="mini" type="danger" plain @click="del(scope.row.id)">删除</el-button>-->
<!--          </template>-->
<!--        </el-table-column>-->
      </el-table>
    </div>

    <div class="pagination">
      <el-pagination
          background
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[5, 10, 20]"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total">
      </el-pagination>
    </div>
  </div>
</template>

<script>
export default {
  name: "share",
  data() {
    return{
      tableData:[], //所有数据
      pageNum: 1,//当前页码
      pageSize: 10,//每页显示数量
      total: 0,
      name: null,
      user: JSON.parse(localStorage.getItem('xm-user')||'{}'),
      ids:[]
    }
  },
  created() {
      this.load(1)
  },
  methods:{
    // del(id){ // 单个删除
    //   this.$confirm('Are you sure you want to delete?','Delete Confirm', {type: "warning"}).then(response => {
    //     this.$request.delete('/diskFiles/delete/'+{id}).then(res =>{
    //       if(res.code == '200'){ // 操作成功
    //         this.$message.success("Operate Successfully")
    //         this.load(1);
    //       }else {
    //         this.$message.error(res.msg);  // 弹出错误的信息
    //       }
    //     })
    //   }).catch(()=>{})
    // },

    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)   //  [1,2]
    },

    delBatch() {   // 批量删除
      if (!this.ids.length) {
        this.$message.warning('Have no items to delete')
        return
      }
      this.$confirm('Are you sure you want to delete?', 'Delete Confirm', {type: "warning"}).then(response => {
        this.$request.delete('/share/delete/batch', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('Operate Successfully')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },

    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/share/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
        setTimeout(()=>{
          this.name = '';
        },500)
      })
    },

    reset() {
      this.name = null
      this.load(1)
    },

    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
  }
}
</script>

<style scoped>

</style>