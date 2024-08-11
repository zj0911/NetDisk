<template>
  <div style="margin-top: 30px">
    <!--分享失败-->
    <div style="margin: 100px auto; text-align: center;
    font-size: 60px; color: #666666" v-if="validateFailed">
      (>_<) The file no longer exists or has been deleted.
    </div>

    <!--分享成功-->
    <el-card class="box-card" style="margin: 5px auto 100px auto; width: 70%"  v-else>
      <div style="color: #666666; display: flex; align-items: center">
        File Shared: <strong>{{share.name}}</strong>
        <i class="el-icon-time" style="margin-left: 20px; margin-right: 5px"></i>{{share.shareTime}}
        <span style="margin-left: 20px">
          <i class="el-icon-info" style="margin-right: 5px"></i>
          <span v-if="share.status?.includes('hours')" style="color: #f13b3b">{{share.status}}</span>
          <span v-else>{{share.status}}</span>
        </span>
      </div>
      <!--分享内容-->
      <div style="padding: 15px 0; margin-top: 30px" >
        <a :href="'/front/openShare?code=' + code + '&shareId=' + shareId" style="font-weight: bold; color: #606266">
          All Files <i class="el-icon-arrow-right" v-if="folders.length"></i>
        </a>
        <a :href="'/front/openShare?code=' + code + '&shareId=' + shareId + '&folderId=' + item.folderId" v-for="(item, index) in folders" :key="item.id"
           :style="{ color: '#606266', fontWeight: index === folders.length - 1 ? 'normal' : 'bold' }">
          {{item.name}} <i class="el-icon-arrow-right" v-if="index !== folders.length-1"/>
        </a>
      </div>
      <!--分享表格-->
      <el-table size="medium" stripe :data="tableData">
        <!--文件名称-->
        <el-table-column label="Name">
          <template v-slot="scope">
            <!--功能按钮-->
            <div style="display: flex; cursor: pointer" @click="openFile(scope.row)">
              <div style=" flex: 1">
                <i style="font-size:25px; color: #409EFF" :class="typeList.find(v => v.text === scope.row.type)?.icon || 'el-icon-file'"></i>
                <span v-if="!scope.row.unSave" style="margin-left: 5px">{{ scope.row.name }}</span>
              </div>
              <div style="color: #409EFF; font-size: 13px">
                <el-tooltip content="Download" effect="light" :open-delay="1000" v-if="scope.row.folder === 'No'">
                  <i class="el-icon-download" style="margin-right: 10px; cursor: pointer" @click="download(scope.row.file)"></i>
                </el-tooltip>
              </div>
            </div>
          </template>
        </el-table-column>
        <!--文件创建时间-->
        <el-table-column prop="updateTime" label="Time" width="200"></el-table-column>
        <!--文件大小-->
        <el-table-column prop="size" label="Size(KB)" width="120"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "OpenShare",
  data(){
    return{
      code: this.$route.query.code,
      shareId: this.$route.query.shareId,
      share: {},
      validateFailed: false,   // 验证失败
      folders: [],
      typeList: [
        { text: 'mp3', icon: 'el-icon-mp3' },
        { text: 'mp4', icon: 'el-icon-mp4' },
        { text: 'jpg', icon: 'el-icon-jpg' },
        { text: 'jpeg', icon: 'el-icon-jpeg' },
        { text: 'png', icon: 'el-icon-png' },
        { text: 'pdf', icon: 'el-icon-pdf' },
        { text: 'docx', icon: 'el-icon-docx' },
        { text: 'txt', icon: 'el-icon-text' },
        { text: 'zip', icon: 'el-icon-zip' },
        { text: 'folder', icon: 'el-icon-folder' },
      ],
      tableData: [],
      folderId: this.$route.query.folderId
    }
  },

  created() {
    if(!this.code || !this.shareId){
      this.validateFailed = true //缺少参数
      return
    }

    this.load()
  },

  methods:{
    //打开文件
    openFile(row) {
      if (row.folder === 'Yes') { // 文件夹  点进来肯定有 folderId
        location.href = '/front/openShare?code=' + this.code + '&shareId=' + this.shareId + '&folderId=' + row.id
      } else {  // 如果是文件的话  直接预览
        window.open(this.$baseUrl + '/diskFiles/preview/' + row.id)
      }
    },

    //下载文件
    download(url){
      window.open(url)  // 文件下载
    },

    load() {
      this.$request.get('/share/selectById/' + this.shareId).then(res=> {
        this.share = res.data || {}
        this.tableData = res.data ? [res.data] : []
        if(this.share.status === 'Expired'){
          this.validateFailed = true
        }
        // 验证code对不对
        if (this.share.code !== this.code) {
          this.validateFailed = true
        } else {
          this.loadFiles()
        }
      })
    },

    //加载分享文档中的文件
    loadFiles() {
      // 查询当前目录的数据
      this.$request.get('/diskFiles/selectShare', {
        params: {
          shareId: this.shareId || null,
          folderId: this.folderId || null
        }
      }).then(res => {
        this.tableData = res.data || []
      })

      // 查询当前目录的路径数据
      this.$request.get('/diskFiles/selectFolders', {
        params: { folderId: this.folderId || null }
      }).then(res => {
        this.folders = res.data || []
        // if (this.folders?.length) {
        //   this.folders.splice(0, 1)
        // }

      })
    },
  }
}
</script>



<style scoped>

</style>