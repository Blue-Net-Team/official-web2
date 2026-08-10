'use client'

import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { App, Button, Modal, Pagination, Spin, Table, Tag, Upload, message } from 'antd'
import {
  UploadOutlined,
  ReloadOutlined,
  DeleteOutlined,
  EyeOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { KnowledgeDocDTO, DocParseStatus } from '@/apis/services/knowledge.service'
import { knowledgeService } from '@/apis/services/knowledge.service'
import { useAuth } from '@/hooks'
import { useRouter } from 'next/navigation'

const STATUS_MAP: Record<DocParseStatus, { label: string; color: string }> = {
  PENDING: { label: '待解析', color: 'default' },
  PARSING: { label: '解析中', color: 'processing' },
  COMPLETED: { label: '已完成', color: 'success' },
  FAILED: { label: '失败', color: 'error' },
  CANCELING: { label: '取消中', color: 'warning' },
  CANCELED: { label: '已取消', color: 'default' },
}

export default function KnowledgeDocsPage() {
  const { message: messageApi } = App.useApp()
  const { isAdmin } = useAuth()
  const router = useRouter()

  const [docs, setDocs] = useState<KnowledgeDocDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)

  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const fetchDocs = useCallback(
    async (silent = false) => {
      if (!silent) setLoading(true)
      try {
        const res = await knowledgeService.listDocuments(page - 1, pageSize)
        if (res.code === 200 && res.data) {
          setDocs(res.data.content)
          setTotal(res.data.totalElements)
        }
      } finally {
        if (!silent) setLoading(false)
      }
    },
    [page, pageSize]
  )

  useEffect(() => {
    fetchDocs()
  }, [fetchDocs])

  // 轮询解析中的文档（静默刷新，不触发 Spin）
  useEffect(() => {
    const hasParsing = docs.some(
      (d) => d.status === 'PENDING' || d.status === 'PARSING' || d.status === 'CANCELING'
    )
    if (hasParsing && !pollingRef.current) {
      pollingRef.current = setInterval(() => {
        fetchDocs(true)
      }, 3000)
    } else if (!hasParsing && pollingRef.current) {
      clearInterval(pollingRef.current)
      pollingRef.current = null
    }
    return () => {
      if (pollingRef.current) {
        clearInterval(pollingRef.current)
        pollingRef.current = null
      }
    }
  }, [docs, fetchDocs])

  const handleUpload = async (file: File) => {
    if (!file.name.toLowerCase().endsWith('.md')) {
      messageApi.error('仅支持上传 .md 文件')
      return false
    }
    setUploading(true)
    try {
      const res = await knowledgeService.uploadDocument(file, file.name)
      if (res.code === 200) {
        messageApi.success('上传成功，开始解析')
        fetchDocs()
      } else {
        messageApi.error(res.msg || '上传失败')
      }
    } catch (e) {
      messageApi.error('上传失败')
    } finally {
      setUploading(false)
    }
    return false
  }

  const handleReparse = async (doc: KnowledgeDocDTO) => {
    try {
      const res = await knowledgeService.reparseDocument(doc.id)
      if (res.code === 200) {
        messageApi.success('重新解析已触发')
        fetchDocs()
      } else {
        messageApi.error(res.msg || '操作失败')
      }
    } catch {
      messageApi.error('操作失败')
    }
  }

  const handleCancel = async (doc: KnowledgeDocDTO) => {
    try {
      const res = await knowledgeService.cancelParse(doc.id)
      if (res.code === 200) {
        messageApi.success('取消请求已发送')
        fetchDocs()
      } else {
        messageApi.error(res.msg || '操作失败')
      }
    } catch {
      messageApi.error('操作失败')
    }
  }

  const [deletingDoc, setDeletingDoc] = useState<KnowledgeDocDTO | null>(null)
  const [deleteModalOpen, setDeleteModalOpen] = useState(false)
  const [deleteLoading, setDeleteLoading] = useState(false)

  const handleDeleteClick = (doc: KnowledgeDocDTO) => {
    setDeletingDoc(doc)
    setDeleteModalOpen(true)
  }

  const handleDeleteConfirm = async () => {
    if (!deletingDoc) return
    setDeleteLoading(true)
    try {
      const res = await knowledgeService.deleteDocument(deletingDoc.id)
      if (res.code === 200) {
        messageApi.success('删除成功')
        setDeleteModalOpen(false)
        fetchDocs()
      } else {
        messageApi.error(res.msg || '删除失败')
      }
    } catch {
      messageApi.error('删除失败')
    } finally {
      setDeleteLoading(false)
    }
  }

  const columns: ColumnsType<KnowledgeDocDTO> = useMemo(
    () => [
      {
        title: 'ID',
        dataIndex: 'id',
        key: 'id',
        width: 80,
      },
      {
        title: '标题',
        dataIndex: 'title',
        key: 'title',
        ellipsis: true,
      },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 120,
        render: (status: DocParseStatus) => {
          const info = STATUS_MAP[status]
          return <Tag color={info.color}>{info.label}</Tag>
        },
      },
      {
        title: '分段数',
        dataIndex: 'chunkCount',
        key: 'chunkCount',
        width: 100,
        render: (count: number) => count || '-',
      },
      {
        title: '创建时间',
        dataIndex: 'createdAt',
        key: 'createdAt',
        width: 180,
        render: (v: string) => (v ? new Date(v).toLocaleString() : '-'),
      },
      {
        title: '操作',
        key: 'actions',
        width: 280,
        fixed: 'right',
        render: (_, record) => (
          <div className="flex gap-1">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => router.push(`/admin/knowledge/docs/${record.id}/chunks`)}
            >
              分段
            </Button>
            {isAdmin &&
              (record.status === 'COMPLETED' ||
                record.status === 'FAILED' ||
                record.status === 'CANCELED') && (
                <Button
                  type="link"
                  size="small"
                  icon={<ReloadOutlined />}
                  onClick={() => handleReparse(record)}
                >
                  重解析
                </Button>
              )}
            {isAdmin && (record.status === 'PENDING' || record.status === 'PARSING') && (
              <Button
                type="link"
                size="small"
                danger
                icon={<CloseCircleOutlined />}
                onClick={() => handleCancel(record)}
              >
                取消
              </Button>
            )}
            {isAdmin && (
              <Button
                type="link"
                size="small"
                danger
                icon={<DeleteOutlined />}
                onClick={() => handleDeleteClick(record)}
              >
                删除
              </Button>
            )}
          </div>
        ),
      },
    ],
    [router]
  )

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium text-white/90 m-0">知识库文档管理</h2>
        {isAdmin && (
          <Upload beforeUpload={handleUpload} showUploadList={false} accept=".md">
            <Button type="primary" icon={<UploadOutlined />} loading={uploading}>
              上传文档
            </Button>
          </Upload>
        )}
      </div>

      <Spin spinning={loading}>
        <Table
          dataSource={docs}
          columns={columns}
          rowKey={(record) => String(record.id)}
          size="small"
          pagination={false}
          locale={{ emptyText: '暂无文档' }}
          scroll={{ x: 'max-content' }}
        />
      </Spin>

      <div className="flex justify-end">
        <Pagination
          current={page}
          pageSize={pageSize}
          total={total}
          showSizeChanger
          onChange={(p, ps) => {
            setPage(p)
            if (ps) setPageSize(ps)
          }}
        />
      </div>

      <Modal
        title="确认删除"
        open={deleteModalOpen}
        onOk={handleDeleteConfirm}
        onCancel={() => setDeleteModalOpen(false)}
        okText="确认删除"
        cancelText="取消"
        confirmLoading={deleteLoading}
        okButtonProps={{ danger: true }}
      >
        <p>确认删除文档「{deletingDoc?.title}」？此操作不可撤销。</p>
      </Modal>
    </div>
  )
}
