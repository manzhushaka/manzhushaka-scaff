<template>
  <div class="register" data-ui-theme="vibehub-admin">
    <div class="entry-grid">
      <section class="entry-hero">
        <div class="entry-badge">MANZHUSHAKA CONSOLE</div>
        <div class="entry-brand">
          <div class="entry-brand-icon">
            <svg-icon icon-class="dashboard" class="brand-svg" />
          </div>
          <span class="entry-brand-text">manzhushaka</span>
        </div>
        <h1 class="entry-title">{{ heroTitle }}</h1>
        <p class="entry-description">{{ heroDescription }}</p>
        <div class="entry-tags">
          <span v-for="item in heroTags" :key="item" class="entry-tag">{{ item }}</span>
        </div>
      </section>

      <section class="auth-card">
        <div class="auth-card__eyebrow">创建账号</div>
        <h2 class="auth-card__title">注册系统</h2>
        <p class="auth-card__description">填写基础信息后创建后台账号并开始使用</p>

        <el-form ref="registerRef" :model="registerForm" :rules="registerRules" class="auth-form" size="large">
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              type="text"
              auto-complete="off"
              placeholder="账号"
            >
              <template #prefix>
                <svg-icon icon-class="user" class="el-input__icon input-icon" />
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password" :rules="registerPwdValidator">
            <el-input
              v-model="registerForm.password"
              type="password"
              auto-complete="off"
              placeholder="密码"
              @keyup.enter="handleRegister"
            >
              <template #prefix>
                <svg-icon icon-class="password" class="el-input__icon input-icon" />
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              auto-complete="off"
              placeholder="确认密码"
              @keyup.enter="handleRegister"
            >
              <template #prefix>
                <svg-icon icon-class="password" class="el-input__icon input-icon" />
              </template>
            </el-input>
          </el-form-item>

          <el-form-item v-if="captchaEnabled" prop="code">
            <div class="auth-code-row">
              <el-input
                v-model="registerForm.code"
                auto-complete="off"
                placeholder="验证码"
                @keyup.enter="handleRegister"
              >
                <template #prefix>
                  <svg-icon icon-class="validCode" class="el-input__icon input-icon" />
                </template>
              </el-input>
              <div class="auth-code-box">
                <img :src="codeUrl" @click="getCode" class="auth-code-img" alt="验证码" />
              </div>
            </div>
          </el-form-item>

          <el-button
            :loading="loading"
            type="primary"
            class="auth-submit"
            @click.prevent="handleRegister"
          >
            <span v-if="!loading">注 册</span>
            <span v-else>注 册 中...</span>
          </el-button>

          <div class="auth-switch">
            <span class="auth-switch__label">已经有账号？</span>
            <router-link class="auth-link" :to="'/login'">返回登录</router-link>
          </div>
        </el-form>
      </section>
    </div>

    <div class="entry-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from "element-plus"
import { getCodeImg, register } from "@/api/login"
import defaultSettings from '@/settings'
import { usePasswordRule } from "@/utils/passwordRule"

const footerContent = defaultSettings.footerContent
const router = useRouter()
const { proxy } = getCurrentInstance()
const { registerPwdValidator } = usePasswordRule()
const heroTitle = "统一管理入口"
const heroDescription = "一次注册后即可接入统一后台，覆盖商户资料维护、商品报备与订单处理等核心场景。"
const heroTags = ["账号开通", "业务接入", "流程管理"]

const registerForm = ref({
  username: "",
  password: "",
  confirmPassword: "",
  code: "",
  uuid: ""
})

