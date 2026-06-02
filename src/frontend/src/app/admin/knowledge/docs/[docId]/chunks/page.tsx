'use client'

import { useCallback, useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { App, Button, Card, Pagination, Spin, Tag } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import type { KnowledgeChunkDTO } from '@/apis/services/knowledge.service'
import { knowledgeService } from '@/apis/services/knowledge.service'
import MarkdownRenderer from '@/components/Assessment/MarkdownRenderer'

export default function KnowledgeChunksPage() {
  const { message: messageApi } = App.useApp()
  const params = useParams()
  const router = useRouter()
  const docId = Number(params.docId)

  const [chunks, setChunks] = useState<KnowledgeChunkDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)

  const fetchChunks = useCallback(async () => {
    if (!docId) return
    setLoading(true)
    try {
      const res = await knowledgeService.listChunks(docId, page - 1, pageSize)
      if (res.code === 200 && res.data) {
        setChunks(res.data.content)
        setTotal(res.data.totalElements)
      }
    } finally {
      setLoading(false)
    }
  }, [docId, page, pageSize])

  useEffect(() => {
    fetchChunks()
  }, [fetchChunks])

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-4">
        <Button icon={<ArrowLeftOutlined />} onClick={() => router.push('/admin/knowledge/docs')}>
          返回
        </Button>
        <h2 className="text-lg font-medium text-white/90 m-0">文档分段详情（文档ID: {docId}）</h2>
      </div>

      <Spin spinning={loading}>
        {chunks.length === 0 ? (
          <div className="text-white/50 text-center py-12">暂无分段数据</div>
        ) : (
          <div className="flex flex-col gap-3">
            {chunks.map((chunk) => (
              <Card
                key={chunk.id}
                size="small"
                className="bg-white/5 border-white/10"
                title={
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="text-white/70 text-sm">ID: {chunk.id}</span>
                    {chunk.source && (
                      <Tag size="small" className="m-0">
                        {chunk.source}
                      </Tag>
                    )}
                    {chunk.tags &&
                      chunk.tags.length > 0 &&
                      chunk.tags.map((tag) => (
                        <Tag key={tag} color="blue" size="small" className="m-0">
                          {tag}
                        </Tag>
                      ))}
                  </div>
                }
              >
                <div className="flex flex-col gap-3">
                  <MarkdownRenderer content={chunk.content} />
                </div>
              </Card>
            ))}
          </div>
        )}
      </Spin>

      {total > 0 && (
        <div className="flex justify-end">
          <Pagination
            current={page}
            pageSize={pageSize}
            total={total}
            showSizeChanger
            pageSizeOptions={[5, 10, 20, 50]}
            onChange={(p, ps) => {
              setPage(p)
              if (ps) setPageSize(ps)
            }}
          />
        </div>
      )}
    </div>
  )
}
