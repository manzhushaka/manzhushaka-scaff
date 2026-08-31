<template>
  <a-modal :title="type === 'log' ? '调度日志详细' : '任务详细'" v-model:visible="dialogVisible" width="780px" render-to-body>
    <div class="detail-wrap">
      <template v-if="type === 'log'">
        <!-- 基本信息 -->
        <div class="detail-card">
          <div class="detail-card-title">
            <span><InfoFilled /></span> 基本信息
          </div>
          <a-row class="detail-row">
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">日志编号</span><span class="detail-value">{{ form.jobLogId }}</span></div>
            </a-col>
            <a-col :span="12">
              <div class="detail-item">
                <span class="detail-label">执行状态</span>
                <a-tag v-if="form.status == 0" color="green" size="small">正常</a-tag>
                <a-tag v-else color="red" size="small">失败</a-tag>
              </div>
            </a-col>
          </a-row>
          <a-row class="detail-row">
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">开始时间</span><span class="detail-value">{{ form.startTime }}</span></div>
            </a-col>
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">结束时间</span><span class="detail-value">{{ form.endTime }}</span></div>
            </a-col>
          </a-row>
          <a-row class="detail-row">
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">记录时间</span><span class="detail-value">{{ form.createTime }}</span></div>
            </a-col>
            <a-col :span="12" v-if="form.status == 0 && form.startTime && form.endTime">
              <div class="detail-item"><span class="detail-label">执行耗时</span><span class="detail-value">{{ costTime }} 毫秒</span></div>
            </a-col>
          </a-row>
        </div>
        <!-- 任务信息 -->
        <div class="detail-card">
          <div class="detail-card-title">
            <span><Clock /></span> 任务信息
          </div>
          <a-row class="detail-row">
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">任务名称</span><span class="detail-value">{{ form.jobName }}</span></div>
            </a-col>
            <a-col :span="12">
              <div class="detail-item">
                <span class="detail-label">任务分组</span>
                <dict-tag :options="sys_job_group" :value="form.jobGroup" />
              </div>
            </a-col>
          </a-row>
          <a-row class="detail-row">
            <a-col :span="24">
              <div class="detail-item"><span class="detail-label">日志信息</span><span class="detail-value">{{ form.jobMessage }}</span></div>
            </a-col>
          </a-row>
        </div>
        <!-- 调用目标 -->
        <div class="detail-card">
          <div class="detail-card-title">
            <span><Operation /></span> 调用目标
          </div>
          <div class="code-body">
            <div class="code-wrap"><pre class="code-pre">{{ form.invokeTarget || '（无）' }}</pre></div>
          </div>
        </div>
        <!-- 过程日志 -->
        <div class="detail-card">
          <div class="detail-card-title">
            <span><Document /></span> 过程日志
          </div>
          <a-spin :loading="processLogLoading" class="process-log-wrap">
            <a-empty v-if="processLogList.length === 0" :image-size="80" description="暂无过程日志" />
            <div v-else class="process-log-list">
              <div v-for="item in processLogList" :key="item.detailId || item.sortNo" class="process-log-item">
                <a-tag :color="getLogLevelColor(item.logLevel)" size="small" class="process-log-level">
                  {{ item.logLevel }}
                </a-tag>
                <pre class="process-log-content">{{ item.logContent }}</pre>
              </div>
            </div>
          </a-spin>
        </div>
        <!-- 异常信息 -->
        <div class="detail-card" v-if="form.status == 1">
          <div class="detail-card-title error-title">
            <span><Warning /></span> 异常信息
          </div>
          <div class="error-body"><div class="error-msg">{{ form.exceptionInfo }}</div></div>
        </div>
      </template>

      <template v-else>
        <!-- 任务配置 -->
        <div class="detail-card">
          <div class="detail-card-title">
            <span><Setting /></span> 任务配置
          </div>
          <a-row class="detail-row">
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">任务编号</span><span class="detail-value">{{ form.jobId }}</span></div>
            </a-col>
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">任务名称</span><span class="detail-value">{{ form.jobName }}</span></div>
            </a-col>
          </a-row>
          <a-row class="detail-row">
            <a-col :span="12">
              <div class="detail-item">
                <span class="detail-label">任务分组</span>
                <dict-tag :options="sys_job_group" :value="form.jobGroup" />
              </div>
            </a-col>
            <a-col :span="12">
              <div class="detail-item">
                <span class="detail-label">执行状态</span>
                <a-tag v-if="form.status == 0" color="green" size="small">正常</a-tag>
                <a-tag v-else color="gray" size="small">暂停</a-tag>
              </div>
            </a-col>
          </a-row>
        </div>
        <!-- 调度信息 -->
        <div class="detail-card">
          <div class="detail-card-title">
            <span><Calendar /></span> 调度信息
          </div>
          <a-row class="detail-row">
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">cron 表达式</span><span class="detail-value mono">{{ form.cronExpression }}</span></div>
            </a-col>
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">下次执行时间</span><span class="detail-value">{{ parseTime(form.nextValidTime) }}</span></div>
            </a-col>
          </a-row>
          <a-row class="detail-row">
            <a-col :span="12">
              <div class="detail-item">
                <span class="detail-label">执行策略</span>
                <a-tag v-if="form.misfirePolicy == 0" color="gray" size="small">默认策略</a-tag>
                <a-tag v-else-if="form.misfirePolicy == 1" color="orange" size="small">立即执行</a-tag>
                <a-tag v-else-if="form.misfirePolicy == 2" color="arcoblue" size="small">执行一次</a-tag>
                <a-tag v-else-if="form.misfirePolicy == 3" color="red" size="small">放弃执行</a-tag>
              </div>
            </a-col>
            <a-col :span="12">
              <div class="detail-item">
                <span class="detail-label">并发执行</span>
                <a-tag v-if="form.concurrent == 0" color="green" size="small">允许</a-tag>
                <a-tag v-else color="red" size="small">禁止</a-tag>
              </div>
            </a-col>
          </a-row>
        </div>
        <!-- 执行方法 -->
        <div class="detail-card">
          <div class="detail-card-title">
            <span><Operation /></span> 执行方法
          </div>
          <div class="code-body">
            <div class="code-wrap"><pre class="code-pre">{{ form.invokeTarget || '（无）' }}</pre></div>
          </div>
        </div>
        <!-- 元信息 -->
        <div class="detail-card">
          <div class="detail-card-title">
            <span><Document /></span> 元信息
          </div>
          <a-row class="detail-row">
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">创建人</span><span class="detail-value">{{ form.createBy || '-' }}</span></div>
            </a-col>
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">创建时间</span><span class="detail-value">{{ form.createTime }}</span></div>
            </a-col>
          </a-row>
          <a-row class="detail-row">
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">更新人</span><span class="detail-value">{{ form.updateBy || '-' }}</span></div>
            </a-col>
            <a-col :span="12">
              <div class="detail-item"><span class="detail-label">更新时间</span><span class="detail-value">{{ form.updateTime || '-' }}</span></div>
            </a-col>
          </a-row>
          <a-row class="detail-row" v-if="form.remark">
            <a-col :span="24">
              <div class="detail-item"><span class="detail-label">备注</span><span class="detail-value">{{ form.remark }}</span></div>
            </a-col>
          </a-row>
        </div>
      </template>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <a-button @click="dialogVisible = false">关 闭</a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup name="JobDetail">
