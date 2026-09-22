'use client'

import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { App, Button, Card, Modal, Pagination, Select, Space, Spin, Tag, Input } from 'antd'
import { ArrowLeftOutlined, EditOutlined, LoadingOutlined } from '@ant-design/icons'
import type { KnowledgeChunkDTO, KnowledgeTagDTO } from '@/apis/services/knowledge.service'
import { knowledgeService } from '@/apis/services/knowledge.service'
import MarkdownRenderer from '@/components/Assessment/MarkdownRenderer'
import { useAuth } from '@/hooks'

export default function KnowledgeChunksPage() {
  const { message: messageApi } = App.useApp()
  const { isAdmin } = useAuth()
  const params = useParams()
  const router = useRouter()
  const docId = Number(params.docId)

  const [chunks, setChunks] = useState<KnowledgeChunkDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [docStatus, setDocStatus] = useState<string>('')
  const [tagPool, setTagPool] = useState<KnowledgeTagDTO[]>([])

  const [editingChunk, setEditingChunk] = useState<KnowledgeChunkDTO | null>(null)
  const [editContent, setEditContent] = useState('')
  const [editTagIds, setEditTagIds] = useState<number[]>([])
  const [editModalOpen, setEditModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)

  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const tagNameMap = useMemo(() => {
    const map = new Map<number, string>()
    tagPool.forEach((tag) => map.set(tag.id, tag.tagName))
    return map
  }, [tagPool])

  const fetchChunks = useCallback(
    async (silent = false) => {
      if (!docId) return
      if (!silent) setLoading(true)
      try {
        const res = await knowledgeService.listChunks(docId, page - 1, pageSize)
        if (res.code === 200 && res.data) {
          setChunks(res.data.content)
          setTotal(res.data.totalElements)
        }
      } finally {
        if (!silent) setLoading(false)
      }
    },
    [docId, page, pageSize]
  )

  const fetchDocStatus = useCallback(async () => {
    if (!docId) return
    try {
      const res = await knowledgeService.getDocumentDetail(docId)
      if (res.code === 200 && res.data) {
        setDocStatus(res.data.status)
      }
    } catch {
      // 状态拉取失败不阻塞页面
    }
  }, [docId])

  const fetchTagPool = useCallback(async () => {
    try {
      const res = await knowledgeService.listTags(0, 500)
      if (res.code === 200 && res.data) {
        setTagPool(res.data.content)
      }
    } catch {
      // 标签池拉取失败不阻塞页面
    }
  }, [])

  useEffect(() => {
    fetchChunks()
  }, [fetchChunks])

  useEffect(() => {
    fetchDocStatus()
    fetchTagPool()
  }, [fetchDocStatus, fetchTagPool])

  // 有分片正在向量化（或文档解析中）时 3 秒轮询，全部同步后停止
  const hasEmbedding = chunks.some((c) => c.vectorStatus === 'embedding')
  const isParsing = docStatus === 'PENDING' || docStatus === 'PARSING' || docStatus === 'CANCELING'

  useEffect(() => {
    const shouldPoll = hasEmbedding || isParsing
    if (shouldPoll && !pollingRef.current) {
      pollingRef.current = setInterval(() => {
        fetchChunks(true)
        fetchDocStatus()
      }, 3000)
    } else if (!shouldPoll && pollingRef.current) {
      clearInterval(pollingRef.current)
      pollingRef.current = null
    }
    return () => {
      if (pollingRef.current) {
        clearInterval(pollingRef.current)
        pollingRef.current = null
      }
    }
  }, [hasEmbedding, isParsing, fetchChunks, fetchDocStatus])

  const handleEditClick = (chunk: KnowledgeChunkDTO) => {
    setEditingChunk(chunk)
    setEditContent(chunk.content)
    setEditTagIds(chunk.tagIds || [])
    setEditModalOpen(true)
  }

  const handleSaveEdit = async () => {
    if (!editingChunk) return
    if (!editContent.trim()) {
      messageApi.error('分片内容不能为空')
      return
    }
    setSaving(true)
    try {
      const res = await knowledgeService.updateChunk(editingChunk.id, {
        content: editContent,
        tagIds: editTagIds,
      })
      if (res.code === 200) {
        messageApi.success('保存成功，向量化后台进行中')
        setEditModalOpen(false)
        fetchChunks(true)
      } else {
        messageApi.error(res.msg || '保存失败')
      }
    } catch {
      messageApi.error('保存失败')
    } finally {
      setSaving(false)
    }
  }

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
          <div className="text-white/50 text-center py-12">
            {isParsing ? '解析中，分段即将生成…' : '暂无分段数据'}
          </div>
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
                    {chunk.source && <Tag className="m-0">{chunk.source}</Tag>}
                    {chunk.tagIds &&
                      chunk.tagIds.length > 0 &&
                      chunk.tagIds.map((tagId) => (
                        <Tag key={tagId} color="blue" className="m-0">
                          {tagNameMap.get(tagId) ?? `标签#${tagId}`}
                        </Tag>
                      ))}
                  </div>
                }
                extra={
                  <Space>
                    {chunk.vectorStatus === 'embedding' && (
                      <Tag icon={<LoadingOutlined spin />} color="processing" className="m-0">
                        向量化中
                      </Tag>
                    )}
                    {isAdmin && (
                      <Button
                        type="link"
                        size="small"
                        icon={<EditOutlined />}
                        onClick={() => handleEditClick(chunk)}
                      >
                        编辑
                      </Button>
                    )}
                  </Space>
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

      <Modal
        title={`编辑分片 - ID: ${editingChunk?.id}`}
        open={editModalOpen}
        onOk={handleSaveEdit}
        onCancel={() => setEditModalOpen(false)}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        width={720}
      >
        <div className="flex flex-col gap-3">
          <div>
            <div className="text-white/70 mb-1">分片内容（Markdown）</div>
            <Input.TextArea
              rows={10}
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
              placeholder="请输入分片内容"
            />
          </div>
          <div>
            <div className="text-white/70 mb-1">标签（仅从现有标签池选择）</div>
            <Select
              mode="multiple"
              style={{ width: '100%' }}
              placeholder="选择标签"
              value={editTagIds}
              onChange={(values) => setEditTagIds(values)}
              options={tagPool.map((tag) => ({ label: tag.tagName, value: tag.id }))}
              optionFilterProp="label"
              showSearch
            />
          </div>
        </div>
      </Modal>
    </div>
  )
}
