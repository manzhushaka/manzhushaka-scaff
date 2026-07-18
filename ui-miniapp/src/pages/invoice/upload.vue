<template>
  <view class="page">
    <!-- 积分条：深色卡 + 金色大数字 -->
    <view class="iip-dark-card hero">
      <view class="hero__main">
        <view class="hero__lab">你已拥有（分）</view>
        <view v-if="userStore.isLogin" class="hero__num iip-gold iip-num">{{ pointsText }}</view>
        <view v-else class="hero__tip">登录后查看积分余额</view>
      </view>
      <view class="hero__link iip-gold" @click="onHeroLink">
        {{ userStore.isLogin ? '查询积分明细 ›' : '去登录 ›' }}
      </view>
    </view>

    <template v-if="userStore.isLogin">
      <!-- 虚线上传区 -->
      <view class="dropbox" @click="chooseImage()">
        <template v-if="!preview">
          <view class="dropbox__cam">
            <view class="dropbox__cam-icon" :style="{ backgroundImage: icons.camera }"></view>
          </view>
          <view class="dropbox__title">上传发票</view>
          <view class="dropbox__desc">支持增值税普票 / 电子发票（数电票）</view>
          <view class="dropbox__desc">发票抬头须与实名信息一致</view>
        </template>
        <template v-else>
          <image class="dropbox__thumb" :src="preview" mode="aspectFill" />
          <view class="dropbox__retip">已选择发票图片，点击重新选择</view>
        </template>
      </view>

      <!-- 拍照 / 相册双入口 -->
      <view class="btns">
        <button class="iip-btn btns__item" @click="chooseImage('camera')">拍照上传</button>
        <button class="iip-btn iip-btn--ghost btns__item" @click="chooseImage('album')">相册 / PDF</button>
      </view>

      <!-- 发票信息表单（选图后展开） -->
      <view v-if="preview" class="iip-card form">
        <view class="form__item">
          <view class="form__label">发票号码 <text class="form__required">*</text></view>
          <input class="form__input" v-model.trim="form.invoiceNo" maxlength="30" placeholder="请输入发票号码" />
        </view>
        <view class="form__item">
          <view class="form__label">发票代码</view>
          <input class="form__input" v-model.trim="form.invoiceCode" maxlength="20" placeholder="请输入发票代码（选填）" />
        </view>
        <view class="form__item">
          <view class="form__label">发票金额（元） <text class="form__required">*</text></view>
          <input class="form__input" v-model="form.amount" type="digit" placeholder="请输入发票金额" />
        </view>
        <view class="form__item">
          <view class="form__label">开票日期 <text class="form__required">*</text></view>
          <picker mode="date" :value="form.invoiceDate" @change="onDateChange">
            <view class="form__input form__input--picker" :class="{ 'form__input--empty': !form.invoiceDate }">
              {{ form.invoiceDate || '请选择开票日期' }}
            </view>
          </picker>
        </view>
        <view class="form__item">
          <view class="form__label">商户名称 <text class="form__required">*</text></view>
          <input class="form__input" v-model.trim="form.merchantName" maxlength="128" placeholder="请输入开票商户名称" />
        </view>
        <button class="iip-btn form__submit" :class="{ 'is-disabled': submitting }" :disabled="submitting" @click="handleSubmit">
          {{ submitting ? '提交中…' : '提交发票' }}
        </button>
      </view>

      <!-- 已传查询 -->
      <view class="recent">
        <view class="recent__head">
          <view class="iip-sect">已传查询</view>
          <view class="recent__more" @click="goList">查看全部 ›</view>
        </view>
        <view class="iip-card recent__card">
          <view class="inv" v-for="item in recentList" :key="item.invoiceId">
            <view class="inv__ficon">
              <view class="inv__ficon-icon" :style="{ backgroundImage: icons.invoice }"></view>
            </view>
            <view class="inv__det">
              <view class="inv__name">{{ item.merchantName }}</view>
              <view class="inv__meta">{{ metaText(item) }}</view>
              <view v-if="item.status === '2' && item.auditRemark" class="inv__reject">
                驳回原因：{{ item.auditRemark }}
              </view>
            </view>
            <view class="inv__side">
              <view class="inv__pts iip-num" :class="{ 'inv__pts--none': !hasPoints(item) }">{{ pointsOf(item) }}</view>
              <view class="iip-chip" :class="statusMeta(item.status).cls">{{ statusMeta(item.status).text }}</view>
            </view>
          </view>
          <view v-if="!recentLoading && !recentList.length" class="recent__empty">暂无已传发票，上传第一张吧</view>
        </view>
      </view>
    </template>

    <!-- 游客引导 -->
    <view v-else class="iip-empty">
      <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
      <text class="iip-empty__title">登录后上传发票换积分</text>
      <text class="iip-empty__desc">发票审核通过后即可获得积分</text>
      <button class="iip-btn guest__btn" @click="goLogin">去登录</button>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { submitInvoice, getInvoiceList } from '@/api/invoice.js'
