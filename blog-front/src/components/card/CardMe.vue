<template>
  <el-card>
    <h1 class="me-author-name">钟霖涛</h1>
    <div class="me-author-description">
      <el-col> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class="el-icon-location-outline">&nbsp;海南省海口市</i>&nbsp;</el-col>
      <el-col> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class="el-icon-school"></i>&nbsp;HKUST<i class="el-icon-right"></i>MSC in IT</el-col>
      <el-col> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class="el-icon-user">&nbsp;JAVA后端开发</i>&nbsp;</el-col>
    </div>

    <div class="me-author-tool">
      <i @click="showTool(wechat)" :title="wechat.title" class="iconfont-wechat icon-weixin"></i>
      <i @click="showTool(mailbox)" :title="mailbox.title" class="iconfont-mailbox icon-youxiang"></i>
      <i @click="showTool(bilibili)" :title="bilibili.title" class="iconfont icon-bilibili-fill"></i>
      <i @click="showTool(github)" :title="github.title" class="iconfont-github icon-github-fill"></i>
    </div>
  </el-card>

</template>

<script>
  export default {
    name: 'CardMe',
    data() {
      return {
        wechat: {title: 'wechat', message: '微信号:TimingZoon'},
        bilibili: {
          title: '哔哩哔哩',
          message: '<a target="_blank" href="https://space.bilibili.com/351332883">https://space.bilibili.com/351332883</a>'
        },
        github: {
          title: 'github',
          message: '<a target="_blank" href="https://github.com/Assainsa">https://github.com/Assainsa</a>'
        },
        mailbox: {
          title: '邮箱',
          message: '邮箱:zhonglintao.cn@gmail.com'
        }
      }
    },
    methods: {
      showTool(tool) {
        this.$message({
          duration: 0,
          showClose: true,
          dangerouslyUseHTMLString: true,
          message: '<strong>' + tool.message + '</strong>'
        });
      }
    }
  }
</script>

<style scoped>
  .me-author-name {
    text-align: center;
    font-size: 30px;
    border-bottom: 1px solid #5FB878;
  }

  .me-author-description {
    padding: 8px 0;
  }

  .me-icon-job {
    padding-left: 16px;
  }

  .me-author-tool {
    text-align: center;
    padding-top: 10px;
  }

  .me-author-tool i {
    cursor: pointer;
    padding: 4px 10px;
    font-size: 30px;
  }
</style>
