'use client'

import ReactMarkdown, { type Components } from 'react-markdown'
import rehypeSanitize from 'rehype-sanitize'
import remarkGfm from 'remark-gfm'

interface MarkdownRendererProps {
  content?: string | null
  emptyText?: string
  className?: string
}

const markdownComponents: Components = {
  h1: ({ children }) => (
    <h1 className="mt-0 mb-4 text-2xl font-semibold leading-tight text-white">{children}</h1>
  ),
  h2: ({ children }) => (
    <h2 className="mt-6 mb-3 text-xl font-semibold leading-tight text-white">{children}</h2>
  ),
  h3: ({ children }) => (
    <h3 className="mt-5 mb-2 text-lg font-semibold leading-tight text-white">{children}</h3>
  ),
  h4: ({ children }) => (
    <h4 className="mt-4 mb-2 text-base font-semibold leading-tight text-white">{children}</h4>
  ),
  p: ({ children }) => <p className="my-3 text-sm leading-relaxed text-white/65">{children}</p>,
  ul: ({ children }) => (
    <ul className="my-3 list-disc space-y-1 pl-6 text-sm leading-relaxed text-white/65">
      {children}
    </ul>
  ),
  ol: ({ children }) => (
    <ol className="my-3 list-decimal space-y-1 pl-6 text-sm leading-relaxed text-white/65">
      {children}
    </ol>
  ),
  li: ({ children }) => <li className="pl-1">{children}</li>,
  a: ({ href, children }) => (
    <a
      href={href}
      target="_blank"
      rel="noreferrer"
      className="text-[#6677ff] underline underline-offset-4 hover:text-[#8594ff]"
    >
      {children}
    </a>
  ),
  blockquote: ({ children }) => (
    <blockquote className="my-4 border-l-4 border-[#6677ff]/50 bg-white/[0.04] py-1 pl-4 text-white/60">
      {children}
    </blockquote>
  ),
  code: ({ className, children }) => {
    const text = String(children)
    const isBlock = Boolean(className) || text.includes('\n')
    if (isBlock) {
      return (
        <code
          className={`${className ?? ''} block whitespace-pre font-mono text-[13px] leading-relaxed`}
        >
          {children}
        </code>
      )
    }
    return (
      <code className="rounded bg-white/[0.08] px-1.5 py-0.5 font-mono text-[12px] text-[#faad14]">
        {children}
      </code>
    )
  },
  pre: ({ children }) => (
    <pre className="my-4 overflow-x-auto rounded-lg border border-white/[0.08] bg-black/30 p-4">
      {children}
    </pre>
  ),
  table: ({ children }) => (
    <div className="my-4 overflow-x-auto">
      <table className="min-w-full border-collapse text-sm text-white/65">{children}</table>
    </div>
  ),
  thead: ({ children }) => <thead className="bg-white/[0.08] text-white/80">{children}</thead>,
  th: ({ children }) => (
    <th className="border border-white/[0.1] px-3 py-2 text-left font-semibold">{children}</th>
  ),
  td: ({ children }) => <td className="border border-white/[0.1] px-3 py-2">{children}</td>,
  hr: () => <hr className="my-5 h-px border-none bg-white/[0.08]" />,
}

export default function MarkdownRenderer({
  content,
  emptyText = '暂无题干内容',
  className,
}: MarkdownRendererProps) {
  const trimmed = content?.trim()

  if (!trimmed) {
    return <div className={className ?? 'text-sm text-white/35'}>{emptyText}</div>
  }

  return (
    <div className={className}>
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeSanitize]}
        components={markdownComponents}
      >
        {trimmed}
      </ReactMarkdown>
    </div>
  )
}
