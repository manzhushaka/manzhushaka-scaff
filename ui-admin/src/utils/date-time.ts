type DateTimeInput = string | number[] | null | undefined;

/**
 * 将系统内时间值统一格式化为 yyyy-MM-dd HH:mm:ss。
 *
 * @param value 时间值，支持 ISO 字符串、Jackson LocalDateTime 数组与空值
 * @returns 统一的日期时间字符串
 */
export function formatStandardDateTime(value: DateTimeInput) {
  if (value == null || value === '') {
    return '--';
  }
  if (Array.isArray(value)) {
    return formatLocalDateTimeArray(value);
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return formatDate(date);
}

/**
 * 生成当前时间的统一展示文本。
 *
 * @param date 可选日期对象
 * @returns yyyy-MM-dd HH:mm:ss 格式字符串
 */
export function formatCurrentDateTime(date = new Date()) {
  return formatDate(date);
}

/**
 * 将 Date 实例格式化为标准日期时间字符串。
 *
 * @param date 日期对象
 * @returns yyyy-MM-dd HH:mm:ss 格式字符串
 */
function formatDate(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  const second = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
}

/**
 * 将 Jackson LocalDateTime 数组格式化为标准日期时间字符串。
 *
 * @param value LocalDateTime 的数组表示
 * @returns yyyy-MM-dd HH:mm:ss 格式字符串
 */
function formatLocalDateTimeArray(value: number[]) {
  if (value.length < 6 || value.some((item) => !Number.isInteger(item))) {
    return value.join(',');
  }
  const [year, month, day, hour, minute, second] = value;
  return `${String(year).padStart(4, '0')}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`;
}
