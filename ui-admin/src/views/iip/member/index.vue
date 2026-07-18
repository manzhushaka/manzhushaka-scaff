<template>
   <div class="app-container ui-list-page">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px" class="ui-filter-card">
         <el-form-item label="关键字" prop="keyword">
            <el-input
               v-model="queryParams.keyword"
               placeholder="昵称或手机号"
               clearable
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="手机号" prop="phone">
            <el-input
               v-model="queryParams.phone"
               placeholder="请输入手机号"
               clearable
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 240px">
               <el-option
                  v-for="item in MEMBER_STATUS_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="注册时间" style="width: 308px;">
            <el-date-picker
               v-model="dateRange"
               value-format="YYYY-MM-DD"
               type="daterange"
               range-separator="-"
               start-placeholder="开始日期"
               end-placeholder="结束日期"
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
               @click="handleExport"
               v-hasPermi="['iip:member:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <div class="ui-table-card">
      <el-table v-loading="loading" :data="memberList">
         <el-table-column label="用户ID" align="center" prop="memberId" width="90" />
         <el-table-column label="昵称" align="center" prop="nickname" :show-overflow-tooltip="true" />
         <el-table-column label="手机号" align="center" prop="phone" width="130" />
         <el-table-column label="性别" align="center" prop="gender" width="80">
            <template #default="scope">
               <span>{{ formatGender(scope.row.gender) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="状态" align="center" prop="status" width="100">
            <template #default="scope">
               <el-switch
                  v-model="scope.row.status"
                  active-value="0"
                  inactive-value="1"
                  @change="handleStatusChange(scope.row)"
                  v-hasPermi="['iip:member:edit']"
               ></el-switch>
            </template>
         </el-table-column>
         <el-table-column label="最近登录" align="center" prop="lastLoginTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.lastLoginTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="注册时间" align="center" prop="createTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
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

<script setup name="Member">
import { ElLoading, ElMessage } from "element-plus"
import { saveAs } from "file-saver"
import { blobValidate } from "@/utils/manzhushaka"
import { listMember, changeMemberStatus, exportMember } from "@/api/iip/member"

const { proxy } = getCurrentInstance()

/** 用户状态（0正常 1停用），iip 业务状态不使用系统字典 */
const MEMBER_STATUS_OPTIONS = [
  { value: "0", label: "正常" },
  { value: "1", label: "停用" }
]
/** 性别映射（0男 1女 2未知） */
const GENDER_MAP = {
  "0": "男",
  "1": "女",
  "2": "未知"
}

const memberList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    keyword: undefined,
    phone: undefined,
    status: undefined
  }
})

const { queryParams } = toRefs(data)

/** 格式化性别 */
function formatGender(gender) {
  return GENDER_MAP[gender] || "未知"
}

/** 查询用户列表 */
function getList() {
  loading.value = true
  listMember(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    memberList.value = response.rows
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

/** 用户状态修改（启用/停用切换） */
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + text + '""' + (row.nickname || row.memberId) + '"用户吗?').then(function () {
    return changeMemberStatus(row.memberId, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(function () {
    row.status = row.status === "0" ? "1" : "0"
  })
}

/** 导出按钮操作（后端导出为 GET 接口，使用 request 的 GET + blob 方式下载保存） */
function handleExport() {
  const loadingInstance = ElLoading.service({ text: "正在下载数据，请稍候", background: "rgba(0, 0, 0, 0.7)" })
  exportMember(proxy.addDateRange(queryParams.value, dateRange.value)).then(async data => {
    const isBlob = blobValidate(data)
    if (isBlob) {
      const blob = new Blob([data])
      saveAs(blob, `member_${new Date().getTime()}.xlsx`)
    } else {
      const resText = await data.text()
      const rspObj = JSON.parse(resText)
      ElMessage.error(rspObj.msg || "下载文件出现错误")
    }
  }).catch(() => {
    ElMessage.error("下载文件出现错误，请联系管理员！")
  }).finally(() => {
    loadingInstance.close()
  })
}

getList()
</script>
