<template>
   <div class="app-container ui-list-page">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
         <!-- 积分账户 -->
         <el-tab-pane label="积分账户" name="account">
            <el-form :model="accountQuery" ref="accountQueryRef" :inline="true" v-show="showSearchAccount" label-width="80px" class="ui-filter-card">
               <el-form-item label="用户ID" prop="memberId">
                  <el-input
                     v-model="accountQuery.memberId"
                     placeholder="请输入用户ID"
                     clearable
                     style="width: 180px"
                     @keyup.enter="handleAccountQuery"
                  />
               </el-form-item>
               <el-form-item label="昵称" prop="nickname">
                  <el-input
                     v-model="accountQuery.nickname"
                     placeholder="请输入昵称关键字"
                     clearable
                     style="width: 180px"
                     @keyup.enter="handleAccountQuery"
                  />
               </el-form-item>
               <el-form-item>
                  <el-button type="primary" icon="Search" @click="handleAccountQuery">搜索</el-button>
                  <el-button icon="Refresh" @click="resetAccountQuery">重置</el-button>
               </el-form-item>
            </el-form>

            <el-row :gutter="10" class="mb8 ui-action-bar">
               <right-toolbar v-model:showSearch="showSearchAccount" @queryTable="getAccountList"></right-toolbar>
            </el-row>

            <div class="ui-table-card">
            <el-table v-loading="accountLoading" :data="accountList">
               <el-table-column label="用户ID" align="center" prop="memberId" width="100" />
               <el-table-column label="昵称" align="center" prop="nickname" min-width="120" :show-overflow-tooltip="true" />
               <el-table-column label="累计获得" align="center" prop="totalPoints" width="100" />
               <el-table-column label="可用积分" align="center" prop="availablePoints" width="100" />
               <el-table-column label="已使用" align="center" prop="usedPoints" width="100" />
               <el-table-column label="已过期" align="center" prop="expiredPoints" width="100" />
               <el-table-column label="更新时间" align="center" prop="updateTime" width="160">
                  <template #default="scope">
                     <span>{{ parseTime(scope.row.updateTime) }}</span>
                  </template>
               </el-table-column>
               <el-table-column label="操作" align="center" width="100" class-name="small-padding fixed-width">
                  <template #default="scope">
                     <el-button link type="primary" icon="EditPen" @click="handleAdjust(scope.row)" v-hasPermi="['iip:points:adjust']">调整</el-button>
                  </template>
               </el-table-column>
            </el-table>
            </div>

            <pagination
               v-show="accountTotal > 0"
               :total="accountTotal"
               v-model:page="accountQuery.pageNum"
               v-model:limit="accountQuery.pageSize"
               @pagination="getAccountList"
            />
         </el-tab-pane>

         <!-- 积分流水 -->
         <el-tab-pane label="积分流水" name="record">
            <el-form :model="recordQuery" ref="recordQueryRef" :inline="true" v-show="showSearchRecord" label-width="80px" class="ui-filter-card">
               <el-form-item label="用户ID" prop="memberId">
                  <el-input
                     v-model="recordQuery.memberId"
                     placeholder="请输入用户ID"
                     clearable
                     style="width: 160px"
                     @keyup.enter="handleRecordQuery"
                  />
               </el-form-item>
               <el-form-item label="变动类型" prop="changeType">
                  <el-select v-model="recordQuery.changeType" placeholder="请选择变动类型" clearable style="width: 140px">
                     <el-option
                        v-for="item in changeTypeOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                     />
                  </el-select>
               </el-form-item>
               <el-form-item label="业务类型" prop="bizType">
                  <el-select v-model="recordQuery.bizType" placeholder="请选择业务类型" clearable style="width: 150px">
                     <el-option
                        v-for="item in bizTypeOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                     />
                  </el-select>
               </el-form-item>
               <el-form-item label="变动时间" style="width: 340px">
                  <el-date-picker
                     v-model="recordDateRange"
                     value-format="YYYY-MM-DD HH:mm:ss"
                     type="daterange"
                     range-separator="-"
                     start-placeholder="开始日期"
                     end-placeholder="结束日期"
                     :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
                  ></el-date-picker>
               </el-form-item>
               <el-form-item>
                  <el-button type="primary" icon="Search" @click="handleRecordQuery">搜索</el-button>
                  <el-button icon="Refresh" @click="resetRecordQuery">重置</el-button>
               </el-form-item>
            </el-form>

            <el-row :gutter="10" class="mb8 ui-action-bar">
               <right-toolbar v-model:showSearch="showSearchRecord" @queryTable="getRecordList"></right-toolbar>
            </el-row>

            <div class="ui-table-card">
            <el-table v-loading="recordLoading" :data="recordList">
               <el-table-column label="用户ID" align="center" prop="memberId" width="100" />
               <el-table-column label="类型" align="center" prop="changeType" width="90">
                  <template #default="scope">
                     <el-tag :type="changeTypeTag(scope.row.changeType)">{{ changeTypeLabel(scope.row.changeType) }}</el-tag>
                  </template>
               </el-table-column>
               <el-table-column label="数量" align="center" width="100">
                  <template #default="scope">
                     <span>{{ formatPoints(scope.row) }}</span>
                  </template>
               </el-table-column>
               <el-table-column label="变动后余额" align="center" prop="balanceAfter" width="110" />
               <el-table-column label="业务类型" align="center" width="110">
                  <template #default="scope">
                     <span>{{ bizTypeLabel(scope.row.bizType) }}</span>
                  </template>
               </el-table-column>
               <el-table-column label="业务ID" align="center" prop="bizId" width="130" :show-overflow-tooltip="true" />
               <el-table-column label="过期时间" align="center" prop="expireTime" width="160">
                  <template #default="scope">
                     <span>{{ scope.row.expireTime ? parseTime(scope.row.expireTime) : "-" }}</span>
                  </template>
               </el-table-column>
               <el-table-column label="备注" align="center" prop="remark" min-width="120" :show-overflow-tooltip="true" />
               <el-table-column label="时间" align="center" prop="createTime" width="160">
                  <template #default="scope">
                     <span>{{ parseTime(scope.row.createTime) }}</span>
                  </template>
               </el-table-column>
            </el-table>
            </div>

            <pagination
               v-show="recordTotal > 0"
               :total="recordTotal"
               v-model:page="recordQuery.pageNum"
               v-model:limit="recordQuery.pageSize"
               @pagination="getRecordList"
            />
         </el-tab-pane>
      </el-tabs>

      <!-- 手工调整积分对话框 -->
      <el-dialog title="手工调整积分" v-model="adjustOpen" width="500px" append-to-body>
         <el-alert
            type="info"
            show-icon
            :closable="false"
            style="margin-bottom: 16px"
            title="正数表示发放，负数表示扣减；扣减超过可用余额时后端将返回错误提示"
         />
         <el-form ref="adjustRef" :model="adjustForm" :rules="adjustRules" label-width="90px">
            <el-form-item label="用户" prop="memberDisplay">
               <el-input v-model="adjustForm.memberDisplay" readonly disabled />
            </el-form-item>
            <el-form-item label="调整积分" prop="points">
               <el-input-number v-model="adjustForm.points" :min="-1000000" :max="1000000" controls-position="right" style="width: 100%" placeholder="正数=发放，负数=扣减" />
            </el-form-item>
            <el-form-item label="调整备注" prop="remark">
               <el-input v-model="adjustForm.remark" type="textarea" :rows="3" placeholder="请输入调整备注（必填）" maxlength="255" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitAdjust">确 定</el-button>
               <el-button @click="cancelAdjust">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="IipPoints">
