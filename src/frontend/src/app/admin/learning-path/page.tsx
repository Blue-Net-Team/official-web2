'use client'

import { useCallback, useEffect, useState } from 'react'
import { App, Button, Grid, Popconfirm, Spin, Table, Tabs, Tag } from 'antd'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { LearningStepDTO } from '@/apis/schema/direction.dto'
import { adminDirectionService } from '@/apis/services/direction.service'
import LearningStepDrawer from './LearningStepDrawer'

const { useBreakpoint } = Grid

const DIRECTION_TABS = [
  { key: 'cv', label: '计算机视觉' },
  { key: 'embed', label: '嵌入式开发' },
  { key: 'struct', label: '结构设计' },
]

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

  const columns: ColumnsType<LearningStepDTO> = [
    {
      title: '步骤序号',
      dataIndex: 'stepNumber',
      key: 'stepNumber',
      width: 100,
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
        <Table
          columns={columns}
          dataSource={steps}
          rowKey="id"
          pagination={false}
          scroll={{ x: isMobile ? 600 : undefined }}
        />
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
