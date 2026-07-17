<template>
  <main class="login" data-ui-theme="vibehub-admin">
    <section class="login-intro" aria-labelledby="login-slogan">
      <div class="login-intro__content">
        <div class="login-brand">
          <div class="login-brand__mark">
            <img :src="brandLogo" class="login-brand__icon" alt="manzhushaka - scaff" />
          </div>
          <strong>manzhushaka - scaff</strong>
          <span class="login-brand__status"><i aria-hidden="true"></i>SECURE ACCESS</span>
        </div>

        <div class="login-intro__copy">
          <p class="login-intro__eyebrow">{{ heroEyebrow }}</p>
          <h1 id="login-slogan" class="login-intro__slogan">{{ heroSlogan }}</h1>
          <p class="login-intro__description">{{ heroDescription }}</p>
        </div>

        <div class="workspace-preview" aria-label="管理工作区预览">
          <header class="workspace-preview__header">
            <div>
              <span>WORKSPACE</span>
              <strong>{{ heroTitle }}</strong>
            </div>
            <span class="workspace-preview__state"><i aria-hidden="true"></i>服务正常</span>
          </header>
          <dl class="workspace-preview__grid">
            <div v-for="item in heroTags" :key="item.label">
              <dt>{{ item.label }}</dt>
              <dd>{{ item.value }}</dd>
            </div>
          </dl>
        </div>
      </div>
    </section>

    <section class="login-panel" aria-labelledby="login-heading">
      <div class="login-panel__content">
        <div class="login-panel__eyebrow">身份验证 · AUTHENTICATION</div>
        <h2 id="login-heading" class="login-panel__title">登录管理后台</h2>
        <p class="login-panel__description">使用系统账号继续访问管理工作区。</p>

        <el-form
          ref="loginRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-panel__form"
          label-position="top"
          size="large"
          :aria-busy="loading"
          @submit.prevent="handleLogin"
        >
          <el-form-item label="账号" prop="username">
            <el-input
              v-model="loginForm.username"
              type="text"
              autocomplete="username"
              placeholder="请输入账号"
              :disabled="loading"
            >
              <template #prefix>
                <svg-icon icon-class="user" class="el-input__icon input-icon" />
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              :type="passwordVisible ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="请输入密码"
              :disabled="loading"
            >
              <template #prefix>
                <svg-icon icon-class="password" class="el-input__icon input-icon" />
              </template>
              <template #suffix>
                <button
                  class="login-password-toggle"
                  type="button"
                  :aria-label="passwordVisible ? '隐藏密码' : '显示密码'"
                  :disabled="loading"
                  @click="passwordVisible = !passwordVisible"
                >
                  <el-icon><Hide v-if="passwordVisible" /><View v-else /></el-icon>
                </button>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item v-if="captchaEnabled" label="验证码" prop="code">
            <div class="login-code-row">
              <el-input
                v-model="loginForm.code"
                autocomplete="off"
                placeholder="请输入验证码"
                :disabled="loading"
              >
                <template #prefix>
                  <svg-icon icon-class="validCode" class="el-input__icon input-icon" />
                </template>
              </el-input>
              <button class="login-code" type="button" aria-label="刷新验证码" :disabled="loading" @click="getCode">
                <img :src="codeUrl" class="login-code__image" alt="验证码" />
              </button>
            </div>
          </el-form-item>

          <div class="login-options">
            <el-checkbox v-model="loginForm.rememberMe" :disabled="loading">记住密码</el-checkbox>
            <router-link v-if="register" class="login-register-link" :to="'/register'">立即注册</router-link>
          </div>

          <el-button
            :loading="loading"
            type="primary"
            size="large"
            class="login-submit"
            native-type="submit"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
        </el-form>
      </div>

      <div class="login-footer"><span>{{ footerContent }}</span></div>
    </section>
  </main>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'
import brandLogo from '@/assets/logo/logo.png'
import { Hide, View } from '@element-plus/icons-vue'

