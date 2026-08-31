<template>
  <div :class="{ 'hidden': hidden }" class="pagination-container">
    <a-pagination
      v-model:current="currentPage"
      v-model:page-size="pageSize"
      :page-size-options="pageSizes"
      :total="total"
      :show-total="showTotal"
      :show-page-size="showPageSize"
      :show-jumper="showJumper"
      :simple="isMobile"
      size="small"
      @page-size-change="handleSizeChange"
      @change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { useWindowSize } from '@vueuse/core'
import { scrollTo } from '@/utils/scroll-to'

const props = defineProps({
  total: {
    required: true,
    type: Number
  },
  page: {
    type: Number,
    default: 1
  },
  limit: {
    type: Number,
    default: 20
  },
  pageSizes: {
    type: Array,
    default() {
      return [10, 20, 30, 50]
    }
  },
  // 移动端页码按钮的数量端默认值5
  pagerCount: {
    type: Number,
    default: document.body.clientWidth < 992 ? 5 : 7
  },
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  background: {
    type: Boolean,
    default: true
  },
  autoScroll: {
    type: Boolean,
    default: true
  },
  hidden: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits()
const { width } = useWindowSize()
const isMobile = computed(() => width.value < 640)
const showTotal = computed(() => props.layout.includes('total'))
const showPageSize = computed(() => props.layout.includes('sizes'))
const showJumper = computed(() => props.layout.includes('jumper'))
const currentPage = computed({
  get() {
    return props.page
  },
  set(val) {
    emit('update:page', val)
  }
})
const pageSize = computed({
  get() {
    return props.limit
  },
  set(val){
    emit('update:limit', val)
  }
})

function handleSizeChange(val) {
  if (currentPage.value * val > props.total) {
    currentPage.value = 1
  }
  emit('pagination', { page: currentPage.value, limit: val })
  if (props.autoScroll) {
    scrollTo(0, 800)
  }
}

function handleCurrentChange(val) {
  emit('pagination', { page: val, limit: pageSize.value })
  if (props.autoScroll) {
    scrollTo(0, 800)
  }
}
</script>

<style scoped>
.pagination-container {
  min-height: 48px;
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 4px;
}
.pagination-container.hidden {
  display: none;
}

@media (max-width: 768px) {
  .pagination-container {
    justify-content: flex-start;
    overflow-x: auto;
  }
}
</style>