import { uploadFile, redirectToLogin } from '@/common/request.js'
import { resolveFileUrl } from '@/common/config.js'
import { fmtDate } from '@/common/format.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

const form = reactive({
  invoiceNo: '',
  invoiceCode: '',
  amount: '',
  invoiceDate: '',
  merchantName: ''
})

/** 已上传的发票图片相对地址（提交用 fileName） */
const imageUrl = ref('')
/** 预览地址（本地临时路径或后端完整 URL） */
const preview = ref('')
const submitting = ref(false)

/** 最近已传发票（全部状态取前 5 条） */
const recentList = ref([])
const recentLoading = ref(false)

/** 当前可用积分（千分位展示） */
const pointsText = computed(() => {
  return String(userStore.availablePoints).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
})

/**
 * 开票日期选择回调。
 *
 * @param {object} e picker change 事件
 */
function onDateChange(e) {
  form.invoiceDate = e.detail.value
}

/**
 * 积分条右侧链接：已登录跳积分明细，游客去登录。
 */
function onHeroLink() {
  if (userStore.isLogin) {
    uni.navigateTo({ url: '/pages/points/records' })
    return
  }
  goLogin()
}

/**
 * 跳转登录页。
 */
function goLogin() {
  redirectToLogin()
}

/**
 * 跳转发票列表页。
 */
function goList() {
  uni.navigateTo({ url: '/pages/invoice/list' })
}

/**
 * 选图并上传到 /common/upload（带 token），成功后将 fileName 作为 imageUrl 提交。
 *
 * @param {string} [sourceType] camera 拍照 / album 相册，缺省两者皆可
 */
function chooseImage(sourceType) {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: sourceType ? [sourceType] : ['album', 'camera'],
    success: async (res) => {
      const filePath = res.tempFilePaths[0]
      preview.value = filePath
      try {
        // 上传响应体：{code, msg, url, fileName, newFileName}
        const body = await uploadFile(filePath)
        imageUrl.value = body.fileName
        preview.value = resolveFileUrl(body.url)
      } catch (e) {
        preview.value = ''
        imageUrl.value = ''
      }
    }
  })
}

/**
 * 表单校验并提交发票；成功后 toast、清空表单并刷新下方已传列表。
 */
async function handleSubmit() {
  if (submitting.value) {
    return
  }
  if (!imageUrl.value) {
    uni.showToast({ title: '请先上传发票图片', icon: 'none' })
    return
  }
  if (!form.invoiceNo) {
    uni.showToast({ title: '请输入发票号码', icon: 'none' })
    return
  }
  const amount = Number(form.amount)
  if (!form.amount || isNaN(amount) || amount <= 0) {
    uni.showToast({ title: '请输入大于 0 的发票金额', icon: 'none' })
    return
  }
  if (!form.invoiceDate) {
    uni.showToast({ title: '请选择开票日期', icon: 'none' })
    return
  }
  if (!form.merchantName) {
    uni.showToast({ title: '请输入商户名称', icon: 'none' })
    return
  }
  const payload = {
    invoiceNo: form.invoiceNo,
    amount,
    merchantName: form.merchantName,
    imageUrl: imageUrl.value,
    invoiceDate: form.invoiceDate
  }
  if (form.invoiceCode) {
    payload.invoiceCode = form.invoiceCode
  }
  submitting.value = true
  try {
    await submitInvoice(payload)
    uni.showToast({ title: '提交成功，等待审核', icon: 'success' })
    resetForm()
    loadRecent()
  } catch (e) {
    // 错误提示已由 request 封装统一 toast（如发票号码重复）
  } finally {
    submitting.value = false
  }
}

