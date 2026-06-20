'use client'

import ColorBends from '@/components/Reactbits/ColorBends/ColorBends'

export default function HomeBackground() {
  return (
    <div className="fixed inset-0 z-0">
      <ColorBends colors={['#2f27b0', '#ff6f3c']} intensity={0.8} />
    </div>
  )
}
