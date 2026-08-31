<template>
  <a-spin :loading="loading" tip="正在加载页面，请稍候！" :style="'height:' + height">
    <iframe
      :id="iframeId"
      style="width: 100%; height: 100%"
      :src="src"
      ref="iframeRef"
      frameborder="no"
    ></iframe>
  </a-spin>
</template>

<script setup>
const props = defineProps({
  src: {
    type: String,
    default: "/"
  },
  iframeId: {
    type: String
  }
})

const loading = ref(true)
const height = ref(document.documentElement.clientHeight - 94.5 + 'px')
const iframeRef = ref(null)

onMounted(() => {
  if (iframeRef.value) {
    iframeRef.value.onload = () => {
      loading.value = false
    }
  }
})
</script>
