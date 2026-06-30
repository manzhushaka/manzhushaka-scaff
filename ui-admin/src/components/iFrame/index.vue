<template>
  <div class="iframe-shell" v-loading="loading" :style="'height:' + height">
    <iframe 
      :src="url" 
      frameborder="no" 
      class="iframe-shell__frame"
      scrolling="auto" />
  </div>
</template>

<script setup>
const props = defineProps({
  src: {
    type: String,
    required: true
  }
})

const height = ref(document.documentElement.clientHeight - 94.5 + "px;")
const loading = ref(true)
const url = computed(() => props.src)

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 300)
  window.onresize = function temp() {
    height.value = document.documentElement.clientHeight - 94.5 + "px;"
  }
})
</script>

<style lang="scss" scoped>
.iframe-shell {
  overflow: hidden;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  background: var(--ui-bg-panel);
  box-shadow: var(--ui-shadow-panel);
}

.iframe-shell__frame {
  width: 100%;
  height: 100%;
  display: block;
  background: var(--ui-bg-panel);
}
</style>
