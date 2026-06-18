'use client'

import { useState } from 'react'
import { Button, Input } from 'antd'
import { SendOutlined } from '@ant-design/icons'

interface ChatInputProps {
  loading?: boolean
  onSend: (text: string) => void
}

export default function ChatInput({ loading = false, onSend }: ChatInputProps) {
  const [text, setText] = useState('')

  const handleSend = () => {
    const trimmed = text.trim()
    if (!trimmed || loading) return
    onSend(trimmed)
    setText('')
  }

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="flex items-end gap-2 border-t border-white/[0.06] bg-white/[0.02] p-3">
      <Input.TextArea
        value={text}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="请输入问题..."
        autoSize={{ minRows: 1, maxRows: 5 }}
        disabled={loading}
        className="flex-1"
      />
      <Button
        type="primary"
        icon={<SendOutlined />}
        loading={loading}
        disabled={!text.trim() || loading}
        onClick={handleSend}
        className="!h-10 !w-10"
      />
    </div>
  )
}