import { listJobLogDetail } from '@/api/monitor/jobLog'

const props = defineProps({
  visible: { type: Boolean, default: false },
  row: { type: Object, default: () => ({}) },
  // 'job' 任务详细 | 'log' 调度日志详细
  type: { type: String, default: 'job' }
})

const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const { proxy } = getCurrentInstance()
const { sys_job_group } = useDict('sys_job_group')

const form = computed(() => props.row || {})
const processLogLoading = ref(false)
const processLogList = ref([])

const costTime = computed(() => {
  if (!form.value.startTime || !form.value.endTime) return 0
  return new Date(form.value.endTime).getTime() - new Date(form.value.startTime).getTime()
})

watch(
  () => [props.visible, props.type, form.value.jobLogId],
  ([visible, type, jobLogId]) => {
    if (visible && type === 'log' && jobLogId) {
      loadProcessLog(jobLogId)
    } else {
      processLogList.value = []
    }
  },
  { immediate: true }
)

function loadProcessLog(jobLogId) {
  processLogLoading.value = true
  listJobLogDetail(jobLogId).then(response => {
    processLogList.value = response.data || []
  }).finally(() => {
    processLogLoading.value = false
  })
}

function getLogLevelColor(logLevel) {
  if (logLevel === 'ERROR') return 'red'
  if (logLevel === 'WARN') return 'orange'
  return 'gray'
}
</script>

<style scoped>
.detail-label {
  width: 80px;
}

.process-log-wrap {
  min-height: 96px;
}

.process-log-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.process-log-item {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
  padding: 10px 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-fill-color-blank);
}

.process-log-level {
  width: 58px;
  justify-content: center;
}

.process-log-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  font-family: var(--ui-font-family-mono);
  color: var(--el-text-color-primary);
}
</style>
