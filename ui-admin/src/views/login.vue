<template>
  <div class="login" data-ui-theme="cool-tower">
    <section class="login-intro" aria-labelledby="login-slogan">
      <div class="login-intro__content">
          <div class="login-brand">
            <div class="login-brand__mark">
              <img :src="brandLogo" class="login-brand__icon" alt="manzhushaka - scaff" />
            </div>
            <span>manzhushaka - scaff</span>
          </div>

        <div class="login-intro__eyebrow">{{ heroEyebrow }}</div>
        <h1 id="login-slogan" class="login-intro__slogan">{{ heroSlogan }}</h1>
        <div class="login-intro__divider" aria-hidden="true"></div>
        <h2 class="login-intro__title">{{ heroTitle }}</h2>
        <p class="login-intro__description">{{ heroDescription }}</p>
        <div class="login-intro__tags" aria-label="平台能力">
          <span v-for="item in heroTags" :key="item" class="login-intro__tag">{{ item }}</span>
        </div>
      </div>
      <div class="login-intro__signal" aria-hidden="true"><span></span><span></span><span></span></div>
    </section>

    <section class="login-panel" aria-labelledby="login-heading">
      <div class="login-panel__content">
        <div class="login-panel__eyebrow">WELCOME BACK</div>
        <h2 id="login-heading" class="login-panel__title">登录系统</h2>
        <p class="login-panel__description">请输入账号信息后进入管理后台</p>

        <el-form
          ref="loginRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-panel__form"
          size="large"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              type="text"
              auto-complete="off"
              placeholder="账号"
            >
              <template #prefix>
                <svg-icon icon-class="user" class="el-input__icon input-icon" />
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              auto-complete="off"
              placeholder="密码"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <svg-icon icon-class="password" class="el-input__icon input-icon" />
              </template>
            </el-input>
          </el-form-item>

          <el-form-item v-if="captchaEnabled" prop="code">
            <div class="login-code-row">
              <el-input
                v-model="loginForm.code"
                auto-complete="off"
                placeholder="验证码"
                @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <svg-icon icon-class="validCode" class="el-input__icon input-icon" />
                </template>
              </el-input>
              <button class="login-code" type="button" aria-label="刷新验证码" @click="getCode">
                <img :src="codeUrl" class="login-code__image" alt="验证码" />
              </button>
            </div>
          </el-form-item>

          <div class="login-options">
            <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
            <router-link v-if="register" class="login-register-link" :to="'/register'">立即注册</router-link>
          </div>

          <el-button
            :loading="loading"
            type="primary"
            size="large"
            class="login-submit"
            @click.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
        </el-form>
      </div>

      <div class="login-footer"><span>{{ footerContent }}</span></div>
    </section>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'
import brandLogo from '@/assets/logo/logo.png'

const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const heroEyebrow = "MANZHUSHAKA - SCAFF"
const heroSlogan = "让管理更清晰"
const heroTitle = "后台管理系统"
const heroDescription = "为管理与运营工作提供统一、清晰的业务入口。"
const heroTags = ["统一入口", "权限管理", "运营支持"]

const loginForm = ref({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关
const captchaEnabled = ref(true)
// 注册开关
const register = ref(false)
const redirect = ref(undefined)

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/png;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

getCode()
getCookie()
</script>

<style lang='scss' scoped>
.login {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  min-height: 100vh;
  font-family: "Avenir Next", "SF Pro Display", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.login-intro {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 80px;
  background: #15304d;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.08),
    inset -56px 0 96px rgba(5, 24, 40, 0.16);
  color: #f3f7f7;
}

.login-intro__content {
  position: relative;
  z-index: 1;
  width: min(100%, 540px);
}

.login-brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: #f3f7f7;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0;
}

.login-brand__mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 1px solid rgba(243, 247, 247, 0.42);
  border-radius: 6px;
  overflow: hidden;
  background: #ffffff;
}

.login-brand__icon {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-intro__eyebrow {
  margin-top: 72px;
  color: #9ed8ce;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
}

.login-intro__slogan {
  max-width: 520px;
  margin: 20px 0 0;
  color: #ffffff;
  font-size: 64px;
  font-weight: 700;
  line-height: 1.12;
  letter-spacing: 0;
}

.login-intro__divider {
  width: 48px;
  height: 2px;
  margin-top: 32px;
  background: #167d71;
}

.login-intro__title {
  margin: 28px 0 0;
  color: #ffffff;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;
  letter-spacing: 0;
}

.login-intro__description {
  max-width: 460px;
  margin: 12px 0 0;
  color: #c5d4dd;
  font-size: 16px;
  line-height: 1.8;
}

.login-intro__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 30px;
}

.login-intro__tag {
  padding: 8px 12px;
  border: 1px solid rgba(197, 212, 221, 0.42);
  border-radius: 4px;
  color: #e3ebef;
  font-size: 13px;
  font-weight: 500;
  line-height: 1;
}

.login-intro__signal {
  position: absolute;
  right: 0;
  bottom: 72px;
  width: 38%;
  min-width: 210px;
  height: 160px;
  opacity: 0.5;
}

.login-intro__signal span {
  position: absolute;
  right: 0;
  height: 1px;
  border-top: 1px solid #8fb7c4;
}

.login-intro__signal span:nth-child(1) {
  top: 0;
  width: 62%;
}

.login-intro__signal span:nth-child(2) {
  top: 56px;
  width: 100%;
}

.login-intro__signal span:nth-child(3) {
  top: 112px;
  width: 78%;
}

.login-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 56px 80px;
  background: #f5f8f8;
  color: #142740;
}