/**
 * 提交成功后清空表单与已选图片。
 */
function resetForm() {
  form.invoiceNo = ''
  form.invoiceCode = ''
  form.amount = ''
  form.invoiceDate = ''
  form.merchantName = ''
  imageUrl.value = ''
  preview.value = ''
}

/**
 * 加载最近已传发票（全部状态按创建时间倒序取前 5 条）。
 */
async function loadRecent() {
  recentLoading.value = true
  try {
    const list = await getInvoiceList()
    recentList.value = list.slice(0, 5)
  } catch (e) {
    // 错误提示已由 request 封装统一处理
  } finally {
    recentLoading.value = false
  }
}

/**
 * 状态 chip 映射（待审核金 / 核验通过绿 / 已驳回红）。
 *
 * @param {string} status 0待审核 1已通过 2已驳回
 * @returns {{text: string, cls: string}} 文案与样式类
 */
function statusMeta(status) {
  const map = {
    0: { text: '待审核', cls: 'iip-chip--y' },
    1: { text: '核验通过', cls: 'iip-chip--g' },
    2: { text: '已驳回', cls: 'iip-chip--r' }
  }
  return map[status] || { text: '未知', cls: 'iip-chip--gray' }
}

/**
 * 发票号遮罩：保留前 4 后 4，中间以 **** 代替。
 *
 * @param {string} no 发票号码
 * @returns {string} 遮罩后的号码
 */
function maskNo(no) {
  const s = String(no || '')
  if (!s) {
    return '—'
  }
  if (s.length <= 8) {
    return s
  }
  return s.slice(0, 4) + '****' + s.slice(-4)
}

/**
 * 行次级信息：开票日期 · 发票号遮罩。
 *
 * @param {object} item InvoiceResult
 * @returns {string} 次级信息文案
 */
function metaText(item) {
  const parts = []
  const date = fmtDate(item.invoiceDate)
  if (date) {
    parts.push(date)
  }
  parts.push('发票号 ' + maskNo(item.invoiceNo))
  return parts.join(' · ')
}

/**
 * 是否有已入账积分（仅核验通过且积分非空时展示 +N）。
 *
 * @param {object} item InvoiceResult
 * @returns {boolean} 是否展示积分
 */
function hasPoints(item) {
  return item.status === '1' && item.points !== null && item.points !== undefined
}

/**
 * 右侧积分文案：核验通过显示 +积分，其余显示 —。
 *
 * @param {object} item InvoiceResult
 * @returns {string} 积分文案
 */
function pointsOf(item) {
  return hasPoints(item) ? '+' + item.points : '—'
}

onShow(() => {
  if (userStore.isLogin) {
    userStore.fetchProfile().catch(() => {})
    loadRecent()
  } else {
    recentList.value = []
  }
})
</script>

<style scoped>
.page {
  padding: var(--iip-sp-24);
}

/* 积分条 */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--iip-sp-32);
}
.hero__lab {
  font-size: var(--iip-fs-22);
  opacity: 0.65;
}
.hero__num {
  margin-top: 4rpx;
  font-size: 60rpx;
  font-weight: 900;
  line-height: 1.2;
}
.hero__tip {
  margin-top: var(--iip-sp-8);
  font-size: var(--iip-fs-26);
  opacity: 0.85;
}
.hero__link {
  flex-shrink: 0;
  margin-left: var(--iip-sp-24);
  font-size: var(--iip-fs-22);
  text-decoration: underline;
  text-underline-offset: 6rpx;
}

