<template>
  <div class="me-ct-body" v-title :data-title="myArticle">
    <el-container class="me-ct-container">
      <el-main>
        <div class="me-ct-articles">
          <article-scroll-page v-bind="article"></article-scroll-page>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script>
  import ArticleScrollPage from '@/views/common/ArticleScrollPage'
  import {getArticleByAuthorId} from '@/api/article'
  import defaultAvatar from '@/assets/img/logo.png'
  import BaseHeader from  '@/views/BaseHeader'


  export default {
    name: 'myArticles',
    created() {
      this.verify()
    },
    components: {
      ArticleScrollPage,
      BaseHeader
    },
    data() {
      return {
        article: {
          query: {
            authorId: ''
          }
        },
      }
    },
    methods: {
      verify(){
        if(this.$store.state.account.length == 0){
          this.$message({type: 'error', message: '你还未登录！', showClose: true})
          this.$router.push({path: `/login`});
          return;
        }
        this.article.query.authorId=this.$store.state.id;
        return;
      }
    }
  }
</script>

<style>
  .me-ct-body {
    margin: 60px auto 140px;
    min-width: 100%;
  }

  .el-main {
    padding: 0;
  }

  .me-ct-title {
    text-align: center;
    height: 150px;
    padding: 20px;
  }

  .me-ct-picture {
    width: 60px;
    height: 60px;
  }

  .me-ct-name {
    font-size: 28px;
  }

  .me-ct-meta {
    font-size: 12px;
    color: #969696;
  }

  .me-ct-articles {
    width: 640px;
    margin: 30px auto;
  }

</style>
