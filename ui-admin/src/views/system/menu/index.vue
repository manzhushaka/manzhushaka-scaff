<template>
   <div class="app-container ui-list-page">
      <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" class="ui-filter-card">
         <a-form-item label="菜单名称" field="menuName">
            <a-input
               v-model="queryParams.menuName"
               placeholder="请输入菜单名称"
               allow-clear
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="状态" field="status">
            <a-select v-model="queryParams.status" placeholder="菜单状态" allow-clear style="width: 200px">
               <a-option
                  v-for="dict in sys_normal_disable"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </a-select>
         </a-form-item>
         <a-form-item>
            <a-button type="primary" @click="handleQuery"><template #icon><Search /></template>搜索</a-button>
            <a-button @click="resetQuery"><template #icon><Refresh /></template>重置</a-button>
         </a-form-item>
      </a-form>

      <a-row class="ui-action-bar">
         <a-col :span="1.5">
            <a-button



               @click="handleAdd"
               v-hasPermi="['system:menu:add']"
             type="outline"><template #icon><Plus /></template>新增</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="warning"


               @click="handleSaveSort"
               v-hasPermi="['system:menu:edit']"
             type="outline"><template #icon><Check /></template>保存排序</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button



               @click="toggleExpandAll"
             type="outline"><template #icon><Sort /></template>展开/折叠</a-button>
         </a-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </a-row>

      <div class="ui-table-card">
      <a-table
         v-if="refreshTable"
         :loading="loading"
         :data="menuList"
         row-key="menuId"
         :default-expand-all-rows="isExpandAll"
       :pagination="false">
         <template #columns>
         <a-table-column data-index="menuName" title="菜单名称" ellipsis width="220" tooltip>
            <template #cell="{ record, rowIndex }">
               <svg-icon :icon-class="record.icon" />
               <span class="ml5">{{ record.menuName }}</span>
            </template>
         </a-table-column>
         <a-table-column data-index="menuName" title="类型" ellipsis width="100" tooltip>
            <template #cell="{ record, rowIndex }">
               <a-tag v-if="record.menuType === 'M' && record.isFrame === '0'" color="red" size="small">外链</a-tag>
               <a-tag v-else-if="record.menuType === 'M'" color="arcoblue" size="small">目录</a-tag>
               <a-tag v-else-if="record.menuType === 'C' && record.isFrame === '0'" color="red" size="small">外链</a-tag>
               <a-tag v-else-if="record.menuType === 'C'" color="green" size="small">菜单</a-tag>
               <a-tag v-else-if="record.menuType === 'F'" color="orange" size="small">按钮</a-tag>
            </template>
         </a-table-column>
         <a-table-column data-index="orderNum" title="排序" width="200">
            <template #cell="{ record, rowIndex }">
               <a-input-number v-model="record.orderNum" controls-position="right" :min="0" style="width: 88px" />
            </template>
         </a-table-column>
         <a-table-column data-index="perms" title="权限标识" ellipsis tooltip />
         <a-table-column data-index="component" title="组件路径" ellipsis tooltip />
         <a-table-column data-index="status" title="状态" width="80">
            <template #cell="{ record, rowIndex }">
               <dict-tag :options="sys_normal_disable" :value="record.status" />
            </template>
         </a-table-column>
         <a-table-column title="操作" align="center" width="240" cell-class="small-padding fixed-width">
            <template #cell="{ record, rowIndex }">
               <a-button type="text" @click="handleUpdate(record)" v-hasPermi="['system:menu:edit']"><template #icon><Edit /></template>修改</a-button>
               <a-button type="text" @click="handleAdd(record)" v-hasPermi="['system:menu:add']"><template #icon><Plus /></template>新增</a-button>
               <a-button type="text" status="danger" @click="handleDelete(record)" v-hasPermi="['system:menu:remove']"><template #icon><Delete /></template>删除</a-button>
            </template>
         </a-table-column>
         </template>
      </a-table>
      </div>

      <!-- 添加或修改菜单抽屉 -->
      <a-drawer :title="title" v-model:visible="open" size="640px" render-to-body>
         <template #default>
         <a-form ref="menuRef" :model="form" :rules="rules" :label-col-props="{ flex: '100px' }">
            <a-row>
               <a-col :span="24">
                  <a-form-item label="上级菜单">
                     <a-tree-select
                        v-model="form.parentId"
                        :data="menuOptions"
                        :field-names="{ key: 'menuId', title: 'menuName', children: 'children' }"
                        placeholder="选择上级菜单"
                     />
                  </a-form-item>
               </a-col>
               <a-col :span="24">
                  <a-form-item label="菜单类型" field="menuType">
                     <a-radio-group v-model="form.menuType">
                        <a-radio value="M">目录</a-radio>
                        <a-radio value="C">菜单</a-radio>
                        <a-radio value="F">按钮</a-radio>
                     </a-radio-group>
                  </a-form-item>
               </a-col>
               <a-col :span="12" v-if="form.menuType != 'F'">
                  <a-form-item label="菜单图标" field="icon">
                     <a-popover
                        placement="bottom-start"
                        :width="540"
                        trigger="click"
                     >
                        <a-input v-model="form.icon" placeholder="点击选择图标" @blur="showSelectIcon" readonly>
                              <template #prefix>
                                 <svg-icon
                                    v-if="form.icon"
                                    :icon-class="form.icon"
                                    style="height: 32px;width: 16px;"
                                 />
                                 <span v-else style="height: 32px;width: 16px;"><search /></span>
                              </template>
                        </a-input>
                        <template #content>
                           <icon-select ref="iconSelectRef" @selected="selected" :active-icon="form.icon" />
                        </template>
                     </a-popover>
                  </a-form-item>
               </a-col>
               <a-col :span="12">
                  <a-form-item label="显示排序" field="orderNum">
                     <a-input-number v-model="form.orderNum" controls-position="right" :min="0" />
                  </a-form-item>
               </a-col>
               <a-col :span="12">
                  <a-form-item label="菜单名称" field="menuName">
                     <a-input v-model="form.menuName" placeholder="请输入菜单名称" />
                  </a-form-item>
               </a-col>
               <a-col :span="12" v-if="form.menuType == 'C'">
                  <a-form-item field="routeName">
                     <template #label>
                        <span>
                           <a-tooltip content="默认不填则和路由地址相同：如地址为：`user`，则名称为`User`（注意：因为router会删除名称相同路由，为避免名字的冲突，特殊情况下请自定义，保证唯一性）" position="top">
                              <span><question-filled /></span>
                           </a-tooltip>
                           路由名称
                        </span>
                     </template>
                     <a-input v-model="form.routeName" placeholder="请输入路由名称" />
                  </a-form-item>
               </a-col>
               <a-col :span="12" v-if="form.menuType != 'F'">
                  <a-form-item>
                     <template #label>
                        <span>
                           <a-tooltip content="选择是外链则路由地址需要以`http(s)://`开头" position="top">
                              <span><question-filled /></span>
                           </a-tooltip>是否外链
                        </span>
                     </template>
                     <a-radio-group v-model="form.isFrame">
                        <a-radio value="0">是</a-radio>
                        <a-radio value="1">否</a-radio>
                     </a-radio-group>
                  </a-form-item>
               </a-col>
               <a-col :span="12" v-if="form.menuType != 'F'">
                  <a-form-item field="path">
                     <template #label>
                        <span>
                           <a-tooltip content="访问的路由地址，如：`user`，如外网地址需内链访问则以`http(s)://`开头" position="top">
                              <span><question-filled /></span>
                           </a-tooltip>
                           路由地址
                        </span>
                     </template>
                     <a-input v-model="form.path" placeholder="请输入路由地址" />
                  </a-form-item>
               </a-col>
               <a-col :span="12" v-if="form.menuType == 'C'">
                  <a-form-item field="component">
                     <template #label>
                        <span>
                           <a-tooltip content="访问的组件路径，如：`system/user/index`，默认在`views`目录下" position="top">
                              <span><question-filled /></span>
                           </a-tooltip>
                           组件路径
                        </span>
                     </template>
                     <a-input v-model="form.component" placeholder="请输入组件路径" />
                  </a-form-item>
               </a-col>
               <a-col :span="12" v-if="form.menuType != 'M'">
                  <a-form-item>
                     <a-input v-model="form.perms" placeholder="请输入权限标识" maxlength="100" />
                     <template #label>
                        <span>
                           <a-tooltip content="控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasPermi('system:user:list')`)" position="top">
                              <span><question-filled /></span>
                           </a-tooltip>
                           权限字符
                        </span>
                     </template>
                  </a-form-item>
               </a-col>
               <a-col :span="12" v-if="form.menuType == 'C'">
                  <a-form-item>
                     <a-input v-model="form.query" placeholder="请输入路由参数" maxlength="255" />
                     <template #label>
                        <span>
                           <a-tooltip content='访问路由的默认传递参数，如：`{"id": 1, "name": "ry"}`' position="top">
                              <span><question-filled /></span>
                           </a-tooltip>
                           路由参数
                        </span>
                     </template>
                  </a-form-item>
               </a-col>
               <a-col :span="12" v-if="form.menuType == 'C'">
                  <a-form-item>
                     <template #label>
                        <span>
                           <a-tooltip content="选择是则会被`keep-alive`缓存，需要匹配组件的`name`和地址保持一致" position="top">
                              <span><question-filled /></span>
                           </a-tooltip>
                           是否缓存
                        </span>
                     </template>
                     <a-radio-group v-model="form.isCache">
                        <a-radio value="0">缓存</a-radio>
                        <a-radio value="1">不缓存</a-radio>
                     </a-radio-group>
                  </a-form-item>
               </a-col>
               <a-col :span="12" v-if="form.menuType != 'F'">
                  <a-form-item>
                     <template #label>
                        <span>
                           <a-tooltip content="选择隐藏则路由将不会出现在侧边栏，但仍然可以访问" position="top">
                              <span><question-filled /></span>
                           </a-tooltip>
                           显示状态
                        </span>
                     </template>
                     <a-radio-group v-model="form.visible">
                        <a-radio
                           v-for="dict in sys_show_hide"
                           :key="dict.value"
                           :value="dict.value"
                        >{{ dict.label }}</a-radio>
                     </a-radio-group>
                  </a-form-item>
               </a-col>
               <a-col :span="12">
                  <a-form-item>
                     <template #label>
                        <span>
                           <a-tooltip content="选择停用则路由将不会出现在侧边栏，也不能被访问" position="top">
                              <span><question-filled /></span>
                           </a-tooltip>
                           菜单状态
                        </span>
                     </template>
                     <a-radio-group v-model="form.status">
                        <a-radio
                           v-for="dict in sys_normal_disable"
                           :key="dict.value"
                           :value="dict.value"
                        >{{ dict.label }}</a-radio>
                     </a-radio-group>
                  </a-form-item>
               </a-col>
            </a-row>
         </a-form>
         </template>
         <template #footer>
            <div class="dialog-footer">
               <a-button type="primary" @click="submitForm">确 定</a-button>
               <a-button @click="cancel">取 消</a-button>
            </div>
         </template>
      </a-drawer>
   </div>
