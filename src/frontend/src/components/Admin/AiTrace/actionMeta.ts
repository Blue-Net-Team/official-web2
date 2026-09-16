/**
 * 处理动作的展示元数据。
 *
 * 列表页标签、详情页左侧摘要、分析面板图例三处共用同一份定义，
 * 避免文案与配色在各处漂移。
 */
export const ACTION_META: Record<string, { label: string; color: string }> = {
  RETRIEVE: { label: '检索', color: '#fa8c16' },
  REFUSE: { label: '拒答', color: '#ff4d4f' },
  DIRECT: { label: '直接回复', color: '#13c2c2' },
}

/** 动作的固定展示顺序，用于统计图表保证图例稳定 */
export const ACTION_ORDER = ['RETRIEVE', 'REFUSE', 'DIRECT']

/** 把后端动作原始值翻译为中文展示名；未知值原样返回 */
export function actionLabel(action: string | null | undefined): string {
  if (!action) return '—'
  return ACTION_META[action]?.label ?? action
}