import { listPointsAccount, listPointsRecord, adjustPoints } from "@/api/iip/points"

const { proxy } = getCurrentInstance()

// 变动类型（对齐后端 iip_points_record.change_type：earn/consume/expire/adjust）
const changeTypeOptions = [
  { value: "earn", label: "获得", tag: "success", sign: "+" },
  { value: "consume", label: "消费", tag: "warning", sign: "-" },
  { value: "expire", label: "过期", tag: "info", sign: "-" },
  { value: "adjust", label: "调整", tag: "primary", sign: "" }
]

// 业务类型（对齐后端 iip_points_record.biz_type）
const bizTypeOptions = [
  { value: "invoice_audit", label: "发票审核" },
  { value: "coupon_exchange", label: "券兑换" },
  { value: "admin_adjust", label: "人工调整" },
  { value: "point_expire", label: "过期结转" }
]

const activeTab = ref("account")
const recordLoaded = ref(false)

const accountList = ref([])
const accountLoading = ref(true)
const showSearchAccount = ref(true)
const accountTotal = ref(0)

const recordList = ref([])
const recordLoading = ref(false)
const showSearchRecord = ref(true)
const recordTotal = ref(0)
const recordDateRange = ref([])

const adjustOpen = ref(false)

const data = reactive({
  accountQuery: {
    pageNum: 1,
    pageSize: 10,
    memberId: undefined,
    nickname: undefined
  },
  recordQuery: {
    pageNum: 1,
    pageSize: 10,
    memberId: undefined,
    changeType: undefined,
    bizType: undefined
  },
  adjustForm: {
    memberId: undefined,
    memberDisplay: undefined,
    points: undefined,
    remark: undefined
  },
  adjustRules: {
    points: [
      { required: true, message: "调整积分不能为空", trigger: "blur" },
      { validator: validateAdjustPoints, trigger: "blur" }
    ],
    remark: [{ required: true, message: "调整备注不能为空", trigger: "blur" }]
  }
})

