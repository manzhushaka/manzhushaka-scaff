<template>
  <div class="login" data-ui-theme="cool-tower">
    <div class="login-bg-glow" />
    <div class="login-container">
      <!-- 头部品牌区 -->
      <div class="login-header">
        <div class="login-brand-icon">
          <svg-icon icon-class="dashboard" class="brand-svg" />
        </div>
        <span class="login-brand-text">满招科技</span>
      </div>

      <!-- 登录卡片 -->
      <div class="login-card">
        <div class="login-card-title">{{ title }}</div>

        <el-form
          ref="loginRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
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

          <el-form-item prop="code" v-if="captchaEnabled">
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
              <div class="login-code">
                <img :src="codeUrl" @click="getCode" class="login-code-img" alt="验证码" />
              </div>
            </div>
          </el-form-item>

          <div class="login-options">
            <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
            <router-link v-if="register" class="link-type" :to="'/register'">立即注册</router-link>
          </div>

          <el-button
            :loading="loading"
            type="primary"
            size="large"
            class="login-btn"
            @click.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
        </el-form>
      </div>

      <!-- 底部版权 -->
      <div class="login-footer">
        <span>{{ footerContent }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

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
      codeUrl.value = "data:image/gif;base64," + res.img
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
// ============================================================
// 登录页 — B 冷感控制塔（固定主题，不跟随 brandTheme）
// ============================================================

.login {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(160deg, #f0f6fe 0%, #dbeafe 35%, #c7dbfe 65%, #e0ecfe 100%);
  overflow: hidden;
}

// 背景装饰光晕
.login-bg-glow {
  position: absolute;
  top: -30%;
  left: -10%;
  width: 60%;
  height: 80%;
  background: radial-gradient(ellipse at center, rgba(14, 165, 233, 0.10) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;

  &::after {
    content: '';
    position: absolute;
    bottom: -20%;
    right: -15%;
    width: 50%;
    height: 60%;
    background: radial-gradient(ellipse at center, rgba(125, 211, 252, 0.08) 0%, transparent 70%);
  }
}

.login-container {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 420px;

  @media (max-width: 480px) {
    width: calc(100vw - 32px);
  }
}

// 头部品牌区
.login-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
}

.login-brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.30);

  .brand-svg {
    width: 22px;
    height: 22px;
    color: #ffffff;
  }
}

.login-brand-text {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: 1px;
}

// 登录卡片
.login-card {
  width: 100%;
  padding: 32px 32px 20px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.60);
  box-shadow:
    0 12px 28px rgba(15, 59, 96, 0.10),
    0 4px 12px rgba(15, 59, 96, 0.06);

  @media (max-width: 480px) {
    padding: 24px 20px 16px;
  }
}

.login-card-title {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 24px;
}

// 表单
.login-form {
  .el-form-item {
    margin-bottom: 18px;
  }

  :deep(.el-input__wrapper) {
    height: 42px;
    background: rgba(255, 255, 255, 0.85);
    border: 1px solid #d8e6ef;
    border-radius: 6px;
    box-shadow: none;
    padding: 1px 12px;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      border-color: #bfd7e6;
    }

    &.is-focus {
      border-color: #0ea5e9;
      box-shadow: 0 0 0 2px rgba(14, 165, 233, 0.12);
    }
  }

  :deep(.el-input__inner) {
    height: 42px;
    font-size: 14px;
    color: #0f172a;

    &::placeholder {
      color: #94a3b8;
    }
  }

  :deep(.input-icon) {
    width: 16px;
    height: 16px;
    color: #64748b;
  }
}

// 验证码行
.login-code-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;

  .el-input {
    flex: 1;
  }
}

.login-code {
  flex-shrink: 0;

  .login-code-img {
    display: block;
    height: 42px;
    border-radius: 6px;
    cursor: pointer;
    border: 1px solid #d8e6ef;
  }
}

// 选项行
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  :deep(.el-checkbox__label) {
    font-size: 13px;
    color: #64748b;
  }

  .link-type {
    font-size: 13px;
    color: #0ea5e9;
    text-decoration: none;

    &:hover {
      color: #0284c7;
      text-decoration: underline;
    }
  }
}

// 登录按钮
.login-btn {
  width: 100%;
  height: 42px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  border-radius: 6px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.28);
  transition: box-shadow 0.2s ease, transform 0.15s ease;

  &:hover {
    box-shadow: 0 6px 16px rgba(14, 165, 233, 0.38);
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(14, 165, 233, 0.25);
  }
}

// 底部版权
.login-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: #64748b;
  letter-spacing: 0.5px;
}
</style>
