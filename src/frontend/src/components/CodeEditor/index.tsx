'use client'

import { useCallback, useMemo } from 'react'
import Editor from '@monaco-editor/react'
import type { ProgrammingLanguage } from '@/apis/schema/assessment.dto'

/** 项目语言标识 → Monaco Editor 语言标识映射 */
const LANGUAGE_MAP: Record<ProgrammingLanguage, string> = {
  python: 'python',
  c: 'c',
  cpp: 'cpp',
  java: 'java',
  javascript: 'javascript',
}

export interface CodeEditorProps {
  /** 编辑器当前值 */
  value?: string
  /** 值变更回调 */
  onChange?: (value: string) => void
  /** 编程语言 */
  language?: ProgrammingLanguage | null
  /** 是否只读 */
  readOnly?: boolean
  /** 编辑器高度 */
  height?: string | number
  /** 占位提示文本 */
  placeholder?: string
  /** 额外的 CSS 类名 */
  className?: string
}

/**
 * 基于 Monaco Editor 的代码编辑器组件。
 *
 * 特性：
 * - 语法高亮（支持 Python/C/C++/Java/JavaScript）
 * - 行号显示、括号匹配
 * - 暗色主题（vs-dark）
 * - 受控组件模式，兼容 Ant Design Form
 * - SSR 安全（仅在客户端渲染）
 */
export default function CodeEditor({
  value = '',
  onChange,
  language = 'python',
  readOnly = false,
  height = 320,
  placeholder,
  className,
}: CodeEditorProps) {
  const monacoLanguage = useMemo(
    () => (language ? LANGUAGE_MAP[language] : 'plaintext') ?? 'plaintext',
    [language]
  )

  const handleChange = useCallback(
    (newValue: string | undefined) => {
      onChange?.(newValue ?? '')
    },
    [onChange]
  )

  return (
    <div className={`rounded-lg overflow-hidden border border-white/[0.08] ${className ?? ''}`}>
      <Editor
        value={value}
        language={monacoLanguage}
        theme="vs-dark"
        height={height}
        options={{
          readOnly,
          minimap: { enabled: false },
          lineNumbers: 'on',
          renderLineHighlight: 'line',
          matchBrackets: 'always',
          automaticLayout: true,
          scrollBeyondLastLine: false,
          fontSize: 13,
          fontFamily: 'monospace',
          tabSize: 4,
          insertSpaces: true,
          wordWrap: 'on',
          folding: true,
          bracketPairColorization: { enabled: true },
          placeholder,
        }}
        onChange={handleChange}
        loading={
          <div className="flex items-center justify-center h-full text-white/40 text-sm">
            编辑器加载中...
          </div>
        }
      />
    </div>
  )
}
