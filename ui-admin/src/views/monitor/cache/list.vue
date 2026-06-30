<template>
  <div class="app-container">
    <el-row :gutter="12" class="cache-list-grid">
      <el-col :xs="24" :sm="24" :lg="8">
        <el-card class="cache-list-card">
          <template #header>
            <div class="cache-list-card__header">
              <span><Collection class="cache-list-card__icon" />缓存列表</span>
            <el-button
              link
              type="primary"
              icon="Refresh"
              @click="refreshCacheNames()"
            ></el-button>
            </div>
          </template>
          <el-table
            v-loading="loading"
            :data="cacheNames"
            :height="tableHeight"
            highlight-current-row
            @row-click="getCacheKeys"
          >
            <el-table-column
              label="序号"
              width="60"
              type="index"
            ></el-table-column>

            <el-table-column
              label="缓存名称"
              align="center"
              prop="cacheName"
              :show-overflow-tooltip="true"
              :formatter="nameFormatter"
            ></el-table-column>

            <el-table-column
              label="备注"
              align="center"
              prop="remark"
              :show-overflow-tooltip="true"
            />
            <el-table-column
              label="操作"
              width="60"
              align="center"
              class-name="small-padding fixed-width"
            >
              <template #default="scope">
                <el-button
                  link
                  type="primary"
                  icon="Delete"
                  @click="handleClearCacheName(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :lg="8">
        <el-card class="cache-list-card">
          <template #header>
            <div class="cache-list-card__header">
              <span><Key class="cache-list-card__icon" />键名列表</span>
            <el-button
              link
              type="primary"
              icon="Refresh"
              @click="refreshCacheKeys()"
            ></el-button>
            </div>
          </template>
          <el-table
            v-loading="subLoading"
            :data="cacheKeys"
            :height="tableHeight"
            highlight-current-row
            @row-click="handleCacheValue"
          >
            <el-table-column
              label="序号"
              width="60"
              type="index"
            ></el-table-column>
            <el-table-column
              label="缓存键名"
              align="center"
              :show-overflow-tooltip="true"
              :formatter="keyFormatter"
            >
            </el-table-column>
            <el-table-column
              label="操作"
              width="60"
              align="center"
              class-name="small-padding fixed-width"
            >
              <template #default="scope">
                <el-button
                  link
                  type="primary"
                  icon="Delete"
                  @click="handleClearCacheKey(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :lg="8">
        <el-card class="cache-list-card">
          <template #header>
            <div class="cache-list-card__header">
              <span><Document class="cache-list-card__icon" />缓存内容</span>
            <el-button
              link
              type="primary"
              icon="Refresh"
              @click="handleClearCacheAll()"
              >清理全部</el-button
            >
            </div>
          </template>
          <el-form :model="cacheForm">
            <el-row :gutter="32">
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存名称:" prop="cacheName">
                  <el-input v-model="cacheForm.cacheName" :readOnly="true" />
                </el-form-item>
              </el-col>
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存键名:" prop="cacheKey">
                  <el-input v-model="cacheForm.cacheKey" :readOnly="true" />
                </el-form-item>
              </el-col>
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存内容:" prop="cacheValue">
                  <el-input
                    v-model="cacheForm.cacheValue"
                    type="textarea"
                    :rows="8"
                    :readOnly="true"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
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

  :deep(.el-card__header) {
    padding: 0 !important;
  }

  :deep(.el-card__body) {
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
