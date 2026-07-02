/**
 * 密码强度规则
 * 根据参数 chrtype 动态生成校验规则，并叠加强密码基线。
 *
 * chrtype 说明：
 *   0 - 任意字符（默认）
 *   1 - 纯数字（0-9）
 *   2 - 纯字母（a-z / A-Z）
 *   3 - 字母 + 数字（必须同时包含）
 *   4 - 字母 + 数字 + 特殊字符（必须同时包含，特殊字符：~!@#$%^&*()-=_+）
 */

import cache from '@/plugins/cache'

// 密码限制类型
const pwdChrType = ref(cache.session.get('pwrChrtype') || '0')
const STRONG_PASSWORD_MIN_LENGTH = 8
const STRONG_PASSWORD_MAX_LENGTH = 20
const ILLEGAL_CHARS_PATTERN = /[<>"'\\|\s]/
const COMMON_WEAK_PASSWORDS = new Set([
  '123456', '12345678', '123456789', '111111', '000000', '666666', '888888',
  'password', 'password1', 'password1!', 'qwerty', 'qwerty123', 'admin',
  'admin123', 'abc123', 'abcd1234', 'welcome', 'welcome1', 'letmein'
])

// 各类型对应的正则、错误提示
const PWD_RULES = {
  '0': { pattern: /^[^<>"'|\\]+$/, message: '密码不能包含非法字符：< > " \' \\ |' },
  '1': { pattern: /^[0-9]+$/, message: '密码只能为数字（0-9）' },
  '2': { pattern: /^[a-zA-Z]+$/, message: '密码只能为英文字母（a-z、A-Z）' },
  '3': { pattern: /^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$/, message: '密码必须同时包含字母和数字' },
  '4': { pattern: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[~!@#$%^&*()\-=_+])[A-Za-z\d~!@#$%^&*()\-=_+]+$/, message: '密码必须同时包含字母、数字和特殊字符（~!@#$%^&*()-=_+）' }
}

function countCharTypes(value) {
  let count = 0
  if (/[A-Z]/.test(value)) count++
  if (/[a-z]/.test(value)) count++
  if (/[0-9]/.test(value)) count++
  if (/[^A-Za-z0-9]/.test(value)) count++
  return count
}

function hasRepeatedChars(value) {
  return /(.)\1{3,}/.test(value.toLowerCase())
}

function hasSequentialChars(value) {
  const normalized = value.toLowerCase()
  for (let i = 0; i <= normalized.length - 4; i++) {
    const codes = normalized.slice(i, i + 4).split('').map(item => item.charCodeAt(0))
    const asc = codes.every((code, index) => index === 0 || code - codes[index - 1] === 1)
    const desc = codes.every((code, index) => index === 0 || code - codes[index - 1] === -1)
    if (asc || desc) {
      return true
    }
  }
  return false
}

function getStrongPasswordMessage(value) {
  if (!value) {
    return '密码不能为空'
  }
  if (value.length < STRONG_PASSWORD_MIN_LENGTH || value.length > STRONG_PASSWORD_MAX_LENGTH) {
    return '密码长度必须介于 8 和 20 之间'
  }
  if (ILLEGAL_CHARS_PATTERN.test(value) || /[^\x21-\x7e]/.test(value)) {
    return '密码只能包含可见ASCII字符，且不能包含非法字符：< > " \' \\ |'
  }
  if (COMMON_WEAK_PASSWORDS.has(value.toLowerCase())) {
    return '密码过于常见，请更换为更复杂的密码'
  }
  if (countCharTypes(value) < 3) {
    return '密码至少需要包含大写字母、小写字母、数字、特殊字符中的三类'
  }
  if (hasRepeatedChars(value)) {
    return '密码不能包含4位及以上重复字符'
  }
  if (hasSequentialChars(value)) {
    return '密码不能包含4位及以上连续字符'
  }
}

function strongPasswordValidator(rule, value, callback) {
  const message = getStrongPasswordMessage(value)
  if (message) {
    callback(new Error(message))
  } else {
    callback()
  }
}

export function usePasswordRule() {
  // 默认密码校验
  const pwdValidator = computed(() => {
    const rule = PWD_RULES[pwdChrType.value] || PWD_RULES['0']
    return [
      { required: true, message: '密码不能为空', trigger: 'blur' },
      { min: 8, max: 20, message: '密码长度必须介于 8 和 20 之间', trigger: 'blur' },
      { pattern: rule.pattern, message: rule.message, trigger: 'blur' },
      { validator: strongPasswordValidator, trigger: 'blur' }
    ]
  })
  // 校验prompt的inputValidator函数
  const pwdPromptValidator = (value) => {
    const rule = PWD_RULES['0']
    const message = getStrongPasswordMessage(value)
    if (message) {
      return message
    }
    if (!rule.pattern.test(value)) {
      return rule.message
    }
  }
  // 个人中心密码校验
  const infoPwdValidator = computed(() => {
    const rule = PWD_RULES[pwdChrType.value] || PWD_RULES['0']
    return [
      { required: true, message: '新密码不能为空', trigger: 'blur' },
      { min: 8, max: 20, message: '新密码长度必须介于 8 和 20 之间', trigger: 'blur' },
      { pattern: rule.pattern, message: rule.message, trigger: 'blur' },
      { validator: strongPasswordValidator, trigger: 'blur' }
    ]
  })
  // 注册页面密码校验
  const registerPwdValidator = computed(() => {
    const rule = PWD_RULES['0']
    return [
      { required: true, message: '请输入您的密码', trigger: 'blur' },
      { min: 6, max: 20, message: '用户密码长度必须介于 6 和 20 之间', trigger: 'blur' },
      { pattern: rule.pattern, message: rule.message, trigger: 'blur' }
    ]
  })

  return {
    pwdChrType,
    pwdValidator,
    infoPwdValidator,
    pwdPromptValidator,
    registerPwdValidator
  }
}
