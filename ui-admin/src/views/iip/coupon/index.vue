<template>
   <div class="app-container ui-list-page">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px" class="ui-filter-card">
         <el-form-item label="券名称" prop="couponName">
            <el-input
               v-model="queryParams.couponName"
               placeholder="请输入券名称"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="券类型" prop="couponType">
            <el-select v-model="queryParams.couponType" placeholder="请选择券类型" clearable style="width: 160px">
               <el-option
                  v-for="item in couponTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="权益品类" prop="category">
            <el-select v-model="queryParams.category" placeholder="请选择品类" clearable style="width: 160px">
               <el-option
                  v-for="item in categoryOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 140px">
               <el-option label="上架" value="0" />
               <el-option label="下架" value="1" />
            </el-select>
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
               v-hasPermi="['iip:coupon:add']"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['iip:coupon:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['iip:coupon:remove']"
            >删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               :loading="exportLoading"
               @click="handleExport"
               v-hasPermi="['iip:coupon:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <div class="ui-table-card">
      <el-table v-loading="loading" :data="couponList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="券名称" align="center" prop="couponName" min-width="150" :show-overflow-tooltip="true" />
         <el-table-column label="类型" align="center" prop="couponType" width="100">
            <template #default="scope">
               <el-tag :type="couponTypeTag(scope.row.couponType)">{{ couponTypeLabel(scope.row.couponType) }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="品类" align="center" width="110">
            <template #default="scope">
               <span>{{ categoryLabel(scope.row.category) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="赞助方" align="center" width="140" :show-overflow-tooltip="true">
            <template #default="scope">
               <span>{{ sponsorLabel(scope.row) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="所需积分" align="center" prop="pointsCost" width="90" />
         <el-table-column label="库存(剩余/总量)" align="center" width="130">
            <template #default="scope">
               <span v-if="scope.row.totalStock === -1">不限</span>
               <span v-else>{{ scope.row.remainStock }}/{{ scope.row.totalStock }}</span>
            </template>
         </el-table-column>
         <el-table-column label="每人限兑" align="center" width="90">
            <template #default="scope">
               <span>{{ scope.row.perMemberLimit === -1 ? "不限" : scope.row.perMemberLimit }}</span>
            </template>
         </el-table-column>
         <el-table-column label="兑换窗口" align="center" width="190">
            <template #default="scope">
               <template v-if="scope.row.exchangeStartTime && scope.row.exchangeEndTime">
                  <div>{{ parseTime(scope.row.exchangeStartTime, "{y}-{m}-{d} {h}:{i}") }}</div>
                  <div>至 {{ parseTime(scope.row.exchangeEndTime, "{y}-{m}-{d} {h}:{i}") }}</div>
               </template>
               <span v-else>不限</span>
            </template>
         </el-table-column>
         <el-table-column label="有效期" align="center" width="190">
            <template #default="scope">
               <span v-if="scope.row.validType === 'days'">领取后 {{ scope.row.validDays }} 天</span>
               <template v-else-if="scope.row.validStartTime && scope.row.validEndTime">
                  <div>{{ parseTime(scope.row.validStartTime, "{y}-{m}-{d}") }}</div>
                  <div>至 {{ parseTime(scope.row.validEndTime, "{y}-{m}-{d}") }}</div>
               </template>
               <span v-else>-</span>
            </template>
         </el-table-column>
         <el-table-column label="状态" align="center" prop="status" width="80">
            <template #default="scope">
               <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? "上架" : "下架" }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="排序" align="center" prop="sort" width="60" />
         <el-table-column label="操作" align="center" width="130" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['iip:coupon:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iip:coupon:remove']">删除</el-button>
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

      <!-- 添加或修改券对话框 -->
      <el-dialog :title="title" v-model="open" width="720px" append-to-body>
         <el-alert
            type="warning"
            show-icon
            :closable="false"
            style="margin-bottom: 16px"
            title="规则提示：已有兑换记录的券，后端将拒绝修改兑换规则（所需积分、有效期）并拒绝删除"
         />
         <el-form ref="couponRef" :model="form" :rules="rules" label-width="96px">
            <el-row :gutter="16">
               <el-col :span="12">
                  <el-form-item label="券名称" prop="couponName">
                     <el-input v-model="form.couponName" placeholder="请输入券名称" maxlength="128" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="券类型" prop="couponType">
                     <el-select v-model="form.couponType" placeholder="请选择券类型" style="width: 100%" @change="handleCouponTypeChange">
                        <el-option
                           v-for="item in couponTypeOptions"
                           :key="item.value"
                           :label="item.label"
                           :value="item.value"
                        />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="权益品类" prop="category">
                     <el-select v-model="form.category" placeholder="请选择权益品类" style="width: 100%">
                        <el-option
                           v-for="item in categoryOptions"
                           :key="item.value"
                           :label="item.label"
                           :value="item.value"
                        />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="适用对象" prop="targetName">
                     <el-input v-model="form.targetName" placeholder="如景区名，选填" maxlength="128" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="所需积分" prop="pointsCost">
                     <el-input-number v-model="form.pointsCost" :min="0" :max="99999999" controls-position="right" style="width: 100%" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="总库存" prop="totalStock">
                     <el-input-number v-model="form.totalStock" :min="-1" :max="99999999" controls-position="right" style="width: 100%" @change="handleTotalStockChange" />
                     <div class="iip-form-tip">-1 表示不限库存</div>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.couponId !== undefined">
                  <el-form-item label="剩余库存" prop="remainStock">
                     <el-input-number
                        v-model="form.remainStock"
                        :min="0"
                        :max="form.totalStock === -1 ? 99999999 : form.totalStock"
                        :disabled="form.totalStock === -1"
                        controls-position="right"
                        style="width: 100%"
                     />
                     <div class="iip-form-tip">新增时由后端按总库存自动初始化</div>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="每人限兑" prop="perMemberLimit">
                     <el-input-number v-model="form.perMemberLimit" :min="-1" :max="99999999" controls-position="right" style="width: 100%" />
                     <div class="iip-form-tip">-1 表示不限</div>
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="兑换窗口" prop="exchangeRange">
                     <el-date-picker
                        v-model="form.exchangeRange"
                        type="datetimerange"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        range-separator="至"
                        start-placeholder="兑换开始时间"
                        end-placeholder="兑换结束时间"
                        style="width: 100%"
                     />
                     <div class="iip-form-tip">不填表示不限兑换时间</div>
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="有效期类型" prop="validType">
                     <el-radio-group v-model="form.validType" @change="handleValidTypeChange">
                        <el-radio value="fixed">固定区间</el-radio>
                        <el-radio value="days">领取后 N 天</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="24" v-if="form.validType === 'fixed'">
                  <el-form-item label="有效期" prop="validRange">
                     <el-date-picker
                        v-model="form.validRange"
                        type="datetimerange"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        range-separator="至"
                        start-placeholder="有效期开始时间"
                        end-placeholder="有效期结束时间"
                        style="width: 100%"
                     />
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-else>
                  <el-form-item label="有效天数" prop="validDays">
                     <el-input-number v-model="form.validDays" :min="1" :max="36500" controls-position="right" style="width: 100%" />
                     <div class="iip-form-tip">自领取之日起的有效天数</div>
                  </el-form-item>
               </el-col>
               <template v-if="form.couponType === 'full_reduction'">
                  <el-col :span="12">
                     <el-form-item label="满减门槛" prop="thresholdAmount">
                        <el-input-number v-model="form.thresholdAmount" :min="0" :precision="2" :max="99999999" controls-position="right" style="width: 100%" />
                        <div class="iip-form-tip">消费满该金额可用（元）</div>
                     </el-form-item>
                  </el-col>
                  <el-col :span="12">
                     <el-form-item label="满减面额" prop="discountAmount">
                        <el-input-number v-model="form.discountAmount" :min="0" :precision="2" :max="99999999" controls-position="right" style="width: 100%" />
                        <div class="iip-form-tip">满足门槛后立减金额（元）</div>
                     </el-form-item>
                  </el-col>
               </template>
               <el-col :span="12">
                  <el-form-item label="绑定商户" prop="merchantId">
                     <el-select v-model="form.merchantId" placeholder="不选择则为平台通用券" clearable filterable style="width: 100%">
                        <el-option
                           v-for="item in merchantOptions"
                           :key="item.merchantId"
                           :label="item.merchantName"
                           :value="item.merchantId"
                        />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="赞助方类型" prop="sponsorType">
                     <el-radio-group v-model="form.sponsorType" @change="handleSponsorTypeChange">
                        <el-radio
                           v-for="item in sponsorTypeOptions"
                           :key="item.value"
                           :value="item.value"
                        >{{ item.label }}</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.sponsorType && form.sponsorType !== 'platform'">
                  <el-form-item label="赞助方名称" prop="sponsorName">
                     <el-input v-model="form.sponsorName" placeholder="如：海南农商银行" maxlength="128" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="显示排序" prop="sort">
                     <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" style="width: 100%" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                     <el-radio-group v-model="form.status">
                        <el-radio value="0">上架</el-radio>
                        <el-radio value="1">下架</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="封面图片" prop="coverImage">
                     <image-upload v-model="form.coverImage" :limit="1" />
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="使用说明" prop="useDesc">
                     <el-input v-model="form.useDesc" type="textarea" :rows="3" placeholder="请输入使用说明" maxlength="500" />
                  </el-form-item>
               </el-col>
            </el-row>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="IipCoupon">
import { listCoupon, getCoupon, delCoupon, addCoupon, updateCoupon, exportCoupon, listMerchantOptions } from "@/api/iip/coupon"
import { saveAs } from "file-saver"
import { blobValidate } from "@/utils/manzhushaka"

const { proxy } = getCurrentInstance()

// 券类型选项（与后端 iip_coupon.coupon_type 白名单一致）
const couponTypeOptions = [
  { value: "ticket", label: "门票", tag: "success" },
  { value: "virtual", label: "虚拟物品", tag: "primary" },
  { value: "full_reduction", label: "满减券", tag: "warning" },
  { value: "discount", label: "折扣券", tag: "danger" }
]

// 权益品类选项（与 iip_coupon.category 取值一致）
const categoryOptions = [
  { value: "general", label: "通用" },
  { value: "scenic_ticket", label: "景区门票" },
  { value: "hotel", label: "酒店券" },
  { value: "dining", label: "餐饮券" },
  { value: "flight_package", label: "机票+权益包" },
  { value: "duty_free", label: "免税周边" }
]

// 赞助方类型选项（与 iip_coupon.sponsor_type 取值一致）
const sponsorTypeOptions = [
  { value: "platform", label: "平台" },
  { value: "bank", label: "银行" },
  { value: "merchant", label: "商户" }
]

const couponList = ref([])
const merchantOptions = ref([])
const open = ref(false)
const loading = ref(true)
const exportLoading = ref(false)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    couponName: undefined,
    couponType: undefined,
    category: undefined,
    status: undefined
  },
  rules: {
    couponName: [{ required: true, message: "券名称不能为空", trigger: "blur" }],
    couponType: [{ required: true, message: "券类型不能为空", trigger: "change" }],
    category: [{ required: true, message: "权益品类不能为空", trigger: "change" }],
    sponsorType: [{ required: true, message: "赞助方类型不能为空", trigger: "change" }],
    sponsorName: [{ validator: validateSponsorName, trigger: "blur" }],
    pointsCost: [{ required: true, message: "兑换所需积分不能为空", trigger: "blur" }],
    totalStock: [{ required: true, message: "总库存不能为空", trigger: "blur" }],
    validType: [{ required: true, message: "有效期类型不能为空", trigger: "change" }],
    validRange: [{ validator: validateValidRange, trigger: "change" }],
    validDays: [{ validator: validateValidDays, trigger: "blur" }],
    thresholdAmount: [{ validator: validateThresholdAmount, trigger: "blur" }],
    discountAmount: [{ validator: validateDiscountAmount, trigger: "blur" }],
    status: [{ required: true, message: "状态不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function couponTypeLabel(type) {
  const item = couponTypeOptions.find(option => option.value === type)
  return item ? item.label : type
}

function couponTypeTag(type) {
  const item = couponTypeOptions.find(option => option.value === type)
  return item ? item.tag : "info"
}

/** 权益品类文案（旧数据无品类时按通用展示） */
function categoryLabel(category) {
  const item = categoryOptions.find(option => option.value === category)
  return item ? item.label : "通用"
}

/** 赞助方类型文案 */
function sponsorTypeLabel(type) {
  const item = sponsorTypeOptions.find(option => option.value === type)
  return item ? item.label : "平台"
}

/** 赞助方列展示：平台券只显示类型，银行/商户券拼接赞助方名称 */
function sponsorLabel(row) {
  const typeLabel = sponsorTypeLabel(row.sponsorType)
  if (!row.sponsorType || row.sponsorType === "platform") {
    return typeLabel
  }
  return row.sponsorName ? `${typeLabel}·${row.sponsorName}` : typeLabel
}

/** 切换赞助方类型：平台券清空赞助方名称与校验状态，避免提交脏数据 */
function handleSponsorTypeChange(value) {
  if (value === "platform") {
    form.value.sponsorName = undefined
  }
  proxy.$refs["couponRef"] && proxy.$refs["couponRef"].clearValidate(["sponsorName"])
}

/** 赞助方类型为银行/商户时赞助方名称必填 */
function validateSponsorName(rule, value, callback) {
  if (form.value.sponsorType && form.value.sponsorType !== "platform" && (!value || !value.trim())) {
    callback(new Error("赞助方类型为银行/商户时，赞助方名称不能为空"))
  } else {
    callback()
  }
}

/** 固定区间有效期必须填写起止时间（对齐后端 validateValidRule） */
function validateValidRange(rule, value, callback) {
  if (form.value.validType === "fixed" && (!Array.isArray(value) || !value[0] || !value[1])) {
    callback(new Error("固定区间有效期必须填写开始与结束时间"))
  } else {
    callback()
  }
}

/** 领取后N天有效期必须填写大于0的天数（对齐后端 validateValidRule） */
function validateValidDays(rule, value, callback) {
  if (form.value.validType === "days" && (value === undefined || value === null || value <= 0)) {
    callback(new Error("领取后N天有效期必须填写大于0的有效天数"))
  } else {
    callback()
  }
}

/** 满减券必须填写门槛金额（对齐后端 validateCouponRules） */
function validateThresholdAmount(rule, value, callback) {
  if (form.value.couponType === "full_reduction" && (value === undefined || value === null)) {
    callback(new Error("满减券必须填写满减门槛金额"))
  } else {
    callback()
  }
}

/** 满减券必须填写面额（对齐后端 validateCouponRules） */
function validateDiscountAmount(rule, value, callback) {
  if (form.value.couponType === "full_reduction" && (value === undefined || value === null)) {
    callback(new Error("满减券必须填写满减面额"))
  } else {
    callback()
  }
}

/** 查询券列表 */
function getList() {
  loading.value = true
  listCoupon(queryParams.value).then(response => {
    couponList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 查询商户下拉（可选绑定商户，不选=平台通用券） */
function getMerchantOptions() {
  listMerchantOptions().then(response => {
    merchantOptions.value = response.rows
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
    couponId: undefined,
    couponName: undefined,
    couponType: "ticket",
    category: "general",
    sponsorType: "platform",
    sponsorName: undefined,
    targetName: undefined,
    pointsCost: undefined,
    totalStock: -1,
    remainStock: -1,
    perMemberLimit: 1,
    exchangeRange: [],
    validType: "fixed",
    validRange: [],
    validDays: 1,
    thresholdAmount: undefined,
    discountAmount: undefined,
    merchantId: undefined,
    coverImage: undefined,
    useDesc: undefined,
    sort: 0,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("couponRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.couponId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 总库存变更时联动剩余库存：-1 不限则剩余也为 -1；由不限切回限量时剩余跟随新总库存 */
function handleTotalStockChange(value, oldValue) {
  if (value === -1) {
    form.value.remainStock = -1
  } else if (oldValue === -1 && value !== undefined && value !== null) {
    form.value.remainStock = value
  }
}

/** 切换券类型：非满减券清空满减字段，避免提交脏数据 */
function handleCouponTypeChange(value) {
  if (value !== "full_reduction") {
    form.value.thresholdAmount = undefined
    form.value.discountAmount = undefined
  }
}

/** 切换有效期类型：清空另一套字段，避免提交脏数据 */
function handleValidTypeChange(value) {
  if (value === "fixed") {
    form.value.validDays = undefined
  } else {
    form.value.validRange = []
  }
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加券"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const couponId = row.couponId || ids.value
  getCoupon(couponId).then(response => {
    form.value = response.data
    // 旧数据可能无新增品类/赞助方字段，补默认值保证表单状态完整
    form.value.category = response.data.category || "general"
    form.value.sponsorType = response.data.sponsorType || "platform"
    form.value.exchangeRange = response.data.exchangeStartTime && response.data.exchangeEndTime
      ? [response.data.exchangeStartTime, response.data.exchangeEndTime]
      : []
    form.value.validRange = response.data.validStartTime && response.data.validEndTime
      ? [response.data.validStartTime, response.data.validEndTime]
      : []
    open.value = true
    title.value = "修改券"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["couponRef"].validate(valid => {
    if (valid) {
      const submitData = { ...form.value }
      // 兑换窗口：范围组件值拆为起止字段，不填传 null
      submitData.exchangeStartTime = Array.isArray(form.value.exchangeRange) && form.value.exchangeRange[0] ? form.value.exchangeRange[0] : null
      submitData.exchangeEndTime = Array.isArray(form.value.exchangeRange) && form.value.exchangeRange[1] ? form.value.exchangeRange[1] : null
      // 有效期：按类型只提交对应一套字段，另一套置空
      if (submitData.validType === "fixed") {
        submitData.validStartTime = Array.isArray(form.value.validRange) && form.value.validRange[0] ? form.value.validRange[0] : null
        submitData.validEndTime = Array.isArray(form.value.validRange) && form.value.validRange[1] ? form.value.validRange[1] : null
        submitData.validDays = null
      } else {
        submitData.validStartTime = null
        submitData.validEndTime = null
      }
      // 非满减券不提交满减字段
      if (submitData.couponType !== "full_reduction") {
        submitData.thresholdAmount = null
        submitData.discountAmount = null
      }
      // 平台券清空赞助方名称（按空串覆盖更新，避免残留脏数据）
      if (submitData.sponsorType === "platform") {
        submitData.sponsorName = ""
      }
      // 不限库存时剩余库存固定为 -1
      if (submitData.totalStock === -1) {
        submitData.remainStock = -1
      }
      delete submitData.exchangeRange
      delete submitData.validRange
      if (submitData.couponId != undefined) {
        updateCoupon(submitData).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        // 新增时剩余库存由后端按总库存自动初始化，无需提交
        delete submitData.remainStock
        addCoupon(submitData).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作（已有兑换记录的券后端禁止删除，会返回错误提示） */
function handleDelete(row) {
  const couponIds = row.couponId || ids.value
  proxy.$modal.confirm('是否确认删除券编号为"' + couponIds + '"的数据项？已有兑换记录的券将被后端拒绝删除。').then(function () {
    return delCoupon(couponIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作（后端为 GET 导出，request 返回 blob 后自行保存） */
function handleExport() {
  proxy.$modal.confirm("是否确认导出所有券数据项？").then(() => {
    exportLoading.value = true
    return exportCoupon(queryParams.value)
  }).then(data => {
    if (blobValidate(data)) {
      const blob = new Blob([data])
      saveAs(blob, `coupon_${new Date().getTime()}.xlsx`)
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
getMerchantOptions()
</script>

<style scoped>
.iip-form-tip {
  font-size: 12px;
  line-height: 1.4;
  color: var(--ui-text-secondary, #909399);
}
</style>
