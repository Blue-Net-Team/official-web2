'use client'

import { useCallback, useState } from 'react'
import { App, Button, Grid, InputNumber, Modal, Pagination, Select, Spin, Table, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { AchievementDTO, AchievementType, AwardLevel } from '@/apis/schema/type'
import { adminAchievementService } from '@/apis/services/admin-achievement.service'
import { AchievementService } from '@/apis/services/achievement.service'
import AchievementDrawer, { type DrawerMode } from './AchievementDrawer'
import { usePagination } from '@/hooks'

const PAGE_SIZE = 20

const { useBreakpoint } = Grid

const ACHIEVEMENT_TYPE_LABELS: Record<AchievementType, string> = {
  PAPER: '论文',
  PATENT: '专利',
  COMPETITION: '竞赛',
}

const AWARD_LEVEL_LABELS: Record<AwardLevel, string> = {
  NATIONAL: '国家级',
  PROVINCIAL: '省级',
  SCHOOL: '校级',
}

const AWARD_LEVEL_COLORS: Record<AwardLevel, string> = {
  NATIONAL: 'red',
  PROVINCIAL: 'orange',
  SCHOOL: 'green',
}

export default function AchievementManagementPage() {
  const { message: messageApi } = App.useApp()
  const screens = useBreakpoint()
  const isMobile = !screens.md

  // Drawer state
  const [drawerVisible, setDrawerVisible] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('create')
  const [editingRecord, setEditingRecord] = useState<AchievementDTO | null>(null)

  // Filter state
  const [filterType, setFilterType] = useState<string | undefined>()
  const [filterAwardLevel, setFilterAwardLevel] = useState<string | undefined>()
  const [filterYear, setFilterYear] = useState<number | undefined>()

  const apiFn = useCallback(
    (page: number, pageSize: number) =>
      AchievementService.getAchievements({
        page,
        size: pageSize,
        type: filterType,
        awardLevel: filterAwardLevel,
        year: filterYear,
      }),
    [filterType, filterAwardLevel, filterYear]
  )

  const { data, total, loading, currentPage, setCurrentPage, refresh, reset } = usePagination(
    apiFn,
    { pageSize: PAGE_SIZE }
  )

  const handleCreate = () => {
    setDrawerMode('create')
    setEditingRecord(null)
    setDrawerVisible(true)
  }

  const handleEdit = (record: AchievementDTO) => {
    setDrawerMode('edit')
    setEditingRecord(record)
    setDrawerVisible(true)
  }

  const handleDelete = (record: AchievementDTO) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除成就 "${record.title}" 吗？此操作不可恢复。`,
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          const response = await adminAchievementService.delete(record.id)
          if (response.code === 200) {
            messageApi.success('删除成功')
            refresh()
          } else {
            messageApi.error(`删除失败: ${response.msg}`)
          }
        } catch (error) {
          console.error('删除成就失败:', error)
          messageApi.error('删除成就失败')
        }
      },
    })
  }

  const handleDrawerSuccess = () => {
    setDrawerVisible(false)
    refresh()
  }

  const handleDrawerCancel = () => {
    setDrawerVisible(false)
  }

  const columns: ColumnsType<AchievementDTO> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
      responsive: ['md'],
    },
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: AchievementType) => (
        <Tag color={type === 'COMPETITION' ? 'blue' : type === 'PAPER' ? 'green' : 'purple'}>
          {ACHIEVEMENT_TYPE_LABELS[type] || type}
        </Tag>
      ),
    },
    {
      title: '关联项',
      dataIndex: 'relateTo',
      key: 'relateTo',
      width: 150,
      responsive: ['md'],
      render: (relateTo: string | null) => relateTo || '-',
    },
    {
      title: '获奖日期',
      dataIndex: 'achieveAt',
      key: 'achieveAt',
      width: 120,
      responsive: ['md'],
      render: (date: string) => date.substring(0, 10),
    },
    {
      title: '奖项级别',
      dataIndex: 'awardLevel',
      key: 'awardLevel',
      width: 100,
      render: (level: AwardLevel | null) =>
        level ? <Tag color={AWARD_LEVEL_COLORS[level]}>{AWARD_LEVEL_LABELS[level]}</Tag> : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <div className="flex gap-2">
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            size="small"
          />
          <Button
            type="text"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record)}
            size="small"
          />
        </div>
      ),
    },
  ]

  return (
    <div>
      <div className="mb-4 flex justify-between items-center">
        <h2 className="m-0">成就管理</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新建成就
        </Button>
      </div>

      {/* 简单的过滤栏，可根据需要扩展 */}
      <div className="mb-4 flex gap-2 flex-wrap">
        <Select
          value={filterType}
          onChange={(value) => {
            setFilterType(value)
            reset()
          }}
          placeholder="全部类型"
          allowClear
          className="w-[120px]"
          options={[
            { value: 'PAPER', label: '论文' },
            { value: 'PATENT', label: '专利' },
            { value: 'COMPETITION', label: '竞赛' },
          ]}
        />
        <Select
          value={filterAwardLevel}
          onChange={(value) => {
            setFilterAwardLevel(value)
            reset()
          }}
          placeholder="全部级别"
          allowClear
          className="w-[120px]"
          options={[
            { value: 'NATIONAL', label: '国家级' },
            { value: 'PROVINCIAL', label: '省级' },
            { value: 'SCHOOL', label: '校级' },
          ]}
        />
        <InputNumber
          placeholder="年份"
          value={filterYear}
          onChange={(value) => {
            setFilterYear(value ?? undefined)
            reset()
          }}
          className="w-[100px]"
          min={1900}
          max={2100}
        />
        <Button onClick={() => reset()}>筛选</Button>
      </div>

      <Spin spinning={loading}>
        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          pagination={false}
          scroll={{ x: isMobile ? 800 : undefined }}
        />
      </Spin>

      <div className="mt-4 flex justify-center">
        <Pagination
          current={currentPage + 1}
          pageSize={PAGE_SIZE}
          total={total}
          onChange={(p) => setCurrentPage(p - 1)}
          showSizeChanger={false}
        />
      </div>

      <AchievementDrawer
        open={drawerVisible}
        mode={drawerMode}
        record={editingRecord}
        onSuccess={handleDrawerSuccess}
        onCancel={handleDrawerCancel}
      />
    </div>
  )
}