</template>

<script setup name="Menu">
import { addMenu, delMenu, getMenu, listMenu, updateMenu, updateMenuSort } from "@/api/system/menu"
import SvgIcon from "@/components/SvgIcon"
import IconSelect from "@/components/IconSelect"

const { proxy } = getCurrentInstance()
const { sys_show_hide, sys_normal_disable } = useDict("sys_show_hide", "sys_normal_disable")

const menuList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const title = ref("")
const menuOptions = ref([])
const isExpandAll = ref(false)
const refreshTable = ref(true)
const iconSelectRef = ref(null)
const originalOrders = ref({})

const data = reactive({
  form: {},
  queryParams: {
    menuName: undefined,
    visible: undefined
  },
  rules: {
    menuName: [{ required: true, message: "菜单名称不能为空", trigger: "blur" }],
    orderNum: [{ required: true, message: "菜单顺序不能为空", trigger: "blur" }],
    path: [{ required: true, message: "路由地址不能为空", trigger: "blur" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

/** 查询菜单列表 */
function getList() {
  loading.value = true
  listMenu(queryParams.value).then(response => {
    menuList.value = proxy.handleTree(response.data, "menuId")
    recordOriginalOrders(menuList.value)
    loading.value = false
  })
}

/** 查询菜单下拉树结构 */
function getTreeselect() {
  menuOptions.value = []
  listMenu().then(response => {
    const menu = { menuId: 0, menuName: "主类目", children: [] }
    menu.children = proxy.handleTree(response.data, "menuId")
    menuOptions.value.push(menu)
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
    menuId: undefined,
    parentId: 0,
    menuName: undefined,
    icon: undefined,
    menuType: "M",
    orderNum: undefined,
    isFrame: "1",
    isCache: "0",
    visible: "0",
    status: "0"
  }
  proxy.resetForm("menuRef")
}

/** 展示下拉图标 */
function showSelectIcon() {
  iconSelectRef.value.reset()
}

/** 选择图标 */
function selected(name) {
  form.value.icon = name
}

/** 搜索按钮操作 */
function handleQuery() {
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd(row) {
  reset()
  getTreeselect()
  if (row != null && row.menuId) {
    form.value.parentId = row.menuId
  } else {
    form.value.parentId = 0
  }
  open.value = true
  title.value = "添加菜单"
}

/** 展开/折叠操作 */
function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

/** 修改按钮操作 */
async function handleUpdate(row) {
  reset()
  await getTreeselect()
  getMenu(row.menuId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改菜单"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["menuRef"].validate(errors => {
    if (!errors) {
      if (form.value.menuId != undefined) {
        updateMenu(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addMenu(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}


/** 递归记录原始排序 */
function recordOriginalOrders(list) {
  list.forEach(item => {
    originalOrders.value[item.menuId] = item.orderNum
    if (item.children && item.children.length) {
      recordOriginalOrders(item.children)
    }
  })
}

/** 保存排序 */
function handleSaveSort() {
  const changedMenuIds = []
  const changedOrderNums = []
  const collectChanged = (list) => {
    list.forEach(item => {
      if (String(originalOrders.value[item.menuId]) !== String(item.orderNum)) {
        changedMenuIds.push(item.menuId)
        changedOrderNums.push(item.orderNum)
      }
      if (item.children && item.children.length) {
        collectChanged(item.children)
      }
    })
  }
  collectChanged(menuList.value)
  if (changedMenuIds.length === 0) {
   proxy.$modal.msgWarning("未检测到排序修改")
    return
  }
  updateMenuSort({ menuIds: changedMenuIds.join(","), orderNums: changedOrderNums.join(",") }).then(() => {
   proxy.$modal.msgSuccess("排序保存成功")
    recordOriginalOrders(menuList.value)
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除名称为"' + row.menuName + '"的数据项?').then(function() {
    return delMenu(row.menuId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>
