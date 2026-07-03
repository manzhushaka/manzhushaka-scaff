<template>
  <el-tree-select
    v-model="selectedValue"
    :data="deptOptions"
    :props="{ value: 'id', label: 'label', children: 'children' }"
    value-key="id"
    :placeholder="placeholder"
    :clearable="clearable"
    :check-strictly="checkStrictly"
    :filterable="filterable"
    :loading="loading"
    style="width: 100%"
  />
</template>

<script setup name="DeptTreeSelect">
import { deptTree } from '@/api/system/dept'

const props = defineProps({
  modelValue: {
    type: [Number, String],
    default: undefined
  },
  deptType: {
    type: String,
    default: 'region'
  },
  placeholder: {
    type: String,
    default: '请选择区域'
  },
  clearable: {
    type: Boolean,
    default: true
  },
  checkStrictly: {
    type: Boolean,
    default: true
  },
  filterable: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'loaded'])
const selectedValue = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const deptOptions = ref([])
const loading = ref(false)

function loadDeptTree() {
  loading.value = true
  deptTree({ deptType: props.deptType, status: '0' }).then(response => {
    deptOptions.value = response.data || []
    emit('loaded', deptOptions.value)
  }).finally(() => {
    loading.value = false
  })
}

watch(() => props.deptType, loadDeptTree)
loadDeptTree()
</script>
