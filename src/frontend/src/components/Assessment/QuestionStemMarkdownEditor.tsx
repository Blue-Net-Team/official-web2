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

  const editorHeight = rows * 24 + 24

  const editor = (
    <Input.TextArea
      value={content}
      onChange={(event) => onChange?.(event.target.value)}
      placeholder={placeholder}
      disabled={disabled}
      rows={rows}
      className="font-mono text-sm"
      style={{ resize: 'none', height: editorHeight }}
    />
  )

  const preview = (
    <div
      className="rounded-lg border border-white/[0.08] bg-white/[0.03] p-4 overflow-y-auto"
      style={{ height: editorHeight }}
    >
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
