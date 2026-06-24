'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import { App, Button, Form, Input, Modal, Pagination, Select, Spin, Switch, Table } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, HolderOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { DndContext, PointerSensor, useSensor, useSensors, closestCenter } from '@dnd-kit/core'
import type { DragEndEvent } from '@dnd-kit/core'
import {
  SortableContext,
  useSortable,
  verticalListSortingStrategy,
  arrayMove,
} from '@dnd-kit/sortable'
import type { SoftwareResourceDTO } from '@/apis/schema/type'
import {
  SOFTWARE_RESOURCE_DIRECTION_LABELS,
  SOFTWARE_RESOURCE_STATUS_LABELS,
  type SoftwareResourceDirection,
} from '@/apis/schema/enumerate'
import { adminSoftwareResourceService } from '@/apis/services/admin-software-resource.service'
import { useAuth } from '@/hooks'

const PAGE_SIZE = 20

/** 可拖拽的表格行组件 */
function DraggableRow({
  'data-row-key': id,
  ...rest
}: React.HTMLAttributes<HTMLTableRowElement> & {
  'data-row-key': string | number
}) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: String(id),
  })

  const style: React.CSSProperties = {
    ...rest.style,
    transform: transform ? `translate3d(0, ${transform.y}px, 0)` : undefined,
    transition,
    ...(isDragging ? { position: 'relative', zIndex: 9999 } : {}),
  }

  return <tr {...rest} ref={setNodeRef} style={style} {...attributes} {...listeners} />
}

interface FormValues {
  name: string
  direction: SoftwareResourceDirection
  category?: string
  description?: string
  externalUrl: string
  sortOrder?: number
  status: 'ACTIVE' | 'DISABLED'
}

