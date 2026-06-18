'use client'

import { useState } from 'react'
import { Button } from 'antd'
import { CaretRightOutlined, CodeOutlined } from '@ant-design/icons'
import { ToolCallItem } from '@/apis/schema/ai-chat.dto'

interface ToolCallCardProps {
  toolCall: ToolCallItem
}

export default function ToolCallCard({ toolCall }: ToolCallCardProps) {
  const [expanded, setExpanded] = useState(false)

  return (
    <div className="mb-2 rounded-lg border border-[#fa8c16]/20 bg-[#fa8c16]/[0.06] px-3 py-2">
      <Button
        type="text"
        size="small"
        className="!h-auto !px-0 !py-0 text-xs text-[#fa8c16]/80 hover:text-[#fa8c16]"
        icon={<CaretRightOutlined rotate={expanded ? 90 : 0} className="transition-transform" />}
        onClick={() => setExpanded((prev) => !prev)}
      >
        <CodeOutlined className="mr-1" />
        {toolCall.name}
      </Button>

      {expanded && (
        <div className="mt-2 space-y-2 text-xs">
          {toolCall.args && Object.keys(toolCall.args).length > 0 && (
            <div>
              <div className="mb-1 text-white/40">参数</div>
              <pre className="overflow-x-auto rounded bg-black/30 p-2 text-white/60">
                {JSON.stringify(toolCall.args, null, 2)}
              </pre>
            </div>
          )}
          {toolCall.result !== undefined && (
            <div>
              <div className="mb-1 text-white/40">结果</div>
              <pre className="max-h-40 overflow-auto rounded bg-black/30 p-2 text-white/60">
                {toolCall.result}
              </pre>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
