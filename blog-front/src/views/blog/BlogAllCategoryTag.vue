<template>
  <div class="me-allct-body" v-title :data-title="categoryTagTitle" >
    <el-container class="me-allct-container">
      <el-main>
        <el-tabs v-model="activeName">
          <el-tab-pane label="文章分类" name="category">
            <ul class="me-allct-items">
              <li v-for="c in categorys" @click="view(c.id)" :key="c.id" class="me-allct-item">
                <div class="me-allct-content">
                  <a class="me-allct-info">
                    <img class="me-allct-img" :src="c.avatar?c.avatar:defaultAvatar"/>
                    <h4 class="me-allct-name">{{c.categoryName}}</h4>
                    <p class="me-allct-description">{{c.description}}</p>
                  </a>

                  <div class="me-allct-meta">
                    <span>{{c.articles}} 文章</span>
                  </div>
                </div>
              </li>
            </ul>
          </el-tab-pane>
          <el-tab-pane label="标签" name="tag">
            <ul class="me-allct-items">
              <li v-for="t in tags"  class="me-allct-item">
                <div class="me-allct-content">
                  <a class="me-view-comment-tool" @click="deleteThisTag(t.id)" v-if="isAdmin==1">
                    <i class="me-icon-comment"></i>&nbsp; 删除
                  </a>
                  <a class="me-allct-info" @click="view(t.id)" :key="t.id">
                    <img class="me-allct-img" :src="t.avatar?t.avatar:defaultAvatar"/>
                    <h4 class="me-allct-name">{{t.tagName}}</h4>
                  </a>
                  <div class="me-allct-meta">
                    <span>{{t.articles}}  文章</span>
                  </div>
                </div>
              </li>
              <li @click="publishVisible=true" class="me-allct-item" v-if="this.$store.state.account.length != 0">
                <div class="me-allct-content">
                  <a class="me-allct-info">
                    <img class="me-allct-img" src="../../assets/img/tagAdd.png"/>
                    <h4 class="me-allct-name">add one</h4>
                  </a>
                  <div class="me-allct-meta">
                    <span>自己创一个</span>
                  </div>
                </div>
              </li>
            </ul>
          </el-tab-pane>
        </el-tabs>
        <el-dialog title="添加新标签"
                   :visible.sync="publishVisible"
                   :close-on-click-modal=false
                   custom-class="me-dialog">

          <el-form :model="articleForm" ref="articleForm" :rules="rules">
            <el-form-item label="新标签名字" prop="tagName">
              <el-input type="textarea"
                        v-model="articleForm.tagName"
                        :rows="6"
                        placeholder="请输入标签名字">
              </el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button @click="publishVisible = false">取 消</el-button>
            <el-button type="primary" @click="add('articleForm')">添加</el-button>
          </div>
        </el-dialog>
      </el-main>
    </el-container>
  </div>
</template>

<script>
  import defaultAvatar from '@/assets/img/logo.png'
  import {getAllCategorysDetail} from '@/api/category'
  import {getAllTagsDetail, addTag, deteleTag} from '@/api/tag'

  export default {
    name: 'BlogAllCategoryTag',
    inject: ['reload'],
    created() {
      this.getCategorys()
      this.getTags()
    },
    data() {
      return {
        defaultAvatar:defaultAvatar,
        categorys: [],
        tags: [],
        isAdmin: this.$store.state.admin,
        currentActiveName: 'category',
        publishVisible: false,
        articleForm: {
          tagName: ''
        },
        rules: {
          tagName: [
            {max: 10, message: '不能大于10个字符', trigger: 'blur'},
            {required: true, message: '请输入标签名', trigger: 'blur'},
          ],
        }
      }
    },
    computed: {
      activeName: {
        get() {
          return (this.currentActiveName = this.$route.params.type)
        },
        set(newValue) {
          this.currentActiveName = newValue
        }
      },
      categoryTagTitle (){
        if(this.currentActiveName == 'category'){
          return '文章分类 - TZSBLOG'
        }
        return '标签 - TZSBLOG'
      }
    },
    methods: {
      view(id) {
        this.$router.push({path: `/${this.currentActiveName}/${id}`})
      },
      getCategorys() {
        let that = this
        getAllCategorysDetail().then(data => {
          that.categorys = data.data
        }).catch(error => {
          if (error !== 'error') {
            that.$message({type: 'error', message: '文章分类加载失败', showClose: true})
          }
        })
      },
      getTags() {
        let that = this
        getAllTagsDetail().then(data => {
          that.tags = data.data
        }).catch(error => {
          if (error !== 'error') {
            that.$message({type: 'error', message: '标签加载失败', showClose: true})
          }
        })
      },
      add(articleForm){
        this.$refs[articleForm].validate((valid) => {
          if (valid) {
            let loading = this.$loading({
              lock: true,
              text: '添加中，请稍后...'
            })
            addTag(this.articleForm.tagName).then((data) =>{
                 if(data.success){
                   loading.close();
                   this.$message({type: 'success', message: '添加成功', showClose: true});
                   this.$router.go(0);
                   // this.$router.push({path: '/tag/all'});
                 }else{
                   this.$message({type: 'error', message: data.msg, showClose: true});
                 }
               }).catch((error) => {
                loading.close();
                if (error !== 'error') {
                  this.$message({message: error, type: '添加失败', showClose: true});
              }
            })
             } else {
               this.$message({type: 'error', message: '请重新输入', showClose: true});
               return false;
             }
           });
          },
          deleteThisTag(tagId){
            this.$confirm('确定删除标签?', '提示', {
                      confirmButtonText: '确定',
                      cancelButtonText: '取消',
                      type: 'warning'
                    }).then(() => {
                      deteleTag(tagId).then(data => {
                        if(data.success){
                          this.$message({type: 'success', message: '删除成功', showClose: true})
                          this.$router.go(0);
                        }else{
                           this.$message({type: 'error', message: data.msg, showClose: true})
                        }
                      }).catch(error => {
                        if (error !== 'error') {
                          this.$message({type: 'error', message: '删除失败', showClose: true})
                        }
                      })
                    }).catch(() => {
                    });
          }
    },
    //组件内的守卫 调整body的背景色
    beforeRouteEnter(to, from, next) {
      window.document.body.style.backgroundColor = '#fff';
      next();
    },
    beforeRouteLeave(to, from, next) {
      window.document.body.style.backgroundColor = '#f5f5f5';
      next();
    }
  }
</script>

<style>
  .me-allct-body {
    margin: 60px auto 140px;
  }

  .me-allct-container {
    width: 1000px;
  }

  .me-allct-items {
    padding-top: 2rem;
  }

  .me-allct-item {
    width: 25%;
    display: inline-block;
    margin-bottom: 2.4rem;
    padding: 0 .7rem;
    box-sizing: border-box;
  }

  .me-allct-content {
    display: inline-block;
    width: 100%;
    background-color: #fff;
    border: 1px solid #f1f1f1;
    transition: border-color .3s;
    text-align: center;
    padding: 1.5rem 0;
  }

  .me-allct-info {
    cursor: pointer;
  }

  .me-allct-img {
    margin: -40px 0 10px;
    width: 60px;
    height: 60px;
    vertical-align: middle;

  }

  .me-allct-name {
    font-size: 21px;
    font-weight: 150;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-top: 4px;
  }

  .me-allct-description {
    min-height: 50px;
    font-size: 13px;
    line-height: 25px;
  }

  .me-allct-meta {
    font-size: 12px;
    color: #969696;
  }
</style>