export default function SoftwareResourceManagementPage() {
  const { message: messageApi } = App.useApp()
  const { isAdmin } = useAuth()
  const [form] = Form.useForm<FormValues>()

  const [resources, setResources] = useState<SoftwareResourceDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)

  // Optimistic list for drag-and-drop
  const [displayList, setDisplayList] = useState<SoftwareResourceDTO[]>([])

  const [modalOpen, setModalOpen] = useState(false)
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create')
  const [editingResource, setEditingResource] = useState<SoftwareResourceDTO | null>(null)

  const [deleteModalOpen, setDeleteModalOpen] = useState(false)
  const [deletingResource, setDeletingResource] = useState<SoftwareResourceDTO | null>(null)

  const fetchResources = useCallback(
    async (currentPage: number) => {
      setLoading(true)
      try {
        const res = await adminSoftwareResourceService.list(currentPage, PAGE_SIZE)
        if (res.code === 200 && res.data) {
          setResources(res.data.content)
          setTotal(res.data.totalElements)
        } else {
          messageApi.error(res.msg || '获取资源列表失败')
        }
      } catch {
        messageApi.error('获取资源列表失败')
      } finally {
        setLoading(false)
      }
    },
    [messageApi]
  )

  useEffect(() => {
    fetchResources(page)
  }, [fetchResources, page])

  // Sync optimistic list with fetched data
  useEffect(() => {
    setDisplayList(resources)
  }, [resources])

  // Drag sensors
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { distance: 5 },
    })
  )

  const openCreateModal = () => {
    setModalMode('create')
    setEditingResource(null)
    form.resetFields()
    form.setFieldsValue({ status: 'ACTIVE', sortOrder: 0 })
    setModalOpen(true)
  }

  const openEditModal = (resource: SoftwareResourceDTO) => {
    setModalMode('edit')
    setEditingResource(resource)
    form.setFieldsValue({
      name: resource.name,
      direction: resource.direction,
      category: resource.category ?? undefined,
      description: resource.description ?? undefined,
      externalUrl: resource.externalUrl,
      sortOrder: resource.sortOrder,
      status: resource.status,
    })
    setModalOpen(true)
  }

  const handleModalSubmit = async () => {
    try {
      const values = await form.validateFields()
      const payload = {
        name: values.name,
        direction: values.direction,
        category: values.category,
        description: values.description,
        externalUrl: values.externalUrl,
        sortOrder: values.sortOrder ?? 0,
      }

      if (modalMode === 'create') {
        const res = await adminSoftwareResourceService.create(payload)
        if (res.code === 200) {
          messageApi.success('创建成功')
          setModalOpen(false)
          fetchResources(page)
        } else {
          messageApi.error(res.msg || '创建失败')
        }
      } else if (editingResource) {
        const res = await adminSoftwareResourceService.update(editingResource.id, {
          ...payload,
          status: values.status,
        })
        if (res.code === 200) {
          messageApi.success('更新成功')
          setModalOpen(false)
          fetchResources(page)
        } else {
          messageApi.error(res.msg || '更新失败')
        }
      }
    } catch {
      // validation failed
    }
  }

  const handleDeleteClick = (resource: SoftwareResourceDTO) => {
    setDeletingResource(resource)
    setDeleteModalOpen(true)
  }

  const handleDeleteConfirm = async () => {
    if (!deletingResource) return
    try {
      const res = await adminSoftwareResourceService.delete(deletingResource.id)
      if (res.code === 200) {
        messageApi.success('删除成功')
        setDeleteModalOpen(false)
        fetchResources(page)
      } else {
        messageApi.error(res.msg || '删除失败')
      }
    } catch {
      messageApi.error('删除失败')
    }
  }

  const handleToggleStatus = async (resource: SoftwareResourceDTO, checked: boolean) => {
    try {
      const res = await adminSoftwareResourceService.update(resource.id, {
        name: resource.name,
        direction: resource.direction,
        category: resource.category ?? undefined,
        description: resource.description ?? undefined,
        externalUrl: resource.externalUrl,
        sortOrder: resource.sortOrder,
        status: checked ? 'ACTIVE' : 'DISABLED',
      })
      if (res.code === 200) {
        messageApi.success(checked ? '已启用' : '已禁用')
        fetchResources(page)
      } else {
        messageApi.error(res.msg || '操作失败')
      }
    } catch {
      messageApi.error('操作失败')
    }
  }

  // Drag end handler - batch update current page sort orders (admin only)
  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event
    if (!over || active.id === over.id) return

    const oldIndex = displayList.findIndex((item) => String(item.id) === active.id)
    const newIndex = displayList.findIndex((item) => String(item.id) === over.id)
    if (oldIndex === -1 || newIndex === -1) return

    // Optimistically reorder
    const newList = arrayMove(displayList, oldIndex, newIndex)
    setDisplayList(newList)

    // Recalculate sortOrder for all items on current page
    const baseSortOrder = page * PAGE_SIZE
    const sortItems = newList.map((item, index) => ({
      id: item.id,
      sortOrder: baseSortOrder + index + 1,
    }))

    try {
      const res = await adminSoftwareResourceService.batchUpdateSortOrder({ items: sortItems })
      if (res.code !== 200) {
        setDisplayList(resources)
        messageApi.error(res.msg || '排序更新失败')
      }
    } catch {
      // Revert on failure
      setDisplayList(resources)
      messageApi.error('排序更新失败')
    }
  }

  const directionOptions = useMemo(
    () =>
      Object.entries(SOFTWARE_RESOURCE_DIRECTION_LABELS).map(([value, label]) => ({
        value,
        label,
      })),
    []
  )

  const columns: ColumnsType<SoftwareResourceDTO> = useMemo(
    () => [
      ...(isAdmin
        ? [
            {
              title: '',
              key: 'drag',
              width: 40,
              render: () => (
                <HolderOutlined className="cursor-grab text-white/30 hover:text-white/60" />
              ),
            },
          ]
        : []),
      {
        title: '名称',
        dataIndex: 'name',
        key: 'name',
        ellipsis: true,
      },
      {
        title: '方向',
        dataIndex: 'direction',
        key: 'direction',
        width: 120,
        render: (direction: SoftwareResourceDirection) =>
          SOFTWARE_RESOURCE_DIRECTION_LABELS[direction] || direction,
      },
      {
        title: '分类',
        dataIndex: 'category',
        key: 'category',
        width: 120,
        render: (category: string | null) => category || '-',
      },
      {
        title: '排序',
        dataIndex: 'sortOrder',
        key: 'sortOrder',
        width: 80,
      },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 100,
        render: isAdmin
          ? (status: 'ACTIVE' | 'DISABLED', record: SoftwareResourceDTO) => (
              <Switch
                checked={status === 'ACTIVE'}
                onChange={(checked) => handleToggleStatus(record, checked)}
                checkedChildren="启用"
                unCheckedChildren="禁用"
              />
            )
          : (status: 'ACTIVE' | 'DISABLED') => (
              <span>{SOFTWARE_RESOURCE_STATUS_LABELS[status] || status}</span>
            ),
      },
      ...(isAdmin
        ? [
            {
              title: '操作',
              key: 'actions',
              width: 160,
              render: (_: unknown, record: SoftwareResourceDTO) => (
                <div className="flex gap-2">
                  <Button
                    type="link"
                    size="small"
                    icon={<EditOutlined />}
                    onClick={() => openEditModal(record)}
                  >
                    编辑
                  </Button>
                  <Button
                    type="link"
                    size="small"
                    danger
                    icon={<DeleteOutlined />}
                    onClick={() => handleDeleteClick(record)}
                  >
                    删除
                  </Button>
                </div>
              ),
            },
          ]
        : []),
    ],
    [page, isAdmin]
  )

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium text-white/90 m-0">软件资源管理</h2>
        {isAdmin && (
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
            新增资源
          </Button>
        )}
      </div>

      <Spin spinning={loading}>
        {isAdmin ? (
          <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragEnd={handleDragEnd}
          >
            <SortableContext
              items={displayList.map((item) => String(item.id))}
              strategy={verticalListSortingStrategy}
            >
              <Table
                dataSource={displayList}
                columns={columns}
                rowKey={(record) => String(record.id)}
                size="small"
                pagination={false}
                components={{
                  body: { row: DraggableRow },
                }}
                locale={{ emptyText: '暂无资源数据' }}
                scroll={{ x: 'max-content' }}
              />
            </SortableContext>
          </DndContext>
        ) : (
          <Table
            dataSource={displayList}
            columns={columns}
            rowKey={(record) => String(record.id)}
            size="small"
            pagination={false}
            locale={{ emptyText: '暂无资源数据' }}
            scroll={{ x: 'max-content' }}
          />
        )}
      </Spin>

      {total > PAGE_SIZE && (
        <div className="flex justify-center">
          <Pagination
            current={page + 1}
            total={total}
            pageSize={PAGE_SIZE}
            showSizeChanger={false}
            onChange={(p) => setPage(p - 1)}
          />
        </div>
      )}

      <Modal
        title={modalMode === 'create' ? '新增资源' : '编辑资源'}
        open={modalOpen}
        onOk={handleModalSubmit}
        onCancel={() => setModalOpen(false)}
        okText="保存"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            label="软件名称"
            name="name"
            rules={[{ required: true, message: '请输入软件名称' }]}
          >
            <Input placeholder="例如：VS Code" />
          </Form.Item>
          <Form.Item
            label="方向"
            name="direction"
            rules={[{ required: true, message: '请选择方向' }]}
          >
            <Select placeholder="请选择方向" options={directionOptions} />
          </Form.Item>
          <Form.Item label="分类" name="category">
            <Input placeholder="例如：IDE、工具链" />
          </Form.Item>
          <Form.Item label="描述" name="description">
            <Input.TextArea rows={3} placeholder="简要描述用途或安装说明" />
          </Form.Item>
          <Form.Item
            label="外部下载链接"
            name="externalUrl"
            rules={[{ required: true, message: '请输入外部下载链接' }]}
          >
            <Input placeholder="https://..." />
          </Form.Item>
          <Form.Item label="排序权重" name="sortOrder">
            <Input type="number" placeholder="数值越小越靠前" />
          </Form.Item>
          {modalMode === 'edit' && (
            <Form.Item label="状态" name="status" rules={[{ required: true }]}>
              <Select
                options={[
                  { value: 'ACTIVE', label: SOFTWARE_RESOURCE_STATUS_LABELS.ACTIVE },
                  { value: 'DISABLED', label: SOFTWARE_RESOURCE_STATUS_LABELS.DISABLED },
                ]}
              />
            </Form.Item>
          )}
        </Form>
      </Modal>

      <Modal
        title="确认删除"
        open={deleteModalOpen}
        onOk={handleDeleteConfirm}
        onCancel={() => setDeleteModalOpen(false)}
        okText="确认删除"
        cancelText="取消"
        okButtonProps={{ danger: true }}
      >
        <p>确认删除资源「{deletingResource?.name}」？此操作不可撤销。</p>
      </Modal>
    </div>
  )
}
