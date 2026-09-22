'use client'

import { useCallback, useEffect, useState } from 'react'
import { App, Button, Input, Modal, Pagination, Popconfirm, Spin, Table } from 'antd'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { KnowledgeTagDTO } from '@/apis/services/knowledge.service'
import { knowledgeService } from '@/apis/services/knowledge.service'
import { useAuth } from '@/hooks'

export default function KnowledgeTagsPage() {
  const { message: messageApi } = App.useApp()
  const { isAdmin } = useAuth()

  const [tags, setTags] = useState<KnowledgeTagDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)

  const [editingTag, setEditingTag] = useState<KnowledgeTagDTO | null>(null)
  const [editModalOpen, setEditModalOpen] = useState(false)
  const [editName, setEditName] = useState('')
  const [editDescription, setEditDescription] = useState('')
  const [saving, setSaving] = useState(false)

  const [createModalOpen, setCreateModalOpen] = useState(false)
  const [createName, setCreateName] = useState('')
  const [createDescription, setCreateDescription] = useState('')

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
    setEditName(tag.tagName)
    setEditDescription(tag.tagDescription || '')
    setEditModalOpen(true)
  }

  const handleSaveEdit = async () => {
    if (!editingTag) return
    if (!editName.trim()) {
      messageApi.error('标签名不能为空')
      return
    }
    setSaving(true)
    try {
      const res = await knowledgeService.updateTag(editingTag.id, {
        tagName: editName.trim(),
        description: editDescription,
      })
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

  const handleCreate = async () => {
    if (!createName.trim()) {
      messageApi.error('标签名不能为空')
      return
    }
    setSaving(true)
    try {
      const res = await knowledgeService.createTag({
        tagName: createName.trim(),
        description: createDescription || undefined,
      })
      if (res.code === 200) {
        messageApi.success('创建成功，标签向量生成中')
        setCreateModalOpen(false)
        setCreateName('')
        setCreateDescription('')
        fetchTags()
      } else {
        messageApi.error(res.msg || '创建失败')
      }
    } catch {
      messageApi.error('创建失败')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (tag: KnowledgeTagDTO) => {
    try {
      const res = await knowledgeService.deleteTag(tag.id)
      if (res.code === 200) {
        messageApi.success(`已删除，自动解除 ${res.data ?? 0} 个分片关联`)
        fetchTags()
      } else {
        messageApi.error(res.msg || '删除失败')
      }
    } catch {
      messageApi.error('删除失败')
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
    ...(isAdmin
      ? [
          {
            title: '操作' as const,
            key: 'actions',
            width: 200,
            render: (_: unknown, record: KnowledgeTagDTO) => (
              <div className="flex gap-1">
                <Button
                  type="link"
                  size="small"
                  icon={<EditOutlined />}
                  onClick={() => handleEditClick(record)}
                >
                  编辑
                </Button>
                <Popconfirm
                  title={`删除标签「${record.tagName}」？`}
                  description={`将自动解除与 ${record.chunksCount} 个分片的关联，此操作不可撤销。`}
                  okText="删除"
                  okButtonProps={{ danger: true }}
                  cancelText="取消"
                  onConfirm={() => handleDelete(record)}
                >
                  <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                    删除
                  </Button>
                </Popconfirm>
              </div>
            ),
          },
        ]
      : []),
  ]

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium text-white/90 m-0">知识库标签管理</h2>
        {isAdmin && (
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalOpen(true)}>
            新建标签
          </Button>
        )}
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
        title={`编辑标签 - ${editingTag?.tagName}`}
        open={editModalOpen}
        onOk={handleSaveEdit}
        onCancel={() => setEditModalOpen(false)}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
      >
        <div className="flex flex-col gap-3">
          <div>
            <div className="text-white/70 mb-1">标签名称（重命名会重新生成标签向量）</div>
            <Input
              value={editName}
              onChange={(e) => setEditName(e.target.value)}
              placeholder="请输入标签名称"
              maxLength={128}
            />
          </div>
          <div>
            <div className="text-white/70 mb-1">描述</div>
            <Input.TextArea
              rows={4}
              value={editDescription}
              onChange={(e) => setEditDescription(e.target.value)}
              placeholder="请输入标签描述"
              maxLength={512}
            />
          </div>
        </div>
      </Modal>

      <Modal
        title="新建标签"
        open={createModalOpen}
        onOk={handleCreate}
        onCancel={() => setCreateModalOpen(false)}
        confirmLoading={saving}
        okText="创建"
        cancelText="取消"
      >
        <div className="flex flex-col gap-3">
          <div>
            <div className="text-white/70 mb-1">标签名称</div>
            <Input
              value={createName}
              onChange={(e) => setCreateName(e.target.value)}
              placeholder="请输入标签名称"
              maxLength={128}
            />
          </div>
          <div>
            <div className="text-white/70 mb-1">描述（可选）</div>
            <Input.TextArea
              rows={4}
              value={createDescription}
              onChange={(e) => setCreateDescription(e.target.value)}
              placeholder="请输入标签描述"
              maxLength={512}
            />
          </div>
        </div>
      </Modal>
    </div>
  )
}
