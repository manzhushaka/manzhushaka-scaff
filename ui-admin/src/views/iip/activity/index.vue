<template>
   <div class="app-container ui-list-page">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px" class="ui-filter-card">
         <el-form-item label="活动名称" prop="activityName">
            <el-input
               v-model="queryParams.activityName"
               placeholder="请输入活动名称"
               clearable
               style="width: 220px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="活动状态" clearable style="width: 160px">
               <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="联营范围" prop="regionType">
            <el-select v-model="queryParams.regionType" placeholder="联营范围" clearable style="width: 140px">
               <el-option
                  v-for="item in regionTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="活动时间" style="width: 308px;">
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
               type="primary"
               plain
               icon="Plus"
               @click="handleAdd"
               v-hasPermi="['iip:activity:add']"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['iip:activity:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['iip:activity:remove']"
            >删除</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <div class="ui-table-card">
      <el-table v-loading="loading" :data="activityList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="活动编号" align="center" prop="activityNo" width="140" :show-overflow-tooltip="true" />
         <el-table-column label="活动名称" align="center" prop="activityName" :show-overflow-tooltip="true" />
         <el-table-column label="地域" align="center" width="160" :show-overflow-tooltip="true">
            <template #default="scope">
               <span>{{ regionLabel(scope.row) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="优先级" align="center" prop="priority" width="80" />
         <el-table-column label="起止时间" align="center" width="220">
            <template #default="scope">
               <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d}') }} ~ {{ parseTime(scope.row.endTime, '{y}-{m}-{d}') }}</span>
            </template>
         </el-table-column>
         <el-table-column label="积分比例" align="center" prop="pointsRatio" width="90" />
         <el-table-column label="商户上限" align="center" width="90">
            <template #default="scope">
               <span>{{ scope.row.merchantLimit === -1 ? '不限' : scope.row.merchantLimit }}</span>
            </template>
         </el-table-column>
         <el-table-column label="发券额度" align="center" width="90">
            <template #default="scope">
               <span>{{ scope.row.couponQuota === -1 ? '不限' : scope.row.couponQuota }}</span>
            </template>
         </el-table-column>
         <el-table-column label="状态" align="center" prop="status" width="80">
            <template #default="scope">
               <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
                  {{ scope.row.status === '0' ? '启用' : '停用' }}
               </el-tag>
            </template>
         </el-table-column>
         <el-table-column label="创建时间" align="center" prop="createTime" width="160">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Setting" @click="handleConfig(scope.row)" v-hasPermi="['iip:activity:config']">配置</el-button>
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['iip:activity:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iip:activity:remove']">删除</el-button>
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

      <!-- 添加或修改活动对话框 -->
      <el-dialog :title="title" v-model="open" width="640px" append-to-body>
         <el-form ref="activityRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="活动名称" prop="activityName">
               <el-input v-model="form.activityName" placeholder="请输入活动名称" maxlength="128" />
            </el-form-item>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="开始时间" prop="startTime">
                     <el-date-picker
                        v-model="form.startTime"
                        type="datetime"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        placeholder="请选择开始时间"
                        style="width: 100%"
                     />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="结束时间" prop="endTime">
                     <el-date-picker
                        v-model="form.endTime"
                        type="datetime"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        placeholder="请选择结束时间"
                        style="width: 100%"
                     />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="积分比例" prop="pointsRatio">
                     <el-input-number
                        v-model="form.pointsRatio"
                        :precision="2"
                        :step="0.01"
                        :min="0.01"
                        controls-position="right"
                        style="width: 100%"
                     />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                     <el-radio-group v-model="form.status">
                        <el-radio value="0">启用</el-radio>
                        <el-radio value="1">停用</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="商户上限" prop="merchantLimit">
                     <el-input-number
                        v-model="form.merchantLimit"
                        :precision="0"
                        :min="-1"
                        controls-position="right"
                        style="width: 100%"
                     />
                     <div class="form-tip">参与商户数量上限，-1 表示不限</div>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="发券额度" prop="couponQuota">
                     <el-input-number
                        v-model="form.couponQuota"
                        :precision="0"
                        :min="-1"
                        controls-position="right"
                        style="width: 100%"
                     />
                     <div class="form-tip">活动发券总额度，-1 表示不限</div>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="24">
                  <el-form-item label="联营范围" prop="regionType">
                     <el-radio-group v-model="form.regionType" @change="handleRegionTypeChange">
                        <el-radio
                           v-for="item in regionTypeOptions"
                           :key="item.value"
                           :value="item.value"
                        >{{ item.label }}</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.regionType && form.regionType !== 'province'">
                  <el-form-item label="适用市县" prop="city">
                     <el-input v-model="form.city" placeholder="如：三亚" maxlength="64" />
                     <div class="form-tip">联营范围为市县时必填，不填表示全省通用</div>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.regionType === 'business_district' || form.regionType === 'scenic'">
                  <el-form-item :label="form.regionType === 'scenic' ? '景区名称' : '商圈名称'" prop="regionName">
                     <el-input
                        v-model="form.regionName"
                        :placeholder="form.regionType === 'scenic' ? '如：东坡文化旅游区' : '如：三亚湾商圈'"
                        maxlength="128"
                     />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="优先级" prop="priority">
                     <el-input-number
                        v-model="form.priority"
                        :precision="0"
                        :min="0"
                        :max="9999"
                        controls-position="right"
                        style="width: 100%"
                     />
                     <div class="form-tip">多活动并行时，数值越大越优先匹配</div>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-form-item label="活动封面" prop="coverImage">
               <image-upload v-model="form.coverImage" :limit="1" />
            </el-form-item>
            <el-form-item label="活动描述" prop="description">
               <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动描述" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>

      <!-- 活动配置抽屉：参与商户 + 活动券 -->
      <el-drawer v-model="configOpen" :title="`活动配置 - ${configActivity.activityName || ''}`" size="min(760px, 100%)">
         <el-tabs v-model="activeTab">
            <el-tab-pane label="参与商户" name="merchant">
               <div class="config-add-bar">
                  <el-select
                     v-model="selectedMerchantId"
                     filterable
                     clearable
                     placeholder="请选择要添加的商户"
                     :disabled="merchantLimitReached"
                     style="width: 320px"
                  >
                     <el-option
                        v-for="item in availableMerchants"
                        :key="item.merchantId"
                        :label="`${item.merchantName}（${item.merchantNo}）`"
                        :value="item.merchantId"
                     />
                  </el-select>
                  <el-button
                     type="primary"
                     plain
                     icon="Plus"
                     :disabled="!selectedMerchantId || merchantLimitReached"
                     @click="handleAddMerchant"
                  >添加商户</el-button>
                  <span v-if="merchantLimitReached" class="config-limit-tip">
                     已达商户上限（{{ configActivity.merchantLimit }} 家），如需继续添加请先修改活动商户上限
                  </span>
                  <span v-else-if="configActivity.merchantLimit !== -1" class="config-count-tip">
                     已配置 {{ activityMerchants.length }} / {{ configActivity.merchantLimit }} 家
                  </span>
               </div>
               <el-table v-loading="merchantLoading" :data="activityMerchants">
                  <el-table-column label="商户编号" align="center" prop="merchantNo" width="130" :show-overflow-tooltip="true" />
                  <el-table-column label="商户名称" align="center" prop="merchantName" :show-overflow-tooltip="true" />
                  <el-table-column label="分类" align="center" prop="category" width="100">
                     <template #default="scope">
                        <span>{{ scope.row.category || '—' }}</span>
                     </template>
                  </el-table-column>
                  <el-table-column label="状态" align="center" prop="merchantStatus" width="90">
                     <template #default="scope">
                        <el-tag :type="merchantStatusTagType(scope.row.merchantStatus)">
                           {{ merchantStatusLabel(scope.row.merchantStatus) }}
                        </el-tag>
                     </template>
                  </el-table-column>
                  <el-table-column label="操作" align="center" width="90" class-name="small-padding fixed-width">
                     <template #default="scope">
                        <el-button link type="danger" icon="Delete" @click="handleRemoveMerchant(scope.row)">移除</el-button>
                     </template>
                  </el-table-column>
               </el-table>
            </el-tab-pane>
            <el-tab-pane label="活动券" name="coupon">
               <div class="config-add-bar">
                  <el-select
                     v-model="selectedCouponId"
                     filterable
                     clearable
                     placeholder="请选择要添加的券"
                     style="width: 320px"
                  >
                     <el-option
                        v-for="item in availableCoupons"
                        :key="item.couponId"
                        :label="`${item.couponName}（${item.pointsCost} 积分）`"
                        :value="item.couponId"
                     />
                  </el-select>
                  <el-input-number
                     v-model="newCouponIssueLimit"
                     :precision="0"
                     :min="-1"
                     controls-position="right"
                     placeholder="发行上限"
                     style="width: 140px"
                  />
                  <el-button
                     type="primary"
                     plain
                     icon="Plus"
                     :disabled="!selectedCouponId"
                     @click="handleAddCoupon"
                  >添加券</el-button>
                  <span class="config-count-tip">发行上限 -1 表示不限</span>
               </div>
               <el-table v-loading="couponLoading" :data="activityCoupons">
                  <el-table-column label="券名" align="center" prop="couponName" :show-overflow-tooltip="true" />
                  <el-table-column label="所需积分" align="center" prop="pointsCost" width="90" />
                  <el-table-column label="库存" align="center" width="110">
                     <template #default="scope">
                        <span>{{ scope.row.totalStock === -1 ? '不限' : `${scope.row.remainStock} / ${scope.row.totalStock}` }}</span>
                     </template>
                  </el-table-column>
                  <el-table-column label="发行上限" align="center" prop="issueLimit" width="90">
                     <template #default="scope">
                        <span>{{ scope.row.issueLimit === -1 ? '不限' : scope.row.issueLimit }}</span>
                     </template>
                  </el-table-column>
                  <el-table-column label="已发数量" align="center" prop="issuedCount" width="90" />
                  <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
                     <template #default="scope">
                        <el-button link type="primary" icon="Edit" @click="handleOpenIssueLimit(scope.row)">修改上限</el-button>
                        <el-button link type="danger" icon="Delete" @click="handleRemoveCoupon(scope.row)">移除</el-button>
                     </template>
                  </el-table-column>
               </el-table>
            </el-tab-pane>
         </el-tabs>

         <!-- 修改活动券发行上限 -->
         <el-dialog title="修改发行上限" v-model="issueLimitOpen" width="420px" append-to-body>
            <el-form label-width="90px">
               <el-form-item label="券名称">
                  <span>{{ editingCoupon.couponName }}</span>
               </el-form-item>
               <el-form-item label="发行上限">
                  <el-input-number
                     v-model="editingCoupon.issueLimit"
                     :precision="0"
                     :min="-1"
                     controls-position="right"
                     style="width: 200px"
                  />
                  <div class="form-tip">该券在本活动内的发行上限，-1 表示不限</div>
               </el-form-item>
            </el-form>
            <template #footer>
               <div class="dialog-footer">
                  <el-button type="primary" @click="submitIssueLimit">确 定</el-button>
                  <el-button @click="issueLimitOpen = false">取 消</el-button>
               </div>
            </template>
         </el-dialog>
      </el-drawer>
   </div>
</template>

<script setup name="IipActivity">
import {
   listActivity, getActivity, delActivity, addActivity, updateActivity,
   listActivityMerchants, addActivityMerchant, removeActivityMerchant,
   listActivityCoupons, addActivityCoupon, updateActivityCoupon, removeActivityCoupon,
   listMerchantOptions, listCouponOptions
} from "@/api/iip/activity"

const { proxy } = getCurrentInstance()

// 活动状态选项（0 启用 1 停用，与 iip_activity.status 一致）
const statusOptions = [
   { value: '0', label: '启用' },
   { value: '1', label: '停用' }
]

// 联营范围选项（与 iip_activity.region_type 取值一致）
const regionTypeOptions = [
   { value: 'province', label: '全省' },
   { value: 'city', label: '市县' },
   { value: 'business_district', label: '商圈' },
   { value: 'scenic', label: '景区' }
]

const activityList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])