const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const heroEyebrow = "OPERATIONS · WORKSPACE"
const heroSlogan = "管理工作，清晰可控。"
const heroTitle = "后台管理系统"
const heroDescription = "系统状态、权限边界与运营记录集中呈现。"
const heroTags = [
  { label: "访问控制", value: "按角色授权" },
  { label: "运行监控", value: "服务与任务" },
  { label: "操作审计", value: "记录可追溯" }
]

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
const passwordVisible = ref(false)
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
  grid-template-columns: minmax(0, 1.12fr) minmax(440px, 0.88fr);
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--ui-bg-panel);
  font-family: "IBM Plex Sans", "Noto Sans SC", "PingFang SC", system-ui, sans-serif;
  letter-spacing: 0;
}

.login-intro {
  position: relative;
  display: grid;
  overflow: hidden;
  padding: clamp(32px, 4.8vw, 72px);
  background: var(--ui-bg-panel-soft);
  border-right: 1px solid var(--ui-border);
  color: var(--ui-text-primary);
}

.login-intro::before {
  position: absolute;
  inset: 0 auto 0 0;
  width: 4px;
  background: var(--ui-primary);
  content: '';
}

.login-intro__content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  width: min(100%, 720px);
  margin: 0 auto;
}

.login-brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: var(--ui-text-primary);
  font-family: "Space Grotesk", "Noto Sans SC", "PingFang SC", sans-serif;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0;
}

.login-brand__mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 1px solid var(--ui-border);
  border-radius: 8px;
  overflow: hidden;
  background: var(--ui-bg-panel);
}

.login-brand__icon {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-brand__status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  padding: 7px 10px;
  color: var(--ui-text-secondary);
  background: var(--ui-bg-panel);
  border: 1px solid var(--ui-border);
  border-radius: 999px;
  font-family: "IBM Plex Mono", "SFMono-Regular", Consolas, monospace;
  font-size: 11px;
  font-weight: 500;
}

.login-brand__status i,
.workspace-preview__state i {
  width: 7px;
  height: 7px;
  background: var(--ui-success);
  border-radius: 50%;
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--ui-success) 14%, transparent);
}

.login-intro__copy {
  max-width: 620px;
  margin-top: auto;
}

.login-intro__eyebrow {
  margin: 0 0 12px;
  color: var(--ui-primary-active);
  font-family: "IBM Plex Mono", "SFMono-Regular", Consolas, monospace;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0;
}

.login-intro__slogan {
  max-width: 520px;
  margin: 0;
  color: var(--ui-text-primary);
  font-family: "Space Grotesk", "Noto Sans SC", "PingFang SC", sans-serif;
  font-size: 48px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: 0;
}

.login-intro__description {
  max-width: 540px;
  margin: 18px 0 0;
  color: var(--ui-text-secondary);
  font-size: 15px;
  line-height: 1.7;
}

.workspace-preview {
  margin-top: 32px;
  margin-bottom: auto;
  padding: 20px;
  background: var(--ui-bg-panel);
  border: 1px solid var(--ui-border);
  border-radius: 16px;
  box-shadow: 0 18px 44px rgba(22, 19, 15, 0.10);
}

.workspace-preview__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.workspace-preview__header > div {
  display: grid;
  gap: 4px;
}

.workspace-preview__header > div span {
  color: var(--ui-text-muted);
  font-family: "IBM Plex Mono", "SFMono-Regular", Consolas, monospace;
  font-size: 10px;
}

.workspace-preview__header strong {
  font-size: 15px;
}

.workspace-preview__state {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--ui-text-secondary);
  font-size: 11px;
  white-space: nowrap;
}

.workspace-preview__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin: 0;
  border: 1px solid var(--ui-border);
  border-radius: 10px;
}

.workspace-preview__grid > div {
  min-width: 0;
  padding: 16px;
}

.workspace-preview__grid > div + div {
  border-left: 1px solid var(--ui-border);
}

.workspace-preview__grid dt {
  color: var(--ui-text-muted);
  font-size: 11px;
}

.workspace-preview__grid dd {
  margin: 6px 0 0;
  overflow: hidden;
  color: var(--ui-text-primary);
  font-size: 14px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.login-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 0;
  min-height: 100vh;
  padding: 48px clamp(24px, 5vw, 80px);
  background: var(--ui-bg-panel);
  color: var(--ui-text-primary);
}

.login-panel__content {
  width: 100%;
  max-width: 420px;
  min-width: 0;
}

