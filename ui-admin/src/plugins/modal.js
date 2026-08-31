import { h, ref } from 'vue'
import { Input, Message, Modal, Notification } from '@arco-design/web-vue'

let loadingInstance

export default {
  // 消息提示
  msg(content) {
    Message.info(content)
  },
  // 错误消息
  msgError(content) {
    Message.error(content)
  },
  // 成功消息
  msgSuccess(content) {
    Message.success(content)
  },
  // 警告消息
  msgWarning(content) {
    Message.warning(content)
  },
  // 弹出提示
  alert(content) {
    return openAlert('info', content)
  },
  // 错误提示
  alertError(content) {
    return openAlert('error', content)
  },
  // 成功提示
  alertSuccess(content) {
    return openAlert('success', content)
  },
  // 警告提示
  alertWarning(content) {
    return openAlert('warning', content)
  },
  // 通知提示
  notify(content) {
    Notification.info({ title: '系统通知', content })
  },
  // 错误通知
  notifyError(content) {
    Notification.error({ title: '系统通知', content })
  },
  // 成功通知
  notifySuccess(content) {
    Notification.success({ title: '系统通知', content })
  },
  // 警告通知
  notifyWarning(content) {
    Notification.warning({ title: '系统通知', content })
  },
  // 确认窗体
  confirm(content) {
    return new Promise((resolve, reject) => {
      Modal.confirm({
        title: '系统提示',
        content,
        okText: '确定',
        cancelText: '取消',
        onOk: () => resolve(true),
        onCancel: () => reject(new Error('cancel'))
      })
    })
  },
  // 提交内容
  prompt(content) {
    const value = ref('')
    return new Promise((resolve, reject) => {
      Modal.confirm({
        title: '系统提示',
        content: () => h('div', { class: 'arco-prompt-content' }, [
          h('div', { class: 'arco-prompt-label' }, content),
          h(Input, {
            modelValue: value.value,
            'onUpdate:modelValue': inputValue => { value.value = inputValue }
          })
        ]),
        okText: '确定',
        cancelText: '取消',
        onOk: () => resolve({ value: value.value }),
        onCancel: () => reject(new Error('cancel'))
      })
    })
  },
  // 打开遮罩层
  loading(content) {
    loadingInstance?.close()
    loadingInstance = Message.loading({
      content,
      duration: 0
    })
  },
  // 关闭遮罩层
  closeLoading() {
    loadingInstance?.close()
    loadingInstance = undefined
  }
}

/**
 * 打开 Arco 提示弹窗。
 *
 * @param {'info'|'error'|'success'|'warning'} type 弹窗类型
 * @param {string} content 提示内容
 * @return {Promise<boolean>} 用户确认结果
 */
function openAlert(type, content) {
  return new Promise(resolve => {
    Modal[type]({
      title: '系统提示',
      content,
      hideCancel: true,
      onOk: () => resolve(true)
    })
  })
}
