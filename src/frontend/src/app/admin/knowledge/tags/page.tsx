'use client'

import { useCallback, useEffect, useState } from 'react'
import { App, Button, Input, Modal, Pagination, Spin, Table } from 'antd'
import { EditOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { KnowledgeTagDTO } from '@/apis/services/knowledge.service'
import { knowledgeService } from '@/apis/services/knowledge.service'

export default function KnowledgeTagsPage() {
  const { message: messageApi } = App.useApp()

  const [tags, setTags] = useState<KnowledgeTagDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)

  const [editingTag, setEditingTag] = useState<KnowledgeTagDTO | null>(null)
  const [editModalOpen, setEditModalOpen] = useState(false)
  const [editDescription, setEditDescription] = useState('')
  const [saving, setSaving] = useState(false)

  const fetchTags = useCallback(async () => {
    setLoading(true)
    try {
      const res = await knowledgeService.listTags(page - 1, pageSize)
      if (res.code === 200 && res.data) {
        setTags(res.data.content)
        setTotal(res.data.totalElements)
      }
    } finally {
      setLoading(false)
    }
  }, [page, pageSize])

  useEffect(() => {
    fetchTags()
  }, [fetchTags])

  const handleEditClick = (tag: KnowledgeTagDTO) => {
    setEditingTag(tag)
    setEditDescription(tag.tagDescription || '')
    setEditModalOpen(true)
  }

  const handleSaveDescription = async () => {
    if (!editingTag) return
    setSaving(true)
    try {
      const res = await knowledgeService.updateTagDescription(editingTag.id, editDescription)
      if (res.code === 200) {
        messageApi.success('更新成功')
        setEditModalOpen(false)
        fetchTags()
      } else {
        messageApi.error(res.msg || '更新失败')
      }
    } catch {
      messageApi.error('更新失败')
    } finally {
      setSaving(false)
    }
  }

  const columns: ColumnsType<KnowledgeTagDTO> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '标签名称',
      dataIndex: 'tagName',
      key: 'tagName',
    },
    {
      title: '描述',
      dataIndex: 'tagDescription',
      key: 'tagDescription',
      ellipsis: true,
      render: (desc: string) => desc || '-',
    },
    {
      title: '关联分段数',
      dataIndex: 'chunksCount',
      key: 'chunksCount',
      width: 120,
    },
    {
      title: '操作',
      key: 'actions',
      width: 120,
      render: (_, record) => (
        <Button
          type="link"
          size="small"
          icon={<EditOutlined />}
          onClick={() => handleEditClick(record)}
        >
          编辑描述
        </Button>
      ),
    },
  ]

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium text-white/90 m-0">知识库标签管理</h2>
      </div>

      <Spin spinning={loading}>
        <Table
          dataSource={tags}
          columns={columns}
          rowKey={(record) => String(record.id)}
          size="small"
          pagination={false}
          locale={{ emptyText: '暂无标签' }}
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
        title={`编辑标签描述 - ${editingTag?.tagName}`}
        open={editModalOpen}
        onOk={handleSaveDescription}
        onCancel={() => setEditModalOpen(false)}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
      >
        <Input.TextArea
          rows={4}
          value={editDescription}
          onChange={(e) => setEditDescription(e.target.value)}
          placeholder="请输入标签描述"
        />
      </Modal>
    </div>
  )
}
