<template>
  <div class="errPage-container ui-entry-page" data-ui-theme="cool-tower">
    <div class="errPage-card">
      <el-button icon="ArrowLeft" class="pan-back-btn" @click="back">
        返回
      </el-button>
      <el-row>
        <el-col :span="12">
          <h1 class="text-jumbo text-ginormous">
            401错误!
          </h1>
          <h2>您没有访问权限！</h2>
          <h6>对不起，您没有访问权限，请不要进行非法操作！您可以返回主页面</h6>
          <ul class="list-unstyled">
            <li class="link-type">
              <router-link to="/">
                回首页
              </router-link>
            </li>
          </ul>
        </el-col>
        <el-col :span="12">
          <img :src="errGif" width="313" height="428" alt="Girl has dropped her ice cream.">
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import errImage from "@/assets/401_images/401.gif"

let { proxy } = getCurrentInstance()

const errGif = ref(errImage + "?" + +new Date())

function back() {
  if (proxy.$route.query.noGoBack) {
    proxy.$router.push({ path: "/" })
  } else {
    proxy.$router.go(-1)
  }
}
</script>

<style lang="scss" scoped>
.errPage-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;

  .errPage-card {
    width: 820px;
    max-width: 100%;
    padding: 34px;
    border: 1px solid var(--ui-border);
    border-radius: var(--ui-radius-panel);
    background: rgba(255, 255, 255, 0.84);
    box-shadow: var(--ui-shadow-popover);
    backdrop-filter: blur(16px);
  }

  .pan-back-btn {
    background: var(--ui-primary);
    color: var(--ui-text-inverse);
    border: none !important;
  }
  .pan-gif {
    margin: 0 auto;
    display: block;
  }
  .pan-img {
    display: block;
    margin: 0 auto;
    width: 100%;
  }
  .text-jumbo {
    font-size: 48px;
    font-weight: 700;
    color: var(--ui-text-primary);
  }

  h2 {
    color: var(--ui-text-primary);
  }

  h6 {
    color: var(--ui-text-secondary);
    line-height: 1.7;
  }
  .list-unstyled {
    font-size: 14px;
    li {
      padding-bottom: 5px;
    }
    a {
      color: var(--ui-primary);
      text-decoration: none;
      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>
