'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import { App, Button, Grid, Modal, Pagination, Spin, Table, Tag } from 'antd'
import { PlusOutlined, HolderOutlined, UpOutlined, DownOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { DndContext, PointerSensor, useSensor, useSensors, closestCenter } from '@dnd-kit/core'
import type { DragEndEvent } from '@dnd-kit/core'
import {
  SortableContext,
  useSortable,
  verticalListSortingStrategy,
  arrayMove,
} from '@dnd-kit/sortable'
import type { CompetitionResponseDTO, CompetitionLevel } from '@/apis/schema/type'
import { COMPETITION_LEVEL_LABELS, COMPETITION_LEVEL_COLORS } from '@/types/competition'
import { adminCompetitionService } from '@/apis/services/admin-competition.service'
import CompetitionDrawer, { type DrawerMode } from './CompetitionDrawer'

const PAGE_SIZE = 20

const { useBreakpoint } = Grid

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

export default function CompetitionManagementPage() {
  const { message: messageApi } = App.useApp()
  const screens = useBreakpoint()
  const isMobile = !screens.md

  // Data state
  const [list, setList] = useState<CompetitionResponseDTO[]>([])
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(1)

  // Drawer state
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('view')
  const [selectedCompetition, setSelectedCompetition] = useState<CompetitionResponseDTO | null>(
    null
  )

  // Delete modal state
  const [deleteModalOpen, setDeleteModalOpen] = useState(false)
  const [deletingCompetition, setDeletingCompetition] = useState<CompetitionResponseDTO | null>(
    null
  )

  // Drag sensors
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { distance: 5 },
    })
  )

  // Fetch list
  const fetchList = useCallback(async () => {
    setLoading(true)
    try {
      const res = await adminCompetitionService.getList(page - 1, PAGE_SIZE)
      if (res.data) {
        setList(res.data.content)
        setTotalElements(res.data.totalElements)
      }
    } finally {
      setLoading(false)
    }
  }, [page])

  useEffect(() => {
    fetchList()
  }, [fetchList])

  // Open drawer for viewing
  const handleRowClick = (record: CompetitionResponseDTO) => {
    setSelectedCompetition(record)
    setDrawerMode('view')
    setDrawerOpen(true)
  }

  // Open drawer for creating
  const handleCreate = () => {
    setSelectedCompetition(null)
    setDrawerMode('create')
    setDrawerOpen(true)
  }

  // Delete flow
  const handleDeleteClick = (competition: CompetitionResponseDTO) => {
    setDeletingCompetition(competition)
    setDeleteModalOpen(true)
  }

  const handleDeleteConfirm = async () => {
    if (!deletingCompetition) return
    try {
      await adminCompetitionService.delete(deletingCompetition.id)
      messageApi.success('删除成功')
      setDeleteModalOpen(false)
      setDrawerOpen(false)
      fetchList()
    } catch {
      messageApi.error('删除失败')
    }
  }

  // Drawer success callback
  const handleDrawerSuccess = () => {
    setDrawerOpen(false)
    fetchList()
  }

  // Drag end handler - batch update current page sort orders
  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event
    if (!over || active.id === over.id) return

    const oldIndex = list.findIndex((item) => String(item.id) === active.id)
    const newIndex = list.findIndex((item) => String(item.id) === over.id)
    if (oldIndex === -1 || newIndex === -1) return

    // Optimistically reorder
    const newList = arrayMove(list, oldIndex, newIndex)
    setList(newList)

    // Recalculate sortOrder for all items on current page
    const baseSortOrder = (page - 1) * PAGE_SIZE
    const sortItems = newList.map((item, index) => ({
      id: item.id,
      sortOrder: baseSortOrder + index + 1,
    }))

    try {
      await adminCompetitionService.batchUpdateSortOrder({ items: sortItems })
    } catch {
      // Revert on failure
      setList(list)
      messageApi.error('排序更新失败')
    }
  }

  // Move up/down handler
  const handleMove = async (id: number, direction: 'UP' | 'DOWN') => {
    try {
      await adminCompetitionService.moveCompetition(id, { direction })
      fetchList()
    } catch {
      messageApi.error(direction === 'UP' ? '上移失败' : '下移失败')
    }
  }

  // Table columns
  const columns: ColumnsType<CompetitionResponseDTO> = useMemo(() => {
    const isFirst = (index: number) => page === 1 && index === 0
    const isLast = (index: number) => {
      const totalPages = Math.ceil(totalElements / PAGE_SIZE)
      return page >= totalPages && index === list.length - 1
    }

    const cols: ColumnsType<CompetitionResponseDTO> = [
      {
        title: '',
        width: 40,
        render: () => <HolderOutlined className="cursor-grab text-white/30 hover:text-white/60" />,
      },
      {
        title: '名称',
        dataIndex: 'name',
        key: 'name',
        ellipsis: true,
      },
    ]

    if (!isMobile) {
      cols.push(
        {
          title: '简称',
          dataIndex: 'shortName',
          key: 'shortName',
          width: 100,
          ellipsis: true,
        },
        {
          title: '级别',
          dataIndex: 'level',
          key: 'level',
          width: 80,
          render: (level: CompetitionLevel) => (
            <Tag color={COMPETITION_LEVEL_COLORS[level]} className="bg-transparent">
              {COMPETITION_LEVEL_LABELS[level]}
            </Tag>
          ),
        },
        {
          title: '月份',
          dataIndex: 'month',
          key: 'month',
          width: 70,
        },
        {
          title: '主办方',
          dataIndex: 'organizer',
          key: 'organizer',
          ellipsis: true,
        }
      )
    } else {
      cols.push({
        title: '级别',
        dataIndex: 'level',
        key: 'level',
        width: 80,
        render: (level: CompetitionLevel) => (
          <Tag color={COMPETITION_LEVEL_COLORS[level]} bordered={false}>
            {COMPETITION_LEVEL_LABELS[level]}
          </Tag>
        ),
      })
    }

    // Move up/down buttons column
    cols.push({
      title: '',
      key: 'move',
      width: 60,
      render: (_: unknown, __: CompetitionResponseDTO, index: number) => (
        <div className="flex gap-1">
          <Button
            type="text"
            size="small"
            icon={<UpOutlined />}
            disabled={isFirst(index)}
            onClick={(e) => {
              e.stopPropagation()
              handleMove(list[index].id, 'UP')
            }}
          />
          <Button
            type="text"
            size="small"
            icon={<DownOutlined />}
            disabled={isLast(index)}
            onClick={(e) => {
              e.stopPropagation()
              handleMove(list[index].id, 'DOWN')
            }}
          />
        </div>
      ),
    })

    return cols
  }, [isMobile, page, totalElements, list])

  return (
    <div className="flex flex-col gap-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium text-white/90 m-0">竞赛管理</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新建竞赛
        </Button>
      </div>

      {/* Table with drag-and-drop */}
      <Spin spinning={loading}>
        <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
          <SortableContext
            items={list.map((item) => String(item.id))}
            strategy={verticalListSortingStrategy}
          >
            <Table
              dataSource={list}
              columns={columns}
              rowKey={(record) => String(record.id)}
              size="small"
              pagination={false}
              onRow={(record) => ({
                onClick: () => handleRowClick(record),
                className: 'cursor-pointer',
              })}
              components={{
                body: { row: DraggableRow },
              }}
              locale={{ emptyText: '暂无竞赛数据' }}
            />
          </SortableContext>
        </DndContext>
      </Spin>

      {/* Pagination */}
      {totalElements > PAGE_SIZE && (
        <div className="flex justify-center">
          <Pagination
            current={page}
            total={totalElements}
            pageSize={PAGE_SIZE}
            showSizeChanger={false}
            onChange={(p) => setPage(p)}
          />
        </div>
      )}

      {/* Competition Drawer */}
      <CompetitionDrawer
        open={drawerOpen}
        competition={selectedCompetition}
        mode={drawerMode}
        onClose={() => setDrawerOpen(false)}
        onSuccess={handleDrawerSuccess}
        onDelete={handleDeleteClick}
        onEdit={() => setDrawerMode('edit')}
      />

      {/* Delete confirmation modal */}
      <Modal
        title="确认删除"
        open={deleteModalOpen}
        onOk={handleDeleteConfirm}
        onCancel={() => setDeleteModalOpen(false)}
        okText="确认删除"
        cancelText="取消"
        okButtonProps={{ danger: true }}
      >
        <p>确认删除竞赛「{deletingCompetition?.name}」？此操作不可撤销。</p>
      </Modal>
    </div>
  )
}