const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, trigger: "blur", message: "请输入您的账号" },
    { min: 2, max: 20, message: "用户账号长度必须介于 2 和 20 之间", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请再次输入您的密码" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
const captchaEnabled = ref(true)

function handleRegister() {
  proxy.$refs.registerRef.validate(valid => {
    if (valid) {
      loading.value = true
      register(registerForm.value).then(res => {
        const username = registerForm.value.username
        ElMessageBox.alert("<font color='red'>恭喜你，您的账号 " + username + " 注册成功！</font>", "系统提示", {
          dangerouslyUseHTMLString: true,
          type: "success",
        }).then(() => {
          router.push("/login")
        }).catch(() => {})
      }).catch(() => {
        loading.value = false
        if (captchaEnabled) {
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
      registerForm.value.uuid = res.uuid
    }
  })
}

getCode()
</script>

<style lang='scss' scoped>
.register {
  position: relative;
  min-height: 100vh;
  padding: 36px 32px 64px;
  overflow: hidden;
  background: var(--ui-bg-page);
  font-family: "IBM Plex Sans", "Noto Sans SC", "PingFang SC", system-ui, sans-serif;

  &::before {
    content: "";
    position: absolute;
    inset: 0;
    width: 4px;
    background: var(--ui-primary);
    pointer-events: none;
  }

  &::after {
    content: "";
    position: absolute;
    display: none;
  }
}

.entry-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 0.94fr) minmax(380px, 500px);
  gap: 48px;
  align-items: center;
  width: min(1240px, 100%);
  min-height: calc(100vh - 100px);
  margin: 0 auto;
}

.entry-hero {
  max-width: 580px;
}

.entry-badge {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 16px;
  border: 1px solid var(--ui-border);
  border-radius: 999px;
  background: var(--ui-bg-panel);
  box-shadow: none;
  color: var(--ui-primary-active);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0;
}

.entry-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}

.entry-brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--ui-radius-control);
  background: var(--ui-primary);
  box-shadow: none;

  .brand-svg {
    width: 21px;
    height: 21px;
    color: #ffffff;
  }
}

.entry-brand-text {
  color: var(--ui-text-primary);
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0;
}

.entry-title {
  max-width: 560px;
  margin: 22px 0 0;
  color: var(--ui-text-primary);
  font-size: 52px;
  font-weight: 800;
  line-height: 1.02;
  letter-spacing: 0;
}

.entry-description {
  max-width: 540px;
  margin: 24px 0 0;
  color: var(--ui-text-secondary);
  font-size: 16px;
  line-height: 1.75;
}

.entry-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 28px;
}

.entry-tag {
  display: inline-flex;
  align-items: center;
  min-height: 42px;
  padding: 0 18px;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-control);
  background: var(--ui-bg-panel);
  box-shadow: none;
  color: var(--ui-text-secondary);
  font-size: 15px;
  font-weight: 700;
}

.auth-card {
  position: relative;
  justify-self: end;
  width: 100%;
  max-width: 500px;
  padding: 38px 34px 30px;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  background: var(--ui-bg-panel);
  box-shadow: var(--ui-shadow-panel-hover);
}

.auth-card__eyebrow {
  color: var(--ui-primary-active);
  font-size: 14px;
  font-weight: 700;
}

.auth-card__title {
  margin: 12px 0 0;
  color: var(--ui-text-primary);
  font-size: 38px;
  font-weight: 800;
  line-height: 1.02;
  letter-spacing: 0;
}

.auth-card__description {
  margin: 14px 0 0;
  color: var(--ui-text-secondary);
  font-size: 15px;
  line-height: 1.65;
}

.auth-form {
  margin-top: 24px;

  .el-form-item {
    margin-bottom: 16px;
  }

  :deep(.el-input__wrapper) {
    min-height: 56px;
    padding: 0 18px;
    border: 1px solid var(--ui-border);
    border-radius: var(--ui-radius-control);
    background: var(--ui-bg-panel);
    box-shadow: none;
    transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;

    &:hover {
      border-color: var(--ui-border-strong);
    }

    &.is-focus {
      border-color: var(--ui-primary);
      box-shadow: var(--ui-focus-ring);
      transform: translateY(-1px);
    }
  }

  :deep(.el-input__inner) {
    height: 56px;
    color: var(--ui-text-primary);
    font-size: 16px;
    font-weight: 500;

    &::placeholder {
      color: var(--ui-text-muted);
      font-weight: 500;
    }
  }

  :deep(.input-icon) {
    width: 18px;
    height: 18px;
    color: var(--ui-text-muted);
  }

  :deep(.el-form-item__error) {
    padding-top: 8px;
  }
}

.auth-code-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 124px;
  gap: 12px;
  width: 100%;
}

