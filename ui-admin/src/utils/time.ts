export function formatNow() {
  return new Date().toLocaleString('zh-CN', { hour12: false });
}