// 活动配置抽屉
const configOpen = ref(false)
const activeTab = ref("merchant")
const configActivity = ref({})
const activityMerchants = ref([])
const activityCoupons = ref([])
const merchantOptions = ref([])
const couponOptions = ref([])
const merchantLoading = ref(false)
const couponLoading = ref(false)
const selectedMerchantId = ref(undefined)
const selectedCouponId = ref(undefined)
const newCouponIssueLimit = ref(-1)
const issueLimitOpen = ref(false)
const editingCoupon = ref({})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    activityName: undefined,
    status: undefined,
    regionType: undefined
  },
  rules: {
    activityName: [{ required: true, message: "活动名称不能为空", trigger: "blur" }],
    startTime: [{ required: true, message: "开始时间不能为空", trigger: "change" }],
    endTime: [{ required: true, message: "结束时间不能为空", trigger: "change" }],
    pointsRatio: [{ required: true, message: "积分比例不能为空", trigger: "blur" }],
    status: [{ required: true, message: "状态不能为空", trigger: "change" }],
    regionType: [{ required: true, message: "联营范围不能为空", trigger: "change" }],
    city: [{ validator: validateCity, trigger: "blur" }],
    regionName: [{ validator: validateRegionName, trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 联营范围为市县时适用市县必填 */
function validateCity(rule, value, callback) {
  if (form.value.regionType === "city" && (!value || !value.trim())) {
    callback(new Error("联营范围为市县时，适用市县不能为空"))
  } else {
    callback()
  }
}

/** 联营范围为商圈/景区时，商圈/景区名称必填 */
function validateRegionName(rule, value, callback) {
  const needName = form.value.regionType === "business_district" || form.value.regionType === "scenic"
  if (needName && (!value || !value.trim())) {
    callback(new Error(form.value.regionType === "scenic" ? "景区名称不能为空" : "商圈名称不能为空"))
  } else {
    callback()
  }
}

/** 联营范围文案 */
function regionTypeLabel(type) {
  const item = regionTypeOptions.find(option => option.value === type)
  return item ? item.label : "全省"
}

/** 地域列展示：范围 + 市县/商圈景区名称拼接，如 商圈·三亚·三亚湾商圈 */
function regionLabel(row) {
  const typeLabel = regionTypeLabel(row.regionType)
  const parts = [row.city, row.regionName].filter(item => item)
  return parts.length ? `${typeLabel}·${parts.join("·")}` : typeLabel
}

/** 切换联营范围：清理不再适用的地域字段与校验状态，避免提交脏数据 */
function handleRegionTypeChange(value) {
  if (value === "province") {
    form.value.city = undefined
  }
  if (value !== "business_district" && value !== "scenic") {
    form.value.regionName = undefined
  }
  proxy.$refs["activityRef"] && proxy.$refs["activityRef"].clearValidate(["city", "regionName"])
}

/** 可选商户：排除已配置项 */
const availableMerchants = computed(() => {
  const configuredIds = new Set(activityMerchants.value.map(item => item.merchantId))
  return merchantOptions.value.filter(item => !configuredIds.has(item.merchantId))
})

/** 可选券：排除已配置项 */
const availableCoupons = computed(() => {
  const configuredIds = new Set(activityCoupons.value.map(item => item.couponId))
  return couponOptions.value.filter(item => !configuredIds.has(item.couponId))
})

/** 商户上限是否已达（-1 不限） */
const merchantLimitReached = computed(() => {
  const limit = configActivity.value.merchantLimit
  return limit !== undefined && limit !== null && limit !== -1 && activityMerchants.value.length >= limit
})

/** 查询活动列表 */
function getList() {
  loading.value = true
  listActivity(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    activityList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    activityId: undefined,
    activityName: undefined,
    startTime: undefined,
    endTime: undefined,
    pointsRatio: 1.00,
    merchantLimit: -1,
    couponQuota: -1,
    coverImage: undefined,
    description: undefined,
    status: "0",
    city: undefined,
    regionType: "province",
    regionName: undefined,
    priority: 0,
    remark: undefined
  }
  proxy.resetForm("activityRef")
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

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.activityId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加活动"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const activityId = row.activityId || ids.value
  getActivity(activityId).then(response => {
    form.value = response.data
    // 旧数据可能无新增地域字段，补默认值保证表单状态完整
    form.value.regionType = response.data.regionType || "province"
    form.value.priority = response.data.priority === undefined || response.data.priority === null ? 0 : response.data.priority
    open.value = true
    title.value = "修改活动"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["activityRef"].validate(valid => {
    if (valid) {
      // 全省活动清空市县字段；非商圈/景区清空名称字段，避免残留脏数据（后端按空串覆盖更新）
      if (form.value.regionType === "province") {
        form.value.city = ""
      }
      if (form.value.regionType !== "business_district" && form.value.regionType !== "scenic") {
        form.value.regionName = ""
      }
      if (form.value.activityId != undefined) {
        updateActivity(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addActivity(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作（进行中的活动后端禁止删除，错误信息由请求拦截器统一提示） */
function handleDelete(row) {
  const activityIds = row.activityId || ids.value
  proxy.$modal.confirm('是否确认删除活动编号为"' + activityIds + '"的数据项？').then(function () {
    return delActivity(activityIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 打开活动配置抽屉 */
function handleConfig(row) {
  configActivity.value = row
  configOpen.value = true
  activeTab.value = "merchant"
  selectedMerchantId.value = undefined
  selectedCouponId.value = undefined
  newCouponIssueLimit.value = -1
  loadActivityMerchants()
  loadActivityCoupons()
  loadMerchantOptions()
  loadCouponOptions()
}

/** 加载活动已配置商户 */
function loadActivityMerchants() {
  merchantLoading.value = true
  listActivityMerchants(configActivity.value.activityId).then(response => {
    activityMerchants.value = response.data || []
    merchantLoading.value = false
  }).catch(() => {
    merchantLoading.value = false
  })
}

/** 加载活动已配置券 */
function loadActivityCoupons() {
  couponLoading.value = true
  listActivityCoupons(configActivity.value.activityId).then(response => {
    activityCoupons.value = response.data || []
    couponLoading.value = false
  }).catch(() => {
    couponLoading.value = false
  })
}

/** 加载商户下拉选项 */
function loadMerchantOptions() {
  listMerchantOptions().then(response => {
    merchantOptions.value = response.rows || []
  })
}

/** 加载券下拉选项 */
function loadCouponOptions() {
  listCouponOptions().then(response => {
    couponOptions.value = response.rows || []
  })
}

/** 添加参与商户 */
function handleAddMerchant() {
  if (!selectedMerchantId.value) {
    return
  }
  addActivityMerchant({
    activityId: configActivity.value.activityId,
    merchantId: selectedMerchantId.value
  }).then(() => {
    proxy.$modal.msgSuccess("添加成功")
    selectedMerchantId.value = undefined
    loadActivityMerchants()
  })
}

/** 移除参与商户 */
function handleRemoveMerchant(row) {
  proxy.$modal.confirm('是否确认将商户"' + row.merchantName + '"从活动中移除？').then(function () {
    return removeActivityMerchant(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess("移除成功")
    loadActivityMerchants()
  }).catch(() => {})
}

/** 添加活动券 */
function handleAddCoupon() {
  if (!selectedCouponId.value) {
    return
  }
  addActivityCoupon({
    activityId: configActivity.value.activityId,
    couponId: selectedCouponId.value,
    issueLimit: newCouponIssueLimit.value
  }).then(() => {
    proxy.$modal.msgSuccess("添加成功")
    selectedCouponId.value = undefined
    newCouponIssueLimit.value = -1
    loadActivityCoupons()
  })
}

/** 打开修改发行上限对话框 */
function handleOpenIssueLimit(row) {
  editingCoupon.value = { id: row.id, couponName: row.couponName, issueLimit: row.issueLimit }
  issueLimitOpen.value = true
}

/** 提交发行上限修改 */
function submitIssueLimit() {
  if (editingCoupon.value.issueLimit === undefined || editingCoupon.value.issueLimit === null) {
    proxy.$modal.msgError("发行上限不能为空")
    return
  }
  updateActivityCoupon({
    id: editingCoupon.value.id,
    issueLimit: editingCoupon.value.issueLimit
  }).then(() => {
    proxy.$modal.msgSuccess("修改成功")
    issueLimitOpen.value = false
    loadActivityCoupons()
  })
}

/** 移除活动券 */
function handleRemoveCoupon(row) {
  proxy.$modal.confirm('是否确认将券"' + row.couponName + '"从活动中移除？').then(function () {
    return removeActivityCoupon(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess("移除成功")
    loadActivityCoupons()
  }).catch(() => {})
}

/** 商户状态文案（0 正常 1 停用 2 待审核） */
function merchantStatusLabel(status) {
  const map = { '0': '正常', '1': '停用', '2': '待审核' }
  return map[status] || '未知'
}

/** 商户状态标签类型 */
function merchantStatusTagType(status) {
  const map = { '0': 'success', '1': 'info', '2': 'warning' }
  return map[status] || 'info'
}

getList()
</script>

<style lang="scss" scoped>
.form-tip {
  width: 100%;
  color: var(--ui-text-muted);
  font-size: 12px;
  line-height: 1.6;
}

.config-add-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.config-limit-tip {
  color: var(--ui-warning);
  font-size: 12px;
}

.config-count-tip {
  color: var(--ui-text-muted);
  font-size: 12px;
}

@media screen and (max-width: 640px) {
  .config-add-bar .el-select,
  .config-add-bar .el-input-number {
    width: 100% !important;
  }
}
</style>
