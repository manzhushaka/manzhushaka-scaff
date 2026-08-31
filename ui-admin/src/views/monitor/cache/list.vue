<template>
  <div class="app-container">
    <a-row :gutter="12" class="cache-list-grid">
      <a-col :xs="24" :sm="24" :lg="8">
        <a-card class="cache-list-card">
          <template #title>
            <div class="cache-list-card__header">
              <span><Collection class="cache-list-card__icon" />缓存列表</span>
            <a-button



              @click="refreshCacheNames()"
            ><template #icon><Refresh /></template></a-button>
            </div>
          </template>
          <a-table
            :loading="loading"
            :data="cacheNames"
            :row-key="record => record.cacheName"
            :scroll="{ y: tableHeight }"
            highlight-current-row
            @row-click="getCacheKeys"
           :pagination="false">
            <a-table-column
              title="序号"
              width="60"
            >
              <template #cell="{ rowIndex }">{{ rowIndex + 1 }}</template>
            </a-table-column>

            <a-table-column
              title="缓存名称"
              align="center"
              data-index="cacheName"
              ellipsis
             tooltip>
              <template #cell="{ record }">{{ nameFormatter(record) }}</template>
            </a-table-column>

            <a-table-column
              title="备注"
              align="center"
              data-index="remark"
              ellipsis
 tooltip />
            <a-table-column
              title="操作"
              width="60"
              align="center"
              cell-class="small-padding fixed-width"
            >
              <template #cell="{ record, rowIndex }">
                <a-button



                  @click="handleClearCacheName(record)"
                ><template #icon><Delete /></template></a-button>
              </template>
            </a-table-column>
          </a-table>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="24" :lg="8">
        <a-card class="cache-list-card">
          <template #title>
            <div class="cache-list-card__header">
              <span><Key class="cache-list-card__icon" />键名列表</span>
            <a-button



              @click="refreshCacheKeys()"
            ><template #icon><Refresh /></template></a-button>
            </div>
          </template>
          <a-table
            :loading="subLoading"
            :data="cacheKeys"
            :row-key="record => record"
            :scroll="{ y: tableHeight }"
            highlight-current-row
            @row-click="handleCacheValue"
           :pagination="false">
            <a-table-column
              title="序号"
              width="60"
            >
              <template #cell="{ rowIndex }">{{ rowIndex + 1 }}</template>
            </a-table-column>
            <a-table-column
              title="缓存键名"
              align="center"
              ellipsis
             tooltip>
              <template #cell="{ record }">{{ keyFormatter(record) }}</template>
            </a-table-column>
            <a-table-column
              title="操作"
              width="60"
              align="center"
              cell-class="small-padding fixed-width"
            >
              <template #cell="{ record, rowIndex }">
                <a-button



                  @click="handleClearCacheKey(record)"
                ><template #icon><Delete /></template></a-button>
              </template>
            </a-table-column>
          </a-table>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="24" :lg="8">
        <a-card class="cache-list-card">
          <template #title>
            <div class="cache-list-card__header">
              <span><Document class="cache-list-card__icon" />缓存内容</span>
            <a-button



              @click="handleClearCacheAll()"
              ><template #icon><Refresh /></template>清理全部</a-button
            >
            </div>
          </template>
          <a-form :model="cacheForm">
            <a-row :gutter="32">
              <a-col :offset="1" :span="22">
                <a-form-item label="缓存名称:" field="cacheName">
                  <a-input v-model="cacheForm.cacheName" :readOnly="true" />
                </a-form-item>
              </a-col>
              <a-col :offset="1" :span="22">
                <a-form-item label="缓存键名:" field="cacheKey">
                  <a-input v-model="cacheForm.cacheKey" :readOnly="true" />
                </a-form-item>
              </a-col>
              <a-col :offset="1" :span="22">
                <a-form-item label="缓存内容:" field="cacheValue">
                  <a-textarea
                    v-model="cacheForm.cacheValue"

                    :rows="8"
                    :readOnly="true"
                   />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup name="CacheList">
import { listCacheName, listCacheKey, getCacheValue, clearCacheName, clearCacheKey, clearCacheAll } from "@/api/monitor/cache"

const { proxy } = getCurrentInstance()

const cacheNames = ref([])
const cacheKeys = ref([])
const cacheForm = ref({})
const loading = ref(true)
const subLoading = ref(false)
const nowCacheName = ref("")
const tableHeight = ref(window.innerHeight - 200)

/** 查询缓存名称列表 */
function getCacheNames() {
  loading.value = true
  listCacheName().then(response => {
    cacheNames.value = response.data
    loading.value = false
  })
}

/** 刷新缓存名称列表 */
function refreshCacheNames() {
  getCacheNames()
  proxy.$modal.msgSuccess("刷新缓存列表成功")
}

/** 清理指定名称缓存 */
function handleClearCacheName(row) {
  clearCacheName(row.cacheName).then(response => {
    proxy.$modal.msgSuccess("清理缓存名称[" + row.cacheName + "]成功")
    getCacheKeys()
  })
}

/** 查询缓存键名列表 */
function getCacheKeys(row) {
  const cacheName = row !== undefined ? row.cacheName : nowCacheName.value
  if (cacheName === "") {
    return
  }
  subLoading.value = true
  listCacheKey(cacheName).then(response => {
    cacheKeys.value = response.data
    subLoading.value = false
    nowCacheName.value = cacheName
  })
}

/** 刷新缓存键名列表 */
function refreshCacheKeys() {
  getCacheKeys()
  proxy.$modal.msgSuccess("刷新键名列表成功")
}

/** 清理指定键名缓存 */
function handleClearCacheKey(cacheKey) {
  clearCacheKey(cacheKey).then(response => {
    proxy.$modal.msgSuccess("清理缓存键名[" + cacheKey + "]成功")
    getCacheKeys()
  })
}

/** 列表前缀去除 */
function nameFormatter(row) {
  return row.cacheName.replace(":", "")
}

/** 键名前缀去除 */
function keyFormatter(cacheKey) {
  return cacheKey.replace(nowCacheName.value, "")
}

/** 查询缓存内容详细 */
function handleCacheValue(cacheKey) {
  getCacheValue(nowCacheName.value, cacheKey).then(response => {
    cacheForm.value = response.data
  })
}

/** 清理全部缓存 */
function handleClearCacheAll() {
  clearCacheAll().then(response => {
    proxy.$modal.msgSuccess("清理全部缓存成功")
  })
}

getCacheNames()
</script>

<style lang="scss" scoped>
.cache-list-grid {
  min-height: calc(100vh - 126px);
}

.cache-list-card {
  height: calc(100vh - 126px);
  overflow: hidden;

  :deep(.arco-card-header) {
    padding: 0 !important;
  }

  :deep(.arco-card-body) {
    height: calc(100% - 45px);
    padding: 12px !important;
    overflow: auto;
  }
}

.cache-list-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 44px;
  padding: 0 14px;

  span {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    color: var(--ui-text-primary);
    font-weight: 700;
  }
}

.cache-list-card__icon {
  width: 16px;
  height: 16px;
  color: var(--ui-primary);
}

@media (max-width: 1199px) {
  .cache-list-card {
    height: auto;
    min-height: 360px;
    margin-bottom: 12px;
  }
}
</style>
