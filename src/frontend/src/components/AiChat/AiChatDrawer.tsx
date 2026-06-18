'use client'

import { Drawer } from 'antd'
import { useAiChat } from '@/hooks/useAiChat'
import AiChatPanel from './AiChatPanel'

interface AiChatDrawerProps {
  open: boolean
  onClose: () => void
}

export default function AiChatDrawer({ open, onClose }: AiChatDrawerProps) {
  const { messages, isStreaming, error, sendMessage, reset } = useAiChat()

  return (
    <Drawer
      title={null}
      placement="right"
      open={open}
      onClose={onClose}
      closable={false}
      styles={{
        body: { padding: 0, overflow: 'hidden' },
        wrapper: {
          width: '100%',
          maxWidth: 420,
          boxShadow: '-8px 0 32px rgba(0,0,0,0.4)',
        },
      }}
    >
      <AiChatPanel
        messages={messages}
        isStreaming={isStreaming}
        error={error}
        onSend={sendMessage}
        onReset={reset}
        onClose={onClose}
      />
    </Drawer>
  )
}
