'use client'

import { useEffect, useRef, useState } from 'react'
import { Button } from 'antd'
import { CaretRightOutlined, LoadingOutlined } from '@ant-design/icons'

interface ReasoningBlockProps {
  reasoning?: string
  done?: boolean
}

export default function ReasoningBlock({ reasoning = '', done = false }: ReasoningBlockProps) {
  const [expanded, setExpanded] = useState(!done)
  const prevDoneRef = useRef(done)

  useEffect(() => {
    if (done && !prevDoneRef.current) {
      setExpanded(false)
    }
    prevDoneRef.current = done
  }, [done])

  if (!reasoning.trim()) {
    return null
  }

  return (
    <div className="mb-2 rounded-lg border border-white/[0.06] bg-white/[0.03] px-3 py-2">
      <Button
        type="text"
        size="small"
        className="!h-auto !px-0 !py-0 text-xs text-white/50 hover:text-white/80"
        icon={
          done ? (
            <CaretRightOutlined rotate={expanded ? 90 : 0} className="transition-transform" />
          ) : (
            <LoadingOutlined className="text-[#fa8c16]" spin />
          )
        }
        onClick={() => setExpanded((prev) => !prev)}
      >
        {done ? `思考过程（${reasoning.length} 字）` : '正在思考...'}
      </Button>

      {expanded && (
        <div className="mt-2 whitespace-pre-wrap text-xs leading-relaxed text-white/45">
          {reasoning}
        </div>
      )}
    </div>
  )
}
