'use client'

import { useRouter } from 'next/navigation'
import SmokedGlassTabs from '@/components/SmokedGlassTabs'

interface DirectionTabsProps {
  tabs: { key: string; label: string }[]
  activeKey: string
}

/**
 * 资源库方向筛选 Tab（客户端组件）
 * 切换时通过路由跳转更新 `?tab=` searchParam，触发 SSR 重新渲染
 */
export default function DirectionTabs({ tabs, activeKey }: DirectionTabsProps) {
  const router = useRouter()

  return (
    <SmokedGlassTabs
      activeKey={activeKey}
      items={tabs.map((t) => ({ key: t.key, label: t.label }))}
      onChange={(key) => router.push(buildHref(key))}
    />
  )
}

function buildHref(tabKey: string): string {
  return `/resources?tab=${tabKey}`
}
