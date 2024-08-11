<template>
  <div class="container">
    <div style="height: 60px; position: fixed; top: 0; display: flex; align-items: center; padding-left: 20px">
      <img src="@/assets/imgs/logo.png" style="width: 50px; height: 50px">
      <span style="color: #4ba4e0; font-size: 30px; font-weight: bold; margin-left: 5px;">NetDisk</span>
    </div>
    <div style="flex: 1; display: flex; justify-content: center">
      <img src="@/assets/imgs/login.png" alt="" style="width: 100%">
    </div>
    <div style="flex: 1; display: flex; justify-content: center">
      <div style="width: 400px; padding: 50px 30px; box-shadow: 0 0 10px rgba(0,0,0,.2); background-color: white; border-radius: 5px;">
      <!--图像验证-->
        <div style="position: absolute; background-color: white; top: 180px; z-index: 999" v-if="slideVerifyShow">
          <slide-verify :l="50"
                        :r="15"
                        :w="350"
                        :h="210"
                        :accuracy="5"
                        :imgs="imgs"
                        slider-text="Swipe right to unlock"
                        @success="onSuccess"
                        ref="slideVerifyRef"
          ></slide-verify>
        </div>
        <div style="text-align: center; font-size: 20px; font-weight:bold; margin-bottom: 30px; color: #333">SIGN IN</div>
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="username" >
            <el-input size="medium" prefix-icon="el-icon-user" placeholder="name" v-model="form.username"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input size="medium" prefix-icon="el-icon-lock" placeholder="password" show-password  v-model="form.password"></el-input>
          </el-form-item>
          <el-form-item prop="role">
            <el-select style="width: 100%" size="medium" v-model="form.role">
              <el-option value="ADMIN" label="Admin"></el-option>
              <el-option value="USER" label="User"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button size="medium" style="width: 100%; background-color: #4ba4e0; color: white" @click="login">LOGIN</el-button>
          </el-form-item>
          <div style="display: flex; align-items: center">
            <div style="flex: 1"></div>
            <div style="text-align: right">
              Don't have an account? <a href="/register">Sign up</a>
            </div>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      form: { role:'ADMIN'},
      rules: {
        username: [
          { required: true, message: 'name', trigger: 'blur' },
        ],
        password: [
          { required: true, message: 'password', trigger: 'blur' },
        ]
      },
      imgs:[
        require('@/assets/imgs/1.jpg'),
        require('@/assets/imgs/2.jpg'),
        require('@/assets/imgs/3.jpg'),
      ],
      slideVerifyShow: false
    }
  },
  created() {

  },
  methods: {
    onSuccess() {
      this.$request.post('/login', this.form).then(res => {
        if (res.code === '200') {
          localStorage.setItem("xm-user", JSON.stringify(res.data))  // 存储用户数据
          setTimeout(() => {
            if (res.data.role == 'ADMIN') {
              location.href = '/home'
            } else {
              location.href = '/front/home?category=all'
            }
          }, 500)
          this.$message.success('Login Successfully!')
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    login(){
      this.$refs['formRef'].validate((valid) => {
        if (valid) {
          this.slideVerifyShow = true;
        }
      })
    }
  }
}
</script>

<style scoped>
.container {
  height: 100vh;
  overflow: hidden;
  background-size: 100%;
  display: flex;
  align-items: center;
}
a {
  color: #2a60c9;
}
</style>