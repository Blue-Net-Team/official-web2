'use client'

import ErrorPage from '@/components/ErrorPage'
import { ERROR_CONFIGS } from '@/components/ErrorPage/configs'

export default function NotFound() {
  return <ErrorPage config={ERROR_CONFIGS[404]} />
}
