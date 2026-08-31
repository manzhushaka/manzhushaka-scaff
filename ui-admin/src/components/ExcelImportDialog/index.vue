<template>
  <a-modal :title="title" v-model:visible="visible" :width="width" render-to-body @close="handleClose">
    <a-upload ref="uploadRef" v-model:file-list="fileList" :limit="1" accept=".xlsx, .xls" :headers="headers" :action="uploadUrl" :disabled="isUploading" :auto-upload="false" draggable @progress="handleProgress" @change="handleFileChange" @success="handleSuccess" @error="handleError">
      <span class="upload-icon"><upload-filled /></span>
      <div class="upload-text">将文件拖到此处，或<em>点击上传</em></div>
      <template #tip>
        <div class="upload-tip text-center">
          <div class="upload-tip">
            <a-checkbox v-model="updateSupport"> {{ updateSupportLabel }} </a-checkbox>
          </div>
          <span>仅允许导入xls、xlsx格式文件。</span>
          <a-link v-if="templateUrl" style="font-size: 12px; vertical-align: baseline" @click="handleDownloadTemplate">下载模板</a-link>
        </div>
      </template>
    </a-upload>
    <template #footer>
      <div class="dialog-footer">
        <a-button type="primary" @click="handleSubmit">确 定</a-button>
        <a-button @click="visible = false">取 消</a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup>
import { getToken } from '@/utils/auth'

const { proxy } = getCurrentInstance()

const props = defineProps({
  // 对话框标题
  title: {
    type: String,
    default: '数据导入'
  },
  // 对话框宽度
  width: {
    type: String,
    default: '400px'
  },
  // 上传接口地址（必传）
  action: {
    type: String,
    required: true
  },
  // 模板下载接口地址，不传则不显示下载模板链接
  templateAction: {
    type: String,
    default: ''
  },
  // 模板文件名前缀
  templateFileName: {
    type: String,
    default: 'template'
  },
  // 覆盖更新勾选框的说明文字
  updateSupportLabel: {
    type: String,
    default: '是否更新已经存在的数据'
  }
})

const emit = defineEmits(['success'])

const uploadRef = ref(null)
const visible = ref(false)
const selectedFile = ref(null)
const fileList = ref([])
const isUploading = ref(false)
const updateSupport = ref(false)
const headers = { Authorization: 'Bearer ' + getToken() }

const uploadUrl = computed(() => {
  return import.meta.env.VITE_APP_BASE_API + props.action + '?updateSupport=' + (updateSupport.value ? 1 : 0)
})

const templateUrl = computed(() => !!props.templateAction)

// 打开对话框（供父组件通过 ref 调用）
function open() {
  updateSupport.value = false
  isUploading.value = false
  visible.value = true
  nextTick(() => {
    selectedFile.value = null
    fileList.value = []
  })
}

// 关闭时清理
function handleClose() {
  isUploading.value = false
  selectedFile.value = null
  fileList.value = []
}

// 下载模板
function handleDownloadTemplate() {
  proxy.download(props.templateAction, {}, `${props.templateFileName}_${new Date().getTime()}.xlsx`)
}

// 上传进度
function handleProgress() {
  isUploading.value = true
}

/** 文件选择处理 */
const handleFileChange = (files, file) => {
  selectedFile.value = file?.file ? file : null
}

// 上传成功
function handleSuccess(file) {
  const response = file.response
  visible.value = false
  isUploading.value = false
  selectedFile.value = null
  fileList.value = []
  proxy.$modal.alert(response.msg.replace(/<[^>]*>/g, ' '))
  emit('success')
}

// 上传失败
function handleError(error) {
  isUploading.value = false
  const message = error?.message || '上传失败，请稍后重试'
  proxy.$modal.msgError(message)
}

// 提交上传
function handleSubmit() {
  const file = selectedFile.value
  if (!file || file.length === 0 || !file.name.toLowerCase().endsWith('.xls') && !file.name.toLowerCase().endsWith('.xlsx')) {
    proxy.$modal.msgError("请选择后缀为 “xls”或“xlsx”的文件。")
    return
  }
  uploadRef.value.submit()
}

defineExpose({ open })
</script>
