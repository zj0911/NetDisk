<template>
  <div>
  <!--增删-->
  <div>
    <div style="padding: 15px; border-bottom: 1px solid #ddd">
      <!--上传文件-->
      <el-upload
          style="display: inline-block; margin-right: 10px"
          :action="uploadUrl"
          :headers="{ token: user.token }"
          :show-file-list="false"
          :on-success="handleFileSuccess"
      >
        <el-button type="primary">Upload</el-button>
      </el-upload>
      <el-button type="primary" plain @click="addFolder">Create New</el-button>
      <el-button type="danger" plain @click="delBatch" :disabled="!tableData.length">Delete</el-button>
    </div>
  </div>

  <!--箭头导航栏-->
  <div style="padding: 15px">
    <a :href="'/front/home?category=' + category" style="font-weight: bold; color: #606266">
      All Files <i class="el-icon-arrow-right" v-if="folders.length"></i>
    </a>
    <a :href="'/front/home?category=' + category + '&folderId=' + item.id" v-for="(item, index) in folders" :key="item.id"
       :style="{ color: '#606266', fontWeight: index === folders.length - 1 ? 'normal' : 'bold' }">
      {{item.name}} <i class="el-icon-arrow-right" v-if="index !== folders.length-1"/>
    </a>
  </div>

  <!--文件列表-->
  <div>
    <el-table stripe :data="tableData" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column label="Name">
        <template v-slot="scope">
          <!--创建文件夹按钮-->
          <div v-if="scope.row.unSave">
            <i style="font-size:25px; color: #409EFF" :class="typeList.find(v => v.text === scope.row.type)?.icon || 'el-icon-file'"></i>
            <el-input size="mini" style="width: 120px; margin:0px 5px;" v-model="scope.row.name"></el-input>
            <el-button type="primary" size="mini" icon="el-icon-check" @click="saveFolder(scope.row)"></el-button>
            <el-button type="danger" size="mini" icon="el-icon-close" @click="cancel(scope.row.$index)"></el-button>
          </div>
          <!--功能按钮-->
          <div style="display: flex; cursor: pointer"  v-else @mouseenter="mouseEnter(scope.row)"  @mouseleave="mouseLeave(scope.row)">
            <div style=" flex: 1">
              <i @click="openFile(scope.row)" style="font-size:25px; color: #409EFF" :class="typeList.find(v => v.text === scope.row.type)?.icon || 'el-icon-file'"></i>
              <span v-if="!scope.row.unSave" style="margin-left: 5px">{{ scope.row.name }}</span>
            </div>
            <div style="color: #409EFF; font-size: 13px" v-if="scope.row.optShow && scope.row.id">
              <el-tooltip content="Share" effect="light" :open-delay="1000">
                <i class="el-icon-share" style="margin-right: 10px; cursor: pointer" @click="share(scope.row)"></i>
              </el-tooltip>
              <el-tooltip content="Download" effect="light" :open-delay="1000" v-if="scope.row.folder === 'No'">
                <i class="el-icon-download" style="margin-right: 10px; cursor: pointer" @click="download(scope.row.file)"></i>
              </el-tooltip>
              <el-tooltip content="Rename" effect="light" :open-delay="1000">
                <i class="el-icon-rename" style="margin-right: 10px; cursor: pointer" @click="rename(scope.row)"></i>
              </el-tooltip>
              <el-tooltip content="Copy" effect="light" :open-delay="1000">
                <i class="el-icon-document-copy" style="cursor: pointer" @click="copy(scope.row)"></i>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="updateTime" label="Time"></el-table-column>
      <el-table-column prop="size" label="Size(KB)"></el-table-column>
    </el-table>
  </div>

    <!--分享弹窗-->
    <el-dialog title="File Share" :visible.sync="shareVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form label-width="150px" style="padding-right: 50px" :model="form">
        <el-form-item label="Share Days">
          <el-radio-group v-model="form.days">
            <el-radio :label="3">3 days</el-radio>
            <el-radio :label="7">7 days</el-radio>
            <el-radio :label="30">30 days</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Share Links" v-if="shareLink">
          <div style="display: flex">
            <el-input style="flex: 1" v-model="shareLink"></el-input>
            <el-button style="margin-left: 10px" type="primary" @click="copyLink(shareLink)">Copy</el-button>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="shareVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveShare">Share</el-button>
      </div>
    </el-dialog>

  </div>
</template>


