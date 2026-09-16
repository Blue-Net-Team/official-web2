/**
 * AI 对话记录后台的展示格式化工具。
 *
 * 列表页与详情页共用，避免各处重复定义导致格式漂移。
 */

/** 把后端返回的时间值渲染为 HH:mm */
export function formatTime(value: string | null | undefined): string {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(date.getHours())}:${pad(date.getMinutes())}`
}

/** 把后端返回的时间值渲染为 yyyy-MM-dd HH:mm:ss */
export function formatDateTime(value: string | null | undefined): string {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
    `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
  )
}

/** 把毫秒耗时渲染为 4.1s */
export function formatDuration(ms: number | null | undefined): string {
  if (ms == null) return '—'
  return `${(ms / 1000).toFixed(1)}s`
}

/** 把 0~1 的比例渲染为百分比，保留一位小数 */
export function formatPercent(ratio: number | null | undefined): string {
  if (ratio == null || Number.isNaN(ratio)) return '—'
  return `${(ratio * 100).toFixed(1)}%`
}

/** 渲染 yyyy-MM-dd HH:mm，用于会话时间范围 */
export function formatMinute(value: string | null | undefined): string {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
    `${pad(date.getHours())}:${pad(date.getMinutes())}`
  )
}

/**
 * 格式化为后端 `LocalDateTime` 可解析的本地 ISO 字符串。
 *
 * 不使用 toISOString()，避免被转成 UTC 而整体偏移时区。
 */
export function toLocalIso(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
  )
}
