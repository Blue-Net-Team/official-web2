'use client'

import { Grid, Input, Tabs } from 'antd'
import MarkdownRenderer from './MarkdownRenderer'

interface QuestionStemMarkdownEditorProps {
  value?: string
  onChange?: (value: string) => void
  disabled?: boolean
  placeholder?: string
  rows?: number
}

const { useBreakpoint } = Grid

export default function QuestionStemMarkdownEditor({
  value,
  onChange,
  disabled,
  placeholder = '支持 Markdown：标题、列表、代码块、表格、链接等',
  rows = 10,
}: QuestionStemMarkdownEditorProps) {
  const screens = useBreakpoint()
  const isDesktop = Boolean(screens.lg)
  const content = value ?? ''

  const editor = (
    <Input.TextArea
      value={content}
      onChange={(event) => onChange?.(event.target.value)}
      placeholder={placeholder}
      disabled={disabled}
      rows={rows}
      className="font-mono text-sm"
    />
  )

  const preview = (
    <div className="min-h-[280px] rounded-lg border border-white/[0.08] bg-white/[0.03] p-4">
      <MarkdownRenderer content={content} />
    </div>
  )

  if (isDesktop) {
    return (
      <div className="grid grid-cols-2 gap-4">
        <div className="min-w-0">
          <div className="mb-2 text-xs font-medium text-white/45">编辑</div>
          {editor}
        </div>
        <div className="min-w-0">
          <div className="mb-2 text-xs font-medium text-white/45">预览</div>
          {preview}
        </div>
      </div>
    )
  }

  return (
    <Tabs
      defaultActiveKey="edit"
      items={[
        {
          key: 'edit',
          label: '编辑',
          children: editor,
        },
        {
          key: 'preview',
          label: '预览',
          children: preview,
        },
      ]}
    />
  )
}
