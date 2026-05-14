'use client'

import React, { useEffect, useRef, useState } from 'react'
import { Modal } from 'antd'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import rehypeSanitize from 'rehype-sanitize'
import mermaid from 'mermaid'
import styles from './PolicyModal.module.css'

interface PolicyModalProps {
  open: boolean
  onClose: () => void
  title: string
  content: string
}

let mermaidInitialized = false

function initMermaid() {
  if (mermaidInitialized) return
  mermaidInitialized = true
  mermaid.initialize({
    startOnLoad: false,
    theme: 'dark',
    securityLevel: 'strict',
    flowchart: {
      useMaxWidth: true,
      htmlLabels: true,
      curve: 'basis',
    },
  })
}

interface MermaidBlockProps {
  code: string
}

const MermaidBlock: React.FC<MermaidBlockProps> = ({ code }) => {
  const containerRef = useRef<HTMLDivElement>(null)
  const [svg, setSvg] = useState<string>('')
  const [error, setError] = useState(false)

  useEffect(() => {
    initMermaid()
    let cancelled = false

    const render = async () => {
      try {
        const id = `mermaid-${Math.random().toString(36).slice(2, 11)}`
        const { svg: renderedSvg } = await mermaid.render(id, code)
        if (!cancelled) {
          setSvg(renderedSvg)
          setError(false)
        }
      } catch {
        if (!cancelled) {
          setError(true)
        }
      }
    }

    render()

    return () => {
      cancelled = true
    }
  }, [code])

  if (error) {
    return (
      <pre className="bg-[#1a1a1a] rounded-lg p-4 overflow-x-auto text-sm text-white/80">
        <code>{code}</code>
      </pre>
    )
  }

  if (!svg) {
    return (
      <div className="bg-[#1a1a1a] rounded-lg p-8 flex items-center justify-center min-h-[120px]">
        <div className="w-6 h-6 border-2 border-[#fa8c16] border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <div
      ref={containerRef}
      className="bg-[#1a1a1a] rounded-lg p-4 overflow-x-auto"
      dangerouslySetInnerHTML={{ __html: svg }}
    />
  )
}

const PolicyModal: React.FC<PolicyModalProps> = ({ open, onClose, title, content }) => {
  return (
    <Modal open={open} onCancel={onClose} footer={null} title={title} width={720}>
      <div className={styles.policyModalBody}>
        <div className={styles.markdown}>
          <ReactMarkdown
            remarkPlugins={[remarkGfm]}
            rehypePlugins={[rehypeSanitize]}
            components={{
              code(props) {
                const { className, children } = props
                const match = /language-mermaid/.exec(className || '')
                const codeString = String(children).replace(/\n$/, '')
                if (match) {
                  return <MermaidBlock code={codeString} />
                }
                return (
                  <pre className="bg-[#1a1a1a] rounded-lg p-4 overflow-x-auto">
                    <code className={className}>{children}</code>
                  </pre>
                )
              },
            }}
          >
            {content}
          </ReactMarkdown>
        </div>
      </div>
    </Modal>
  )
}

export default PolicyModal
