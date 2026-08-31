<template>
  <div class="user-info-head" @click="editCropper()">
    <img :src="options.img" title="点击上传头像" class="img-circle img-lg" />
    <a-modal :title="title" v-model:visible="open" width="800px" render-to-body :footer="false" @open="modalOpened" @close="closeDialog">
      <a-row>
        <a-col :xs="24" :md="12" :style="{ height: '350px' }">
          <vue-cropper
            ref="cropper"
            :img="options.img"
            :info="true"
            :autoCrop="options.autoCrop"
            :autoCropWidth="options.autoCropWidth"
            :autoCropHeight="options.autoCropHeight"
            :fixedBox="options.fixedBox"
            :outputType="options.outputType"
            @realTime="realTime"
            v-if="visible"
          />
        </a-col>
        <a-col :xs="24" :md="12" :style="{ height: '350px' }">
          <div class="avatar-upload-preview">
            <img :src="options.previews.url" :style="options.previews.img" />
          </div>
        </a-col>
      </a-row>
      <br />
      <a-row>
        <a-col :lg="2" :md="2">
          <a-upload
            :auto-upload="false"
            :show-file-list="false"
            :before-upload="beforeUpload"
            @change="handleAvatarChange"
          >
            <a-button>
              选择
              <span class="button-icon-right"><Upload /></span>
            </a-button>
          </a-upload>
        </a-col>
        <a-col :lg="{ span: 1, offset: 2 }" :md="2">
          <a-button @click="changeScale(1)"><template #icon><Plus /></template></a-button>
        </a-col>
        <a-col :lg="{ span: 1, offset: 1 }" :md="2">
          <a-button @click="changeScale(-1)"><template #icon><Minus /></template></a-button>
        </a-col>
        <a-col :lg="{ span: 1, offset: 1 }" :md="2">
          <a-button @click="rotateLeft()"><template #icon><RefreshLeft /></template></a-button>
        </a-col>
        <a-col :lg="{ span: 1, offset: 1 }" :md="2">
          <a-button @click="rotateRight()"><template #icon><RefreshRight /></template></a-button>
        </a-col>
        <a-col :lg="{ span: 2, offset: 6 }" :md="2">
          <a-button type="primary" @click="uploadImg()">提 交</a-button>
        </a-col>
      </a-row>
    </a-modal>
  </div>
</template>

<script setup>
import "vue-cropper/dist/index.css"
import { VueCropper } from "vue-cropper"
import { uploadAvatar } from "@/api/system/user"
import useUserStore from "@/store/modules/user"

const userStore = useUserStore()
const { proxy } = getCurrentInstance()

const open = ref(false)
const visible = ref(false)
const title = ref("修改头像")

//图片裁剪数据
const options = reactive({
  img: userStore.avatar,     // 裁剪图片的地址
  autoCrop: true,            // 是否默认生成截图框
  autoCropWidth: 200,        // 默认生成截图框宽度
  autoCropHeight: 200,       // 默认生成截图框高度
  fixedBox: true,            // 固定截图框大小 不允许改变
  outputType: "png",         // 默认生成截图为PNG格式
  filename: 'avatar',        // 文件名称
  previews: {}               //预览数据
})

/** 编辑头像 */
function editCropper() {
  open.value = true
}

/** 打开弹出层结束时的回调 */
function modalOpened() {
  visible.value = true
}

/** 向左旋转 */
function rotateLeft() {
  proxy.$refs.cropper.rotateLeft()
}

/** 向右旋转 */
function rotateRight() {
  proxy.$refs.cropper.rotateRight()
}

/** 图片缩放 */
function changeScale(num) {
  num = num || 1
  proxy.$refs.cropper.changeScale(num)
}

/** 上传预处理 */
function beforeUpload(file) {
  if (file.type.indexOf("image/") == -1) {
    proxy.$modal.msgError("文件格式错误，请上传图片类型,如：JPG，PNG后缀的文件。")
    return false
  }
  return true
}

function handleAvatarChange(fileList, fileItem) {
  const file = fileItem?.file
  if (!file) return
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => {
    options.img = reader.result
    options.filename = file.name
  }
}

/** 上传图片 */
function uploadImg() {
  proxy.$refs.cropper.getCropBlob(data => {
    let formData = new FormData()
    formData.append("avatarfile", data, options.filename)
    uploadAvatar(formData).then(response => {
      open.value = false
      options.img = import.meta.env.VITE_APP_BASE_API + response.imgUrl
      userStore.avatar = options.img
      proxy.$modal.msgSuccess("修改成功")
      visible.value = false
    })
  })
}

/** 实时预览 */
function realTime(data) {
  options.previews = data
}

/** 关闭窗口 */
function closeDialog() {
  options.img = userStore.avatar
  options.visible = false
}
</script>

<style lang='scss' scoped>
.user-info-head {
  position: relative;
  display: inline-block;
  height: 120px;
}

.user-info-head:hover:after {
  content: "+";
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  color: var(--ui-text-inverse);
  background: rgba(0, 0, 0, 0.5);
  font-size: 24px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  cursor: pointer;
  line-height: 110px;
  border-radius: 50%;
}
</style>
