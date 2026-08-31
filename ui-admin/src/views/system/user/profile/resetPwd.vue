<template>
  <a-form ref="pwdRef" :model="user" :rules="rules" :label-col-props="{ flex: '80px' }">
    <a-form-item label="旧密码" field="oldPassword">
      <a-input-password v-model="user.oldPassword" placeholder="请输入旧密码"  />
    </a-form-item>
    <a-form-item label="新密码" field="newPassword" :rules="infoPwdValidator">
      <a-input-password v-model="user.newPassword" placeholder="请输入新密码"  />
    </a-form-item>
    <a-form-item label="确认密码" field="confirmPassword">
      <a-input-password v-model="user.confirmPassword" placeholder="请确认新密码"  />
    </a-form-item>
    <a-form-item>
      <a-button type="primary" @click="submit">保存</a-button>
      <a-button status="danger" @click="close">关闭</a-button>
    </a-form-item>
  </a-form>
</template>

<script setup>
import { usePasswordRule } from "@/utils/passwordRule"
import { updateUserPwd } from "@/api/system/user"
import useUserStore from "@/store/modules/user"

const { proxy } = getCurrentInstance()
const { infoPwdValidator } = usePasswordRule()
const userStore = useUserStore()

const user = reactive({
  oldPassword: undefined,
  newPassword: undefined,
  confirmPassword: undefined
})

const equalToPassword = (value, callback) => {
  if (user.newPassword !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const rules = ref({
  oldPassword: [{ required: true, message: "旧密码不能为空", trigger: "blur" }],
  confirmPassword: [{ required: true, message: "确认密码不能为空", trigger: "blur" }, { required: true, validator: equalToPassword, trigger: "blur" }]
})

/** 提交按钮 */
function submit() {
  proxy.$refs.pwdRef.validate(errors => {
    if (!errors) {
      updateUserPwd(user.oldPassword, user.newPassword).then(() => {
        userStore.setPasswordSecurityState(false, false)
        proxy.$modal.msgSuccess("修改成功")
      })
    }
  })
}

/** 关闭按钮 */
function close() {
  proxy.$tab.closePage()
}
</script>
