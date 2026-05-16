'use client'

import { useCallback, useMemo, useState } from 'react'
import {
  App,
  Button,
  DatePicker,
  Form,
  Grid,
  InputNumber,
  Modal,
  Pagination,
  Select,
  Spin,
  Switch,
  Table,
  Tag,
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, TeamOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import dayjs from 'dayjs'
import type {
  AssessmentTimeDTO,
  CreateAssessmentTimeRequestDTO,
  UpdateAssessmentTimeRequestDTO,
} from '@/apis/schema/assessment.dto'
import { Direction, DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { adminAssessmentTimeService } from '@/apis/services/admin-assessment-time.service'
import { useAuth, usePagination } from '@/hooks'
import { getRoleLevel } from '@/utils/RoleUtils'
import AssessmentTimeDrawer, { type DrawerMode } from './AssessmentTimeDrawer'

const PAGE_SIZE = 20
const { useBreakpoint } = Grid

/** 获取考核状态 */
function getAssessmentStatus(startTime: string, endTime: string) {
  const now = dayjs()
  const start = dayjs(startTime)
  const end = dayjs(endTime)
  if (now.isBefore(start)) return { label: '未开始', color: 'default' }
  if (now.isAfter(end)) return { label: '已结束', color: 'red' }
  return { label: '进行中', color: 'green' }
}

export default function AssessmentTimeManagementPage() {
  const { message: messageApi } = App.useApp()
  const screens = useBreakpoint()
  const isMobile = !screens.md

  const { userInfo } = useAuth()
  const isSuperAdmin = getRoleLevel(userInfo?.roleName || '') >= 3
  const userDirection = userInfo?.direction

  // Data state
  const { data, total, totalPages, loading, currentPage, setCurrentPage, refresh } = usePagination(
    adminAssessmentTimeService.getList.bind(adminAssessmentTimeService),
    { pageSize: PAGE_SIZE }
  )

  // Filter state
  const [filterDirection, setFilterDirection] = useState<Direction | undefined>(undefined)
  const [filterGrade, setFilterGrade] = useState<number | undefined>(undefined)

  // Drawer state
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('view')
  const [selectedItem, setSelectedItem] = useState<AssessmentTimeDTO | null>(null)

  // Delete modal state
  const [deleteModalOpen, setDeleteModalOpen] = useState(false)
  const [deletingItem, setDeletingItem] = useState<AssessmentTimeDTO | null>(null)

  // Client-side filtering
  const filteredList = useMemo(() => {
    return data.filter((item) => {
      if (filterDirection && item.direction !== filterDirection) return false
      if (filterGrade !== undefined && item.grade !== filterGrade) return false
      return true
    })
  }, [data, filterDirection, filterGrade])

  // Unique grades from current data for filter dropdown
  const gradeOptions = useMemo(() => {
    const grades = [...new Set(data.map((item) => item.grade))].sort((a, b) => b - a)
    return grades.map((g) => ({ label: `${g}级`, value: g }))
  }, [data])

  // Check if current user can operate on a given direction
  const canOperate = useCallback(
    (direction: Direction) => {
      return isSuperAdmin || direction === userDirection
    },
    [isSuperAdmin, userDirection]
  )

  // Handlers
  const handleCreate = () => {
    setSelectedItem(null)
    setDrawerMode('create')
    setDrawerOpen(true)
  }

  const handleRowClick = (record: AssessmentTimeDTO) => {
    setSelectedItem(record)
    setDrawerMode('view')
    setDrawerOpen(true)
  }

  const handleEdit = (record: AssessmentTimeDTO) => {
    setSelectedItem(record)
    setDrawerMode('edit')
    setDrawerOpen(true)
  }

  const handleDeleteClick = (item: AssessmentTimeDTO) => {
    setDeletingItem(item)
    setDeleteModalOpen(true)
  }

  const handleDeleteConfirm = async () => {
    if (!deletingItem) return
    try {
      await adminAssessmentTimeService.delete(deletingItem.id)
      messageApi.success('删除成功')
      setDeleteModalOpen(false)
      setDrawerOpen(false)
      refresh()
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { msg?: string } } })?.response?.data?.msg
      messageApi.error(msg || '删除失败')
    }
  }

  const handleDrawerSuccess = () => {
    setDrawerOpen(false)
    refresh()
  }

  const handleFilterDirectionChange = (value: Direction | undefined) => {
    setFilterDirection(value)
    setCurrentPage(0)
  }

  const handleFilterGradeChange = (value: number | undefined) => {
    setFilterGrade(value)
    setCurrentPage(0)
  }

  // Table columns
  const columns: ColumnsType<AssessmentTimeDTO> = useMemo(() => {
    const cols: ColumnsType<AssessmentTimeDTO> = [
      {
        title: '方向',
        dataIndex: 'direction',
        key: 'direction',
        width: 110,
        render: (direction: Direction) => <Tag bordered={false}>{DIRECTION_LABELS[direction]}</Tag>,
      },
      {
        title: '轮次',
        dataIndex: 'epoch',
        key: 'epoch',
        width: 60,
      },
      {
        title: '年级',
        dataIndex: 'grade',
        key: 'grade',
        width: 70,
        render: (grade: number) => `${grade}级`,
      },
      {
        title: '开始时间',
        dataIndex: 'startTime',
        key: 'startTime',
        width: isMobile ? undefined : 150,
        render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm'),
      },
      {
        title: '结束时间',
        dataIndex: 'endTime',
        key: 'endTime',
        width: isMobile ? undefined : 150,
        render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm'),
      },
      {
        title: '限时',
        key: 'timeLimit',
        width: 80,
        render: (_: unknown, record: AssessmentTimeDTO) =>
          record.timeLimit ? `${record.timeLimitMinutes} 分钟` : '不限时',
      },
      {
        title: '组队',
        key: 'allowTeam',
        width: 80,
        render: (_: unknown, record: AssessmentTimeDTO) =>
          record.allowTeam ? (
            <Tag color="blue" icon={<TeamOutlined />} bordered={false}>
              允许
            </Tag>
          ) : (
            <Tag bordered={false}>不允许</Tag>
          ),
      },
      {
        title: '状态',
        key: 'status',
        width: 80,
        render: (_: unknown, record: AssessmentTimeDTO) => {
          const status = getAssessmentStatus(record.startTime, record.endTime)
          return (
            <Tag color={status.color} bordered={false}>
              {status.label}
            </Tag>
          )
        },
      },
      {
        title: '操作',
        key: 'action',
        width: 80,
        render: (_: unknown, record: AssessmentTimeDTO) => {
          const canEdit = canOperate(record.direction)
          return canEdit ? (
            <div className="flex gap-1">
              <Button
                type="text"
                size="small"
                icon={<EditOutlined />}
                onClick={(e) => {
                  e.stopPropagation()
                  handleEdit(record)
                }}
              />
              <Button
                type="text"
                size="small"
                danger
                icon={<DeleteOutlined />}
                onClick={(e) => {
                  e.stopPropagation()
                  handleDeleteClick(record)
                }}
              />
            </div>
          ) : null
        },
      },
    ]

    // Hide some columns on mobile
    if (isMobile) {
      return cols.filter((c) => !['grade', 'timeLimit', 'allowTeam'].includes(c.key as string))
    }

    return cols
  }, [isMobile, canOperate])

  return (
    <div className="flex flex-col gap-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium text-white/90 m-0">考核时间管理</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新增考核时间
        </Button>
      </div>

      {/* Filters */}
      <div className="flex gap-3 items-center flex-wrap">
        <Select
          placeholder="筛选方向"
          allowClear
          className="w-[140px]"
          value={filterDirection}
          onChange={handleFilterDirectionChange}
          options={Object.entries(DIRECTION_LABELS).map(([value, label]) => ({
            value,
            label,
          }))}
        />
        <Select
          placeholder="筛选年级"
          allowClear
          className="w-[120px]"
          value={filterGrade}
          onChange={handleFilterGradeChange}
          options={gradeOptions}
        />
      </div>

      {/* Table */}
      <Spin spinning={loading}>
        <Table
          dataSource={filteredList}
          columns={columns}
          rowKey={(record) => String(record.id)}
          size="small"
          pagination={false}
          onRow={(record) => ({
            onClick: () => handleRowClick(record),
            className: 'cursor-pointer',
          })}
          locale={{ emptyText: '暂无考核时间数据' }}
        />
      </Spin>

      {/* Pagination */}
      {total > PAGE_SIZE && (
        <div className="flex justify-center">
          <Pagination
            current={currentPage + 1}
            total={total}
            pageSize={PAGE_SIZE}
            showSizeChanger={false}
            onChange={(p) => setCurrentPage(p - 1)}
          />
        </div>
      )}

      {/* Drawer */}
      <AssessmentTimeDrawer
        open={drawerOpen}
        assessmentTime={selectedItem}
        mode={drawerMode}
        onClose={() => setDrawerOpen(false)}
        onSuccess={handleDrawerSuccess}
        onDelete={handleDeleteClick}
        onEdit={() => setDrawerMode('edit')}
        isSuperAdmin={isSuperAdmin}
        userDirection={userDirection ?? null}
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
        <p>
          确认删除考核时间「{DIRECTION_LABELS[deletingItem?.direction as Direction]} 第
          {deletingItem?.epoch} 轮 {deletingItem?.grade}级」？此操作不可撤销。
        </p>
      </Modal>
    </div>
  )
}
