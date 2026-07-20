<template>
   <div class="app-container ui-list-page">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px" class="ui-filter-card">
         <el-form-item label="券名称" prop="couponName">
            <el-input
               v-model="queryParams.couponName"
               placeholder="请输入券名称"
               clearable
               style="width: 180px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="用户ID" prop="memberId">
            <el-input
               v-model="queryParams.memberId"
               placeholder="请输入用户ID"
               clearable
               style="width: 160px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 140px">
               <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="核销码" prop="verifyCode">
            <el-input
               v-model="queryParams.verifyCode"
               placeholder="请输入核销码"
               clearable
               style="width: 180px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="兑换时间" style="width: 340px">
            <el-date-picker
               v-model="dateRange"
               value-format="YYYY-MM-DD HH:mm:ss"
               type="daterange"
               range-separator="-"
               start-placeholder="开始日期"
               end-placeholder="结束日期"
               :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
            ></el-date-picker>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8 ui-action-bar">
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               :loading="exportLoading"
               @click="handleExport"
               v-hasPermi="['iip:exchange:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <div class="ui-table-card">
      <el-table v-loading="loading" :data="exchangeList">
         <el-table-column label="券名称" align="center" prop="couponName" min-width="150" :show-overflow-tooltip="true" />
         <el-table-column label="用户ID" align="center" prop="memberId" width="100" />
         <el-table-column label="消耗积分" align="center" prop="pointsCost" width="90" />
         <el-table-column label="核销码" align="center" prop="verifyCode" width="120" :show-overflow-tooltip="true" />
         <el-table-column label="状态" align="center" prop="status" width="90">
            <template #default="scope">
               <el-tag :type="statusTag(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="兑换时间" align="center" prop="exchangeTime" width="160">
            <template #default="scope">
               <span>{{ parseTime(scope.row.exchangeTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="有效期" align="center" width="190">
            <template #default="scope">
               <template v-if="scope.row.validStartTime && scope.row.validEndTime">
                  <div>{{ parseTime(scope.row.validStartTime, "{y}-{m}-{d}") }}</div>
                  <div>至 {{ parseTime(scope.row.validEndTime, "{y}-{m}-{d}") }}</div>
               </template>
               <span v-else>-</span>
            </template>
         </el-table-column>
         <el-table-column label="核销时间" align="center" prop="verifyTime" width="160">
            <template #default="scope">
               <span>{{ scope.row.verifyTime ? parseTime(scope.row.verifyTime) : "-" }}</span>
            </template>
         </el-table-column>
         <el-table-column label="核销商户" align="center" width="120" :show-overflow-tooltip="true">
            <template #default="scope">
               <span v-if="scope.row.verifyMerchantId">商户ID:{{ scope.row.verifyMerchantId }}</span>
               <span v-else>-</span>
            </template>
         </el-table-column>
         <el-table-column label="作废信息" align="center" width="160" :show-overflow-tooltip="true">
            <template #default="scope">
               <span v-if="scope.row.status === '3'">{{ scope.row.voidReason || '-' }}</span>
               <span v-else>-</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="90" fixed="right">
            <template #default="scope">
               <el-button
                  v-if="scope.row.status === '0'"
                  link
                  type="danger"
                  icon="CircleClose"
                  v-hasPermi="['iip:exchange:void']"
                  @click="handleVoid(scope.row)"
               >作废</el-button>
            </template>
         </el-table-column>
      </el-table>
      </div>

      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />
   </div>
</template>

<script setup name="IipExchange">
import { listExchange, exportExchange, voidExchange } from "@/api/iip/exchange"
import { saveAs } from "file-saver"
import { blobValidate } from "@/utils/manzhushaka"

const { proxy } = getCurrentInstance()

// 券实例状态（与后端 iip_coupon_record.status 一致：0未使用 1已使用 2已过期 3已作废）
const statusOptions = [
  { value: "0", label: "未使用", tag: "success" },
  { value: "1", label: "已使用", tag: "info" },
  { value: "2", label: "已过期", tag: "warning" },
  { value: "3", label: "已作废", tag: "danger" }
]

const exchangeList = ref([])
const loading = ref(true)
const exportLoading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    couponName: undefined,
    memberId: undefined,
    status: undefined,
    verifyCode: undefined
  }
})

const { queryParams } = toRefs(data)

function statusLabel(status) {
  const item = statusOptions.find(option => option.value === status)
  return item ? item.label : status
}

function statusTag(status) {
  const item = statusOptions.find(option => option.value === status)
  return item ? item.tag : "info"
}

/** 查询兑换记录列表 */
function getList() {
  loading.value = true
  listExchange(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    exchangeList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 作废未使用券；后端事务内恢复库存、活动额度并退回积分 */
function handleVoid(row) {
  proxy.$prompt(`请输入「${row.couponName}」的作废原因`, "作废兑换券", {
    confirmButtonText: "确定作废",
    cancelButtonText: "取消",
    inputPattern: /\S+/,
    inputErrorMessage: "作废原因不能为空",
    inputValidator: value => !value || value.length <= 255 || "作废原因不能超过255个字符"
  }).then(({ value }) => {
    return voidExchange(row.recordId, value.trim())
  }).then(() => {
    proxy.$modal.msgSuccess("作废成功，兑换积分已退回")
    getList()
  }).catch(() => {})
}

/** 导出按钮操作（后端为 GET 导出，request 返回 blob 后自行保存） */
function handleExport() {
  proxy.$modal.confirm("是否确认导出所有兑换记录数据项？").then(() => {
    exportLoading.value = true
    return exportExchange(proxy.addDateRange(queryParams.value, dateRange.value))
  }).then(data => {
    if (blobValidate(data)) {
      const blob = new Blob([data])
      saveAs(blob, `exchange_${new Date().getTime()}.xlsx`)
    } else {
      return data.text().then(text => {
        const rspObj = JSON.parse(text)
        proxy.$modal.msgError(rspObj.msg || "导出失败")
      })
    }
  }).catch(() => {}).finally(() => {
    exportLoading.value = false
  })
}

getList()
</script>
