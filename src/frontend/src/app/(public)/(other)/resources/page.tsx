import Link from 'next/link'
import { softwareResourceService } from '@/apis/services/software-resource.service'
import { SOFTWARE_RESOURCE_DIRECTION_LABELS } from '@/apis/schema/enumerate'
import type { SoftwareResourceDTO } from '@/apis/schema/type'

export const revalidate = 3600

interface PageProps {
  searchParams: Promise<{
    tab?: string
    page?: string
  }>
}

const TABS = [
  { key: 'all', label: '全部', direction: undefined },
  { key: 'general', label: '通用', direction: 'GENERAL' },
  { key: 'computer_vision', label: '计算机视觉', direction: 'COMPUTER_VISION' },
  { key: 'structural_design', label: '结构设计', direction: 'STRUCTURAL_DESIGN' },
  { key: 'embedded', label: '嵌入式开发', direction: 'EMBEDDED' },
]

const PAGE_SIZE = 20

function buildHref(tabKey: string, page: number): string {
  if (page === 0) {
    return `/resources?tab=${tabKey}`
  }
  return `/resources?tab=${tabKey}&page=${page}`
}

export default async function ResourcesPage({ searchParams }: PageProps) {
  const { tab = 'all', page: pageParam = '0' } = await searchParams
  const activeTab = TABS.find((item) => item.key === tab) ?? TABS[0]
  const page = Math.max(0, parseInt(pageParam, 10) || 0)

  const response = await softwareResourceService.list({
    direction: activeTab.direction,
    page,
    size: PAGE_SIZE,
  })
  const pageData = response.data
  const resources: SoftwareResourceDTO[] = pageData?.content ?? []
  const totalElements = pageData?.totalElements ?? 0
  const totalPages = pageData?.totalPages ?? 0

  return (
    <div className="w-full min-h-screen bg-[#0a0a0a] text-white relative overflow-x-hidden">
      <div className="fixed top-0 left-0 w-full h-full bg-[radial-gradient(ellipse_80%_50%_at_20%_40%,rgba(102,119,255,0.15)_0%,transparent_50%),radial-gradient(ellipse_60%_40%_at_80%_60%,rgba(255,107,53,0.1)_0%,transparent_50%),radial-gradient(ellipse_50%_30%_at_50%_100%,rgba(47,39,176,0.2)_0%,transparent_50%)] z-0 pointer-events-none" />
      <main className="flex flex-col items-center w-full min-h-screen py-8 px-[147px] max-lg:px-10 max-md:px-4 box-border relative z-1">
        <section className="text-center mb-8 w-full pt-8">
          <h1 className="text-5xl max-md:text-4xl max-sm:text-[28px] font-bold text-white mb-4 bg-gradient-to-br from-white to-white/80 bg-clip-text text-transparent">
            软件资源库
          </h1>
          <p className="text-lg max-sm:text-sm text-white/50 max-w-[600px] mx-auto leading-relaxed">
            收集各方向常用软件与学习工具，方便新成员快速上手
          </p>
        </section>

        <nav className="w-full flex flex-wrap justify-center gap-3 mb-8">
          {TABS.map((item) => {
            const active = activeTab.key === item.key
            return (
              <Link
                key={item.key}
                href={buildHref(item.key, 0)}
                className={`px-5 py-2 rounded-full text-sm font-medium transition-colors ${
                  active
                    ? 'bg-white/20 text-white border border-white/30'
                    : 'bg-white/5 text-white/70 border border-white/10 hover:bg-white/10 hover:text-white'
                }`}
              >
                {item.label}
              </Link>
            )
          })}
        </nav>

        <section className="w-full max-w-[960px]">
          {resources.length === 0 ? (
            <div className="text-center py-20 text-white/40">该分类下暂无资源</div>
          ) : (
            <div className="grid grid-cols-1 gap-4">
              {resources.map((resource) => (
                <ResourceCard key={resource.id} resource={resource} />
              ))}
            </div>
          )}

          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-2 mt-8">
              {page > 0 && (
                <Link
                  href={buildHref(activeTab.key, page - 1)}
                  className="px-4 py-2 rounded-lg bg-white/5 text-white/80 hover:bg-white/10 border border-white/10"
                >
                  上一页
                </Link>
              )}
              <span className="text-white/60 text-sm">
                第 {page + 1} / {totalPages} 页，共 {totalElements} 条
              </span>
              {page + 1 < totalPages && (
                <Link
                  href={buildHref(activeTab.key, page + 1)}
                  className="px-4 py-2 rounded-lg bg-white/5 text-white/80 hover:bg-white/10 border border-white/10"
                >
                  下一页
                </Link>
              )}
            </div>
          )}
        </section>
      </main>
    </div>
  )
}

function ResourceCard({ resource }: { resource: SoftwareResourceDTO }) {
  return (
    <a
      href={resource.externalUrl}
      target="_blank"
      rel="noopener noreferrer"
      className="block p-5 rounded-xl bg-white/5 border border-white/10 hover:bg-white/10 transition-colors group"
    >
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1">
          <div className="flex items-center gap-3 mb-2">
            <h3 className="text-lg font-semibold text-white group-hover:text-blue-300 transition-colors">
              {resource.name}
            </h3>
            {resource.category && (
              <span className="px-2 py-0.5 rounded text-xs bg-white/10 text-white/70">
                {resource.category}
              </span>
            )}
            <span className="px-2 py-0.5 rounded text-xs bg-white/10 text-white/70">
              {SOFTWARE_RESOURCE_DIRECTION_LABELS[resource.direction]}
            </span>
          </div>
          {resource.description && (
            <p className="text-white/60 text-sm leading-relaxed">{resource.description}</p>
          )}
        </div>
        <span className="text-blue-400 text-sm whitespace-nowrap">前往下载 →</span>
      </div>
    </a>
  )
}