.login-panel__eyebrow {
  color: var(--ui-primary-active);
  font-family: "IBM Plex Mono", "SFMono-Regular", Consolas, monospace;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0;
}

.login-panel__title {
  margin: 12px 0 0;
  color: var(--ui-text-primary);
  font-size: 30px;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: 0;
}

.login-panel__description {
  margin: 12px 0 0;
  color: var(--ui-text-muted);
  font-size: 14px;
  line-height: 1.7;
}

.login-panel__form {
  margin-top: 28px;

  .el-form-item {
    margin-bottom: 20px;
  }

  :deep(.el-form-item__label) {
    height: auto;
    margin-bottom: 8px;
    color: var(--ui-text-primary);
    font-size: 13px;
    font-weight: 600;
    line-height: 20px;
  }

  :deep(.el-input__wrapper) {
    min-height: 48px;
    padding: 0 16px;
    border: 1px solid var(--ui-border-strong);
    border-radius: 10px;
    background: var(--ui-bg-panel);
    box-shadow: none;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      border-color: var(--ui-text-muted);
    }

    &.is-focus {
      border-color: var(--ui-primary);
      box-shadow: 0 0 0 3px color-mix(in srgb, var(--ui-primary) 14%, transparent);
    }
  }

  :deep(.el-input__inner) {
    height: 48px;
    color: var(--ui-text-primary);
    font-size: 14px;

    &::placeholder {
      color: var(--ui-text-muted);
    }
  }

  :deep(.input-icon) {
    width: 18px;
    height: 18px;
    color: var(--ui-text-muted);
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
  min-width: 0;
}

.login-password-toggle {
  display: inline-grid;
  width: 32px;
  height: 32px;
  padding: 0;
  place-items: center;
  color: var(--ui-text-muted);
  background: transparent;
  border: 0;
  border-radius: 8px;
  cursor: pointer;
}

.login-password-toggle:hover:not(:disabled) {
  color: var(--ui-primary-active);
  background: var(--ui-primary-soft);
}

.login-password-toggle:disabled {
  cursor: not-allowed;
}

.login-code {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
  padding: 7px;
  border: 1px solid var(--ui-border-strong);
  border-radius: 10px;
  background: var(--ui-bg-panel);
  cursor: pointer;
}

.login-code:hover {
  border-color: var(--ui-text-muted);
}

.login-code:disabled {
  cursor: not-allowed;
  opacity: 0.62;
}

.login-code:focus-visible {
  outline: 3px solid color-mix(in srgb, var(--ui-primary) 18%, transparent);
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
    color: var(--ui-text-secondary);
    font-size: 14px;
  }
}

.login-register-link {
  color: var(--ui-primary-active);
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;

  &:hover {
    color: var(--ui-primary-active);
  }
}

.login-submit {
  width: 100%;
  min-height: 48px;
  border: 0;
  border-radius: 10px;
  background: var(--ui-primary);
  box-shadow: none;
  color: var(--ui-text-inverse);
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0;
  transition: background-color 0.2s ease;

  &:hover {
    background: var(--ui-primary-hover);
  }
}

.login-footer {
  position: absolute;
  right: 32px;
  bottom: 24px;
  left: 32px;
  color: var(--ui-text-muted);
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
    font-size: 40px;
  }
}

@media (max-width: 719px) {
  .login {
    grid-template-columns: 1fr;
    border-top: 4px solid var(--ui-primary);
  }

  .login-intro {
    min-height: auto;
    padding: 18px 24px;
    border-right: 0;
    border-bottom: 1px solid var(--ui-border);
  }

  .login-intro::before {
    display: none;
  }

  .login-intro__content {
    display: block;
  }

  .login-intro__copy,
  .workspace-preview {
    display: none;
  }

  .login-panel {
    min-height: calc(100dvh - 81px);
    padding: 40px 24px 28px;
  }

  .login-panel__content {
    width: 100%;
    max-width: 420px;
  }

  .login-brand {
    font-size: 14px;
  }

  .login-brand__mark {
    width: 36px;
    height: 36px;
  }

  .login-panel__title {
    font-size: 28px;
  }

  .login-code-row {
    grid-template-columns: minmax(0, 1fr) 108px;
  }

  .login-options {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .login-footer {
    position: static;
    margin-top: 40px;
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
