<template>
  <div>
    <div style="display: flex; color: #666">
      <!--左侧面板-->
      <div style="width: 200px; text-align: center;  border-right: 1px solid #ddd; min-height: calc(100vh - 86px)">
        <!-- 第一部分-->
        <div style="padding: 10px 0">
          <div @click="loadFiles(item.category)" class="category" style="font-weight: bold; padding: 10px 0" v-for="item in categoryList" :key="item.text"
               :class="{ 'category-active' : category === item.category}">
            <i v-if="item.icon" :class="item.icon" style="margin-right: 5px"></i>
            <span>{{ item.text }}</span>
          </div>
        </div>

        <!-- 第二部分-->
        <div style="border-top: 1px solid #ddd; text-align: center; padding-top: 10px">
          <div @click="loadFiles('share')" style="padding: 10px 0; display: flex; justify-content: center" class="category" :class="{ 'category-active' : category === 'share'}">
            <div style="width: 75px; text-align: left">
              <i class="el-icon-share" style="margin-right: 5px"></i>
              <span style="font-weight: bold">Share</span>
            </div>
          </div>
          <div @click="loadFiles('trash')" style="padding: 10px 0; display: flex; justify-content: center" class="category" :class="{ 'category-active' : category === 'trash'}">
            <div style="width: 75px; text-align: left">
              <i class="el-icon-delete" style="margin-right: 5px"></i>
              <span style="font-weight: bold">Trash</span>
            </div>
          </div>
        </div>
      </div>

      <!--右侧面板-->
      <div style="flex: 1">
<!--        <p>Current Category: {{ category }}</p> &lt;!&ndash; 调试信息 &ndash;&gt;-->
        <Trash :type-list="typeList"  v-if="category === 'trash'" />
        <Share :type-list="typeList" v-else-if="category === 'share'" />
        <Main :type-list="typeList" v-else />
      </div>
    </div>
  </div>
</template>

<script>

import Main from "@/components/Main";
import Trash from "@/components/Trash";
import Share from "@/components/Share";

export default {
  components:{
    Share,
    Trash,
    Main
  },
  data() {
    return {
      categoryList: [
        {"text": 'All Files', category: 'all', icon: ''},
        {"text": 'Image', category: 'img', icon: 'el-icon-picture-outline'},
        {"text": 'Video', category: 'video', icon: 'el-icon-video-play'},
        {"text": 'Zip', category: 'zip', icon: 'el-icon-box'},
      ],
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
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      category: this.$route.query.category || '', //分类
      // folderId: this.$route.query.folderId, //文件夹id
    }
  },

  mounted() {
    if (!this.category) {
      this.category = 'all'; // 设置默认分类（如果需要）
    }
  },

  methods: {
    loadFiles(category){
      location.href= '/front/home?category='+category
    }
  }
}
</script>

<style scoped>
.category {
  cursor: pointer;
}
.category:hover {
  background-color: #f8f8f8;
  color: #000;
}
.category-active {
  color: #409EFF;
}
</style>
