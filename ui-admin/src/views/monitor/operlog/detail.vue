<template>
  <a-modal title="操作日志详细" v-model:visible="dialogVisible" width="780px" render-to-body :footer="false" @close="$emit('update:visible', false)">
    <div class="detail-wrap">
      <!-- 基本信息 -->
      <div class="detail-card">
        <div class="detail-card-title"><span><InfoFilled /></span> 基本信息</div>
        <a-row class="detail-row">
          <a-col :span="12">
            <div class="detail-item"><span class="detail-label">操作模块</span><span class="detail-value">{{ form.title }}</span></div>
          </a-col>
          <a-col :span="12">
            <div class="detail-item"><span class="detail-label">业务类型</span><span class="detail-value">{{ typeLabel }}</span></div>
          </a-col>
        </a-row>
        <a-row class="detail-row">
          <a-col :span="12">
            <div class="detail-item"><span class="detail-label">操作时间</span><span class="detail-value">{{ form.operTime }}</span></div>
          </a-col>
          <a-col :span="12">
            <div class="detail-item">
              <span class="detail-label">执行状态</span>
              <a-tag v-if="form.status === 0" color="green" size="small">正常</a-tag>
              <a-tag v-else color="red" size="small">异常</a-tag>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 操作人员 -->
      <div class="detail-card">
        <div class="detail-card-title"><span><User /></span> 操作人员</div>
        <a-row class="detail-row">
          <a-col :span="12">
            <div class="detail-item"><span class="detail-label">操作人员</span><span class="detail-value">{{ form.operName }}</span></div>
          </a-col>
          <a-col :span="12" v-if="form.deptName">
            <div class="detail-item"><span class="detail-label">所属部门</span><span class="detail-value">{{ form.deptName }}</span></div>
          </a-col>
        </a-row>
        <a-row class="detail-row">
          <a-col :span="24">
            <div class="detail-item">
              <span class="detail-label">操作地址</span>
              <span class="detail-value">{{ form.operIp }}&nbsp;&nbsp;<span class="detail-location">{{ form.operLocation }}</span></span>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 请求信息 -->
      <div class="detail-card">
        <div class="detail-card-title"><span><Sort /></span> 请求信息</div>
        <a-row class="detail-row">
          <a-col :span="24">
            <div class="detail-item">
              <span class="detail-label">请求地址</span>
              <span class="detail-value">
                <span :class="'method-tag method-' + form.requestMethod">{{ form.requestMethod }}</span>
                {{ form.operUrl }}
              </span>
            </div>
          </a-col>
        </a-row>
        <a-row class="detail-row">
          <a-col :span="24">
            <div class="detail-item"><span class="detail-label">操作方法</span><span class="detail-value mono">{{ form.method }}</span></div>
          </a-col>
        </a-row>
        <a-row class="detail-row">
          <a-col :span="12">
            <div class="detail-item"><span class="detail-label">消耗时间</span><span class="detail-value">{{ form.costTime }} 毫秒</span></div>
          </a-col>
        </a-row>
      </div>

      <!-- 请求参数 -->
      <div class="detail-card">
        <div class="detail-card-title"><span><Upload /></span> 请求参数</div>
        <div class="code-body">
          <div class="code-wrap">
            <div class="code-action">
              <a-button size="small" :icon="CopyDocument" @click="copyText(form.operParam)">复制</a-button>
            </div>
            <pre class="code-pre">{{ formatJson(form.operParam) }}</pre>
          </div>
        </div>
      </div>

      <!-- 返回参数 -->
      <div class="detail-card">
        <div class="detail-card-title"><span><Download /></span> 返回参数</div>
        <div class="code-body">
          <div class="code-wrap">
            <div class="code-action">
              <a-button size="small" :icon="CopyDocument" @click="copyText(form.jsonResult)">复制</a-button>
            </div>
            <pre class="code-pre">{{ formatJson(form.jsonResult) }}</pre>
          </div>
        </div>
      </div>

      <!-- 异常信息 -->
      <div class="detail-card" v-if="form.status !== 0">
        <div class="detail-card-title error-title"><span><Warning /></span> 异常信息</div>
        <div class="error-body">
          <div class="error-msg">{{ form.errorMsg }}</div>
        </div>
      </div>

    </div>
  </a-modal>
</template>

<script setup>
import { Message } from '@arco-design/web-vue'
const props = defineProps({
  visible: { type: Boolean, default: false },
  row: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})


const { sys_oper_type } = useDict('sys_oper_type')

const form = computed(() => props.row || {})
const typeLabel = computed(() => selectDictLabel(sys_oper_type.value, form.value.businessType) || '-')

function formatJson(str) {
  if (!str) return '（无数据）'
  try { return JSON.stringify(JSON.parse(str), null, 2) } catch { return str }
}

function copyText(str) {
  const text = formatJson(str)
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text).then(() => Message.success({ content: '已复制', duration: 1500 }))
  } else {
    const ta = document.createElement('textarea')
    ta.value = text
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
    Message.success({ content: '已复制', duration: 1500 })
  }
}
</script>