.auth-code-box {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 56px;
  padding: 8px;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-control);
  background: var(--ui-bg-panel);
  box-shadow: none;
}

.auth-code-img {
  display: block;
  width: 100%;
  max-height: 38px;
  object-fit: contain;
  cursor: pointer;
  border-radius: 14px;
}

.auth-submit {
  width: 100%;
  min-height: 58px;
  border: none;
  border-radius: var(--ui-radius-control);
  background: var(--ui-primary);
  box-shadow: 0 14px 30px color-mix(in srgb, var(--ui-primary) 24%, transparent);
  color: var(--ui-text-inverse);
  font-size: 19px;
  font-weight: 700;
  letter-spacing: 0;
  transition: background-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    background: var(--ui-primary-hover);
    transform: translateY(-2px);
    box-shadow: 0 18px 34px color-mix(in srgb, var(--ui-primary) 28%, transparent);
  }
}

.auth-switch {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
}

.auth-switch__label {
  color: var(--ui-text-secondary);
  font-size: 13px;
}

.auth-link {
  color: var(--ui-primary-active);
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;

  &:hover {
    color: var(--ui-primary-deep);
  }
}

.entry-footer {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 18px;
  z-index: 1;
  text-align: center;
  color: var(--ui-text-muted);
  font-size: 12px;
  letter-spacing: 0;
}

@media (max-width: 1280px) {
  .entry-grid {
    gap: 36px;
    grid-template-columns: minmax(0, 1fr) minmax(340px, 460px);
  }

  .auth-card {
    padding: 34px 30px 28px;
  }

  .entry-title {
    font-size: 48px;
  }
}

@media (max-width: 1080px) {
  .register {
    padding: 28px 20px 72px;
  }

  .entry-grid {
    grid-template-columns: 1fr;
    gap: 28px;
    min-height: auto;
  }

  .entry-hero {
    max-width: none;
  }

  .entry-title {
    max-width: 620px;
    font-size: 44px;
  }

  .auth-card {
    max-width: 560px;
    justify-self: stretch;
  }
}

@media (max-width: 640px) {
  .register {
    padding: 18px 14px 62px;
  }

  .entry-badge {
    min-height: 36px;
    padding: 0 16px;
    font-size: 12px;
    letter-spacing: 0.14em;
  }

  .entry-brand {
    margin-top: 20px;
  }

  .entry-brand-icon {
    width: 42px;
    height: 42px;
    border-radius: var(--ui-radius-control);

    .brand-svg {
      width: 22px;
      height: 22px;
    }
  }

  .entry-brand-text {
    font-size: 20px;
  }

  .entry-title {
    margin-top: 22px;
    font-size: 42px;
  }

  .entry-description {
    margin-top: 20px;
    font-size: 16px;
    line-height: 1.8;
  }

  .entry-tags {
    gap: 10px;
    margin-top: 26px;
  }

  .entry-tag {
    min-height: 42px;
    padding: 0 18px;
    font-size: 15px;
  }

  .auth-card {
    padding: 28px 22px 24px;
    border-radius: var(--ui-radius-panel);
  }

  .auth-card__eyebrow {
    font-size: 15px;
  }

  .auth-card__title {
    margin-top: 12px;
    font-size: 36px;
  }

  .auth-card__description {
    margin-top: 14px;
    font-size: 15px;
  }

  .auth-form {
    margin-top: 24px;

    :deep(.el-input__wrapper) {
      min-height: 58px;
      border-radius: var(--ui-radius-control);
    }

    :deep(.el-input__inner) {
      height: 58px;
      font-size: 18px;
    }
  }

  .auth-code-row {
    grid-template-columns: minmax(0, 1fr) 118px;
    gap: 10px;
  }

  .auth-code-box {
    min-height: 58px;
    border-radius: var(--ui-radius-control);
  }

  .auth-code-img {
    max-height: 38px;
  }

  .auth-submit {
    min-height: 60px;
    border-radius: var(--ui-radius-control);
    font-size: 22px;
  }

  .auth-switch {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .auth-switch__label,
  .auth-link {
    font-size: 14px;
  }
}
</style>