<script>
export default {
  name: "Main",
  props:{
    typeList: null
  },
  data(){
    return {
      category: this.$route.query.category, //分类
      folderId: this.$route.query.folderId, //文件夹id
      folders: [],
      tableData: [],
      ids: [],
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      uploadUrl: '',
      form:{},
      shareVisible: false,
      shareLink: '',
    }
  },

  mounted() {
    this.load()

    this.uploadUrl = this.$baseUrl + '/diskFiles/add?folder=No'
    if (this.folderId) {  // 如果不这么判断  就会传 undefined到后台 就会报错
      this.uploadUrl += '&folderId=' + this.folderId
    }
  },

  methods: {
    //分享文件
    saveShare(){
      if(this.shareLink){
        this.shareVisible = false
        return
      }
      this.$request.post('/diskFiles/share', this.form).then(res=>{
        if (res.code === '200') {
          this.$message.success("Share Successfully")

          let shareData = res.data
          let currentUrl = location.href.substring(0, location.href.indexOf('/front'))
          this.shareLink = currentUrl　+　'/front/openShare?code=' + shareData.code + '&shareId=' +shareData.id
        } else {
          this.$message.error(res.msg)
        }
      })
    },

    //复制分享链接
    copyLink(link) {
      let _input = document.createElement("input");   // 直接构建input
      _input.value = link;  // 设置内容
      document.body.appendChild(_input);    // 添加临时实例
      _input.select();   // 选择实例内容
      document.execCommand("Copy");   // 执行复制
      document.body.removeChild(_input)
      this.$message.success("Copy Successfully")
    },

    //打开文件分享弹窗
    share(row){
      this.form = JSON.parse(JSON.stringify(row))
      this.form.days = 3
      this.shareLink = ''
      this.shareVisible = true
    },

    //复制文件
    copy(row) {
      this.$request.post('/diskFiles/copy/' + row.id).then(res => {
        if (res.code === '200') {
          this.$message.success("Copy Successfully")
          this.load()
        } else {
          this.$message.error(res.msg)
        }
      })
    },

    //打开文件
    openFile(row){
      if(row.folder === 'Yes'){//文件夹
        location.href='/front/home?category=' + this.category + '&folderId=' + row.id
        this.load()
      }else{
        window.open(this.$baseUrl + '/diskFiles/preview/' + row.id)
      }
    },
    mouseEnter(row) {
      if (!row.unSave) {
        this.$set(row, 'optShow', true)
      }
    },
    mouseLeave(row) {
      if (!row.unSave) {
        this.$set(row, 'optShow', false)
      }
    },

    //批量删除
    delBatch() {
      if (!this.ids.length) {
        this.$message.warning('Have no items to delete')
        return
      }
      this.$confirm('Are you sure you want to delete?', 'Delete Confirm', {type: "warning"}).then(response => {
        this.$request.delete('/diskFiles/trash/batch', {data: this.ids}).then(res => {
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

    //下载文件
    download(url){
      window.open(url)  // 文件下载
    },

    //重新命名
    rename(row){
      this.$set(row, 'unSave', true)
    },

    //确认创建文件夹
    saveFolder(row){
      if(row.id){//重命名后确认
        this.$request.put('/diskFiles//update', row).then(res=>{
          if (res.code === '200') {
            this.$message.success("Create Successfully")
            this.load()
          } else {
            this.$message.error(res.msg)
          }
        })
      }else {
        let url = '/diskFiles/add?folder=Yes&name=' + row.name
        if (this.folderId) {  // 外层的folderId
          url += '&folderId=' + this.folderId
        }
        this.$request.post(url).then(res => {
          if (res.code === '200') {
            this.$message.success("Create Successfully")
            this.load()
          } else {
            this.$message.error(res.msg)
          }
        })
      }
    },

    //取消创建文件夹
    cancel(index){
      this.tableData.splice(index,1)
      this.load()
    },

    //创建文件夹
    addFolder(){
      this.tableData.unshift({name:'New Folder', type:'folder',unSave: true, folderId: this.folderId || null})
    },

    //加载数据
    load() {
      this.$request.get('/diskFiles/selectAll',{
        params:{
          category: this.category || null,
          folderId: this.folderId || null
        }
      }).then(res => {
        this.tableData = res.data || []
        this.tableData.forEach(item => {
          this.$set(item, 'optShow', false)   // 强制设置每行  加一个 optShow属性
        })
      })

      this.$request.get('/diskFiles/selectFolders',{
        params:{
          folderId: this.folderId || null,
        }
      }).then(res=>{
        this.folders = res.data || []
        console.log(this.folders)
      })
    },

    //处理上传成功
    handleFileSuccess(res) {
      if (res.code === '200') {
        this.$message.success("Upload Successfully")
        this.load()
      } else {
        this.$message.error('Upload Failed')
      }
    },

    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)
    },
  }
}
</script>


<style scoped>
/deep/.el-form-item__label{
  padding-right: 30px !important;
}

</style>