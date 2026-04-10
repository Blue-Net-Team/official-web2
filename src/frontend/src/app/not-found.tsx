'use client'

import NavBar from '@/components/PublicNavbar'
import ErrorPage from '@/components/ErrorPage'
import { ERROR_CONFIGS } from '@/components/ErrorPage/configs'

export default function NotFound() {
  return (
    <>
      <NavBar />
      <ErrorPage config={ERROR_CONFIGS[404]} />
    </>
  )
}
