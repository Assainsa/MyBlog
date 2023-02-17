<template>
  <el-container style="height: 100%; width: 100%; border: 1px solid #eee">

    <el-container style="height: 100%; width: 100%;">
      <el-header class="me-area">
        <el-row class="me-header">

          <el-col :span="2" class="me-header-left">
            <router-link to="/" class="me-title">
              <img src="../assets/img/logo.png" />
            </router-link>
          </el-col>

        </el-row>
      </el-header>

      <el-main style="height: 100%; width: 100%;">
        <el-row style="margin-top: 5%; margin-bottom: 2%;">
          <el-col style="text-align: center;">
            <img class="me-header-picture" style="width: 80px; height: 80px;" :src="currentAvatar"/>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-form :model="ruleForm" status-icon :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm" style="margin-right: 25%; margin-left: 25%;">
              <el-form-item label="点击修改头像" prop="avatar">
                <el-select style="text-align: center;" v-model="ruleForm.avatar" placeholder="请选择">
                    <el-option
                      v-for="item in cities"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value">
                      <span @click="currentAvatar=ruleForm.avatar" style="float: left">{{ item.label }}</span>
                      <img @click="currentAvatar=ruleForm.avatar" class="me-header-picture" style="float: right; width: 36px; height: 36px;" :src="item.value"/>
                    </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="ruleForm.nickname" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="用户名" prop="account">
                <el-input v-model="ruleForm.account" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="新密码" prop="pass">
                <el-input type="password" v-model="ruleForm.pass" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="确认新密码" prop="checkPass">
                <el-input type="password" v-model="ruleForm.checkPass" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitForm('ruleForm')">提交</el-button>
                <el-button @click="resetForm('ruleForm')">重置</el-button>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
  import {getUserInfo,updateUser} from '@/api/login'
  import default_avatar from '@/assets/img/default_avatar.png'

   export default {
     name: 'PersonalInfo',
     created() {
       this.getUser()
     },
       /* computed: {
         user() {
           let login = this.$store.state.account
           let nickname = this.$store.state.nickname
           let avatar = this.$store.state.avatar
           let password = this.$store.state.password
           return {
             login, nickname, avatar, password
           }
         }
       }, */
       data() {
         var checkAge = (rule, value, callback) => {
           if (!value) {
             return callback(new Error('年龄不能为空'));
           }
           setTimeout(() => {
             if (!Number.isInteger(value)) {
               callback(new Error('请输入数字值'));
             } else {
               if (value < 18) {
                 callback(new Error('必须年满18岁'));
               } else {
                 callback();
               }
             }
           }, 1000);
         };
         var validatePass2 = (rule, value, callback) => {
           if (value !== this.ruleForm.pass) {
             callback(new Error('两次输入密码不一致!'));
           } else {
             callback();
           }
         };
         var checkNickName = (rule, value, callback) => {
           if (!value) {
             return callback(new Error('昵称不能为空'));
           } else if (length(value)>10) {
              callback(new Error('不能大于10个字符'));
            } else {
              callback();
            }
         };
         return {
           currentAvatar: '',
           currentUser: {
             nick: '',
             avatar: '',
             account: ''
           },
           ruleForm: {
             nickname: '',
             account: '',
             pass: '',
             checkPass: '',
             avatar: ''
           },
           rules: {
             pass: [
               {max: 10, message: '不能大于10个字符', trigger: 'blur'},
               {required: true, message: '请输入密码', trigger: 'blur'},
             ],
             checkPass: [
               { validator: validatePass2, trigger: 'blur' },
               {required: true, message: '请再次输入密码', trigger: 'blur'},
             ],
             account: [
               {required: true, message: '请输入用户名', trigger: 'blur'},
               {max: 10, message: '不能大于10个字符', trigger: 'blur'}
             ],
             nickname: [
               {required: true, message: '请输入昵称', trigger: 'blur'},
               {max: 10, message: '不能大于10个字符', trigger: 'blur'}
             ],
           },
           cities: [{
                     value: '/static/user/user_1.png',
                     label: '用户1'
                   }, {
                     value: '/static/user/user_2.png',
                     label: '用户2'
                   }, {
                     value: '/static/user/user_3.png',
                     label: '用户3'
                   }, {
                     value: '/static/user/user_4.png',
                     label: '用户4'
                   }, {
                     value: '/static/user/user_5.png',
                     label: '用户5'
                   }, {
                     value: '/static/user/user_6.png',
                     label: '用户6'
                   }],
         };
       },
       methods: {
         getUser(){
           let that = this
           getUserInfo(this.$store.state.token).then((data) => {
             if(data.success){
               that.currentUser.nickname = data.data.nickname
               that.currentUser.account = data.data.account
               that.currentUser.avatar = data.data.avatar
               that.ruleForm.nickname = data.data.nickname
               that.ruleForm.account = data.data.account
               that.currentAvatar = data.data.avatar
             }
           }).catch((error) => {
             loading.close();
             if (error !== 'error') {
               that.$message({message: error, type: 'error', showClose: true});
             }
           })
         },
         submitForm(ruleForm) {
           this.$refs[ruleForm].validate((valid) => {
             if (valid) {
               let user = {
                 account: '',
                 nickname: '',
                 password: this.ruleForm.checkPass,
                 avatar: this.ruleForm.avatar
               }
               if(this.ruleForm.account!=this.currentUser.account){
                 user.account=this.ruleForm.account
               }
               if(this.ruleForm.nickname!=this.currentUser.nickname){
                 user.nickname=this.ruleForm.nickname
               }
               let loading = this.$loading({
                 lock: true,
                 text: '修改中，请稍后...'
               })
               updateUser(user, this.$store.state.token).then((data) =>{
                 if(data.success){
                   loading.close();
                   this.$message({type: 'success', message: '修改成功', showClose: true});
                   this.$router.push({path: '/'});
                 }else{
                   this.$message({type: 'error', message: '修改失败', showClose: true});
                 }
               }).catch((error) => {
                loading.close();
                if (error !== 'error') {
                  this.$message({message: error, type: '修改失败', showClose: true});
              }
            })
             } else {
               this.$message({type: 'error', message: '请重新输入', showClose: true});
               return false;
             }
           });
         },
         resetForm(formName) {
           this.$refs[formName].resetFields();
         }
       }
     }
</script>

<style>


    .el-aside {
      color: #333;
    }
    .el-header {
      position: fixed;
      z-index: 1024;
      min-width: 100%;
      box-shadow: 0 2px 3px hsla(0, 0%, 7%, .1), 0 0 0 1px hsla(0, 0%, 7%, .1);
    }

    .me-title {
      margin-top: 10px;
      font-size: 24px;
    }

    .me-header-left {
      margin-top: 10px;
    }

    .me-title img {
      max-height: 2.4rem;
      max-width: 100%;
    }

</style>
