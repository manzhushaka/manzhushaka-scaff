<template>
  <el-select
    v-model="selectedValue"
    filterable
    placeholder="请选择税目"
    style="width: 100%"
    @change="value => emit('change', value)"
  >
    <el-option
      v-for="item in items"
      :key="item.id"
      :label="taxItemLabel(item)"
      :value="item.id"
    />
  </el-select>
</template>

<script setup name="TaxItemSelector">
const props = defineProps({
  modelValue: {
    type: [Number, String],
    default: undefined
  },
  items: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'change'])
const selectedValue = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

function taxItemLabel(item) {
  const name = item.name || item.taxItemName || item.taxItemCode || item.id
  const rate = item.taxRate === undefined || item.taxRate === null ? '-' : item.taxRate
  return `${name} (税率 ${rate}%)`
}
</script>
