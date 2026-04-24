import { dirname } from 'path'
import { fileURLToPath } from 'url'
import { FlatCompat } from '@eslint/eslintrc'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const compat = new FlatCompat({
  baseDirectory: __dirname,
})

const eslintConfig = [
  ...compat.extends('next', 'next/typescript'),
  {
    rules: {
      // TypeScript 规则 - 降级为警告
      '@typescript-eslint/no-explicit-any': 'warn',
      '@typescript-eslint/no-unused-vars': 'warn',
      // React Hooks 规则 - 禁用 exhaustive-deps 规则
      'react-hooks/exhaustive-deps': 'off',
      // React 19 在 useEffect 中 setState 的限制过于严格，许多合法模式（如派生状态计算）也被拦截
      'react-hooks/set-state-in-effect': 'off',
      // Next.js 规则 - 禁用 img 元素检查
      '@next/next/no-img-element': 'off',
    }
  }
]

export default eslintConfig
