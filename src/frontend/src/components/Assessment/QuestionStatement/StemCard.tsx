'use client'

import type { CSSProperties } from 'react'
import { FileTextOutlined } from '@ant-design/icons'
import SmokedGlassCard from '@/components/SmokedGlassCard'
import MarkdownRenderer from '../MarkdownRenderer'

interface StemCardProps {
  /** 题目标题，渲染在卡片头部 */
  title: string
  /** 题干 Markdown 源码 */
  content?: string | null
  emptyText?: string
  className?: string
}

/**
 * 题干卡：考生端与管理端预览共用的题面头部。
 *
 * 从考生答题页原样抽出（图标、间距、分隔线均未改动），
 * 任何样式调整都会同时影响考生端与管理端预览，改动前请确认两端都要变。
 */
export default function StemCard({
  title,
  content,
  emptyText = '暂无题目描述',
  className = 'h-fit',
}: StemCardProps) {
  return (
    <SmokedGlassCard
      radius={12}
      className={className}
      style={{ '--sgc-padding': '28px' } as CSSProperties}
    >
      <div className="flex items-center gap-2.5">
        <FileTextOutlined className="text-xl text-[#fa8c16]" />
        <h2 className="text-base font-semibold text-white m-0">{title}</h2>
      </div>
      <hr className="w-full h-px bg-white/[0.04] border-none m-0 my-4" />
      <MarkdownRenderer content={content} emptyText={emptyText} />
    </SmokedGlassCard>
  )
}
