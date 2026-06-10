/**
 * 日期格式化工具
 */

/** 将 ISO 日期时间字符串格式化为 "YYYY-MM-DD HH:mm:ss" */
export function formatDateTime(val: string | null | undefined): string {
  if (!val) return '-'
  return val.replace('T', ' ').substring(0, 19)
}

/** 将 ISO 日期字符串格式化为 "YYYY-MM-DD" */
export function formatDate(val: string | null | undefined): string {
  if (!val) return '-'
  return val.substring(0, 10)
}
