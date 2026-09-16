'use client'

import { Tag } from 'antd'
import { ACTION_META } from './actionMeta'

/**
 * 处理动作标签。
 *
 * 风格与设计稿一致：浅色底 + 同色文字（而非 antd Tag 的实心底）。
 */
export default function ActionTag({ action }: { action: string | null | undefined }) {
  if (!action) {
    return <span className="text-white/25">—</span>
  }

  const meta = ACTION_META[action]

  if (!meta) {
    return <Tag bordered={false}>{action}</Tag>
  }

  return (
    <Tag
      bordered={false}
      style={{
        background: `${meta.color}24`,
        color: meta.color,
        margin: 0,
      }}
    >
      {meta.label}
    </Tag>
  )
}