.login-panel__content {
  width: min(100%, 420px);
}

.login-panel__eyebrow {
  color: #167d71;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
}

.login-panel__title {
  margin: 12px 0 0;
  font-size: 36px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: 0;
}

.login-panel__description {
  margin: 12px 0 0;
  color: #607582;
  font-size: 15px;
  line-height: 1.7;
}

.login-panel__form {
  margin-top: 32px;

  .el-form-item {
    margin-bottom: 18px;
  }

  :deep(.el-input__wrapper) {
    min-height: 54px;
    padding: 0 16px;
    border: 1px solid #cbd7d6;
    border-radius: 6px;
    background: #ffffff;
    box-shadow: none;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      border-color: #9eb7b3;
    }

    &.is-focus {
      border-color: #167d71;
      box-shadow: 0 0 0 3px rgba(22, 125, 113, 0.14);
    }
  }

  :deep(.el-input__inner) {
    height: 54px;
    color: #142740;
    font-size: 16px;

    &::placeholder {
      color: #93a4ad;
    }
  }

  :deep(.input-icon) {
    width: 18px;
    height: 18px;
    color: #6c848d;
  }

  :deep(.el-form-item__error) {
    padding-top: 7px;
  }
}

.login-code-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px;
  gap: 10px;
  width: 100%;
}

.login-code {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 54px;
  padding: 7px;
  border: 1px solid #cbd7d6;
  border-radius: 6px;
  background: #ffffff;
  cursor: pointer;
}

.login-code:hover {
  border-color: #9eb7b3;
}

.login-code:focus-visible {
  outline: 3px solid rgba(22, 125, 113, 0.18);
  outline-offset: 2px;
}

.login-code__image {
  display: block;
  width: 100%;
  max-height: 36px;
  object-fit: contain;
}

.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: 2px 0 22px;

  :deep(.el-checkbox__label) {
    color: #607582;
    font-size: 14px;
  }
}

.login-register-link {
  color: #167d71;
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;

  &:hover {
    color: #10685e;
  }
}

.login-submit {
  width: 100%;
  min-height: 54px;
  border: 0;
  border-radius: 6px;
  background: #167d71;
  box-shadow: none;
  color: #ffffff;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0;
  transition: background-color 0.2s ease;

  &:hover {
    background: #10685e;
  }
}

.login-footer {
  position: absolute;
  right: 32px;
  bottom: 24px;
  left: 32px;
  color: #82929b;
  font-size: 12px;
  text-align: center;
}

@media (max-width: 1180px) {
  .login-intro,
  .login-panel {
    padding-right: 56px;
    padding-left: 56px;
  }

  .login-intro__slogan {
    font-size: 54px;
  }
}

@media (max-width: 960px) {
  .login {
    grid-template-columns: 1fr;
  }

  .login-intro {
    min-height: 430px;
    padding: 56px 48px;
  }

  .login-intro__eyebrow {
    margin-top: 48px;
  }

  .login-intro__slogan {
    max-width: 620px;
    font-size: 52px;
  }

  .login-panel {
    min-height: auto;
    padding: 56px 48px 92px;
  }

  .login-panel__content {
    width: min(100%, 480px);
  }
}

@media (max-width: 640px) {
  .login-intro {
    min-height: auto;
    padding: 42px 24px 74px;
  }

  .login-brand {
    font-size: 16px;
  }

  .login-brand__mark {
    width: 36px;
    height: 36px;
  }

  .login-intro__eyebrow {
    margin-top: 38px;
    font-size: 11px;
  }

  .login-intro__slogan {
    margin-top: 16px;
    font-size: 42px;
  }

  .login-intro__title {
    margin-top: 24px;
    font-size: 20px;
  }

  .login-intro__description {
    font-size: 15px;
  }

  .login-intro__signal {
    right: -20px;
    bottom: 24px;
    min-width: 160px;
    height: 96px;
  }

  .login-intro__signal span:nth-child(2) {
    top: 34px;
  }

  .login-intro__signal span:nth-child(3) {
    top: 68px;
  }

  .login-panel {
    padding: 42px 24px 84px;
  }

  .login-panel__title {
    font-size: 32px;
  }

  .login-code-row {
    grid-template-columns: minmax(0, 1fr) 108px;
  }

  .login-options {
    align-items: flex-start;
    flex-wrap: wrap;
  }
}

@media (prefers-reduced-motion: no-preference) {
  .login-panel__content {
    animation: login-enter 360ms ease-out both;
  }
}

@keyframes login-enter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
