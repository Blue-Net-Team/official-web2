'use client'

import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { App, Button, Grid, Popconfirm, Spin, Table, Tabs, Tag } from 'antd'
import { DeleteOutlined, EditOutlined, HolderOutlined, PlusOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { DndContext, PointerSensor, useSensor, useSensors, closestCenter } from '@dnd-kit/core'
import type { DragEndEvent } from '@dnd-kit/core'
import {
  SortableContext,
  useSortable,
  verticalListSortingStrategy,
  arrayMove,
} from '@dnd-kit/sortable'
import type { LearningStepDTO } from '@/apis/schema/direction.dto'
import { adminDirectionService } from '@/apis/services/direction.service'
import LearningStepDrawer from './LearningStepDrawer'

const { useBreakpoint } = Grid

const DIRECTION_TABS = [
  { key: 'cv', label: '计算机视觉' },
  { key: 'embed', label: '嵌入式开发' },
  { key: 'struct', label: '结构设计' },
]

/** 把行级拖拽监听暴露给把手单元格，避免整行铺满 listeners 抢占按钮点击 */
type DragHandleProps = Pick<ReturnType<typeof useSortable>, 'attributes' | 'listeners'>

const DragHandleContext = createContext<DragHandleProps | null>(null)

/** 可拖拽的表格行组件：setNodeRef/transform 作用于整行，listeners 只给把手 */
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

  return (
    <DragHandleContext.Provider value={{ attributes, listeners }}>
      <tr {...rest} ref={setNodeRef} style={style} />
    </DragHandleContext.Provider>
  )
}

/** 拖拽把手单元格：只有这里绑定拖拽监听 */
function DragHandleCell() {
  const dragHandle = useContext(DragHandleContext)
  if (!dragHandle) {
    return <HolderOutlined className="text-white/30" />
  }
  return (
    <span
      {...dragHandle.attributes}
      {...dragHandle.listeners}
      className="inline-flex cursor-grab touch-none items-center text-white/30 hover:text-white/60"
    >
      <HolderOutlined />
    </span>
  )
}

export default function LearningPathManagementPage() {
  const { message: messageApi } = App.useApp()
  const screens = useBreakpoint()
  const isMobile = !screens.md

  const [activeDirection, setActiveDirection] = useState('cv')
  const [steps, setSteps] = useState<LearningStepDTO[]>([])
  const [loading, setLoading] = useState(false)

  // Drawer state
  const [drawerVisible, setDrawerVisible] = useState(false)
  const [editingRecord, setEditingRecord] = useState<LearningStepDTO | null>(null)

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { distance: 5 },
    })
  )

  const fetchSteps = useCallback(
    async (slug: string) => {
      setLoading(true)
      try {
        const response = await adminDirectionService.getLearningPath(slug)
        if (response.code === 200 && response.data) {
          setSteps(response.data.steps)
        } else {
          messageApi.error(response.msg || '获取学习路径失败')
        }
      } catch (error) {
        console.error('获取学习路径失败:', error)
        messageApi.error('获取学习路径失败')
      } finally {
        setLoading(false)
      }
    },
    [messageApi]
  )

  useEffect(() => {
    fetchSteps(activeDirection)
  }, [activeDirection, fetchSteps])

  const handleCreate = () => {
    setEditingRecord(null)
    setDrawerVisible(true)
  }

  const handleEdit = (record: LearningStepDTO) => {
    setEditingRecord(record)
    setDrawerVisible(true)
  }

  const handleDelete = async (record: LearningStepDTO) => {
    try {
      const response = await adminDirectionService.deleteStep(record.id)
      if (response.code === 200) {
        messageApi.success('删除成功')
        fetchSteps(activeDirection)
      } else {
        messageApi.error(`删除失败: ${response.msg}`)
      }
    } catch (error) {
      console.error('删除学习步骤失败:', error)
      messageApi.error('删除失败')
    }
  }

  const handleDrawerSuccess = () => {
    setDrawerVisible(false)
    fetchSteps(activeDirection)
  }

  /** 拖拽结束：乐观更新本地顺序，失败则回滚 */
  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event
    if (!over || active.id === over.id) return

    const oldIndex = steps.findIndex((item) => String(item.id) === active.id)
    const newIndex = steps.findIndex((item) => String(item.id) === over.id)
    if (oldIndex === -1 || newIndex === -1) return

    const previousList = steps
    const newList = arrayMove(steps, oldIndex, newIndex)
    setSteps(newList)

    try {
      const response = await adminDirectionService.batchUpdateSortOrder(activeDirection, {
        items: newList.map((item, index) => ({ id: item.id, sortOrder: index + 1 })),
      })
      if (response.code !== 200) {
        throw new Error(response.msg || '排序更新失败')
      }
    } catch (error) {
      console.error('排序更新失败:', error)
      setSteps(previousList)
      messageApi.error('排序更新失败')
    }
  }

  const columns: ColumnsType<LearningStepDTO> = [
    {
      title: '',
      key: 'drag',
      width: 40,
      render: () => <DragHandleCell />,
    },
    {
      title: '步骤序号',
      key: 'index',
      width: 100,
      // 序号由行位置派生，不依赖后端字段（接口不分页，整份列表一次返回）
      render: (_, __, index) => String(index + 1).padStart(2, '0'),
    },
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
    },
    {
      title: '相关链接',
      dataIndex: 'relatedLink',
      key: 'relatedLink',
      ellipsis: true,
      responsive: ['md'],
      render: (link: string | null) =>
        link ? (
          <a href={link} target="_blank" rel="noreferrer">
            {link}
          </a>
        ) : (
          <Tag>无</Tag>
        ),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <div className="flex gap-1">
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            size="small"
          />
          <Popconfirm
            title="删除学习步骤"
            description={`确定删除「${record.title}」吗？`}
            okText="删除"
            cancelText="取消"
            okButtonProps={{ danger: true }}
            onConfirm={() => handleDelete(record)}
          >
            <Button type="text" danger icon={<DeleteOutlined />} size="small" />
          </Popconfirm>
        </div>
      ),
    },
  ]

  return (
    <div>
      <div className="mb-4 flex justify-between items-center">
        <h2 className="m-0">学习路线管理</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新增步骤
        </Button>
      </div>

      <Tabs
        activeKey={activeDirection}
        onChange={setActiveDirection}
        items={DIRECTION_TABS.map((tab) => ({ key: tab.key, label: tab.label }))}
      />

      <Spin spinning={loading}>
        <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
          <SortableContext
            items={steps.map((step) => String(step.id))}
            strategy={verticalListSortingStrategy}
          >
            <Table
              columns={columns}
              dataSource={steps}
              rowKey="id"
              pagination={false}
              scroll={{ x: isMobile ? 600 : undefined }}
              components={{ body: { row: DraggableRow } }}
            />
          </SortableContext>
        </DndContext>
      </Spin>

      <LearningStepDrawer
        open={drawerVisible}
        direction={activeDirection}
        record={editingRecord}
        onSuccess={handleDrawerSuccess}
        onCancel={() => setDrawerVisible(false)}
      />
    </div>
  )
}