/* 虚线上传区（虚线色值对齐原型 s-upload） */
.dropbox {
  margin-top: var(--iip-sp-24);
  padding: var(--iip-sp-48) var(--iip-sp-32);
  text-align: center;
  background-color: var(--iip-color-surface);
  border: 2rpx dashed #d8c9a8;
  border-radius: var(--iip-radius-32);
}
.dropbox__cam {
  width: 112rpx;
  height: 112rpx;
  margin: 0 auto 20rpx;
  border-radius: 50%;
  background-color: var(--iip-color-chip-gold);
  display: flex;
  align-items: center;
  justify-content: center;
}
.dropbox__cam-icon {
  width: 52rpx;
  height: 52rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.dropbox__title {
  font-size: var(--iip-fs-30);
  font-weight: 800;
  color: var(--iip-color-ink);
}
.dropbox__desc {
  margin-top: 8rpx;
  font-size: var(--iip-fs-22);
  line-height: 1.6;
  color: var(--iip-color-text-secondary);
}
.dropbox__thumb {
  width: 100%;
  height: 320rpx;
  border-radius: var(--iip-radius-24);
}
.dropbox__retip {
  margin-top: var(--iip-sp-16);
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}

/* 拍照 / 相册双按钮 */
.btns {
  margin-top: var(--iip-sp-24);
  display: flex;
  gap: 20rpx;
}
.btns__item {
  flex: 1;
  padding-left: 0;
  padding-right: 0;
}

/* 表单卡 */
.form {
  margin-top: var(--iip-sp-24);
  padding: var(--iip-sp-32);
}
.form__item {
  margin-bottom: var(--iip-sp-24);
}
.form__label {
  margin-bottom: var(--iip-sp-16);
  font-size: var(--iip-fs-26);
  color: var(--iip-color-text-secondary);
}
.form__required {
  color: var(--iip-color-primary);
}
.form__input {
  height: 88rpx;
  padding: 0 var(--iip-sp-24);
  font-size: var(--iip-fs-28);
  color: var(--iip-color-ink);
  background-color: var(--iip-color-surface);
  border: 1rpx solid var(--iip-color-line);
  border-radius: var(--iip-radius-16);
  display: flex;
  align-items: center;
}
.form__input--empty {
  color: var(--iip-color-text-faint);
}
.form__submit {
  margin-top: var(--iip-sp-8);
}

/* 已传查询 */
.recent {
  margin-top: var(--iip-sp-32);
}
.recent__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--iip-sp-16);
}
.recent__more {
  font-size: var(--iip-fs-24);
  color: var(--iip-color-gold);
}
.recent__card {
  padding: 0 var(--iip-sp-32);
}
.recent__empty {
  padding: var(--iip-sp-48) 0;
  text-align: center;
  font-size: var(--iip-fs-24);
  color: var(--iip-color-text-secondary);
}

/* 发票行 */
.inv {
  display: flex;
  align-items: center;
  gap: var(--iip-sp-24);
  padding: 28rpx 0;
  border-bottom: 1rpx solid var(--iip-color-line);
}
.inv:last-child {
  border-bottom: none;
}
.inv__ficon {
  width: 76rpx;
  height: 76rpx;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--iip-color-cream);
  border: 1rpx solid var(--iip-color-line);
  border-radius: 20rpx;
}
.inv__ficon-icon {
  width: 36rpx;
  height: 36rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.inv__det {
  flex: 1;
  min-width: 0;
}
.inv__name {
  font-size: var(--iip-fs-26);
  font-weight: 700;
  color: var(--iip-color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.inv__meta {
  margin-top: 6rpx;
  font-size: var(--iip-fs-20);
  color: var(--iip-color-text-secondary);
}
.inv__reject {
  margin-top: var(--iip-sp-8);
  font-size: var(--iip-fs-22);
  color: var(--iip-color-primary);
}
.inv__side {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--iip-sp-8);
}
.inv__pts {
  font-size: var(--iip-fs-30);
  font-weight: 700;
  color: var(--iip-color-gold);
}
.inv__pts--none {
  color: var(--iip-color-text-faint);
}

/* 游客登录按钮 */
.guest__btn {
  margin-top: var(--iip-sp-32);
}
</style>
