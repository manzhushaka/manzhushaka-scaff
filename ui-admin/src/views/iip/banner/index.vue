<template>
   <div class="app-container ui-list-page">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px" class="ui-filter-card">
         <el-form-item label="标题" prop="title">
            <el-input
               v-model="queryParams.title"
               placeholder="请输入 banner 标题"
               clearable
               style="width: 220px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 160px">
               <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
               />
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
               v-hasPermi="['iip:banner:add']"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['iip:banner:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['iip:banner:remove']"
            >删除</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <div class="ui-table-card">
      <el-table v-loading="loading" :data="bannerList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="图片" align="center" width="120">
            <template #default="scope">
               <el-image
                  v-if="scope.row.imageUrl"
                  :src="bannerImgUrl(scope.row.imageUrl)"
                  :preview-src-list="[bannerImgUrl(scope.row.imageUrl)]"
                  preview-teleported
                  fit="cover"
                  class="banner-thumb"
               />
               <span v-else>—</span>
            </template>
         </el-table-column>
         <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
         <el-table-column label="跳转类型" align="center" prop="linkType" width="110">
            <template #default="scope">
               <span>{{ linkTypeLabel(scope.row.linkType) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="排序" align="center" prop="sort" width="80" />
         <el-table-column label="状态" align="center" prop="status" width="90">
            <template #default="scope">
               <el-switch
                  v-model="scope.row.status"
                  active-value="0"
                  inactive-value="1"
                  @change="handleStatusChange(scope.row)"
                  v-hasPermi="['iip:banner:edit']"
               ></el-switch>
            </template>
         </el-table-column>
         <el-table-column label="创建时间" align="center" prop="createTime" width="160">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['iip:banner:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iip:banner:remove']">删除</el-button>
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

      <!-- 添加或修改 banner 对话框 -->
      <el-dialog :title="title" v-model="open" width="560px" append-to-body>
         <el-form ref="bannerRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="标题" prop="title">
               <el-input v-model="form.title" placeholder="请输入 banner 标题" maxlength="128" />
            </el-form-item>
            <el-form-item label="banner 图片" prop="imageUrl">
               <image-upload v-model="form.imageUrl" :limit="1" />
            </el-form-item>
            <el-form-item label="跳转类型" prop="linkType">
               <el-select v-model="form.linkType" placeholder="请选择跳转类型" style="width: 100%" @change="handleLinkTypeChange">
                  <el-option
                     v-for="item in linkTypeOptions"
                     :key="item.value"
                     :label="item.label"
                     :value="item.value"
                  />
               </el-select>
            </el-form-item>
            <el-form-item v-if="form.linkType && form.linkType !== 'none'" label="跳转参数" prop="linkValue">
               <el-input v-model="form.linkValue" placeholder="选填，跳转目标附加参数" maxlength="256" />
               <div class="form-tip">纯展示类型无需填写；活动规则/积分商城如需附加参数可在此填写</div>
            </el-form-item>
            <el-form-item label="排序" prop="sort">
               <el-input-number
                  v-model="form.sort"
                  :precision="0"
                  :min="0"
                  :max="9999"
                  controls-position="right"
                  style="width: 100%"
               />
               <div class="form-tip">数值越小展示越靠前</div>
            </el-form-item>
            <el-form-item label="状态" prop="status">
               <el-radio-group v-model="form.status">
                  <el-radio value="0">启用</el-radio>
                  <el-radio value="1">停用</el-radio>
               </el-radio-group>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="500" />
            </el-form-item>
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

<script setup name="IipBanner">
import { listBanner, getBanner, delBanner, addBanner, updateBanner } from "@/api/iip/banner"

const { proxy } = getCurrentInstance()

// banner 状态选项（0 启用 1 停用，与 iip_banner.status 一致）
const statusOptions = [
   { value: '0', label: '启用' },
   { value: '1', label: '停用' }
]

// 跳转类型选项（与 iip_banner.link_type 取值一致）
const linkTypeOptions = [
   { value: 'none', label: '纯展示' },
   { value: 'rules', label: '活动规则' },
   { value: 'mall', label: '积分商城' }
]

const bannerList = ref([])
const open = ref(false)
const loading = ref(true)
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
    title: undefined,
    status: undefined
  },
  rules: {
    title: [{ required: true, message: "标题不能为空", trigger: "blur" }],
    imageUrl: [{ required: true, message: "banner 图片不能为空", trigger: "change" }],
    linkType: [{ required: true, message: "跳转类型不能为空", trigger: "change" }],
    sort: [{ required: true, message: "排序不能为空", trigger: "blur" }],
    status: [{ required: true, message: "状态不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 跳转类型文案 */
function linkTypeLabel(type) {
  const item = linkTypeOptions.find(option => option.value === type)
  return item ? item.label : "纯展示"
}

/** 图片地址补全：相对路径（/profile/...）拼接待接口前缀，与 ImageUpload 组件显示逻辑一致 */
const baseApi = import.meta.env.VITE_APP_BASE_API
function bannerImgUrl(url) {
  if (!url) {
    return ""
  }
  return /^https?:\/\//.test(url) ? url : baseApi + url
}

/** 切换跳转类型：纯展示时清理跳转参数与校验状态，避免提交脏数据 */
function handleLinkTypeChange(value) {
  if (value === "none") {
    form.value.linkValue = undefined
  }
  proxy.$refs["bannerRef"] && proxy.$refs["bannerRef"].clearValidate(["linkValue"])
}

/** 查询 banner 列表 */
function getList() {
  loading.value = true
  listBanner(queryParams.value).then(response => {
    bannerList.value = response.rows
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
    bannerId: undefined,
    title: undefined,
    imageUrl: undefined,
    linkType: "none",
    linkValue: undefined,
    sort: 0,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("bannerRef")
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
  ids.value = selection.map(item => item.bannerId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加 banner"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const bannerId = row.bannerId || ids.value
  getBanner(bannerId).then(response => {
    form.value = response.data
    // 旧数据可能无跳转类型，补默认值保证表单状态完整
    form.value.linkType = response.data.linkType || "none"
    open.value = true
    title.value = "修改 banner"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["bannerRef"].validate(valid => {
    if (valid) {
      // 纯展示类型清空跳转参数，避免残留脏数据（后端按空串覆盖更新）
      if (form.value.linkType === "none") {
        form.value.linkValue = ""
      }
      if (form.value.bannerId != undefined) {
        updateBanner(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addBanner(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 状态快速启停（启用/停用切换，走整体修改接口） */
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + text + '""' + (row.title || row.bannerId) + '"吗?').then(function () {
    return updateBanner(row)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(function () {
    row.status = row.status === "0" ? "1" : "0"
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const bannerIds = row.bannerId || ids.value
  proxy.$modal.confirm('是否确认删除 banner 编号为"' + bannerIds + '"的数据项？').then(function () {
    return delBanner(bannerIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

<style lang="scss" scoped>
.banner-thumb {
  width: 88px;
  height: 44px;
  border-radius: 4px;
  vertical-align: middle;
}

.form-tip {
  width: 100%;
  color: var(--ui-text-muted);
  font-size: 12px;
  line-height: 1.6;
}
</style>
