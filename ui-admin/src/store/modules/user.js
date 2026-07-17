import router from '@/router'
import cache from '@/plugins/cache'
import { ElMessageBox, } from 'element-plus'
import { login as loginApi, logout as logoutApi, getInfo as getInfoApi } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { isHttp, isEmpty } from "@/utils/validate"
import useLockStore from '@/store/modules/lock'
import defAva from '@/assets/images/profile.jpg'

const useUserStore = defineStore(
  'user',
  {
    state: () => ({
      token: getToken(),
      id: '',
      name: '',
      nickName: '',
      avatar: '',
      roles: [],
      permissions: [],
      forceChangePassword: false,
      passwordExpired: false
    }),
    actions: {
      // 登录
      async login(userInfo) {
        const username = userInfo.username.trim()
        const password = userInfo.password
        const code = userInfo.code
        const uuid = userInfo.uuid
        const res = await loginApi(username, password, code, uuid)
        setToken(res.token)
        this.token = res.token
        useLockStore().unlockScreen()
      },
      // 获取用户信息
      async getInfo() {
        const res = await getInfoApi()
        const user = res.user
        let avatar = user.avatar || ""
        if (!isHttp(avatar)) {
          avatar = isEmpty(avatar) ? defAva : import.meta.env.VITE_APP_BASE_API + avatar
        }
        if (res.roles && res.roles.length > 0) {
          this.roles = res.roles
          this.permissions = res.permissions
        } else {
          this.roles = ['ROLE_DEFAULT']
          this.permissions = []
        }
        this.id = user.userId
        this.name = user.userName
        this.nickName = user.nickName
        this.avatar = avatar
        cache.session.set('pwrChrtype', res.pwdChrtype)
        this.setPasswordSecurityState(!!res.isDefaultModifyPwd, !!res.isPasswordExpired)
        if (res.isDefaultModifyPwd) {
          ElMessageBox.alert('您的密码还是初始密码，请修改密码！', '安全提示', {
            confirmButtonText: '确定',
            type: 'warning'
          }).then(() => {
            router.push({ name: 'Profile', params: { activeTab: 'resetPwd' } })
          }).catch(() => {})
        }
        if (!res.isDefaultModifyPwd && res.isPasswordExpired) {
          ElMessageBox.confirm('您的密码已过期，请尽快修改密码！', '安全提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            router.push({ name: 'Profile', params: { activeTab: 'resetPwd' } })
          }).catch(() => {})
        }
        return res
      },
      setPasswordSecurityState(forceChangePassword, passwordExpired) {
        this.forceChangePassword = forceChangePassword
        this.passwordExpired = passwordExpired
      },
      // 退出系统
      resetAuthState() {
        this.token = ''
        this.id = ''
        this.name = ''
        this.nickName = ''
        this.avatar = ''
        this.roles = []
        this.permissions = []
        this.setPasswordSecurityState(false, false)
        cache.session.remove('pwrChrtype')
        removeToken()
        useLockStore().unlockScreen()
      },
      // 退出系统
      async logOut() {
        try {
          await logoutApi()
        } catch {
          // 后端不可用或令牌已失效时仍需完成本地退出
        } finally {
          this.resetAuthState()
        }
      }
    }
  })

export default useUserStore
