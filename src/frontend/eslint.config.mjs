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
      // React Hooks 规则 - 降级为警告
      'react-hooks/exhaustive-deps': 'warn',
      // Next.js 规则 - 禁用 img 元素检查
      '@next/next/no-img-element': 'off',
    }
  }
]

export default eslintConfig
