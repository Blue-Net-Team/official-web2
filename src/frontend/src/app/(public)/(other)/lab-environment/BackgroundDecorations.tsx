'use client'

import { useEffect, useRef } from 'react'

export default function BackgroundDecorations() {
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const container = containerRef.current
    if (!container) return

    const createGlow = (side: 'left' | 'right', colorIndex: number) => {
      const glow = document.createElement('div')

      const colors = ['rgba(74, 144, 226, 0.6)', 'rgba(232, 104, 53, 0.6)']

      glow.style.cssText = `
        position: fixed;
        width: 600px;
        height: 600px;
        border-radius: 50%;
        filter: blur(150px);
        opacity: 0.35;
        pointer-events: none;
        z-index: 0;
        background: ${colors[colorIndex]};
      `

      const startX = side === 'left' ? -150 : window.innerWidth - 450
      glow.style.left = `${startX}px`
      glow.style.top = `${100 + Math.random() * 200}px`

      container.appendChild(glow)

      const animateGlow = () => {
        const duration = 15000 + Math.random() * 10000
        const startY = parseFloat(glow.style.top)
        const endY = startY + (Math.random() - 0.5) * 300

        let startTime: number | null = null

        const animate = (currentTime: number) => {
          if (!startTime) startTime = currentTime
          const elapsed = currentTime - startTime
          const progress = Math.min(elapsed / duration, 1)

          const easeProgress = Math.sin(progress * Math.PI)

          glow.style.top = `${startY + (endY - startY) * easeProgress}px`
          glow.style.opacity = `${0.25 + easeProgress * 0.15}`

          if (progress < 1) {
            requestAnimationFrame(animate)
          } else {
            animateGlow()
          }
        }

        requestAnimationFrame(animate)
      }

      animateGlow()
    }

    createGlow('left', 0)
    createGlow('right', 1)
  }, [])

  return <div ref={containerRef} />
}