const { accountQuery, recordQuery, adjustForm, adjustRules } = toRefs(data)

/** 调整积分非零校验（对齐后端：调整积分不能为0） */
function validateAdjustPoints(rule, value, callback) {
  if (value === 0) {
    callback(new Error("调整积分不能为0"))
  } else {
    callback()
  }
}

function changeTypeLabel(changeType) {
  const item = changeTypeOptions.find(option => option.value === changeType)
  return item ? item.label : changeType
}

function changeTypeTag(changeType) {
  const item = changeTypeOptions.find(option => option.value === changeType)
  return item ? item.tag : "info"
}

function bizTypeLabel(bizType) {
  const item = bizTypeOptions.find(option => option.value === bizType)
  return item ? item.label : bizType
}

/** 流水数量带符号展示：获得为 +，消费/过期为 -（后端统一存正数） */
function formatPoints(row) {
  const item = changeTypeOptions.find(option => option.value === row.changeType)
  const sign = item ? item.sign : ""
  return sign + row.points
}

/** 查询积分账户列表 */
function getAccountList() {
  accountLoading.value = true
  listPointsAccount(accountQuery.value).then(response => {
    accountList.value = response.rows
    accountTotal.value = response.total
    accountLoading.value = false
  })
}

/** 查询积分流水列表 */
function getRecordList() {
  recordLoading.value = true
  listPointsRecord(proxy.addDateRange(recordQuery.value, recordDateRange.value)).then(response => {
    recordList.value = response.rows
    recordTotal.value = response.total
    recordLoading.value = false
  })
}

/** 切换 tab：流水页首次进入时再加载 */
function handleTabChange(name) {
  if (name === "record" && !recordLoaded.value) {
    recordLoaded.value = true
    getRecordList()
  }
}

/** 账户搜索按钮操作 */
function handleAccountQuery() {
  accountQuery.value.pageNum = 1
  getAccountList()
}

/** 账户重置按钮操作 */
function resetAccountQuery() {
  proxy.resetForm("accountQueryRef")
  handleAccountQuery()
}

/** 流水搜索按钮操作 */
function handleRecordQuery() {
  recordQuery.value.pageNum = 1
  getRecordList()
}

/** 流水重置按钮操作 */
function resetRecordQuery() {
  recordDateRange.value = []
  proxy.resetForm("recordQueryRef")
  handleRecordQuery()
}

/** 调整按钮操作：回显用户信息（只读），打开调整对话框 */
function handleAdjust(row) {
  adjustForm.value = {
    memberId: row.memberId,
    memberDisplay: row.memberId + (row.nickname ? "（" + row.nickname + "）" : ""),
    points: undefined,
    remark: undefined
  }
  proxy.resetForm("adjustRef")
  adjustOpen.value = true
}

/** 取消调整 */
function cancelAdjust() {
  adjustOpen.value = false
  proxy.resetForm("adjustRef")
}

/** 提交调整（余额不足等后端业务错误由响应拦截器统一提示） */
function submitAdjust() {
  proxy.$refs["adjustRef"].validate(valid => {
    if (valid) {
      adjustPoints({
        memberId: adjustForm.value.memberId,
        points: adjustForm.value.points,
        remark: adjustForm.value.remark
      }).then(() => {
        proxy.$modal.msgSuccess("调整成功")
        adjustOpen.value = false
        getAccountList()
      })
    }
  })
}

getAccountList()
</script>
