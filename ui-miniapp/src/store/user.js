/**
 * 用户状态（pinia）。
 *
 * token 与 member 持久化在 storage（key 与 common/request.js 中常量一致），
 * 应用启动时从 storage 恢复；登录成功写入，登出或 401 时清除。
 */
import { defineStore } from 'pinia'
import { request, setUnauthorizedHandler, TOKEN_KEY, MEMBER_KEY } from '@/common/request.js'

export const useUserStore = defineStore('user', {
  state: () => ({
    /** 登录 token（RuoYi JWT，Redis 会话） */
    token: uni.getStorageSync(TOKEN_KEY) || '',
    /** 用户资料 MemberProfileResult：{ memberId, nickname, avatar, phone, availablePoints, totalPoints } */
    member: uni.getStorageSync(MEMBER_KEY) || null
  }),
  getters: {
    /** 是否已登录 */
    isLogin: (state) => !!state.token,
    /** 可用积分（未登录或资料未加载时为 0） */
    availablePoints: (state) => (state.member && state.member.availablePoints) || 0,
    /** 累计获得积分（未登录或资料未加载时为 0） */
    totalPoints: (state) => (state.member && state.member.totalPoints) || 0
  },
  actions: {
    /**
     * 写入登录态（登录接口返回 {token, member}）。
     *
     * @param {string} token 登录 token
     * @param {object} member MemberProfileResult
     */
    setLogin(token, member) {
      this.token = token || ''
      this.member = member || null
      uni.setStorageSync(TOKEN_KEY, this.token)
      uni.setStorageSync(MEMBER_KEY, this.member)
    },
    /**
     * 拉取最新用户资料（GET /miniapp/member/profile，响应体顶层为 member）。
     *
     * @returns {Promise<object>} MemberProfileResult
     */
    async fetchProfile() {
      const res = await request({ url: '/miniapp/member/profile' })
      this.member = res.member || null
      uni.setStorageSync(MEMBER_KEY, this.member)
      return this.member
    },
    /**
     * 退出登录：清空本地登录态（后端 token 由 Redis 过期接管）。
     */
    logout() {
      this.token = ''
      this.member = null
      uni.removeStorageSync(TOKEN_KEY)
      uni.removeStorageSync(MEMBER_KEY)
    }
  }
})

/**
 * token 失效（401）时同步清理 pinia 内存登录态，
 * 避免 storage 已清但页面仍按已登录渲染。
 */
setUnauthorizedHandler(() => {
  const store = useUserStore()
  store.token = ''
  store.member = null
})
