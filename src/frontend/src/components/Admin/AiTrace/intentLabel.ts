/**
 * 意图标签的中文展示映射。
 *
 * 镜像 ai-service 的 agent/intent.py 中的意图常量。
 * 后端返回的是原始值（如 SOFTWARE_DOWNLOAD），此处仅做展示层翻译；
 * 筛选时仍传原始值。
 */
const INTENT_LABELS: Record<string, string> = {
  REGISTRATION: '报名加入',
  ASSESSMENT_PROCESS: '考核流程',
  LAB_INTRODUCTION: '团队介绍',
  SOFTWARE_DOWNLOAD: '软件下载',
  BLOCKED_ASSESSMENT_CONTENT: '考核内容求助',
  BLOCKED_CODING: '编程求助',
  BLOCKED_TECH_SUPPORT: '技术支持',
  BLOCKED_DEPLOYMENT: '部署问题',
  BLOCKED_SECURITY: '安全相关',
  BLOCKED_IRRELEVANT: '无关问题',
  GREETING: '问候',
  CLARIFY: '未能理解',
}

/** 意图筛选项，值为后端原始枚举值 */
export const INTENT_OPTIONS = Object.entries(INTENT_LABELS).map(([value, label]) => ({
  value,
  label,
}))

/** 把后端意图原始值翻译为中文展示名 */
export function intentLabel(intent: string | null | undefined): string {
  if (!intent) return '—'
  return INTENT_LABELS[intent] ?? intent
}
