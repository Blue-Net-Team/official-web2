'use client'

import Image from 'next/image'
import { FireOutlined } from '@ant-design/icons'
import { useState } from 'react'

interface CompetitionLogoProps {
  src: string
  alt: string
}

const CompetitionLogo = ({ src, alt }: CompetitionLogoProps) => {
  const [error, setError] = useState(false)

  if (error) {
    return <FireOutlined className="text-[28px] text-white" />
  }

  return (
    <Image
      src={src}
      alt={alt}
      width={64}
      height={44}
      className="object-contain"
      onError={() => setError(true)}
    />
  )
}

export default CompetitionLogo
