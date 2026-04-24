'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import { App, Button, Grid, Modal, Pagination, Select, Spin, Table, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, PaperClipOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type {
  AssessmentQuestionDTO,
  AssessmentTimeDTO,
  QuestionType,
} from '@/apis/schema/assessment.dto'
import { Direction, DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { adminAssessmentTimeService } from '@/apis/services/admin-assessment-time.service'
import { adminAssessmentQuestionService } from '@/apis/services/admin-assessment-question.service'
import { useAuth } from '@/hooks'
import { getRoleLevel } from '@/utils/RoleUtils'
import QuestionDrawer, { QUESTION_TYPE_LABELS, type DrawerMode } from './QuestionDrawer'

const PAGE_SIZE = 20
const { useBreakpoint } = Grid

const QUESTION_TYPE_COLORS: Record<QuestionType, string> = {
  FILE_UPLOAD: 'blue',
  SINGLE_CHOICE: 'green',
  MULTIPLE_CHOICE: 'purple',
  ALGORITHM: 'orange',
}

export default function AssessmentQuestionManagementPage() {
  const { message: messageApi } = App.useApp()
  const screens = useBreakpoint()
  const isMobile = !screens.md

  const { userInfo } = useAuth()
  const isSuperAdmin = getRoleLevel(userInfo?.roleName || '') >= 3
  const roleLevel = getRoleLevel(userInfo?.roleName || '')
  const userDirection = userInfo?.direction

  // Filter state
  const [filterDirection, setFilterDirection] = useState<Direction | undefined>(
    isSuperAdmin ? undefined : (userDirection ?? undefined)
  )
  const [filterTimeId, setFilterTimeId] = useState<number | undefined>(undefined)

  // Assessment times for the selected direction
  const [assessmentTimes, setAssessmentTimes] = useState<AssessmentTimeDTO[]>([])

  // Questions data
  const [questions, setQuestions] = useState<AssessmentQuestionDTO[]>([])
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)

  // Drawer state
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('view')
  const [selectedQuestion, setSelectedQuestion] = useState<AssessmentQuestionDTO | null>(null)

  // Delete modal state
  const [deleteModalOpen, setDeleteModalOpen] = useState(false)
  const [deletingItem, setDeletingItem] = useState<AssessmentQuestionDTO | null>(null)

  // Permission: DIRECTION_ADMIN can only operate on own direction
  const canOperate = useMemo(() => {
    return isSuperAdmin || filterDirection === userDirection
  }, [isSuperAdmin, filterDirection, userDirection])

  // Direction options: SUPER_ADMIN sees all, DIRECTION_ADMIN sees only own
  const directionOptions = useMemo(() => {
    const entries = Object.entries(DIRECTION_LABELS) as [Direction, string][]
    if (!isSuperAdmin && userDirection) {
      return entries.filter(([value]) => value === userDirection)
    }
    return entries
  }, [isSuperAdmin, userDirection])

  // 方向变更后加载该方向下的考核时间。
  const fetchAssessmentTimes = useCallback(async (direction: Direction) => {
    try {
      const res = await adminAssessmentTimeService.getList(0, 100)
      if (res.data) {
        setAssessmentTimes(res.data.content.filter((t) => t.direction === direction))
      }
    } catch {
      // silent
    }
  }, [])

  useEffect(() => {
    if (filterDirection) {
      fetchAssessmentTimes(filterDirection)
    } else {
      setAssessmentTimes([])
    }
    setFilterTimeId(undefined)
  }, [filterDirection, fetchAssessmentTimes])

  // 考核时间下拉选项。
  const timeOptions = useMemo(() => {
    return assessmentTimes.map((t) => ({
      value: t.id,
      label: `第 ${t.epoch} 轮 · ${t.grade}级`,
    }))
  }, [assessmentTimes])

  // 当前选中的考核时间上下文。
  const selectedTime = useMemo(() => {
    return assessmentTimes.find((t) => t.id === filterTimeId) ?? null
  }, [assessmentTimes, filterTimeId])

  // 按当前考核时间分页加载考题。
  const fetchQuestions = useCallback(async () => {
    if (!filterTimeId) {
      setQuestions([])
      setTotalElements(0)
      return
    }
    setLoading(true)
    try {
      const res = await adminAssessmentQuestionService.getList(filterTimeId, page - 1, PAGE_SIZE)
      if (res.data) {
        setQuestions(res.data.content)
        setTotalElements(res.data.totalElements)
      }
    } finally {
      setLoading(false)
    }
  }, [filterTimeId, page])

  useEffect(() => {
    fetchQuestions()
  }, [fetchQuestions])

  // 切换方向筛选并重置分页。
  const handleDirectionChange = (value: Direction | undefined) => {
    setFilterDirection(value)
    setPage(1)
  }

  // 切换考核时间筛选并重置分页。
  const handleTimeChange = (value: number | undefined) => {
    setFilterTimeId(value)
    setPage(1)
  }

  // 打开新增考题抽屉。
  const handleCreate = () => {
    setSelectedQuestion(null)
    setDrawerMode('create')
    setDrawerOpen(true)
  }

  // 点击表格行时以只读模式查看考题。
  const handleRowClick = (record: AssessmentQuestionDTO) => {
    setSelectedQuestion(record)
    setDrawerMode('view')
    setDrawerOpen(true)
  }

  // 打开编辑考题抽屉。
  const handleEdit = (record: AssessmentQuestionDTO) => {
    setSelectedQuestion(record)
    setDrawerMode('edit')
    setDrawerOpen(true)
  }

  // 打开删除确认弹窗。
  const handleDeleteClick = (item: AssessmentQuestionDTO) => {
    setDeletingItem(item)
    setDeleteModalOpen(true)
  }

  // 确认删除考题后刷新列表。
  const handleDeleteConfirm = async () => {
    if (!deletingItem) return
    try {
      await adminAssessmentQuestionService.delete(deletingItem.id)
      messageApi.success('删除成功')
      setDeleteModalOpen(false)
      setDrawerOpen(false)
      fetchQuestions()
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { msg?: string } } })?.response?.data?.msg
      messageApi.error(msg || '删除失败')
    }
  }

  // 抽屉保存成功后关闭抽屉并刷新列表。
  const handleDrawerSuccess = () => {
    setDrawerOpen(false)
    fetchQuestions()
  }

  // 考题表格列定义。
  const columns: ColumnsType<AssessmentQuestionDTO> = useMemo(() => {
    const cols: ColumnsType<AssessmentQuestionDTO> = [
      {
        title: '题号',
        dataIndex: 'questionNo',
        key: 'questionNo',
        width: 60,
      },
      {
        title: '题型',
        dataIndex: 'questionType',
        key: 'questionType',
        width: 90,
        render: (type: QuestionType) => (
          <Tag color={QUESTION_TYPE_COLORS[type]} bordered={false}>
            {QUESTION_TYPE_LABELS[type]}
          </Tag>
        ),
      },
      {
        title: '标题',
        dataIndex: 'title',
        key: 'title',
        ellipsis: true,
      },
      {
        title: '分值',
        dataIndex: 'score',
        key: 'score',
        width: 60,
      },
      {
        title: '附件',
        key: 'attachment',
        width: 50,
        render: (_: unknown, record: AssessmentQuestionDTO) =>
          record.attachmentId ? <PaperClipOutlined className="text-white/50" /> : '-',
      },
      {
        title: '操作',
        key: 'action',
        width: 80,
        render: (_: unknown, record: AssessmentQuestionDTO) =>
          canOperate ? (
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
          ) : null,
      },
    ]

    if (isMobile) {
      return cols.filter((c) => !['score', 'attachment'].includes(c.key as string))
    }

    return cols
  }, [isMobile, canOperate])

  // 空状态文案随方向和考核时间筛选状态变化。
  const emptyText = !filterDirection
    ? '请先选择方向'
    : !filterTimeId
      ? '请选择考核时间'
      : '暂无考题，点击新增添加'

  return (
    <div className="flex flex-col gap-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium text-white/90 m-0">考题管理</h2>
        {canOperate && filterTimeId && (
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            新增考题
          </Button>
        )}
      </div>

      {/* Filters */}
      <div className="flex gap-3 items-center flex-wrap">
        <Select
          placeholder="筛选方向"
          allowClear
          className="w-[140px]"
          value={filterDirection}
          onChange={handleDirectionChange}
          options={directionOptions.map(([value, label]) => ({ value, label }))}
        />
        <Select
          placeholder="选择考核时间"
          allowClear
          className="w-[200px]"
          value={filterTimeId}
          onChange={handleTimeChange}
          options={timeOptions}
          disabled={!filterDirection}
        />
      </div>

      {/* Table */}
      <Spin spinning={loading}>
        <Table
          dataSource={questions}
          columns={columns}
          rowKey={(record) => String(record.id)}
          size="small"
          pagination={false}
          onRow={(record) => ({
            onClick: () => handleRowClick(record),
            className: 'cursor-pointer',
          })}
          locale={{ emptyText }}
        />
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

      {/* Drawer */}
      <QuestionDrawer
        open={drawerOpen}
        question={selectedQuestion}
        assessmentTimeId={filterTimeId ?? null}
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
        <p>确认删除考题「{deletingItem?.title}」？此操作不可撤销。</p>
      </Modal>
    </div>
  )
}
